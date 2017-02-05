package de.rbb.battleship.model.ai;

import javax.swing.event.ChangeListener;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;

import de.rbb.battleship.core.GameMode;
import de.rbb.battleship.model.AbstractGameMaster;
import de.rbb.battleship.model.Firing;
import de.rbb.battleship.model.Player;
import de.rbb.battleship.model.Position;

public class AIGameMaster extends AbstractGameMaster implements ChangeListener, Runnable {

    /** Log4J Logger Instanz, siehe Log4J Dokumentation */
    private static final Logger log = Logger.getLogger(AIGameMaster.class);

    private boolean firstPlayersTurn;

    public AIGameMaster(Player player1, Player player2) throws NullArgumentException {
        if (player1 == null) {
            throw new NullArgumentException("player1");
        }
        else if (player2 == null) {
            throw new NullArgumentException("player2");
        }

        this.player1 = player1;
        this.player2 = player2;

        this.player1.setGameMaster(this);
        this.player2.setGameMaster(this);

        this.addGameListener(player1);
        this.addGameListener(player2);
        this.init();

    }

    private void init() {
        this.player1.init();
        this.player2.init();

        this.player1.getBoard().addChangeListener(this);
        this.player2.getBoard().addChangeListener(this);
    }

    public void finish() {
        this.isRunning = false;
        this.isFinished = true;

        Player player;
        if (this.player1.getBoard().isDestroyed()) {
            player = this.player2;
        }
        else {
            player = this.player1;
        }

        this.fireGameFinished(player);

        log.info("Game is finished");
    }

    public void abort() {
        log.info("Game was aborted");
        if (!this.isAborted && this.isRunning) {
            this.isAborted = true;
            this.isRunning = false;
            this.player2.getBoard().fireChangeEvent();
            this.fireGameAborted();
        }
    }

    public void run() {
        log.info("Game is started");
        this.isRunning = true;
        this.isAborted = false;
        this.isFinished = false;

        this.firstPlayersTurn = true;
        Player player = this.player1;

        log.info("Starting player is: " + player + " (" + this.firstPlayersTurn + ')');

        this.fireGameStarted();
        this.fireTurnEventToPlayer(player);
    }

    public void setPlayerReady(Player player, boolean ready) {
        if (player == null) {
            throw new NullArgumentException("player");
        }

        if (player.equals(this.player1)) {
            log.info("Player 1 (" + this.player1.getName() + ") is now " + (ready ? "ready!" : "not ready!"));
            this.player1IsReady = ready;
        }
        else {
            log.info("Player 2 (" + this.player2.getName() + ") is now " + (ready ? "ready!" : "not ready!"));
            this.player2IsReady = ready;
        }

        if (this.player1IsReady && this.player2IsReady) {
            log.info("Both players are ready ... starting the game!");
            this.run();
        }
    }

    public synchronized Firing fireAtEnemy(Player theFiringPlayer, Position pos) {
        Player nextPlayer = null;
        Firing firing = null;

        if (this.isRunning) {
            if (this.isFinished) {
                log.warn("Game is already finished!");
            }
            else {
                log.info("--------[FIRING APPEARED]--------");
                log.info("On turn is " + (this.firstPlayersTurn ? this.player2 : this.player1));
                log.info("The firing player is " + theFiringPlayer + "it's first players turn: "
                        + this.firstPlayersTurn);

                if (theFiringPlayer.equals(this.player1) && this.firstPlayersTurn) {
                    firing = this.player2.getBoard().fireAtField(pos);
                    log.info(this.player1 + " has fired on " + this.player2 + " and striked? " + firing.isStrike());
                    if (firing.isStrike() && SET_PLAYER_ON_TURN_AGAIN_AFTER_STRIKE) {
                        this.firstPlayersTurn = true;
                        nextPlayer = this.player1;
                    }
                    else {
                        this.firstPlayersTurn = false;
                        nextPlayer = this.player2;
                    }
                }
                else if (theFiringPlayer.equals(this.player2) && !this.firstPlayersTurn) {
                    firing = this.player1.getBoard().fireAtField(pos);
                    log.info(this.player2 + " has fired on " + this.player1 + " and striked? " + firing.isStrike());
                    if (firing.isStrike() && SET_PLAYER_ON_TURN_AGAIN_AFTER_STRIKE) {
                        this.firstPlayersTurn = false;
                        nextPlayer = this.player2;
                    }
                    else {
                        this.firstPlayersTurn = true;
                        nextPlayer = this.player1;
                    }
                }
                else {
                    log.warn("The player is not the valid Player.");
                }

                if (this.player1.getBoard().isDestroyed() || this.player2.getBoard().isDestroyed()) {
                    this.finish();
                }
            }

            log.info("---------------------------------");
            final Player finalPlayer = nextPlayer;
            if (nextPlayer != null) {
                new Thread(new Runnable() {
                    public void run() {
                        AIGameMaster.this.fireTurnEventToPlayer(finalPlayer);
                    }
                }).start();
            }
        }

        return firing;
    }

    @Override
    public Player getPlayer1() {
        return this.player1;
    }

    @Override
    public Player getPlayer2() {
        return this.player2;
    }

    @Override
    public void start() {
        this.run();
    }

    private boolean isComputerVsComputer() {
        return this.player1 instanceof AIPlayer && this.player2 instanceof AIPlayer;
    }

    @Override
    public GameMode getGameMode() {
        if (this.isComputerVsComputer()) {
            return GameMode.COMPUTER_VS_COMPUTER;
        }
        else {
            return GameMode.SINGLEPLAYER;
        }
    }
}
