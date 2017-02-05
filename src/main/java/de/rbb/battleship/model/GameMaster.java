package de.rbb.battleship.model;

import javax.swing.event.ChangeEvent;

import de.rbb.battleship.core.GameMode;
import de.rbb.battleship.model.AbstractGameMaster.GameListener;

public interface GameMaster {

    public static final boolean SET_PLAYER_ON_TURN_AGAIN_AFTER_STRIKE = true;

    void finish();

    void abort();

    void start();

    void setPlayerReady(Player player, boolean ready);

    Firing fireAtEnemy(Player theFiringPlayer, Position pos);

    Player getPlayer1();

    Player getPlayer2();

    void addGameListener(GameListener listener);

    void stateChanged(ChangeEvent e);

    boolean isRunning();

    boolean isAborted();

    boolean isFinished();

    GameMode getGameMode();
}