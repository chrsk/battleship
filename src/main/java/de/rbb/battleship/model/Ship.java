package de.rbb.battleship.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class Ship {

    /** Log4J Logger Instanz, siehe Log4J Dokumentation */
    private static final Logger log = Logger.getLogger(Ship.class);

    public enum ShipCategory {

        BATTLESHIP(5), DESTROYER(4, 2), CRUISER(3, 3), SUBMARINE(2, 4);

        public static final int MINIMUM_SIZE = 1;

        public final int quantity;
        public final int size;

        private ShipCategory(int size, int quantity) {
            this.quantity = quantity;
            this.size = size;
        }

        private ShipCategory(int size) {
            this(size, 1);
        }

        @Override
        public String toString() {
            return super.toString() + '(' + this.quantity + ',' + this.size + ')';
        }
    }

    private final ShipCategory category;

    private int shipInviolate;

    private final List<Position> positions;

    public Ship(ShipCategory category, List<Position> positions) throws IllegalArgumentException {
        if (category == null) {
            throw new IllegalArgumentException("arguments can't be null");
        }

        if (positions.size() != category.size) {
            throw new IllegalArgumentException("positions length is not comptable to this ship category");
        }

        int finalX = positions.get(0).x, finalY = positions.get(0).y;
        for (Position position : positions) {
            if (finalX != position.x && finalY != position.y) {
                throw new IllegalArgumentException("your positions are not linear");
            }
        }

        this.shipInviolate = category.size;
        this.positions = positions;
        this.category = category;
    }

    @Override
    public int hashCode() {
        final int multiplier = 42;

        return multiplier * this.getCategory().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        boolean equals = true;
        Ship ship;

        if (obj instanceof Ship) {
            ship = (Ship) obj;

            equals = equals && ship.category == this.category;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ship [");
        stringBuilder.append(this.category.toString());
        stringBuilder.append("] lays on coordinates ");

        for (Position position : this.positions) {
            stringBuilder.append('{');
            stringBuilder.append(position.x);
            stringBuilder.append('|');
            stringBuilder.append(position.y);
            stringBuilder.append('}');
        }

        return stringBuilder.toString();
    }

    public String getPositionsAsString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (Position position : this.positions) {
            stringBuilder.append(position.x);
            stringBuilder.append('-');
            stringBuilder.append(position.y);
            stringBuilder.append(Position.POSITION_DELIMITER);
        }

        return stringBuilder.toString();
    }

    public List<Position> getPositionsAroundShip() {
        List<Position> positionList = new ArrayList<Position>();
        int[][] checkAround = { { 0, 0 }, { 0, 1 }, { 1, 0 }, { 1, 1 }, { -1, 0 }, { 0, -1 }, { 1, -1 }, { -1, 1 },
                { -1, -1 } };
        Position[] array = this.positions.toArray(new Position[0]);
        for (Position position : array) {
            for (int[] is : checkAround) {
                int x = position.x + is[0];
                int y = position.y + is[1];

                try {
                    Position checkAroundPosition = new Position(x, y);
                    positionList.add(checkAroundPosition);
                }
                catch (IllegalArgumentException e) {
                    // catching bad range of position, nothing to do!
                }
            }
        }

        return positionList;
    }

    public void fireAtShip() throws IllegalArgumentException {
        if (this.shipInviolate <= 0) {
            throw new IllegalStateException("ship is already sunk");
        }

        this.shipInviolate--;
        log.debug(this + " was fired, now has " + this.shipInviolate + " fields left");
    }

    public boolean isSunk() {
        return this.shipInviolate == 0 ? true : false;
    }

    public boolean containsPosition(Position pos) {
        return this.positions.contains(pos);
    }

    public ShipCategory getCategory() {
        return this.category;
    }

    public List<Position> getPositions() {
        return this.positions;
    }
}
