package de.rbb.battleship.persistence;

import java.beans.DefaultPersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import de.rbb.battleship.model.Board;
import de.rbb.battleship.model.Position;
import de.rbb.battleship.model.Ship;

public class Persistence {

	private char[][] board1Primitives;

	private char[][] board2Primitives;

	private int[][][] ships1;

	private int[][][] ships2;

	public Persistence() {

	}

	public Persistence(char[][] board1Primitives, char[][] board2Primitives, int[][][] ships1, int[][][] ships2) {
		this.board1Primitives = board1Primitives;
		this.board2Primitives = board2Primitives;
		this.ships1 = ships1;
		this.ships2 = ships2;
	}

	public synchronized void saveBoards(File file, Board board1, Board board2) {
		this.board1Primitives = board1.asPrimitiveBoard();
		this.board2Primitives = board2.asPrimitiveBoard();

		this.ships1 = this.getShipsFromBoard(board1);
		this.ships2 = this.getShipsFromBoard(board1);

		XMLEncoder encoder = null;
		try {
			encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String[] propertyNames = new String[] { "board1Primitives", "board2Primitives", "ships1", "ships2" };
		encoder.setPersistenceDelegate(Persistence.class, new DefaultPersistenceDelegate(propertyNames));

		encoder.writeObject(this);
		encoder.close();
	}

	private int[][][] getShipsFromBoard(Board board1) {
		int[][][] object = new int[Board.SHIPS][][];
		final List<Ship> shipList = board1.getShipList();
		for (int i = 0; i < shipList.size(); i++) {
			List<Position> positions = shipList.get(i).getPositions();
			object[i] = new int[positions.size()][];
			for (int j = 0; j < positions.size(); j++) {
				Position position = positions.get(j);

				object[i][j] = new int[2];
				object[i][j][0] = position.x;
				object[i][j][1] = position.y;
			}
		}

		return object;
	}

	public synchronized void loadBoards(File file) {
		XMLDecoder decoder = null;
		try {
			decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(file)));
		} catch (FileNotFoundException e) {
		}

		Persistence persistence = (Persistence) decoder.readObject();
		this.board1Primitives = persistence.board1Primitives;
		this.board2Primitives = persistence.board2Primitives;

		this.ships1 = persistence.ships1;
		this.ships2 = persistence.ships2;
	}

	public char[][] getBoard1Primitives() {
		return this.board1Primitives;
	}

	public char[][] getBoard2Primitives() {
		return this.board2Primitives;
	}

	public int[][][] getShips1() {
		return this.ships1;
	}

	public int[][][] getShips2() {
		return this.ships2;
	}

	public void setBoard1Primitives(char[][] board1Primitives) {
		this.board1Primitives = board1Primitives;
	}

	public void setBoard2Primitives(char[][] board2Primitives) {
		this.board2Primitives = board2Primitives;
	}
}
