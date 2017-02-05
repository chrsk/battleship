package de.rbb.battleship.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import de.rbb.battleship.model.Field.State;
import de.rbb.battleship.model.Ship.ShipCategory;

public class Board {

	/** Log4J Logger Instanz, siehe Log4J Dokumentation */
	private static final Logger log = Logger.getLogger(Board.class);

	public static final int BOARD_CASKETS = 10;

	public static final int SHIPS = 10;

	public static final boolean SHIPS_NEED_DISTANCE_TO_EACH_OTHER = true;

	public static final boolean MARK_BORDERING_FIELDS = true;

	private final List<List<Field>> fieldList;

	private final List<Ship> shipList;

	private final ArrayList<ChangeListener> changeListener;

	private final Player player;

	private Position lastModifiedPosition;

	private Ship lastFiringSunkAShip;

	public Board(Player player) {
		this.player = player;
		this.fieldList = new ArrayList<List<Field>>(BOARD_CASKETS);
		this.shipList = new ArrayList<Ship>(SHIPS);
		this.changeListener = new ArrayList<ChangeListener>();
		this.init();
	}

	private void init() {
		for (int i = 0; i < BOARD_CASKETS; i++) {
			ArrayList<Field> innerFieldList = new ArrayList<Field>(BOARD_CASKETS);

			for (int j = 0; j < BOARD_CASKETS; j++) {
				Field field = new Field(this, j, i);
				field.setState(State.WATER);
				innerFieldList.add(field);
			}

			this.fieldList.add(innerFieldList);
		}
	}

	public boolean addShip(Ship ship) {
		boolean success = true;
		ShipCategory category = ship.getCategory();

		int multipleShipCounter = this.getShipAmountForCategory(category);
		int quantity = category.quantity;

		if (multipleShipCounter > quantity) {
			throw new IllegalArgumentException("player already reached his maximum of ships from given type ("
					+ quantity + ')');
		}

		List<Position> positions = ship.getPositions();
		for (Position pos : positions) {
			if (SHIPS_NEED_DISTANCE_TO_EACH_OTHER) {
				// if ship lays on any position adding the ship fails
				int[][] checkAround = { { 0, 0 }, { 0, 1 }, { 1, 0 }, { 1, 1 }, { -1, 0 }, { 0, -1 }, { 1, -1 },
						{ -1, 1 }, { -1, -1 } };
				for (int[] element : checkAround) {
					if (!(element[0] == -1 && pos.x == 0 || element[0] == 1 && pos.x == 9 || element[1] == -1
							&& pos.y == 0 || element[1] == 1 && pos.y == 9)) {
						int x = pos.x + element[0];
						int y = pos.y + element[1];
						Position newPos = new Position(x, y);
						if (this.getFieldAt(newPos).getState() == State.SHIP) {
							success = false;
							break;
						}
					}
				}
			} else {
				if (this.getFieldAt(pos).getState() == State.SHIP) {
					success = false;
					break;
				}
			}
		}

		if (success) {
			this.shipList.add(ship);

			// who knows neater java, please fix it.
			Position[] pos = new Position[0];
			Position[] array = positions.toArray(pos);

			log.debug(ship);

			this.markFieldsAs(State.SHIP, array);
			this.fireChangeEvent();
		}

		return success;
	}

	public void markFieldsAs(State state, Position... positions) {
		for (Position pos : positions) {
			Field field = this.getFieldAt(pos);

			final boolean sunkChangesToStrike = field.getState() == State.SUNK && state == State.SHIP;
			final boolean stateIsNotStrike = field.getState() != State.STRIKE;
			final boolean actualStateIsSunk = state == State.SUNK;

			if ((stateIsNotStrike || actualStateIsSunk) && !sunkChangesToStrike) {
				field.setState(state);
			}
		}
	}

	public int getShipAmountForCategory(ShipCategory ship) {
		int multipleShipCounter = 0;
		for (Ship localShip : this.shipList) {
			if (localShip.getCategory() == ship) {
				multipleShipCounter++;
			}
		}

		return multipleShipCounter;
	}

	public boolean isCompleted() {
		return this.shipList.size() == Board.SHIPS;
	}

	public int getPlacedShipsAmount() {
		return this.shipList.size();
	}

	public Field getFieldAt(Position pos) {
		return this.fieldList.get(pos.x).get(pos.y);
	}

	public Firing fireAtField(Position pos) throws IllegalArgumentException {
		boolean strike = false;
		boolean sunk = false;

		Field field = this.fieldList.get(pos.x).get(pos.y);
		if (field.getState() == State.SHIP || field.getState() == State.STRIKE || field.getState() == State.SUNK) {
			field.setState(State.STRIKE);
			sunk = this.fireAtShipLaysOnPosition(pos);
			strike = true;
		} else {
			field.setState(State.DROP);
		}

		this.lastModifiedPosition = pos;

		this.fireChangeEvent();
		return new Firing(this.lastFiringSunkAShip, strike, sunk);
	}

	public Ship getLastFiringShipIfSunk() {
		return this.lastFiringSunkAShip;
	}

	public Position getLastModifiedPosition() {
		return this.lastModifiedPosition;
	}

	private boolean fireAtShipLaysOnPosition(Position pos) {
		boolean shipIsSunk = false;

		for (Ship ship : this.shipList) {
			if (ship.containsPosition(pos)) {
				ship.fireAtShip();

				this.lastFiringSunkAShip = null;
				if (ship.isSunk()) {
					this.lastFiringSunkAShip = ship;
					log.info(ship + " is sunk!");
					Position[] positions = new Position[0];
					Position[] array;

					if (SHIPS_NEED_DISTANCE_TO_EACH_OTHER && MARK_BORDERING_FIELDS) {
						array = ship.getPositionsAroundShip().toArray(positions);
						this.markFieldsAs(State.DROP, array);
					}

					array = ship.getPositions().toArray(positions);
					this.markFieldsAs(State.SUNK, array);

					shipIsSunk = true;
				}
			}
		}

		return shipIsSunk;
	}

	public void markBorderingFields(Position... array) {
		int[][] checkAround = { { 0, 0 }, { 0, 1 }, { 1, 0 }, { 1, 1 }, { -1, 0 }, { 0, -1 }, { 1, -1 }, { -1, 1 },
				{ -1, -1 } };
		for (Position position : array) {
			for (int[] is : checkAround) {
				int x = position.x + is[0];
				int y = position.y + is[1];

				try {
					Position checkAroundPosition = new Position(x, y);
					Field field = this.getFieldAt(checkAroundPosition);
					State state = field.getState();
					if (state == State.WATER) {
						this.markFieldsAs(State.DROP, checkAroundPosition);
					}
				} catch (IllegalArgumentException e) {
					// catching bad range of position, nothing to do!
				}
			}
		}
	}

	public boolean isDestroyed() {
		boolean destroyed = true;

		for (List<Field> innerFieldList : this.fieldList) {
			for (Field field : innerFieldList) {
				if (field.getState() == Field.State.SHIP) {
					destroyed = false;
					break;
				}
			}
		}

		return destroyed;
	}

	public synchronized void clearBoard() {
		for (List<Field> innerFieldList : this.fieldList) {
			for (Field field : innerFieldList) {
				field.setState(State.WATER);
			}
		}

		this.shipList.clear();
		this.fireChangeEvent();
	}

	public synchronized void placeShipsRandom() {
		this.clearBoard();
		Random r = new Random();

		for (ShipCategory category : ShipCategory.values()) {
			final int MAXIMUM_ITERATIONS = 10000;
			int iterations = 0;

			for (int i = 0; i < category.quantity && iterations < MAXIMUM_ITERATIONS; iterations++) {
				final int randomX = r.nextInt(Board.BOARD_CASKETS - 1);
				final int randomY = r.nextInt(Board.BOARD_CASKETS - 1);
				final int shipLength = category.size;

				// make xy random
				final boolean xyBool = r.nextBoolean();

				List<Position> positions = new ArrayList<Position>(shipLength);

				if (shipLength - randomX >= 0 && xyBool) {
					// creating horizontal ship
					for (int x = shipLength; x > 0; x--) {
						positions.add(new Position(x + randomX - 1, randomY));
					}
				} else if (shipLength - randomY >= 0 && !xyBool) {
					// creating vertical ship
					for (int y = shipLength; y > 0; y--) {
						positions.add(new Position(randomX, y + randomY - 1));
					}
				} else if (shipLength + randomX <= Board.BOARD_CASKETS && xyBool) {
					// creating horizontal ship
					for (int x = 0; x < shipLength; x++) {
						positions.add(new Position(x + randomX, randomY));
					}
				} else if (shipLength + randomY <= Board.BOARD_CASKETS && !xyBool) {
					// creating vertical ship
					for (int y = 0; y < shipLength; y++) {
						positions.add(new Position(randomX, y + randomY));
					}
				} else {
					// no fitting position was created, try again
					continue;
				}

				Ship ship = new Ship(category, positions);
				if (this.addShip(ship)) {
					// mark this iteration as successful and switch to next
					i++;
				}
			}
		}

		if (Board.SHIPS != this.getPlacedShipsAmount()) {
			this.placeShipsRandom();
		}
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();

		for (List<Field> innerFieldList : this.fieldList) {
			for (Field field : innerFieldList) {
				stringBuilder.append(field.getState().getChar());
				stringBuilder.append(' ');
			}
			stringBuilder.append("\n");
		}
		return stringBuilder.toString();
	}

	public char[][] asPrimitiveBoard() {
		char[][] primitives = new char[Board.BOARD_CASKETS][Board.BOARD_CASKETS];
		for (int x = 0; x < this.fieldList.size(); x++) {
			List<Field> list = this.fieldList.get(x);
			for (int y = 0; y < primitives.length; y++) {
				Field field = list.get(y);
				primitives[x][y] = field.getState().getChar();
			}
		}

		return primitives;
	}

	public void mapPrimitivesToBoard(char[][] primitives) {
		if (primitives.length != Board.BOARD_CASKETS || primitives[0].length != Board.BOARD_CASKETS) {
			throw new IllegalArgumentException("array hasn't the right size");
		}

		this.clearBoard();

		for (int x = 0; x < primitives.length; x++) {
			char[] innerPrimitives = primitives[x];
			for (int y = 0; y < innerPrimitives.length; y++) {
				char primitive = innerPrimitives[y];

				Field field = this.fieldList.get(x).get(y);

				for (State state : State.values()) {
					if (state.getChar() == primitive) {
						field.setState(state);
						break;
					}
				}
				primitives[x][y] = field.getState().getChar();
			}
		}
	}

	public void addChangeListener(ChangeListener changeListener) {
		this.changeListener.add(changeListener);
	}

	public void fireChangeEvent() {
		for (ChangeListener listener : this.changeListener) {
			listener.stateChanged(new ChangeEvent(this));
		}
	}

	public void findAndRemoveShip(Position pos) {
		for (Ship ship : this.shipList) {
			if (ship.containsPosition(pos)) {
				this.shipList.remove(ship);
				final List<Position> positions = ship.getPositions();

				Position[] position = new Position[0];
				Position[] array = positions.toArray(position);

				log.debug("REMOVED: " + ship);

				this.markFieldsAs(State.WATER, array);
				this.fireChangeEvent();
				break;
			}
		}
	}

	public Player getPlayer() {
		return this.player;
	}

	@Override
	public boolean equals(Object obj) {
		boolean equals = true;

		if (obj instanceof Board) {
			Board board = (Board) obj;
			equals = equals && board.fieldList.equals(this.fieldList);
			equals = equals && board.shipList.equals(this.shipList);
		}

		return equals;
	}

	public List<Ship> getShipList() {
		return this.shipList;
	}

	public void mapShipsToBoard(int[][][] ships1) {
		for (int[][] is : ships1) {
			ShipCategory shipCategory = null;
			for (ShipCategory category : ShipCategory.values()) {
				if (category.size == is.length) {
					shipCategory = category;
					break;
				}
			}
			List<Position> list = new ArrayList<Position>();

			for (int[] element : is) {
				list.add(new Position(element[1], element[0]));
			}

			Ship ship = new Ship(shipCategory, list);
			this.shipList.add(ship);
		}
	}

	public List<Position> getPositionsWhereAlreadyFired() {
		List<Position> posList = new ArrayList<Position>();

		for (List<Field> list : this.fieldList) {
			for (Field field : list) {
				final State state = field.getState();
				if (field.getState() == State.DROP || state == State.STRIKE) {
					posList.add(new Position(field.y, field.x));
				}
			}
		}

		return posList;
	}
}
