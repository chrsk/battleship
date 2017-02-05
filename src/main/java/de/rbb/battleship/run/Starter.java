package de.rbb.battleship.run;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import de.rbb.battleship.gui.RootFrame;

public class Starter {

	/** Log4J Logger Instanz, siehe Log4J Dokumentation */
	private static final Logger log = Logger.getLogger(Starter.class);

	public Starter() {
		Starter.this.initLookAndFeel();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new RootFrame();
			}
		});
	}

	public static void main(String[] args) {
		new Starter();
	}

	/**
	 * Versucht das System LookAndFeel im UIManager zu setzen.
	 */
	private void initLookAndFeel() {
		try {
			String systemLookAndFeel = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(systemLookAndFeel);
		} catch (Exception e) {
			log.error("Initialization of look-&-feel failed!", e);
		}
	}
}
