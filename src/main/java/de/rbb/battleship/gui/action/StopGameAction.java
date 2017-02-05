package de.rbb.battleship.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.rbb.battleship.gui.Game;

public class StopGameAction extends AbstractAction {

	private static final long serialVersionUID = 8844872708197420335L;

	private final Game game;

	public StopGameAction(Game game) {
		this.game = game;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.game.getGameMaster().abort();
	}

}
