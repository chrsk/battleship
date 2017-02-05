package de.rbb.battleship.net;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import de.rbb.battleship.model.Player;

public class ClientGameMaster extends NetworkGameMaster {

	/** Log4J Logger Instanz, siehe Log4J Dokumentation */
	private static final Logger log = Logger.getLogger(ClientGameMaster.class);

	public ClientGameMaster(String connection) throws IOException {
		super();
		this.socket = this.connect(connection);
	}

	private Socket connect(String connection) throws IOException {
		String[] split = connection.split(":");
		Socket socket = null;

		if (split.length == 2) {
			Integer port = Integer.valueOf(split[1]);
			String ip = split[0];

			socket = this.connect(ip, port);
		}

		return socket;
	}

	private Socket connect(String ip, int port) throws IOException {
		Socket result = new Socket(ip, port);
		log.info("Succesful connecting on ip: " + ip + ':' + port);
		return result;
	}

	@Override
	public void finish() {
		// finish the game
	}

	@Override
	public void setPlayerReady(Player player, boolean ready) {
		Protocol readyStatus = Protocol.UNREADY;
		if (ready) {
			readyStatus = Protocol.READY;
		}
		this.sendResponse(readyStatus);
	}

	@Override
	public void run() {
		this.isConnected = true;

		this.fireGameCreated();
		this.sendResponse(Protocol.NAME, this.player1.getName());

		while (this.isConnected()) {
			String receive = this.getRequest();

			if (receive != null) {
				try {
					Protocol protocol = Protocol.valueOf(receive);
					if (protocol != null) {
						switch (protocol) {
						case END_GAME:
							this.sendShips();
							this.abort();
							break;
						case DESTROYED:
							this.sendShips();
							this.sendResponse(Protocol.END_GAME);
							this.fireGameFinished(this.player1);
							break;
						case START_GAME:
							this.handleGameStarts();
							break;
						}
					}
				} catch (IllegalArgumentException e) {
					this.handleComplexProtocolMessages(receive);
				}
			}
		}
	}

	private void handleGameStarts() {
		log.info("Starting the game!");
		this.isRunning = true;
		this.fireGameStarted();
		this.fireTurnEventToPlayer(this.player2);
	}

	@Override
	public String getConnection() {
		return this.socket.getInetAddress().getHostAddress() + " (Client)";
	}

	@Override
	protected void releaseResources() throws IOException {
		this.socket.close();
	}

	@Override
	public boolean isServer() {
		return false;
	}
}
