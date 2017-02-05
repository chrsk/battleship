package de.rbb.battleship.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import de.rbb.battleship.net.NetworkGameMaster;

public class Chat implements ChatListener {

	/** Log4J Logger Instanz, siehe Log4J Dokumentation */
	private static final Logger log = Logger.getLogger(Chat.class);

	public static final boolean TIMESTAMP = true;

	private JPanel chatPanel;
	private JTextPane chatLogPane;
	private JPanel inputArea;
	private JButton sendButton;
	private JTextField inputField;
	private StyledDocument styledDocument;

	private NetworkGameMaster gameMaster;

	private JScrollPane scrollPane;

	public enum ChatStyle {
		SERVER, CLIENT, GAMEMASTER, REGULAR
	}

	public Chat() {
		this(null);
	}

	public Chat(NetworkGameMaster gameMaster) {
		this.gameMaster = gameMaster;
		this.init();
	}

	private void init() {
		this.chatPanel = new JPanel(new MigLayout("", "[grow, fill][]", "[grow, fill][]"));
		this.chatPanel.setPreferredSize(new Dimension(this.chatPanel.getPreferredSize().width, 100));

		this.initChatLogPane();
		this.initInputArea();
		this.addToLayout();
	}

	private void initInputArea() {
		this.inputArea = new JPanel(new MigLayout("", "[fill, grow][]", ""));
		this.inputField = new JTextField("");

		this.createActionListener();

		this.sendButton = new JButton("Senden");
		this.sendButton.setAction(new ChatMessageAction());
	}

	private void createActionListener() {
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
		Action escapeAction = new ChatMessageAction();
		this.inputField.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, "ENTER");
		this.inputField.getActionMap().put("ENTER", escapeAction);
	}

	private void handleChatMessage() {
		String text = this.inputField.getText();
		this.addChatMessage(this.gameMaster.getPlayer1().getName(), text, ChatStyle.CLIENT);
		this.gameMaster.sendChatMessage(text);
		this.inputField.setText("");
	}

	private void addToLayout() {
		this.scrollPane = new JScrollPane(this.chatLogPane);
		this.scrollPane.setBorder(null);
		this.chatPanel.add(this.scrollPane);

		this.inputArea.add(this.inputField);
		this.inputArea.add(this.sendButton);

		this.chatPanel.add(this.inputArea, "newline");
	}

	public void addChatMessage(String name, String text) {
		this.addChatMessage(name, text, ChatStyle.REGULAR);
	}

	public void addChatMessage(String name, String text, ChatStyle style) {
		if (TIMESTAMP) {
			DateFormat timeInstance = DateFormat.getTimeInstance(DateFormat.SHORT);
			String format = timeInstance.format(new Date());

			this.insertString('(' + format + ") ");
		}
		this.insertString(name + ": ", style);
		this.insertString(text + "\n");

		final JScrollBar verticalScrollBar = this.scrollPane.getVerticalScrollBar();
		int value = verticalScrollBar.getValue();
		verticalScrollBar.setValue(value + 100);
	}

	private void insertString(String string) {
		this.insertString(string, ChatStyle.REGULAR);
	}

	private void insertString(String string, ChatStyle style) {
		Style chatStyle = this.styledDocument.getStyle(style.toString());
		int offset = this.styledDocument.getLength();

		try {
			this.styledDocument.insertString(offset, string, chatStyle);
		} catch (BadLocationException e) {
			log.error("Cant insert String into chat", e);
		}
	}

	private void initChatLogPane() {
		this.chatLogPane = new JTextPane();
		this.chatLogPane.setEditable(false);
		this.styledDocument = this.chatLogPane.getStyledDocument();
		this.registerStyles();
		this.chatLogPane.setOpaque(false);
	}

	private void registerStyles() {
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regular = this.styledDocument.addStyle(ChatStyle.REGULAR.toString(), def);

		Style bold = this.styledDocument.addStyle("bold", regular);
		StyleConstants.setBold(bold, true);

		Style blue = this.styledDocument.addStyle(ChatStyle.SERVER.toString(), bold);
		StyleConstants.setForeground(blue, Color.blue);

		Style red = this.styledDocument.addStyle(ChatStyle.CLIENT.toString(), bold);
		StyleConstants.setForeground(red, Color.red);
	}

	public JPanel getChat() {
		return this.chatPanel;
	}

	public void setGameMaster(NetworkGameMaster gameMaster) {
		if (this.gameMaster != null) {
			this.gameMaster.removeChatListener(this);
		}

		gameMaster.addChatListener(this);

		this.gameMaster = gameMaster;
	}

	public void setActive(boolean active) {
		if (!active) {
			this.addChatMessage("", "-------------------------------------");
		}
		this.chatPanel.setVisible(active);
	}

	public boolean isActive() {
		return this.chatPanel.isVisible();
	}

	private final class ChatMessageAction extends AbstractAction {
		private static final long serialVersionUID = -2021306482793281158L;

		public ChatMessageAction() {
			this.putValue(NAME, "Senden");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Chat.this.handleChatMessage();
		}
	}

	@Override
	public void chatMessage(ChatMessage message) {
		this.addChatMessage(message.getName(), message.getText(), message.getChatStyle());
	}

}
