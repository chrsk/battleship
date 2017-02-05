package de.rbb.battleship.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

import de.rbb.battleship.gui.Game;
import de.rbb.battleship.model.Player;
import de.rbb.battleship.persistence.Persistence;

public class SaveGameAction extends AbstractAction {

	/** Log4J Logger Instanz, siehe Log4J Dokumentation */
	private static final Logger log = Logger.getLogger(SaveGameAction.class);

	private static final long serialVersionUID = 8443414531780131591L;

	private final Persistence persistence;

	private final Game game;

	public SaveGameAction(Game game) {
		this.game = game;
		this.persistence = new Persistence();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		log.info("persisting actual game");

		final Player player1 = this.game.getGameMaster().getPlayer1();
		final Player player2 = this.game.getGameMaster().getPlayer2();

		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			this.persistence.saveBoards(file, player1.getBoard(), player2.getBoard());
		}
	}

}
