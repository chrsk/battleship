package de.rbb.battleship.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class Server {

	/** Log4J Logger Instanz, siehe Log4J Dokumentation */
	private static final Logger log = Logger.getLogger(Server.class);

	public static final int SERVER_TIMEOUT = 30000;

	public static final int SERVER_PORT = 1337;

	public static String getIP() {
		String result = null;
		try {
			result = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException ex) {
			log.error("No interface found!", ex);
		}

		return result;
	}
}
