package de.rbb.battleship.model;

public class Firing {
    private final boolean strike;
    private final boolean sunk;
    private final Ship ship;

    public Firing(Ship ship, boolean strike, boolean sunk) {
        this.ship = ship;
        this.strike = strike;
        this.sunk = sunk;
    }

    public Firing(boolean strike, boolean sunk) {
        this(null, strike, sunk);
    }

    public boolean isStrike() {
        return this.strike;
    }

    public boolean isSunk() {
        return this.sunk;
    }

    @Override
    public String toString() {
        return this.isSunk() ? "Firing sunk a ship" : this.isStrike() ? "Firing striked a ship"
                : "Firing striked nothing";
    }

    public Ship getShip() {
        return ship;
    }
}
