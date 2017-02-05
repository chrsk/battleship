package de.rbb.battleship.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class CloseAction extends AbstractAction {

	private static final long serialVersionUID = -3137127390218801780L;
	private final Window window;

	public CloseAction(Window window) {
		this.window = window;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.window.dispose();
	}

}
