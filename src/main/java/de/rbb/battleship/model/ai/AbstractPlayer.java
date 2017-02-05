package de.rbb.battleship.model.ai;

import de.rbb.battleship.model.Board;
import de.rbb.battleship.model.GameMaster;
import de.rbb.battleship.model.Player;
import de.rbb.battleship.model.AbstractGameMaster.GameAdapter;

public abstract class AbstractPlayer extends GameAdapter implements Player {

	protected String name;

	protected Board board;

	protected GameMaster gameMaster;

	public AbstractPlayer() {
		this.name = "Human";
		this.board = new Board(this);
	}

	public boolean isReady() {
		return this.getBoard().getPlacedShipsAmount() == Board.SHIPS;
	}

	public void setName(String name) {
		if (name != null && !name.isEmpty()) {
			this.name = name;
		}
	}

	public String getName() {
		return this.name;
	}

	public Board getBoard() {
		return this.board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	@Override
	public GameMaster getGameMaster() {
		return this.gameMaster;
	}

	@Override
	public void setGameMaster(GameMaster gameMaster) {
		this.gameMaster = gameMaster;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public boolean equals(Object obj) {
		boolean equals = true;

		if (obj instanceof AbstractPlayer) {
			AbstractPlayer player = (AbstractPlayer) obj;
			equals = equals && player.board.equals(this.board);
			equals = equals && player.name.equals(this.name);
		}

		return equals;
	}
}
