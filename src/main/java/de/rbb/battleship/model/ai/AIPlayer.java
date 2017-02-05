package de.rbb.battleship.model.ai;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.rbb.battleship.model.Firing;
import de.rbb.battleship.model.Position;
import de.rbb.battleship.model.AbstractGameMaster.PlayerEvent;

public abstract class AIPlayer extends AbstractPlayer {

    public static final int TRESHOLD_FOR_NEXT_FIRING = 1000;

    /** Log4J Logger Instanz, siehe Log4J Dokumentation */
    protected static final Logger log = Logger.getLogger(AIPlayer.class);

    protected List<Position> firingHistory;

    protected final boolean loaded;

    public AIPlayer(boolean loaded) {
        this.loaded = loaded;
        this.firingHistory = new ArrayList<Position>(100);
        this.name = "Computer [" + AIPlayer.class.getSimpleName() + ']';
    }

    public AIPlayer() {
        this(false);
    }

    public void init() {
        if (!this.loaded) {
            this.board.placeShipsRandom();
        }
        else {
            this.firingHistory = this.board.getPositionsWhereAlreadyFired();
        }
        this.gameMaster.setPlayerReady(this, true);
    }

    @Override
    public String toString() {
        return this.name;
    }

    protected abstract Firing fireAtEnemy();

    protected boolean saveFiring(Position pos) {
        boolean bResult = false;
        if (!this.firingHistory.contains(pos)) {
            this.firingHistory.add(pos);
            bResult = true;
        }
        return bResult;
    }

    protected boolean historyContainsPosition(Position pos) {
        return this.firingHistory.contains(pos);
    }

    @Override
    public void nextPlayerTurned(PlayerEvent playerTurn) {
        if (this.equals(playerTurn.getPlayer())) {
            try {
                Thread.sleep(TRESHOLD_FOR_NEXT_FIRING);
                this.fireAtEnemy();
            }
            catch (InterruptedException e) {
                log.error("Couldn't use the Threshold, got interrupted!", e);
            }
        }
    }
}
