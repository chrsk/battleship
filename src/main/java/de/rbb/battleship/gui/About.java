package de.rbb.battleship.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import de.prodv.framework.configuration.ResourceData;
import de.prodv.framework.configuration.ResourceService;
import de.rbb.battleship.util.ServiceLocator;

/**
 * About Dialog welcher einen Dialog anzeigt mit Informationen über das Schiffe
 * versenken Spiel
 */
public class About extends JDialog implements Dialog {

	ResourceService resourceService = ServiceLocator.getResourceService();

	/** serialVersionUID */
	private static final long serialVersionUID = -7894972524229103773L;

	/** Internal Panel */
	private JPanel panel, action;

	/** JLabel */
	private JLabel image;

	/** JTextArea */
	private JTextArea info;

	/** JButton */
	private JButton okay;

	/** Info/Danksagung Switch */
	boolean information = true;

	private ResourceData resourceData;

	public void open() {
		this.resourceData = this.resourceService.getResourceData("lang.main");
		this.init();
	}

	/**
	 * Initialisierung
	 */
	private void init() {
		this.setSizes();
		this.setTitle(this.resourceData.getString("system.title"));
		this.setModal(true);
		this.setAlwaysOnTop(true);
		this.createComponent();
		this.addToLayout();
		this.createActionListener();
		this.getRootPane().setDefaultButton(this.okay);
		this.setVisible(true);
	}

	/**
	 * erstellt die benötigten Komponenten.
	 */
	private void createComponent() {

		this.panel = new JPanel(new BorderLayout());
		this.panel.setBackground(Color.white);
		this.image = new JLabel(this.resourceData.getIcon("about"));

		this.info = new JTextArea();
		this.info.setText(this.resourceData.getString("about.text"));
		final Font font = UIManager.getFont("TextField.font");
		this.info.setFont(font.deriveFont(11f));
		this.info.setEditable(false);
		this.info.setLineWrap(true);
		this.info.setWrapStyleWord(true);
		this.info.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		this.okay = new JButton("Ok");
		this.okay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				About.this.dispose();
			}
		});

		this.action = new JPanel(new BorderLayout());

		this.action.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createMatteBorder(1, 0, 0, 0, Color.gray), BorderFactory.createMatteBorder(1, 0, 0, 0, Color.white)),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));
	}

	private void createActionListener() {
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		Action escapeAction = new AbstractActionExtension();
		this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		this.getRootPane().getActionMap().put("ESCAPE", escapeAction);
	}

	/**
	 * fügt die Komponenten dem JFrame hinzu.
	 */
	private void addToLayout() {
		this.panel.add(this.image, BorderLayout.NORTH);
		this.panel.add(this.info, BorderLayout.CENTER);
		this.panel.add(this.action, BorderLayout.SOUTH);
		this.action.add(this.okay, BorderLayout.EAST);
		this.add(this.panel);
	}

	/**
	 * alle relevanten Maße bezüglich des AboutFrames von der
	 * <code>ResourceService</code> Klasse geholt und entsprechend gesetzt.
	 */
	private void setSizes() {
		Dimension frameSize = new Dimension(300, 400);
		this.setSize(frameSize);
		this.setMinimumSize(frameSize);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
	}

	/**
	 * Action welche den <code>About</code> Dialog schließt, wird über die
	 * <code>ActionMap</code> mit dem <code>KeyStroke</code> "Escape"
	 * eingehängt.
	 */
	private final class AbstractActionExtension extends AbstractAction {
		private static final long serialVersionUID = 8284505223025060271L;

		public void actionPerformed(ActionEvent e) {
			About.this.dispose();
		}
	}
}
