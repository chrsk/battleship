/**
 * 
 */
package de.rbb.battleship.model.ai;

import de.rbb.battleship.model.Position;
import de.rbb.battleship.model.ai.HardAI.Alignment;

public enum Direction {
    NORTH(Alignment.VERTICAL, 0, -1) {
        @Override
        public Direction next() {
            return EAST;
        }
    },
    EAST(Alignment.HORIZONTAL, 1, 0) {
        @Override
        public Direction next() {
            return SOUTH;
        }
    },
    SOUTH(Alignment.VERTICAL, 0, 1) {
        @Override
        public Direction next() {
            return WEST;
        }
    },
    WEST(Alignment.HORIZONTAL, -1, 0) {
        @Override
        public Direction next() {
            return NORTH;
        }
    };

    public final int offsetX;
    public final int offsetY;
    private final Alignment alignment;

    private Direction(Alignment alignment, int offsetX, int offsetY) {
        if (offsetX > 1 || offsetX < -1 || offsetY > 1 || offsetY < -1) {
            throw new IllegalArgumentException("offset is out of range (range is from -1 to 1");
        }

        this.alignment = alignment;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public Position transform(Position pos) {
        return new Position(pos.x + this.offsetX, pos.y + this.offsetY);
    }

    public abstract Direction next();

    public Alignment getAlignment() {
        return this.alignment;
    }

    public Direction invert() {
        return this.next().next();
    }
}