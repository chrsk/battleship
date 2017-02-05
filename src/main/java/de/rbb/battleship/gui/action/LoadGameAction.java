package de.rbb.battleship.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import de.rbb.battleship.gui.Game;
import de.rbb.battleship.model.Player;
import de.rbb.battleship.model.ai.EasyAI;
import de.rbb.battleship.model.ai.AIGameMaster;
import de.rbb.battleship.model.ai.HumanPlayer;
import de.rbb.battleship.persistence.Persistence;

public class LoadGameAction extends AbstractAction {

	private static final long serialVersionUID = 7174829803511360274L;

	private final Game game;

	private final Persistence persistence;

	public LoadGameAction(Game game) {
		this.game = game;
		this.persistence = new Persistence();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			this.persistence.loadBoards(file);

			Player humanPlayer = new HumanPlayer();
			humanPlayer.getBoard().mapPrimitivesToBoard(this.persistence.getBoard1Primitives());
			humanPlayer.getBoard().mapShipsToBoard(this.persistence.getShips1());

			Player aiPlayer = new EasyAI(true);
			aiPlayer.getBoard().mapPrimitivesToBoard(this.persistence.getBoard2Primitives());
			aiPlayer.getBoard().mapShipsToBoard(this.persistence.getShips2());

			final AIGameMaster gameMaster = new AIGameMaster(humanPlayer, aiPlayer);

			this.game.gameAborted(null);
			this.game.setGameMaster(gameMaster);
		}

	}

}
