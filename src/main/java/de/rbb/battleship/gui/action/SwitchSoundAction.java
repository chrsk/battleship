package de.rbb.battleship.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.prodv.framework.configuration.ResourceData;
import de.prodv.framework.configuration.ResourceService;
import de.rbb.battleship.util.MP3;
import de.rbb.battleship.util.ServiceLocator;

public class SwitchSoundAction extends AbstractAction {

	private static final ResourceService resourceService = ServiceLocator.getResourceService();

	private final ResourceData resourceData;

	public SwitchSoundAction() {
		this.resourceData = resourceService.getResourceData("lang.main");
	}

	private static final long serialVersionUID = 2162940952771854831L;

	@Override
	public void actionPerformed(ActionEvent e) {
		MP3.SOUND_IS_ENABLED = !MP3.SOUND_IS_ENABLED;

		if (MP3.SOUND_IS_ENABLED) {
			this.putValue(Action.SMALL_ICON, this.resourceData.getIcon("sound.enabled"));
		} else {
			this.putValue(Action.SMALL_ICON, this.resourceData.getIcon("sound.disabled"));
		}
	}
}
