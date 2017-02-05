package de.rbb.battleship.model;

import de.rbb.battleship.model.AbstractGameMaster.GameListener;

public interface Player extends GameListener {

	void init();

	void setGameMaster(GameMaster gameMaster);

	GameMaster getGameMaster();

	boolean isReady();

	void setName(String name);

	String getName();

	Board getBoard();

}