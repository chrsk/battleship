package de.rbb.battleship.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;

import de.rbb.battleship.model.Player;

public class ServerGameMaster extends NetworkGameMaster {

	/** Log4J Logger Instanz, siehe Log4J Dokumentation */
	private static final Logger log = Logger.getLogger(ServerGameMaster.class);

	private ServerSocket serverSocket;

	public ServerGameMaster() throws IllegalStateException {
		super();
	}

	@Override
	public void run() {
		this.isConnected = true;
		this.isRunning = false;
		this.isAborted = false;
		this.isFinished = false;

		log.info("Waiting for starting the game");

		this.fireGameCreated();
		this.sendResponse(Protocol.NAME, this.player1.getName());
		while (ServerGameMaster.this.isConnected()) {
			String response = this.getRequest();
			this.handleRequest(response);
		}
	}

	private void handleRequest(String response) {
		if (response != null) {
			try {
				Protocol protocol = Protocol.valueOf(response);
				if (protocol != null) {
					switch (protocol) {
					case READY:
						this.handleClientIsReady(true);
						break;
					case UNREADY:
						this.handleClientIsReady(false);
						break;
					case NOT_SUNK:
						this.lastFiringPosition = null;
						break;
					case END_GAME:
						this.abort();
						break;
					case DESTROYED:
						this.sendShips();
						this.sendResponse(Protocol.END_GAME);
						this.fireGameFinished(this.player1);
						break;
					default:
						break;
					}
				}
			} catch (IllegalArgumentException e) {
				this.handleComplexProtocolMessages(response);
			}
		}
	}

	private void handleClientIsReady(boolean ready) {
		this.setPlayerReady(this.player2, ready);
	}

	public Socket hostGame(int port) {
		Socket socket = null;
		this.serverSocket = null;
		this.thisPlayerIsOnTurn = true;

		log.info("Starting Server");

		try {
			this.serverSocket = new ServerSocket(port);
			this.serverSocket.setSoTimeout(Server.SERVER_TIMEOUT);

			// Waiting for one connection
			this.socket = socket = this.serverSocket.accept();
			log.info("Connection accepted, server is started");
		} catch (SocketException e) {
			log.error("Socket was closed");
		} catch (IOException e) {
			log.error("Couldn't open port or was aborted", e);
		} finally {
			this.isConnected = false;
		}

		return socket;
	}

	@Override
	protected void releaseResources() throws IOException, NullPointerException {
		this.serverSocket.close();
		this.socket.close();
	}

	@Override
	public void finish() {
		// finish the game
	}

	@Override
	public void setPlayerReady(Player player, boolean ready) {
		if (player == null) {
			throw new NullArgumentException("player");
		}

		if (player.equals(this.player1)) {
			log.info("Server-Player is now " + (ready ? "ready!" : "not ready!"));
			this.player1IsReady = ready;
		} else {
			log.info("Client-Player is now " + (ready ? "ready!" : "not ready!"));
			this.player2IsReady = ready;
		}

		if (this.player1IsReady && this.player2IsReady) {
			log.info("Both players are ready ... starting the game!");
			this.sendResponse(Protocol.START_GAME);
			this.isRunning = true;
			this.fireGameStarted();
		}
	}

	@Override
	public String getConnection() {
		return this.socket.getInetAddress().getHostAddress() + " (Server)";
	}

	@Override
	public boolean isServer() {
		return true;
	}
}
