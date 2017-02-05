package de.rbb.battleship.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.rbb.battleship.gui.Game;
import de.rbb.battleship.util.MP3;

public class PlaceShipsRandomAction extends AbstractAction {
	private static final long serialVersionUID = -4785877087207826764L;

	private final Game game;

	public PlaceShipsRandomAction(Game game) {
		this.game = game;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new MP3("zerknitternKurz.mp3").play();
		this.game.placeShipsRandom();
	}
}
