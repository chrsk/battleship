package de.rbb.battleship.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.rbb.battleship.model.Board;
import de.rbb.battleship.model.Position;

public class RemoveShipAction extends AbstractAction {

	private static final long serialVersionUID = 2511464705298686892L;

	private final Position pos;

	private final Board board;

	public RemoveShipAction(Board board, Position pos) {
		this.board = board;
		this.pos = pos;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.board.findAndRemoveShip(this.pos);
	}

}
