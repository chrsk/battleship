package de.rbb.battleship.gui;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import de.prodv.framework.configuration.ResourceData;
import de.prodv.framework.configuration.ResourceService;
import de.rbb.battleship.core.GameMode;
import de.rbb.battleship.model.Board;
import de.rbb.battleship.model.Field;
import de.rbb.battleship.model.GameMaster;
import de.rbb.battleship.model.Player;
import de.rbb.battleship.model.Position;
import de.rbb.battleship.model.Field.State;
import de.rbb.battleship.model.ai.HumanPlayer;
import de.rbb.battleship.util.ServiceLocator;

public class Casket extends JLabel {

	private static final long serialVersionUID = 1482541677950906340L;

	private static final ResourceService resourceService = ServiceLocator.getResourceService();

	private final ResourceData resourceData;

	private Field field;

	private boolean marked;

	public Casket(Field field) {
		this.resourceData = resourceService.getResourceData("lang.main");
		this.field = field;
		this.init();
	}

	private void init() {
		this.setOpaque(false);
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Player player = Casket.this.field.getBoard().getPlayer();
				GameMaster gameMaster = player.getGameMaster();

				if (!(player instanceof HumanPlayer)) {
					if (gameMaster.isRunning()) {
						State state = Casket.this.field.getState();
						if (state != State.STRIKE && state != State.DROP && state != State.SUNK) {
							this.fireAtSelectedField(e);
						}
					}
				}
			}

			private void fireAtSelectedField(MouseEvent e) throws IllegalArgumentException {
				boolean leftMouseButton = SwingUtilities.isLeftMouseButton(e);

				if (leftMouseButton) {
					final int fieldX = Casket.this.field.x;
					final int fieldY = Casket.this.field.y;
					final Position pos = new Position(fieldY, fieldX);
					final Board board = Casket.this.field.getBoard();
					final GameMaster gameMaster = board.getPlayer().getGameMaster();

					gameMaster.fireAtEnemy(gameMaster.getPlayer1(), pos);
				}

				Casket.this.repaint();
			}
		});
	}

	public Casket() {
		this(null);
	}

	/**
	 * Überschreibt die getIcon Methode, welche hier entscheidet welches Icon
	 * ausgewählt wird in dem der Feld-Typ geprüft wird.
	 */
	@Override
	public Icon getIcon() {
		ImageIcon result = null;

		if (this.field != null) {
			State type = this.field.getState();

			switch (type) {
			case SHIP:
				final Player player = Casket.this.field.getBoard().getPlayer();
				final GameMaster gameMaster = player.getGameMaster();

				if (player instanceof HumanPlayer || gameMaster.isAborted() || gameMaster.isFinished()
						|| gameMaster.getGameMode() != GameMode.SINGLEPLAYER) {
					result = (ImageIcon) this.resourceData.getIcon("ship");
				}
				break;
			case STRIKE:
				result = (ImageIcon) this.resourceData.getIcon("ship_hit");

				Position lastModifiedPosition = this.field.getBoard().getLastModifiedPosition();
				if (lastModifiedPosition != null && this.field.x == lastModifiedPosition.y
						&& this.field.y == lastModifiedPosition.x) {
					result = (ImageIcon) this.resourceData.getIcon("ship_hit_highlight");
				}
				break;
			case DROP:
				result = (ImageIcon) this.resourceData.getIcon("drop");

				lastModifiedPosition = this.field.getBoard().getLastModifiedPosition();
				if (lastModifiedPosition != null && this.field.x == lastModifiedPosition.y
						&& this.field.y == lastModifiedPosition.x) {
					result = (ImageIcon) this.resourceData.getIcon("drop_highlight");
				}
				break;
			case SUNK:
				result = (ImageIcon) this.resourceData.getIcon("sunk");
				break;
			default:
				break;
			}

			if (this.marked) {
				result = (ImageIcon) this.resourceData.getIcon("marked");
			}
		}

		return result;
	}

	public void setField(Field field) {
		this.field = field;
		this.repaint();
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}

	public boolean isMarked() {
		return this.marked;
	}

	public Field getField() {
		return this.field;
	}
}
