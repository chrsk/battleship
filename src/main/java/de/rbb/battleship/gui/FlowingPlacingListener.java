package de.rbb.battleship.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import de.rbb.battleship.gui.action.RemoveShipAction;
import de.rbb.battleship.model.Board;
import de.rbb.battleship.model.Field;
import de.rbb.battleship.model.Position;
import de.rbb.battleship.model.Ship;
import de.rbb.battleship.model.Ship.ShipCategory;
import de.rbb.battleship.util.MP3;

public class FlowingPlacingListener extends MouseAdapter {

	/** Log4J Logger Instanz, siehe Log4J Dokumentation */
	private static final Logger log = Logger.getLogger(FlowingPlacingListener.class);

	private boolean enabled = true;

	private boolean flowingIsActive;

	private int startPosX, startPosY, endPosX, endPosY;

	private Board board;

	private final Game game;

	public FlowingPlacingListener(Game game) {
		this.game = game;
		this.resetPositions();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		this.board = this.game.getGameMaster().getPlayer1().getBoard();
		if (this.enabled) {

			final Object source = e.getSource();
			if (source instanceof Casket) {
				Field startField = ((Casket) e.getSource()).getField();

				if (startField != null) {
					if (SwingUtilities.isRightMouseButton(e)) {
						log.debug("Removing Ship at Position: x = " + startField.x + " y = " + startField.y);
						new RemoveShipAction(this.board, new Position(startField.y, startField.x))
								.actionPerformed(null);
					} else {

						if (this.board.getPlacedShipsAmount() != Board.SHIPS) {
							this.flowingIsActive = true;

							this.startPosX = startField.x;
							this.startPosY = startField.y;

							log.debug("Start Position is set to: x = " + this.startPosX + " y = " + this.startPosY);
						}
					}
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (this.enabled && this.flowingIsActive) {
			final Object source = e.getSource();
			if (source instanceof Casket) {
				this.game.clearCasketsFromMarkingState();

				Field actualField = ((Casket) e.getSource()).getField();

				log.debug("Actual Position is set to: x = " + actualField.x + " y = " + actualField.y);

				this.endPosX = actualField.x;
				this.endPosY = actualField.y;

				ShipCategory category = this.determineShipCategory(this.startPosX, this.startPosY, actualField.x,
						actualField.y);

				final int shipAmount = this.board.getShipAmountForCategory(category);
				if (category == null || shipAmount == category.quantity) {
					log.debug("Illegal ship determined or category is already full!");
				} else {
					// paint ship, its legal!
					List<Position> posList = this.createPositionList(this.startPosX, this.startPosY, actualField.x,
							actualField.y);

					for (Position position : posList) {
						final Casket casket = this.game.getCasketFromOwnBoardAt(position);
						casket.setMarked(true);
						casket.repaint();
					}

				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (this.enabled && this.flowingIsActive) {
			this.flowingIsActive = false;
			if (this.startPosX != -1 && this.endPosY != -1 && this.startPosY != -1 && this.endPosX != -1) {

				this.game.clearCasketsFromMarkingState();

				Ship ship = this.createShip();
				if (ship != null) {
					boolean shipIsAdded = this.board.addShip(ship);
					if (shipIsAdded) {
						final String[] filenames = { "kreuz.mp3", "kreis1.mp3", "kreis2.mp3", "strich.mp3" };
						new MP3(filenames[new Random().nextInt(filenames.length - 1)]).play(ship.getCategory().size);
					}
				}
			}
		}
	}

	private Ship createShip() {
		List<Position> posList = this.createPositionList(this.startPosX, this.startPosY, this.endPosX, this.endPosY);

		ShipCategory shipCategory = this.determineShipCategory(this.startPosX, this.startPosY, this.endPosX,
				this.endPosY);

		this.resetPositions();
		Ship ship = null;

		final int shipAmount = this.board.getShipAmountForCategory(shipCategory);
		if (shipCategory != null && shipAmount != shipCategory.quantity) {
			if (posList.size() > ShipCategory.MINIMUM_SIZE) {
				ship = new Ship(shipCategory, posList);
			}
		}

		return ship;
	}

	private void resetPositions() {
		this.startPosX = -1;
		this.startPosY = -1;
		this.endPosX = -1;
		this.endPosY = -1;
	}

	private List<Position> createPositionList(int startPosX, int startPosY, int endPosX, int endPosY)
			throws IllegalArgumentException {

		int constantValue, startPos, endPos;

		if (endPosX > startPosX) {
			// ship is horizontal and goes right
			constantValue = startPosY;
			startPos = startPosX;
			endPos = endPosX;
		} else if (endPosX < startPosX) {
			// ship is horizontal and goes left
			constantValue = startPosY;
			startPos = endPosX;
			endPos = startPosX;
		} else if (endPosY > startPosY) {
			// ship is vertical and goes down
			constantValue = startPosX;
			startPos = startPosY;
			endPos = endPosY;
		} else {
			// ship is vertical and goes up
			constantValue = startPosX;
			startPos = endPosY;
			endPos = startPosY;
		}

		final boolean vertical = startPosX - endPosX == 0;
		List<Position> posList = new ArrayList<Position>();
		for (int actualPos = startPos; actualPos <= endPos; actualPos++) {
			Position pos;
			if (vertical) {
				pos = new Position(actualPos, constantValue);
			} else {
				pos = new Position(constantValue, actualPos);
			}
			posList.add(pos);
		}

		return posList;
	}

	private ShipCategory determineShipCategory(int startPosX, int startPosY, int endPosX, int endPosY) {
		int shipLength;
		ShipCategory shipCategory = null;

		if (endPosX > startPosX) {
			// ship is horizontal and goes right
			shipLength = endPosX - startPosX;
		} else if (endPosX < startPosX) {
			// ship is horizontal and goes left
			shipLength = startPosX - endPosX;
		} else if (endPosY > startPosY) {
			// ship is vertical and goes down
			shipLength = endPosY - startPosY;
		} else {
			// ship is vertical and goes up
			shipLength = startPosY - endPosY;
		}

		// increment in cause of self position
		shipLength++;
		for (ShipCategory category : ShipCategory.values()) {
			if (shipLength == category.size) {
				shipCategory = category;
				break;
			}
		}

		log.debug("determining ship category, length is = " + shipLength + " found category = " + shipCategory
				+ " positions = [" + startPosX + '|' + startPosY + "] - [" + endPosX + '|' + endPosY + ']');

		return shipCategory;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return this.enabled;
	}
}
