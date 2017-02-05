package de.rbb.battleship.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Position {

	public static final char POSITION_DELIMITER = ',';

	public final int x;

	public final int y;

	public Position(int x, int y) throws IllegalArgumentException {
		if (x < 0 || x >= Board.BOARD_CASKETS || y < 0 || y >= Board.BOARD_CASKETS) {
			throw new IllegalArgumentException("bad range (x = " + x + " y = " + y + ')');
		}

		this.x = x;
		this.y = y;
	}

	@Override
	public int hashCode() {
		return 42 * this.x + this.y;
	}

	@Override
	public boolean equals(Object obj) {
		boolean equals = true;

		if (obj instanceof Position) {
			Position pos = (Position) obj;
			equals = equals && pos.x == this.x;
			equals = equals && pos.y == this.y;
		}

		return equals;
	}

	@Override
	public String toString() {
		return "x = " + this.x + " | y = " + this.y;
	}

	public static Position parsePosition(String pos) {
		Pattern pattern = Pattern.compile("[\\d]+");
		Matcher match = pattern.matcher(pos);

		match.find();
		final String xString = match.group();

		match.find();
		final String yString = match.group();

		int x = Integer.parseInt(xString);
		int y = Integer.parseInt(yString);

		return new Position(x, y);
	}

	public static List<Position> parsePositions(String positions) {
		List<Position> positionList = new ArrayList<Position>();

		String[] split = positions.split(String.valueOf(POSITION_DELIMITER));
		for (String position : split) {
			Position parsedPosition = Position.parsePosition(position);
			positionList.add(parsedPosition);
		}

		return positionList;
	}
}
