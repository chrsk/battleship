package de.rbb.battleship.model.ai;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.rbb.battleship.model.Firing;
import de.rbb.battleship.model.Position;
import de.rbb.battleship.model.Ship;

public class HardAI extends EasyAI {

    enum Alignment {
        HORIZONTAL, VERTICAL, NONE;
    }

    enum Switch {
        NEXT, INVERT
    }

    /** Log4J Logger Instanz, siehe Log4J Dokumentation */
    private static final Logger log = Logger.getLogger(HardAI.class);

    private Position lastFiring;

    private Direction lastDirection = Direction.NORTH;

    private int actualShipStrikes;

    private boolean shipFound;

    private Alignment alignment = Alignment.NONE;

    private final List<Position> strikeHistory;

    public HardAI() {
        this.strikeHistory = new ArrayList<Position>(30);
    }

    @Override
    protected synchronized Firing fireAtEnemy() {
        log.info("Starting the firing algorithm");
        Firing firing = null;

        if (!this.shipFound) {
            log.info("There is no ship under attack currently");
            // no ship found, fire random to find a ship
            Position pos = this.getFreeRandomPosition();

            log.info("Fire at free random position");
            firing = this.fireAtEnemy(pos);
        }
        else {
            log.info("There is a ship that is under attack currently");
            // ship is actually under attack

            if (this.alignment != Alignment.NONE) {
                log.info("The alignment of the ship is: " + this.alignment);
                // alignment is determined
                if (this.lastFiringStrikedShip) {
                    try {
                        log.info("The last firing to the ship was successful, go to the next position (direction: "
                                + this.lastDirection + ')');
                        // it seems to be still the right direction
                        this.fireNextFreeField(this.lastFiring, this.lastDirection, Switch.INVERT);
                    }
                    catch (IllegalStateException e) {
                        // we have to invert the search direction
                        this.invertDirectionAndFireFirstPosition();
                    }
                }
                else {
                    // switch direction and go to first shooting to this ship
                    this.invertDirectionAndFireFirstPosition();
                }
            }
            else {
                log.info("no alignment is determined until now, searching for next free field");
                firing = this.fireNextFreeField(this.lastFiring, this.lastDirection, Switch.NEXT);
            }
        }

        return firing;
    }

    private void invertDirectionAndFireFirstPosition() throws IllegalStateException {
        log.info("The last firing wasn't successful -- inverting the direction and firing the first pos");
        int index = this.strikeHistory.size() - this.actualShipStrikes;
        Position firstFiringPosition = this.strikeHistory.get(index);
        this.fireNextFreeField(firstFiringPosition, this.lastDirection.invert(), Switch.INVERT);
    }

    private Firing fireNextFreeField(Position startPosition, Direction startDirection, Switch switchType)
            throws IllegalStateException {
        Firing firing;
        try {
            log.debug("Transforming original position into the given Direction");
            Position transformedPosition = startDirection.transform(startPosition);

            if (this.historyContainsPosition(transformedPosition)) {
                throw new IllegalArgumentException("position already fired");
            }
            firing = this.fireAtEnemy(transformedPosition);

            if (firing.isStrike() && !firing.isSunk()) {
                this.alignment = startDirection.getAlignment();
            }
            else {
                this.lastFiring = startPosition;
            }
            this.lastDirection = startDirection;
            log.debug("We succesfully determined an Position and fired it!");
        }
        catch (IllegalArgumentException e) {
            log.debug("This transformed Position was invalid or already fired");

            startDirection = startDirection.invert();
            if (switchType == Switch.NEXT) {
                startDirection = startDirection.next();
            }

            if (startDirection == this.lastDirection) {
                throw new IllegalStateException();
            }
            firing = this.fireNextFreeField(startPosition, startDirection, switchType);
        }
        return firing;
    }

    private Firing fireAtEnemy(Position position) {
        Firing firing = this.gameMaster.fireAtEnemy(this, position);

        // save informations about this firing
        if (firing != null) {
            this.lastFiringStrikedShip = firing.isStrike();

            // the firing is not allowed to change shipfound to false, because
            // there could be a mislead firing
            boolean shipFound = firing.isStrike() && !firing.isSunk();
            if (shipFound) {
                this.shipFound = true;
            }
            this.lastFiring = position;

            // add to history lists
            this.saveFiring(position);

            if (firing.isStrike()) {
                this.strikeHistory.add(position);
                this.actualShipStrikes++;
            }

            // clean up
            if (firing.isSunk()) {
                this.handleShipIsSunk(firing);
            }
        }
        return firing;
    }

    private void handleShipIsSunk(Firing firing) {
        log.info("Ship is now sunk, cleaning up the helpers");

        Ship ship = firing.getShip();
        if (ship != null) {
            log.info("Marking positions around the ship as fired!");
            List<Position> positionsAroundShip = ship.getPositionsAroundShip();
            for (Position positionAround : positionsAroundShip) {
                this.saveFiring(positionAround);
            }
        }

        this.alignment = Alignment.NONE;
        this.shipFound = false;
        this.actualShipStrikes = 0;
        this.lastDirection = Direction.NORTH;
        this.lastFiring = null;
    }

    @Override
    public String toString() {
        return this.name + " (AI: hard)";
    }
}
