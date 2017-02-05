package de.rbb.battleship.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EnumSet;
import java.util.MissingResourceException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;

import de.prodv.framework.configuration.ResourceData;
import de.prodv.framework.configuration.ResourceService;
import de.rbb.battleship.core.GameMode;
import de.rbb.battleship.gui.action.CloseAction;
import de.rbb.battleship.gui.action.LoadGameAction;
import de.rbb.battleship.gui.action.OpenDialogAction;
import de.rbb.battleship.gui.action.PlaceShipsRandomAction;
import de.rbb.battleship.gui.action.SaveGameAction;
import de.rbb.battleship.gui.action.StopGameAction;
import de.rbb.battleship.gui.action.SwitchSoundAction;
import de.rbb.battleship.model.AbstractGameMaster.GameAdapter;
import de.rbb.battleship.model.AbstractGameMaster.GameCreation;
import de.rbb.battleship.model.AbstractGameMaster.PlayerEvent;
import de.rbb.battleship.net.NetworkGameMaster;
import de.rbb.battleship.util.ServiceLocator;

public class RootFrame {

	private final JFrame frame;

	private static final ResourceService resourceService = ServiceLocator.getResourceService();

	private final ResourceData resourceData;

	private JMenuBar menuBar;

	private Action saveGame, quitGame, newGame, exitSystem, loadGame, about, rules, setRandomShips, muteSound;

	private JLabel serverLabel;

	private JLabel timerLabel;

	private Game game;

	private Chat chat;

	public RootFrame() {
		this.resourceData = resourceService.getResourceData("lang.main");
		this.frame = new JFrame();
		this.init();
	}

	private void init() {
		this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.frame.setTitle(this.resourceData.getString("system.title"));
		this.frame.setIconImage(this.resourceData.getImage("tray"));
		this.frame.setMinimumSize(this.resourceData.getDimension("system.dimension"));
		this.frame.setPreferredSize(this.resourceData.getDimension("system.dimension"));
		this.frame.setResizable(false);
		this.frame.getContentPane().setLayout(new BorderLayout());

		this.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				RootFrame.this.game.getGameMaster().abort();
				System.exit(0);
			}
		});

		this.initializeBoards();

		this.initializeMenuBar();
		this.initializeToolbar();
		this.initializeStatusBar();

		GameAdapter adapter = new RootFrameGameAdapter();
		this.game.addGameListener(adapter);
		this.game.createRandomGame();

		this.frame.pack();
		this.frame.setLocationRelativeTo(null);
		this.frame.setVisible(true);
	}

	private void initializeBoards() {
		this.game = new Game(this);

		JPanel split = new JPanel(new BorderLayout());

		this.chat = new Chat();
		JPanel chatPanel = this.chat.getChat();
		chatPanel.setVisible(false);

		split.add(this.game);
		split.add(chatPanel, BorderLayout.SOUTH);

		this.frame.getContentPane().add(split, BorderLayout.CENTER);
	}

	private void initializeStatusBar() {
		final JToolBar statusBar = new JToolBar();
		statusBar.setFloatable(false);
		final Color color = this.resourceData.getColor("border");
		final MatteBorder createMatteBorder = BorderFactory.createMatteBorder(1, 0, 0, 0, color);
		final Border createEmptyBorder = BorderFactory.createEmptyBorder(5, 3, 5, 3);
		CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(createMatteBorder, createEmptyBorder);
		statusBar.setBorder(compoundBorder);

		String string;

		this.serverLabel = new JLabel();
		this.serverLabel.setVisible(false);
		this.serverLabel.setIcon(this.resourceData.getIcon("server"));

		string = this.resourceData.getString("time.left", 14);
		this.timerLabel = new JLabel(string);
		this.timerLabel.setIcon(this.resourceData.getIcon("time"));
		this.timerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

		statusBar.add(this.serverLabel);
		statusBar.addSeparator();
		statusBar.add(this.timerLabel);

		this.frame.getContentPane().add(statusBar, BorderLayout.SOUTH);
	}

	private void initializeToolbar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		final Color color = this.resourceData.getColor("border");
		final MatteBorder createMatteBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, color);
		final Border createEmptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(createMatteBorder, createEmptyBorder);
		toolbar.setBorder(compoundBorder);

		this.setRandomShips = this.resourceData.configureAction(new PlaceShipsRandomAction(this.game), "random");

		this.muteSound = this.resourceData.configureAction(new SwitchSoundAction(), "sound");

		toolbar.add(this.newGame);
		toolbar.add(this.loadGame);
		toolbar.add(this.saveGame);
		toolbar.add(this.setRandomShips);
		toolbar.addSeparator();
		toolbar.add(this.rules);
		toolbar.add(this.muteSound);

		this.frame.getContentPane().add(toolbar, BorderLayout.NORTH);
	}

	private void initializeMenuBar() throws MissingResourceException {

		this.menuBar = new JMenuBar();

		JMenu gameMenu = this.configureGameMenu();
		JMenu helpMenu = this.configureHelpMenu();

		this.menuBar.add(gameMenu);
		this.menuBar.add(helpMenu);

		this.frame.setJMenuBar(this.menuBar);
	}

	private JMenu configureGameMenu() throws MissingResourceException {
		final JMenu game = new JMenu(this.resourceData.getString("menu.game"));
		final Integer mnemonicKey = this.resourceData.getMnemonicKey("menu.game.mnemonicKey");
		game.setMnemonic(mnemonicKey);

		this.newGame = this.resourceData.configureAction(new OpenDialogAction(new NewGame(this.game)),
				"menu.game.item.new");

		this.saveGame = this.resourceData.configureAction(new SaveGameAction(this.game), "menu.game.item.save");

		this.quitGame = this.resourceData.configureAction(new CloseAction(this.frame), "menu.game.item.quit");

		this.exitSystem = this.resourceData.configureAction(new StopGameAction(this.game), "menu.game.item.exit");

		this.loadGame = this.resourceData.configureAction(new LoadGameAction(this.game), "menu.game.item.load");

		game.add(this.newGame);
		game.addSeparator();
		game.add(this.loadGame);
		game.add(this.saveGame);
		game.add(this.exitSystem);
		game.addSeparator();
		game.add(this.quitGame);

		return game;
	}

	private JMenu configureHelpMenu() throws MissingResourceException {
		final JMenu game = new JMenu(this.resourceData.getString("menu.help"));
		final Integer mnemonicKey = this.resourceData.getMnemonicKey("menu.help.mnemonicKey");
		game.setMnemonic(mnemonicKey);

		this.about = this.resourceData.configureAction(new OpenDialogAction(new About()), "menu.help.item.info");

		this.rules = this.resourceData.configureAction(new OpenDialogAction(new GameRules()), "menu.help.item.rules");

		game.add(this.about);
		game.add(this.rules);

		return game;
	}

	public Action getSetRandomShips() {
		return this.setRandomShips;
	}

	public Chat getChat() {
		return this.chat;
	}

	private final class RootFrameGameAdapter extends GameAdapter {
		@Override
		public void gameStarted(ChangeEvent changeEvent) {
			RootFrame.this.saveGame.setEnabled(true);
			if (RootFrame.this.game.getGameMaster().getGameMode() == GameMode.NETWORKGAME) {
				RootFrame.this.saveGame.setEnabled(false);
			}
			RootFrame.this.getSetRandomShips().setEnabled(false);
		}

		private void handleNetworkGame() {
			NetworkGameMaster gameMaster = (NetworkGameMaster) RootFrame.this.game.getGameMaster();
			String connection = gameMaster.getConnection();
			final String string = RootFrame.this.resourceData.getString("connected.with", connection);

			this.setChatVisible(true);
			RootFrame.this.serverLabel.setText(string);
			RootFrame.this.serverLabel.setVisible(true);
		}

		@Override
		public void gameCreated(GameCreation changeEvent) {

			GameMode gameMode = changeEvent.getGameMode();
			RootFrame.this.setRandomShips.setEnabled(true);
			RootFrame.this.saveGame.setEnabled(false);

			if (EnumSet.of(GameMode.SINGLEPLAYER, GameMode.COMPUTER_VS_COMPUTER).contains(gameMode)) {
				this.setChatVisible(false);
				RootFrame.this.serverLabel.setVisible(false);
			} else {
				this.handleNetworkGame();
			}
		}

		private void setChatVisible(boolean visible) {
			Dimension preferredSize = RootFrame.this.frame.getPreferredSize();
			if (!visible && RootFrame.this.chat.isActive()) {
				RootFrame.this.frame.setPreferredSize(new Dimension(preferredSize.width, preferredSize.height - 100));
				RootFrame.this.chat.setActive(false);
			}

			if (visible && !RootFrame.this.chat.isActive()) {
				RootFrame.this.frame.setPreferredSize(new Dimension(preferredSize.width, preferredSize.height + 100));
				RootFrame.this.chat.setActive(true);
			}
			RootFrame.this.frame.pack();
		}

		@Override
		public void gameFinished(PlayerEvent playerEvent) {
			RootFrame.this.saveGame.setEnabled(false);

			JOptionPane.showMessageDialog(RootFrame.this.frame, RootFrame.this.resourceData.getString("player.win",
					playerEvent.getPlayer().getName()));
			this.setChatVisible(false);
		}

		@Override
		public void gameAborted(ChangeEvent changeEvent) {
			RootFrame.this.saveGame.setEnabled(false);

			JOptionPane.showMessageDialog(RootFrame.this.frame, RootFrame.this.resourceData.getString("game.aborted"));
			this.setChatVisible(false);
		}
	}

}
