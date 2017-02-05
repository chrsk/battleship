package de.rbb.battleship.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.rbb.battleship.gui.Dialog;

public class OpenDialogAction extends AbstractAction {

	private static final long serialVersionUID = -1491817796382709406L;

	private final Dialog dialog;

	@Override
	public void actionPerformed(ActionEvent e) {
		this.dialog.open();
	}

	public OpenDialogAction(Dialog dialog) {
		this.dialog = dialog;
	}
}
