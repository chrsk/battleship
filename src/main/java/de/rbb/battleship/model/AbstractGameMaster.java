package de.rbb.battleship.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;

import org.apache.log4j.Logger;

import de.rbb.battleship.core.GameMode;
import de.rbb.battleship.gui.ChatListener.DefaultChatBroadcaster;

public abstract class AbstractGameMaster extends DefaultChatBroadcaster implements GameMaster, Runnable {

    /** Log4J Logger Instanz, siehe Log4J Dokumentation */
    private static final Logger log = Logger.getLogger(AbstractGameMaster.class);

    protected boolean isRunning;

    protected boolean isFinished;

    protected boolean isAborted;

    protected boolean player1IsReady, player2IsReady;

    private final List<GameListener> gameListeners;

    protected Player player1;

    protected Player player2;

    public AbstractGameMaster() {
        this.gameListeners = new ArrayList<GameListener>();
    }

    public void addGameListener(GameListener listener) {
        this.gameListeners.add(listener);
    }

    protected void fireGameFinished(Player player) {
        for (GameListener gameListener : this.gameListeners) {
            gameListener.gameFinished(new PlayerEvent(player));
        }
    }

    protected void fireFieldStateChanged(ChangeEvent changeEvent) {
        for (GameListener gameListener : this.gameListeners) {
            gameListener.fieldStateChanged(changeEvent);
        }
    }

    public void fireGameCreated() {
        log.info("New Game Started in " + this.getGameMode() + " mode");
        for (GameListener gameListener : this.gameListeners) {
            gameListener.gameCreated(new GameCreation(this.getGameMode()));
        }
    }

    protected void fireGameAborted() {
        for (GameListener gameListener : this.gameListeners) {
            gameListener.gameAborted(new ChangeEvent(this));
        }
    }

    protected void fireGameStarted() {
        log.info("Game started!");
        for (GameListener gameListener : this.gameListeners) {
            gameListener.gameStarted(new ChangeEvent(this));
        }
    }

    protected void fireTurnEventToPlayer(Player player) {
        log.info("Now on turn: " + player);
        for (GameListener gameListener : this.gameListeners) {
            gameListener.nextPlayerTurned(new PlayerEvent(player));
        }
    }

    public static interface GameListener {
        public void gameCreated(GameCreation changeEvent);

        public void gameStarted(ChangeEvent changeEvent);

        public void fieldStateChanged(ChangeEvent changeEvent);

        public void gameFinished(PlayerEvent playerEvent);

        public void gameAborted(ChangeEvent changeEvent);

        public void nextPlayerTurned(PlayerEvent playerEvent);
    }

    public static class GameAdapter implements GameListener {
        public void gameCreated(GameCreation changeEvent) {

        }

        public void gameStarted(ChangeEvent changeEvent) {
        }

        public void fieldStateChanged(ChangeEvent changeEvent) {
        }

        public void gameFinished(PlayerEvent playerEvent) {
        }

        public void gameAborted(ChangeEvent changeEvent) {
        }

        public void nextPlayerTurned(PlayerEvent playerTurn) {
        }
    }

    public static class GameCreation {
        private final GameMode gameMode;

        public GameCreation(GameMode gameMode) {
            this.gameMode = gameMode;
        }

        public GameMode getGameMode() {
            return this.gameMode;
        }
    }

    public static class PlayerEvent {
        private final Player player;

        public PlayerEvent(Player player) {
            this.player = player;
        }

        public Player getPlayer() {
            return this.player;
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        // its a message from board
        this.fireFieldStateChanged(e);
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public boolean isAborted() {
        return this.isAborted;
    }

    @Override
    public boolean isFinished() {
        return this.isFinished;
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
}
