package de.rbb.battleship.util;

import java.io.InputStream;

import javazoom.jl.player.Player;

import org.apache.log4j.Logger;

public class MP3 {
	/** Log4J Logger Instanz, siehe Log4J Dokumentation */
	private static final Logger log = Logger.getLogger(MP3.class);

	public static boolean SOUND_IS_ENABLED = true;

	private final String filename;

	private Player player;

	public MP3(String filename) {
		this.filename = filename;
	}

	public void close() {
		if (this.player != null) {
			this.player.close();
		}
	}

	public void play(final int times) {
		if (SOUND_IS_ENABLED) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i < times; i++) {
						final Thread makeThread = MP3.this.makeThread();
						if (makeThread != null) {
							makeThread.start();
							try {
								makeThread.join();
							} catch (InterruptedException e) {
								log.error("cant play sound multiple times", e);
							}
						}
					}
				}
			}).start();
		}
	}

	public Thread makeThread() {
		Thread thread = null;
		try {
			InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(this.filename);
			this.player = new Player(resource);

			thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						MP3.this.player.play();
					} catch (Exception e) {
						System.out.println(e);
					}
				}
			});
		} catch (Exception e) {
			log.error("Problem playing file " + this.filename);
		}

		return thread;
	}

	public void play() {
		if (SOUND_IS_ENABLED) {
			this.play(1);
		}
	}
}
