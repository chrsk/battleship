package de.rbb.battleship.gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;

import net.miginfocom.swing.MigLayout;
import de.prodv.framework.configuration.ResourceData;
import de.prodv.framework.configuration.ResourceService;
import de.rbb.battleship.core.GameMode;
import de.rbb.battleship.model.AbstractGameMaster;
import de.rbb.battleship.model.Board;
import de.rbb.battleship.model.Field;
import de.rbb.battleship.model.GameMaster;
import de.rbb.battleship.model.Player;
import de.rbb.battleship.model.Position;
import de.rbb.battleship.model.AbstractGameMaster.GameCreation;
import de.rbb.battleship.model.AbstractGameMaster.GameListener;
import de.rbb.battleship.model.AbstractGameMaster.PlayerEvent;
import de.rbb.battleship.model.Ship.ShipCategory;
import de.rbb.battleship.model.ai.AIGameMaster;
import de.rbb.battleship.model.ai.AIPlayer;
import de.rbb.battleship.model.ai.HardAI;
import de.rbb.battleship.model.ai.HumanPlayer;
import de.rbb.battleship.net.NetworkGameMaster;
import de.rbb.battleship.util.MP3;
import de.rbb.battleship.util.ServiceLocator;

public class Game extends JPanel implements GameListener {

    private static final String PANEL_KEY_USER = "user";

    private static final String PANEL_KEY_RIBBON = "ribbon";

    private static final int CASKET_SIZE = 26;

    private static final ResourceService resourceService = ServiceLocator.getResourceService();

    private static final long serialVersionUID = -4505772440670934082L;

    private final ResourceData resourceData;

    private final List<List<Casket>> casketList1;
    private final List<List<Casket>> casketList2;

    private Image img;

    private AbstractGameMaster gameMaster;

    private JPanel boardPresentation1;

    private JPanel boardPresentation2;

    private JTextArea shipStatus;

    private JToggleButton readyButton;

    private final FlowingPlacingListener fpListener;

    private JPanel ribbon;

    private final RootFrame frame;

    private CardLayout switchRibbonLayout;

    private JLabel usersTurnLabel;

    private JPanel cardPanel;

    private final List<GameListener> gameListeners;

    protected Player player1;

    protected Player player2;

    public Game(RootFrame frame) {
        this.frame = frame;

        this.gameListeners = new ArrayList<GameListener>();

        this.resourceData = resourceService.getResourceData("lang.main");
        this.fpListener = new FlowingPlacingListener(this);

        this.casketList1 = new ArrayList<List<Casket>>();
        this.initFieldList(this.casketList1, true);

        this.casketList2 = new ArrayList<List<Casket>>();
        this.initFieldList(this.casketList2, false);

        this.init();
    }

    public void createRandomGame() {

        HumanPlayer humanPlayer = new HumanPlayer();
        AIPlayer aiPlayer = new HardAI();

        AIGameMaster gameMaster = new AIGameMaster(humanPlayer, aiPlayer);

        this.setGameMaster(gameMaster);

        this.gameMaster.getPlayer1().getBoard().placeShipsRandom();
        this.gameMaster.getPlayer2().getBoard().placeShipsRandom();

    }

    public void setGameMaster(AbstractGameMaster gameMaster) {
        Player player1 = gameMaster.getPlayer1();
        final Board board = player1.getBoard();
        final Board board2 = gameMaster.getPlayer2().getBoard();

        gameMaster.addGameListener(this);

        if (this.gameMaster != null) {
            this.updateShipsToBePlaced(board);
        }

        boolean boardIsCompleted = board.isCompleted();
        if (this.readyButton != null) {
            this.readyButton.setEnabled(boardIsCompleted);
        }

        Action setRandomShips = this.frame.getSetRandomShips();
        if (setRandomShips != null) {
            setRandomShips.setEnabled(true);
        }

        if (gameMaster instanceof NetworkGameMaster) {
            this.frame.getChat().setGameMaster((NetworkGameMaster) gameMaster);
        }

        this.gameMaster = gameMaster;
        gameMaster.fireGameCreated();
        this.reloadFields(this.casketList1, board);
        this.reloadFields(this.casketList2, board2);
    }

    private void initFieldList(List<List<Casket>> fieldList, boolean flowingPlacing) {

        for (int x = 0; x < Board.BOARD_CASKETS; x++) {
            ArrayList<Casket> innerFieldList = new ArrayList<Casket>(Board.BOARD_CASKETS);

            for (int y = 0; y < Board.BOARD_CASKETS; y++) {
                Casket label = new Casket();
                final Dimension preferredSize = new Dimension(CASKET_SIZE, CASKET_SIZE);

                if (flowingPlacing) {
                    label.addMouseListener(this.fpListener);
                }
                label.setPreferredSize(preferredSize);
                label.setMaximumSize(preferredSize);
                label.setMinimumSize(preferredSize);
                label.setSize(preferredSize);

                innerFieldList.add(label);
            }

            fieldList.add(innerFieldList);
        }
    }

    private void reloadFields(List<List<Casket>> fieldList, Board board) {
        for (int i = 0; i < fieldList.size(); i++) {
            List<Casket> innerList = fieldList.get(i);
            for (int j = 0; j < innerList.size(); j++) {
                Casket label = innerList.get(j);
                final Field field = board.getFieldAt(new Position(i, j));
                label.setField(field);
            }
        }
    }

    public void placeShipsRandom() {
        this.gameMaster.getPlayer1().getBoard().placeShipsRandom();
    }

    private void init() {
        this.initializeMainBoard();

        final String hConstraint = "63[270, shrink 0]113[270, shrink 0, grow 0]";
        final String vConstraint = "102[shrink 0]40[]";
        final MigLayout mgr = new MigLayout("", hConstraint, vConstraint);
        this.setLayout(mgr);

        this.boardPresentation1 = this.createBoardPresentation(this.casketList1);
        this.boardPresentation2 = this.createBoardPresentation(this.casketList2);

        this.add(this.boardPresentation1, "grow");
        this.add(this.boardPresentation2, "grow");

        this.initalizeShipsStatus();
    }

    private void initializeMainBoard() {
        this.img = this.resourceData.getImage("game.background");
        this.setBackground(Color.WHITE);

        final int width = this.img.getWidth(null);
        final int height = this.img.getHeight(null);
        Dimension size = new Dimension(width, height);
        this.setPreferredSize(size);
        this.setMinimumSize(size);
        this.setMaximumSize(size);
        this.setSize(size);
    }

    private void initalizeShipsStatus() {
        this.shipStatus = new JTextArea();
        this.shipStatus.setText(this.resourceData.getString("shipsToBePlaced", ShipCategory.BATTLESHIP.quantity,
                ShipCategory.CRUISER.quantity, ShipCategory.DESTROYER.quantity, ShipCategory.SUBMARINE.quantity));

        final Font font = UIManager.getFont("TextField.font");
        this.shipStatus.setFont(font.deriveFont(9.5f));
        this.shipStatus.setEditable(false);
        this.shipStatus.setLineWrap(true);
        this.shipStatus.setWrapStyleWord(true);
        this.shipStatus.setOpaque(false);
        this.shipStatus.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        this.shipStatus.setForeground(Color.WHITE);

        this.readyButton = new JToggleButton();
        this.readyButton.setEnabled(false);
        this.readyButton.setOpaque(false);

        final SetPlayerReadyAction setPlayerReadyAction = new SetPlayerReadyAction(this.readyButton);
        this.readyButton.setAction(this.resourceData.configureAction(setPlayerReadyAction, "action.ready"));

        this.ribbon = new JPanel(new MigLayout("", "[fill, 220][][fill]", "[]"));
        this.ribbon.setOpaque(false);
        this.ribbon.add(this.shipStatus, "aligny center, growx");
        this.ribbon.add(this.readyButton, "aligny center");

        this.usersTurnLabel = new JLabel(this.resourceData.getIcon("action.ready.smallIcon"));
        this.usersTurnLabel.setText(this.resourceData.getString("your.turn"));
        this.usersTurnLabel.setForeground(Color.WHITE);
        this.usersTurnLabel.setFont(this.usersTurnLabel.getFont().deriveFont(Font.BOLD));
        this.usersTurnLabel.setVisible(false);

        this.switchRibbonLayout = new CardLayout();

        this.cardPanel = new JPanel(this.switchRibbonLayout);
        this.cardPanel.setOpaque(false);
        this.cardPanel.add(this.ribbon, PANEL_KEY_RIBBON);
        this.cardPanel.add(this.usersTurnLabel, PANEL_KEY_USER);

        this.add(this.cardPanel, "newline, spanx 2, growx");
    }

    private JPanel createBoardPresentation(final List<List<Casket>> fieldList) {
        JPanel boardPresentation = new JPanel();
        boardPresentation.setOpaque(false);
        boardPresentation.setLayout(new GridLayout(Board.BOARD_CASKETS, Board.BOARD_CASKETS, 1, 1));

        for (int x = 0; x < Board.BOARD_CASKETS; x++) {
            for (int y = 0; y < Board.BOARD_CASKETS; y++) {
                boardPresentation.add(fieldList.get(x).get(y));
            }
        }

        return boardPresentation;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(this.img, 5, 5, null);
    }

    private void updateShipsToBePlaced(Board board) {
        final int battleships = ShipCategory.BATTLESHIP.quantity
                - board.getShipAmountForCategory(ShipCategory.BATTLESHIP);
        final int cruiser = ShipCategory.CRUISER.quantity - board.getShipAmountForCategory(ShipCategory.CRUISER);
        final int destroyer = ShipCategory.DESTROYER.quantity - board.getShipAmountForCategory(ShipCategory.DESTROYER);
        final int submarine = ShipCategory.SUBMARINE.quantity - board.getShipAmountForCategory(ShipCategory.SUBMARINE);

        if (board.isCompleted()) {
            this.readyButton.setEnabled(true);
        }
        else {
            this.fpListener.setEnabled(true);
            this.readyButton.setSelected(false);
            this.readyButton.setEnabled(false);
        }

        this.shipStatus.setText(this.resourceData.getString("shipsToBePlaced", battleships, cruiser, destroyer,
                submarine));
    }

    public Casket getCasketFromOwnBoardAt(Position pos) {
        return this.casketList1.get(pos.x).get(pos.y);
    }

    public Casket getCasketFromEnemygBoardAt(Position pos) {
        return this.casketList2.get(pos.x).get(pos.y);
    }

    public GameMaster getGameMaster() {
        return this.gameMaster;
    }

    public class SetPlayerReadyAction extends AbstractAction {
        private static final long serialVersionUID = 6661152697785864564L;

        private final JToggleButton toggleButton;

        public SetPlayerReadyAction(JToggleButton toggleButton) {
            this.toggleButton = toggleButton;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Game.this.gameMaster.setPlayerReady(Game.this.gameMaster.getPlayer1(), this.toggleButton.isSelected());
        }
    }

    public void clearCasketsFromMarkingState() {
        for (List<Casket> caskList : this.casketList1) {
            for (Casket casket : caskList) {
                casket.setMarked(false);
                casket.repaint();
            }
        }
    }

    @Override
    public void fieldStateChanged(ChangeEvent changeEvent) {
        Board board = (Board) changeEvent.getSource();

        if (this.gameMaster.getPlayer1().getBoard().equals(board)) {
            this.reloadFields(this.casketList1, board);
            this.updateShipsToBePlaced(board);
        }
        else {
            this.reloadFields(this.casketList2, board);
        }

        if (this.gameMaster.isRunning()) {
            final String[] filenames = { "kreuz.mp3", "kreis1.mp3", "kreis2.mp3", "strich.mp3" };
            new MP3(filenames[new Random().nextInt(filenames.length - 1)]).play();
        }
    }

    @Override
    public void gameFinished(PlayerEvent playerEvent) {
        this.ribbon.setVisible(false);

        for (GameListener gameListener : this.gameListeners) {
            gameListener.gameFinished(playerEvent);
        }
    }

    @Override
    public void gameStarted(ChangeEvent changeEvent) {
        this.switchRibbonLayout.show(this.cardPanel, PANEL_KEY_USER);
        this.readyButton.setSelected(false);
        this.readyButton.setEnabled(false);
        this.fpListener.setEnabled(false);

        for (GameListener gameListener : this.gameListeners) {
            gameListener.gameStarted(changeEvent);
        }
    }

    @Override
    public void nextPlayerTurned(PlayerEvent playerTurn) {
        if (playerTurn.getPlayer().equals(this.gameMaster.getPlayer1())) {
            this.usersTurnLabel.setText(this.resourceData.getString("your.turn"));
            this.usersTurnLabel.setIcon(this.resourceData.getIcon("action.ready.smallIcon"));
        }
        else {
            this.usersTurnLabel.setText(this.resourceData.getString("enemy.turn"));
            this.usersTurnLabel.setIcon(this.resourceData.getIcon("wait"));
        }
    }

    @Override
    public void gameAborted(ChangeEvent changeEvent) {
        if (changeEvent != null) {
            for (GameListener gameListener : this.gameListeners) {
                gameListener.gameAborted(changeEvent);
            }
        }
    }

    @Override
    public void gameCreated(GameCreation changeEvent) {
        this.switchRibbonLayout.show(this.cardPanel, PANEL_KEY_RIBBON);
        this.fpListener.setEnabled(true);
        this.readyButton.setEnabled(false);
        this.cardPanel.setVisible(true);

        if (this.gameMaster.getGameMode().equals(GameMode.COMPUTER_VS_COMPUTER)) {
            this.cardPanel.setVisible(false);
        }

        for (GameListener gameListener : this.gameListeners) {
            gameListener.gameCreated(changeEvent);
        }
    }

    public void addGameListener(GameListener listener) {
        this.gameListeners.add(listener);
    }
}
