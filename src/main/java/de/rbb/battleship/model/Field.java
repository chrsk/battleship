package de.rbb.battleship.model;

public class Field {

	public enum State {
		WATER(' '), DROP('-'), STRIKE('x'), SHIP('o'), MARKED('#'), SUNK('%');

		private char primitive;

		private State(char c) {
			this.primitive = c;
		}

		public final char getChar() {
			return this.primitive;
		}
	}

	private State state;

	public final int x;

	public final int y;

	private final Board board;

	public Field(Board board, int x, int y) throws IllegalArgumentException {
		if (x < 0 || y < 0) {
			throw new IllegalArgumentException();
		}

		this.board = board;
		this.x = x;
		this.y = y;
	}

	public void setState(State state) {
		this.state = state;
	}

	public State getState() {
		return this.state;
	}

	public Board getBoard() {
		return this.board;
	}

	@Override
	public boolean equals(Object obj) {
		boolean equals = true;

		if (obj instanceof Field) {
			Field field = (Field) obj;
			equals = equals && field.state == this.state;
			equals = equals && field.x == this.x;
			equals = equals && field.x == this.y;
		}

		return equals;
	}
}
