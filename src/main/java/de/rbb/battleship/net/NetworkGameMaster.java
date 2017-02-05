package de.rbb.battleship.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import de.rbb.battleship.core.GameMode;
import de.rbb.battleship.gui.Chat.ChatStyle;
import de.rbb.battleship.gui.ChatListener.ChatMessage;
import de.rbb.battleship.model.AbstractGameMaster;
import de.rbb.battleship.model.Board;
import de.rbb.battleship.model.Field;
import de.rbb.battleship.model.Firing;
import de.rbb.battleship.model.Player;
import de.rbb.battleship.model.Position;
import de.rbb.battleship.model.Ship;
import de.rbb.battleship.model.Field.State;
import de.rbb.battleship.model.ai.HumanPlayer;

public abstract class NetworkGameMaster extends AbstractGameMaster implements ChangeListener {

	/** Log4J Logger Instanz, siehe Log4J Dokumentation */
	private static final Logger log = Logger.getLogger(NetworkGameMaster.class);

	protected boolean isConnected;

	protected Socket socket;

	protected Position lastFiringPosition;

	protected boolean thisPlayerIsOnTurn;

	private BufferedReader inputReader;

	public NetworkGameMaster() {
		this.player1 = new HumanPlayer();
		this.player2 = new NetworkPlayer();

		this.init();
	}

	private void init() {
		this.player1.setGameMaster(this);
		this.player2.setGameMaster(this);

		this.addGameListener(this.player1);
		this.addGameListener(this.player2);

		this.player1.init();
		this.player2.init();

		this.player1.getBoard().addChangeListener(this);
		this.player2.getBoard().addChangeListener(this);
	}

	@Override
	public void abort() {
		if (!this.isAborted) {
			log.info("Network Game was aborted, client shutting down.");
			this.isConnected = false;
			this.isRunning = false;
			this.isAborted = true;
			this.sendResponse(Protocol.END_GAME);

			try {
				log.debug("Trying to release the resources");
				this.releaseResources();
			} catch (Exception e) {
				log.error("Couldn't close client socket");
			} finally {
				this.fireGameAborted();
			}
		}
	}

	protected void handleChatMessage(String response) {
		String chatMessage = response.substring(Protocol.CHAT.toString().length()).trim();
		ChatMessage message = new ChatMessage(ChatStyle.SERVER, this.player2.getName(), chatMessage);

		this.fireChatMessageEvent(message);
	}

	protected boolean handleFiringAnswer(String response) {
		String stateOfField = response.substring(Protocol.STRIKE.toString().length()).trim();
		boolean strike = false;

		try {
			State state = Field.State.valueOf(stateOfField);
			final Board board = this.player2.getBoard();

			board.markFieldsAs(state, this.lastFiringPosition);
			board.fireChangeEvent();

			if (state.equals(State.DROP)) {
				this.fireTurnEventToPlayer(this.player2);
			} else {
				strike = true;
			}
			// clearing last firing position
			this.lastFiringPosition = null;
		} catch (IllegalArgumentException ex) {
			log.error("State of Field is not known");
		}

		this.changePlayerTurn(!strike);

		return strike;
	}

	private void changePlayerTurn(boolean strike) {
		if (strike) {
			this.thisPlayerIsOnTurn = false;
		} else {
			this.thisPlayerIsOnTurn = true;
		}
	}

	@Override
	public Firing fireAtEnemy(Player theFiringPlayer, Position pos) {
		if (this.thisPlayerIsOnTurn) {
			log.info("Fire occured from " + (this.isServer() ? "server." : "client."));
			// ignore the firing player here and fire at client position

			final boolean lastFiringIsAlreadyHandled = this.lastFiringPosition == null;
			if (lastFiringIsAlreadyHandled) {
				this.lastFiringPosition = pos;
				this.sendResponse(Protocol.FIRE, pos.toString());
			} else {
				log.info("Last Firing is not handled");
			}
		} else {
			log.info("Fire is not allowed yet");
		}

		return null;
	}

	protected boolean handleFiring(String receive) throws IllegalArgumentException {
		String position = receive.substring(Protocol.FIRE.toString().length());
		Position parsedPosition = Position.parsePosition(position);

		final Board board = this.player1.getBoard();
		boolean fieldIsDestroyed = board.fireAtField(parsedPosition).isStrike();

		State state = State.DROP;
		if (fieldIsDestroyed) {
			state = State.STRIKE;
		} else {
			// change the turn
			this.fireTurnEventToPlayer(this.player1);
		}

		this.sendResponse(Protocol.STRIKE, state.toString());
		this.sendSunkState(board);

		if (this.player1.getBoard().isDestroyed()) {
			this.sendResponse(Protocol.DESTROYED);
			this.fireGameFinished(this.player2);
		}

		this.changePlayerTurn(fieldIsDestroyed);

		return fieldIsDestroyed;
	}

	protected void sendShips() {
		List<Ship> shipList = this.player1.getBoard().getShipList();
		for (Ship ship : shipList) {
			this.sendResponse(Protocol.SHIPS, ship.getPositionsAsString());
		}
	}

	private void sendSunkState(final Board board) {
		// determine if last firing sunk a ship
		final Ship ship = board.getLastFiringShipIfSunk();

		if (ship != null) {
			this.sendResponse(Protocol.SUNK, ship.getPositionsAsString());
		} else {
			this.sendResponse(Protocol.NOT_SUNK);
		}
	}

	protected void handlePlayerName(String response) {
		String name = response.substring(Protocol.NAME.toString().length()).trim();
		this.player2.setName(name);
	}

	protected void handleLastFiringSunksShip(String response) {
		String positions = response.substring(Protocol.SUNK.toString().length()).trim();
		List<Position> parsedPositions = Position.parsePositions(positions);

		log.info("Server sunk a ship with these positions: " + parsedPositions);
		final Position[] positionArray = parsedPositions.toArray(new Position[0]);
		final Board board = this.player2.getBoard();

		board.markFieldsAs(State.SUNK, positionArray);
		board.markBorderingFields(positionArray);
		board.fireChangeEvent();

	}

	protected void handleShips(String response) {
		String positions = response.substring(Protocol.SUNK.toString().length()).trim();
		List<Position> parsedPositions = Position.parsePositions(positions);

		log.info("Get positions of ship: " + parsedPositions);
		final Position[] positionArray = parsedPositions.toArray(new Position[0]);
		final Board board = this.player2.getBoard();

		board.markFieldsAs(State.SHIP, positionArray);
		board.markBorderingFields(positionArray);
		board.fireChangeEvent();
	}

	protected void handleComplexProtocolMessages(String receive) throws IllegalArgumentException {
		log.info("Message is complex, starting to parse it");

		if (receive.startsWith(Protocol.FIRE.toString())) {
			this.handleFiring(receive);
		} else if (receive.startsWith(Protocol.STRIKE.toString())) {
			this.handleFiringAnswer(receive);
		} else if (receive.startsWith(Protocol.SUNK.toString())) {
			this.handleLastFiringSunksShip(receive);
		} else if (receive.startsWith(Protocol.NAME.toString())) {
			this.handlePlayerName(receive);
		} else if (receive.startsWith(Protocol.CHAT.toString())) {
			this.handleChatMessage(receive);
		} else if (receive.startsWith(Protocol.SHIPS.toString())) {
			this.handleShips(receive);
		}
	}

	protected String getRequest() {
		log.info("Waiting for message from client ...");

		String response = null;

		try {
			BufferedReader in = this.getReader();
			response = in.readLine();

			// Trying to read object from Stream
			// Object object = new ObjectInputStream(inputStream).readObject();

			if (response == null) {
				// experimental!
				log.warn("The response was 'null', closing the server");
				this.abort();
			}
			log.info("Received message from client: " + response);
		} catch (NullPointerException e) {
			log.error("Sockets wasnt initialized");
		} catch (SocketException e) {
			log.info("Socket was closed, shutting down the server");
		} catch (IOException e) {
			log.error("Can't read message from client socket", e);
		}

		return response;
	}

	private BufferedReader getReader() throws IOException {
		if (this.inputReader == null) {
			final InputStream inputStream = this.socket.getInputStream();
			final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			this.inputReader = new BufferedReader(inputStreamReader);
		}
		return this.inputReader;
	}

	protected void sendResponse(Protocol type, String... text) {
		String responseToLog = "Sending protocol type:" + type + " arguments: ";
		for (String string : text) {
			responseToLog += string;
		}

		log.info(responseToLog);
		try {
			PrintStream writer = new PrintStream(this.socket.getOutputStream());

			StringBuilder argumentsBuilder = new StringBuilder();
			for (String string : text) {
				argumentsBuilder.append(' ');
				argumentsBuilder.append(string);
			}
			writer.println(type + argumentsBuilder.toString());

			log.debug("Sending was successful");
		} catch (IOException e) {
			log.error("Couldn't sending to client", e);
		} catch (NullPointerException e) {
			log.error("Socket may not be initialized");
		}
	}

	public void sendChatMessage(String text) {
		this.sendResponse(Protocol.CHAT, text);
	}

	public boolean isConnected() {
		return this.isConnected;
	}

	@Override
	public GameMode getGameMode() {
		return GameMode.NETWORKGAME;
	}

	public abstract boolean isServer();

	public abstract String getConnection();

	protected abstract void releaseResources() throws IOException;

}
