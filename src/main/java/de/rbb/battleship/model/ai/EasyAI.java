package de.rbb.battleship.model.ai;

import java.util.Random;

import de.rbb.battleship.model.Firing;
import de.rbb.battleship.model.Position;

public class EasyAI extends AIPlayer {

    protected boolean lastFiringStrikedShip;

    public EasyAI(boolean loaded) {
        super(loaded);
    }

    public EasyAI() {
        super();
    }

    @Override
    protected Firing fireAtEnemy() {
        Position position = this.getFreeRandomPosition();
        return this.gameMaster.fireAtEnemy(this, position);
    }

    protected Position getFreeRandomPosition() {
        Position position = null;

        boolean foundValidFiring = false;
        while (!foundValidFiring) {
            Random r = new Random();
            final int randomX = r.nextInt(10);
            final int randomY = r.nextInt(10);

            position = new Position(randomX, randomY);
            foundValidFiring = this.saveFiring(position);
        }
        return position;
    }

}
