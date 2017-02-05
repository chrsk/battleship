package de.rbb.battleship.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

public final class PlaceholderAction extends AbstractAction {

	private static final long serialVersionUID = -873370025408194196L;

	/** Log4J Logger Instanz, siehe Log4J Dokumentation */
	private static final Logger log = Logger.getLogger(PlaceholderAction.class);

	@Override
	public void actionPerformed(ActionEvent e) {
		log.debug(this.getValue(NAME));
	}
}