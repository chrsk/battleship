package de.rbb.battleship.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.JToggleButton.ToggleButtonModel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import de.prodv.framework.configuration.ResourceData;
import de.prodv.framework.configuration.ResourceService;
import de.rbb.battleship.core.AILevel;
import de.rbb.battleship.model.AbstractGameMaster;
import de.rbb.battleship.model.GameMaster;
import de.rbb.battleship.model.Player;
import de.rbb.battleship.model.ai.AIGameMaster;
import de.rbb.battleship.model.ai.AIPlayer;
import de.rbb.battleship.model.ai.EasyAI;
import de.rbb.battleship.model.ai.HardAI;
import de.rbb.battleship.model.ai.HumanPlayer;
import de.rbb.battleship.net.ClientGameMaster;
import de.rbb.battleship.net.Server;
import de.rbb.battleship.net.ServerGameMaster;
import de.rbb.battleship.util.ServiceLocator;

public class NewGame extends JDialog implements Dialog {

	private static final long serialVersionUID = -445883058745615006L;

	private static final ResourceService resourceService = ServiceLocator.getResourceService();

	private final ResourceData resourceData;

	private JPanel panel;

	private JPanel info;

	private final Game game;

	private ButtonGroup aiLevelGroup1, aiLevelGroup2;

	private JTextField userNameTextField;

	private JTextField ownIP;

	private JTextField enemyIP;

	private ButtonGroup aiLevelGroupSP;

	public NewGame(Game game) {
		this.game = game;
		this.resourceData = resourceService.getResourceData("lang.main");
		this.init();
	}

	private void init() {
		this.setTitle(this.resourceData.getString("new.game.title"));

		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setModal(true);

		this.createComponent();
		this.initContent();
		this.addToLayout();
		this.addEscapeAction();

		this.pack();
		this.setLocationRelativeTo(null);
	}

	private JPanel createSingleplayerPanel() {
		this.aiLevelGroupSP = new ButtonGroup();
		final String aiLevelText = this.resourceData.getString("computer.ai.level");
		JPanel aiLevel = this.createAILevelPanel(this.aiLevelGroupSP, aiLevelText);

		JPanel singleGame = new JPanel(new MigLayout("", "[fill, grow]", "[][]"));
		singleGame.setBackground(Color.WHITE);

		JTextArea infoText = new JTextArea();
		infoText.setText(this.resourceData.getString("singleplayer.info"));
		final Font font = UIManager.getFont("TextField.font");
		infoText.setFont(font.deriveFont(11f));
		infoText.setEditable(false);
		infoText.setLineWrap(true);
		infoText.setWrapStyleWord(true);
		infoText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final JLabel spLabel = new JLabel(this.resourceData.getString("singleplayer"));
		spLabel.setIcon(this.resourceData.getIcon("singleplayer.icon"));
		spLabel.setFont(spLabel.getFont().deriveFont(14f).deriveFont(Font.BOLD));

		final JButton startSPGame = new JButton();
		startSPGame.setAction(this.resourceData.configureAction(new NewSinglePlayerGameAction(),
				"new.singlePlayer.game"));

		singleGame.add(spLabel);
		singleGame.add(infoText, "newline");
		singleGame.add(aiLevel, "newline");
		singleGame.add(startSPGame, "newline");

		return singleGame;

	}

	private JPanel createComputerPanel() {
		this.aiLevelGroup1 = new ButtonGroup();
		JPanel aiLevel = this.createAILevelPanel(this.aiLevelGroup1, "Computer 1");

		this.aiLevelGroup2 = new ButtonGroup();
		JPanel aiLevel2 = this.createAILevelPanel(this.aiLevelGroup2, "Computer 2");

		JPanel singleGame = new JPanel(new MigLayout("", "[fill, grow]", "[][]"));
		singleGame.setBackground(Color.WHITE);

		JPanel aiLevelPanel = new JPanel(new MigLayout("", "[][]", "[]"));
		aiLevelPanel.add(aiLevel);
		aiLevelPanel.add(aiLevel2);

		JTextArea infoText = new JTextArea();
		infoText.setText(this.resourceData.getString("computer.vs.computer.info"));
		final Font font = UIManager.getFont("TextField.font");
		infoText.setFont(font.deriveFont(11f));
		infoText.setEditable(false);
		infoText.setLineWrap(true);
		infoText.setWrapStyleWord(true);
		infoText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final JLabel spLabel = new JLabel(this.resourceData.getString("computer.vs.computer"));
		spLabel.setIcon(this.resourceData.getIcon("computer.icon"));
		spLabel.setFont(spLabel.getFont().deriveFont(14f).deriveFont(Font.BOLD));

		final JButton startSPGame = new JButton();
		startSPGame.setAction(this.resourceData.configureAction(new NewComputerVsComputerAction(),
				"computer.vs.computer.new"));

		singleGame.add(spLabel);
		singleGame.add(infoText, "newline");
		singleGame.add(aiLevelPanel, "newline");
		singleGame.add(startSPGame, "newline");

		return singleGame;

	}

	private JPanel createAILevelPanel(ButtonGroup buttonGroup, String title) {
		JRadioButton aiLevel_easy = new JRadioButton(this.resourceData.getString("ailevel.easy"));
		aiLevel_easy.setModel(new EnumToggleButtonModel<AILevel>(AILevel.EASY));
		JRadioButton aiLevel_middle = new JRadioButton(this.resourceData.getString("ailevel.middle"));
		aiLevel_middle.setModel(new EnumToggleButtonModel<AILevel>(AILevel.MEDIUM));
		JRadioButton aiLevel_hard = new JRadioButton(this.resourceData.getString("ailevel.hard"));
		aiLevel_hard.setModel(new EnumToggleButtonModel<AILevel>(AILevel.HARD));

		aiLevel_middle.setSelected(true);

		aiLevel_easy.setOpaque(false);
		aiLevel_middle.setOpaque(false);
		aiLevel_hard.setOpaque(false);

		buttonGroup.add(aiLevel_easy);
		buttonGroup.add(aiLevel_middle);
		buttonGroup.add(aiLevel_hard);

		JPanel aiLevel = new JPanel();
		aiLevel.setLayout(new BoxLayout(aiLevel, BoxLayout.Y_AXIS));
		final JLabel label = new JLabel(title);
		label.setFont(label.getFont().deriveFont(Font.BOLD));

		label.setOpaque(false);

		aiLevel.add(label);
		aiLevel.add(aiLevel_easy);
		aiLevel.add(aiLevel_middle);
		aiLevel.add(aiLevel_hard);
		aiLevel.setOpaque(false);
		return aiLevel;
	}

	private void initContent() {
		this.info.setLayout(new MigLayout("", "[160, grow, fill|200, grow, fill|200, grow, fill]",
				"[][200:280, grow, fill]"));

		final JPanel singleGame = this.createSingleplayerPanel();
		final JPanel networkGame = this.createMultiplayerPanel();
		final JPanel userName = this.createUserNamePanel();
		final JPanel aiGame = this.createComputerPanel();

		this.info.add(userName, "spanx 3");
		this.info.add(singleGame, "newline");
		this.info.add(networkGame);
		this.info.add(aiGame);
	}

	private JPanel createUserNamePanel() {
		final JPanel userName = new JPanel(new MigLayout("", "[][fill, grow]", "[]"));
		final JLabel userNameLabel = new JLabel(this.resourceData.getString("your.name"));
		this.userNameTextField = new JTextField();

		userName.add(userNameLabel, "growx");
		userName.add(this.userNameTextField);
		return userName;
	}

	private JPanel createMultiplayerPanel() {
		JPanel networkGame = new JPanel(new MigLayout("", "[fill, grow]", "[][]"));
		networkGame.setBackground(Color.WHITE);

		final String connection = Server.getIP() + ':' + Server.SERVER_PORT;

		this.ownIP = new JTextField(connection);
		this.enemyIP = new JTextField(connection);
		this.ownIP.setEnabled(false);

		final Font font = UIManager.getFont("TextField.font");
		JTextArea infoText2 = new JTextArea();
		infoText2.setText(this.resourceData.getString("network.info"));
		infoText2.setFont(font.deriveFont(11f));
		infoText2.setEditable(false);
		infoText2.setLineWrap(true);
		infoText2.setWrapStyleWord(true);
		infoText2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final JButton joinGameButton = new JButton();
		joinGameButton.setAction(new JoinServerAction());

		final JButton hostGameButton = new JButton();
		hostGameButton.setAction(new NewServerAction());

		final JLabel ownIPLabel = new JLabel(this.resourceData.getString("your.ip"));
		final JLabel enemyIPLabel = new JLabel(this.resourceData.getString("enemy.ip"));

		final JLabel nLabel = new JLabel(this.resourceData.getString("network"));
		nLabel.setIcon(this.resourceData.getIcon("network.icon"));
		nLabel.setFont(nLabel.getFont().deriveFont(14f).deriveFont(Font.BOLD));

		networkGame.add(nLabel);
		networkGame.add(infoText2, "grow, newline");
		networkGame.add(ownIPLabel, "growx, newline");
		networkGame.add(this.ownIP, "growx, newline");
		networkGame.add(hostGameButton, "growx, newline");
		networkGame.add(enemyIPLabel, "growx, newline");
		networkGame.add(this.enemyIP, "growx, newline");
		networkGame.add(joinGameButton, "growx, newline");
		return networkGame;
	}

	/**
	 * erstellt die benötigten Komponenten.
	 */
	private void createComponent() {
		this.info = new JPanel();
		this.panel = new JPanel(new BorderLayout());
		this.panel.setBackground(Color.white);
	}

	/**
	 * fügt die Komponenten dem JFrame hinzu.
	 */
	private void addToLayout() {
		this.panel.add(this.info, BorderLayout.CENTER);
		this.add(this.panel);
	}

	private void addEscapeAction() {
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		Action escapeAction = new AbstractActionExtension();
		this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		this.getRootPane().getActionMap().put("ESCAPE", escapeAction);
	}

	/**
	 * Action welche den <code>About</code> Dialog schließt, wird über die
	 * <code>ActionMap</code> mit dem <code>KeyStroke</code> "Escape"
	 * eingehängt.
	 */
	private final class AbstractActionExtension extends AbstractAction {
		private static final long serialVersionUID = 8284505223025060271L;

		public void actionPerformed(ActionEvent e) {
			NewGame.this.dispose();
		}
	}

	@Override
	public void open() {
		this.setVisible(true);
	}

	public class EnumToggleButtonModel<T> extends ToggleButtonModel {

		private static final long serialVersionUID = 9176103120313097006L;

		private T enumeration;

		public EnumToggleButtonModel() {
			super();
		}

		public EnumToggleButtonModel(T enumeration) {
			this.enumeration = enumeration;
		}

		public void setEnumeration(T enumeration) {
			this.enumeration = enumeration;
		}

		public T getEnumeration() {
			return this.enumeration;
		}
	}

	public class JoinServerAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		/** Log4J Logger Instanz, siehe Log4J Dokumentation */
		private final Logger log = Logger.getLogger(JoinServerAction.class);

		public JoinServerAction() {
			this.putValue(NAME, NewGame.this.resourceData.getString("new.network.game.join"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String serverIP = NewGame.this.enemyIP.getText();

			try {
				final ClientGameMaster gameMaster = new ClientGameMaster(serverIP);
				gameMaster.getPlayer1().setName(NewGame.this.userNameTextField.getText());
				NewGame.this.game.setGameMaster(gameMaster);
				NewGame.this.game.gameAborted(null);
				new Thread(gameMaster).start();
			} catch (ConnectException ex) {
				JOptionPane.showMessageDialog(NewGame.this, NewGame.this.resourceData
						.getString("error.connecting.to.server"));
			} catch (IOException ex) {
				this.log.error("Couldn't connect", ex);
			} finally {
				NewGame.this.dispose();
			}
		}
	}

	public class NewServerAction extends AbstractAction {

		/** Log4J Logger Instanz, siehe Log4J Dokumentation */
		private final Logger log = Logger.getLogger(NewSinglePlayerGameAction.class);

		private static final long serialVersionUID = 8989827372943375496L;

		public NewServerAction() {
			this.putValue(NAME, NewGame.this.resourceData.getString("new.network.game.host"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.log.info("Starting new Network Game");

			final ServerGameMaster gameMaster = new ServerGameMaster();
			gameMaster.getPlayer1().setName(NewGame.this.userNameTextField.getText());

			new WatingForPlayerDialog(NewGame.this, gameMaster);
		}
	}

	public class WatingForPlayerDialog extends JDialog {

		/** Log4J Logger Instanz, siehe Log4J Dokumentation */
		private final Logger log = Logger.getLogger(WatingForPlayerDialog.class);

		private static final long serialVersionUID = 1L;

		private final ServerGameMaster gameMaster;

		public WatingForPlayerDialog(JDialog owner, ServerGameMaster gameMaster) {
			super(owner);
			this.gameMaster = gameMaster;
			this.init();
			this.setLocationRelativeTo(owner);
			this.start();
			this.setVisible(true);
		}

		private void start() {
			new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {

					Socket socket = WatingForPlayerDialog.this.gameMaster.hostGame(Server.SERVER_PORT);

					if (socket == null) {
						WatingForPlayerDialog.this.log.error("Error starting Server");

						WatingForPlayerDialog.this.dispose();
						JOptionPane.showMessageDialog(NewGame.this, NewGame.this.resourceData
								.getString("error.starting.server"));
					} else {
						NewGame.this.dispose();
						WatingForPlayerDialog.this.dispose();

						NewGame.this.game.gameAborted(null);
						NewGame.this.game.setGameMaster(WatingForPlayerDialog.this.gameMaster);

						new Thread(WatingForPlayerDialog.this.gameMaster).start();
					}

					return null;
				}
			}.execute();
		}

		private void init() {
			final AbortGameAction abortGameAction = new AbortGameAction(this, WatingForPlayerDialog.this.gameMaster);
			final JProgressBar progress = new JProgressBar();

			progress.setIndeterminate(true);

			this.setTitle("Auf Spieler warten");
			this.setLayout(new MigLayout("", "[grow]", "[][][]"));
			int seconds = Server.SERVER_TIMEOUT / 1000;
			String serverURL = Server.getIP() + ':' + Server.SERVER_PORT;
			String waitingForPlayerText = NewGame.this.resourceData.getString("waiting.for.player", serverURL, seconds);

			final JTextArea info = new JTextArea();
			final Font font = UIManager.getFont("TextField.font");

			info.setOpaque(false);
			info.setText(waitingForPlayerText);
			info.setFont(font.deriveFont(11f));
			info.setEditable(false);
			info.setLineWrap(true);
			info.setWrapStyleWord(true);
			info.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			this.add(info, "grow, spanx2");
			this.add(progress, "grow, newline, spanx2");
			this.add(new JButton(abortGameAction), "newline");

			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					abortGameAction.actionPerformed(null);
				}
			});

			this.setPreferredSize(new Dimension(300, 150));
			this.setResizable(false);
			this.setModal(true);
			this.setAlwaysOnTop(true);
			this.pack();
		}
	}

	public class AbortGameAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private final GameMaster gameMaster;
		private final JDialog dialog;

		public AbortGameAction(JDialog dialog, GameMaster gameMaster) {
			this.dialog = dialog;
			this.putValue(NAME, NewGame.this.resourceData.getString("cancel"));
			this.gameMaster = gameMaster;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.gameMaster.abort();
			this.dialog.dispose();
		}
	}

	public class NewSinglePlayerGameAction extends AbstractAction {
		private static final long serialVersionUID = 489611032948199700L;

		/** Log4J Logger Instanz, siehe Log4J Dokumentation */
		private final Logger log = Logger.getLogger(NewSinglePlayerGameAction.class);

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent e) {
			Player human = new HumanPlayer();
			Player enemy = null;

			EnumToggleButtonModel<AILevel> selection = (EnumToggleButtonModel<AILevel>) NewGame.this.aiLevelGroupSP
					.getSelection();

			switch (selection.enumeration) {
			case EASY:
				enemy = new EasyAI();
				break;
			case MEDIUM:
			case HARD:
				enemy = new HardAI();
				break;
			}

			this.log.info(selection.enumeration + " " + enemy);
			human.setName(NewGame.this.userNameTextField.getText());

			final AbstractGameMaster gameMaster = new AIGameMaster(human, enemy);
			NewGame.this.game.getGameMaster().abort();
			NewGame.this.game.setGameMaster(gameMaster);
			NewGame.this.game.gameAborted(null);
			NewGame.this.dispose();
		}
	}

	public class NewComputerVsComputerAction extends AbstractAction {
		private static final long serialVersionUID = 489611032948199700L;

		@Override
		public void actionPerformed(ActionEvent e) {
			AIPlayer player1 = this.determineAILevel(NewGame.this.aiLevelGroup1);
			AIPlayer player2 = this.determineAILevel(NewGame.this.aiLevelGroup2);

			final AbstractGameMaster gameMaster = new AIGameMaster(player1, player2);
			NewGame.this.game.setGameMaster(gameMaster);
			NewGame.this.game.gameAborted(null);
			NewGame.this.dispose();
		}

		private AIPlayer determineAILevel(ButtonGroup buttonGroup) {
			AIPlayer enemy = null;

			@SuppressWarnings("unchecked")
			EnumToggleButtonModel<AILevel> selection = (EnumToggleButtonModel<AILevel>) buttonGroup.getSelection();

			switch (selection.enumeration) {
			case EASY:
				enemy = new EasyAI();
				break;
			case MEDIUM:
			case HARD:
				enemy = new HardAI();
				break;
			}

			return enemy;
		}
	}

}
