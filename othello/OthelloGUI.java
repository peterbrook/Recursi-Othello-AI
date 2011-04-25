package othello;

/*
 Reversi

 Reversi (Othello) is a game based on a grid with eight rows and eight columns, played between you and the computer, by adding pieces with two sides: black and white.
 At the beginning of the game there are 4 pieces in the grid, the player with the black pieces is the first one to place his piece on the board.
 Each player must place a piece in a position that there exists at least one straight (horizontal, vertical, or diagonal) line between the new piece and another piece of the same color, with one or more contiguous opposite pieces between them. 

 Usage:  java Reversi

 10-12-2006 version 0.1: initial release
 26-12-2006 version 0.15: added support for applet
 01-11-2007 version 0.16: minor improvement in level handling

 Requirement: Java 1.5 or later

 future features:
 - undo
 - save/load board on file, logging of moves
 - autoplay
 - sound

 This software is released under the GNU GENERAL PUBLIC LICENSE, see attached file gpl.txt
 */

import gamePlayer.Decider;
import gamePlayer.InvalidActionException;
import gamePlayer.State.Status;
import gamePlayer.algorithms.NegaMaxDecider;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;

@SuppressWarnings("serial")
class GPanel extends JPanel implements MouseListener {

	OthelloState board;
	int gameLevel;
	ImageIcon button_black, button_white;
	JLabel score_black, score_white;
	String gameTheme;
	Object hint = null;
	boolean inputEnabled, active;

	public GPanel(OthelloState board, JLabel score_black, JLabel score_white,
			String theme, int level) {
		super();
		this.board = board;
		this.score_black = score_black;
		this.score_white = score_white;
		gameLevel = level;
		setTheme(theme);
		addMouseListener(this);
		inputEnabled = true;
		active = true;
	}

	public void setTheme(String gameTheme) {
		hint = null;
		this.gameTheme = gameTheme;
		if (gameTheme.equals("Classic")) {
			button_black = new ImageIcon(OthelloGUI.class
					.getResource("button_black.jpg"));
			button_white = new ImageIcon(OthelloGUI.class
					.getResource("button_white.jpg"));
			setBackground(Color.green);
		} else if (gameTheme.equals("Electric")) {
			button_black = new ImageIcon(OthelloGUI.class
					.getResource("button_blu.jpg"));
			button_white = new ImageIcon(OthelloGUI.class
					.getResource("button_red.jpg"));
			setBackground(Color.white);
		} else {
			gameTheme = "Flat"; // default theme "Flat"
			setBackground(Color.green);
		}
		repaint();
	}

	public void setLevel(int level) {
		if ((level > 1) && (level < 7))
			gameLevel = level;
	}

	public void drawPanel(Graphics g) {
		// int currentWidth = getWidth();
		// int currentHeight = getHeight();
		for (int i = 1; i < 8; i++) {
			g.drawLine(i * OthelloGUI.Square_L, 0, i * OthelloGUI.Square_L,
					OthelloGUI.Height);
		}
		g.drawLine(OthelloGUI.Width, 0, OthelloGUI.Width, OthelloGUI.Height);
		for (int i = 1; i < 8; i++) {
			g.drawLine(0, i * OthelloGUI.Square_L, OthelloGUI.Width, i
					* OthelloGUI.Square_L);
		}
		g.drawLine(0, OthelloGUI.Height, OthelloGUI.Width, OthelloGUI.Height);
		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 8; j++)
				switch (board.at(i, j)) {
				case '2':
					if (gameTheme.equals("Flat")) {
						g.setColor(Color.white);
						g.fillOval(1 + i * OthelloGUI.Square_L, 1 + j
								* OthelloGUI.Square_L, OthelloGUI.Square_L - 1,
								OthelloGUI.Square_L - 1);
					} else
						g.drawImage(button_white.getImage(), 1 + i
								* OthelloGUI.Square_L, 1 + j
								* OthelloGUI.Square_L, this);
					break;
				case '3':
					if (gameTheme.equals("Flat")) {
						g.setColor(Color.black);
						g.fillOval(1 + i * OthelloGUI.Square_L, 1 + j
								* OthelloGUI.Square_L, OthelloGUI.Square_L - 1,
								OthelloGUI.Square_L - 1);
					} else
						g.drawImage(button_black.getImage(), 1 + i
								* OthelloGUI.Square_L, 1 + j
								* OthelloGUI.Square_L, this);
					break;
				}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawPanel(g);
	}

	public Dimension getPreferredSize() {
		return new Dimension(OthelloGUI.Width, OthelloGUI.Height);
	}

	public void showWinner() {
		inputEnabled = false;
		active = false;/*
						 * if (board.counter[0] > board.counter[1])
						 * JOptionPane.showMessageDialog(this,
						 * "You win!","Reversi"
						 * ,JOptionPane.INFORMATION_MESSAGE); else if
						 * (board.counter[0] < board.counter[1])
						 * JOptionPane.showMessageDialog(this,
						 * "I win!","Reversi",JOptionPane.INFORMATION_MESSAGE);
						 * else JOptionPane.showMessageDialog(this,
						 * "Drawn!","Reversi",JOptionPane.INFORMATION_MESSAGE);
						 */
	}

	public void clear() {
		/*
		 * board.clear();
		 * score_black.setText(Integer.toString(board.getCounter(TKind.black)));
		 * score_white.setText(Integer.toString(board.getCounter(TKind.white)));
		 */
		inputEnabled = true;
		active = true;
	}

	public void computerMove() {
		if (board.getStatus() != Status.Ongoing) {
			showWinner();
			return;
		}
		
		
		/*
		 * Move move = new Move(); if
		 * (board.findMove(TKind.white,gameLevel,move)) {
		 * board.move(move,TKind.white);
		 * score_black.setText(Integer.toString(board.getCounter(TKind.black)));
		 * score_white.setText(Integer.toString(board.getCounter(TKind.white)));
		 * repaint(); if (board.gameEnd()) showWinner(); else if
		 * (!board.userCanMove(TKind.black)) {
		 * JOptionPane.showMessageDialog(this,
		 * "You pass...","Reversi",JOptionPane.INFORMATION_MESSAGE);
		 * javax.swing.SwingUtilities.invokeLater(new Runnable() { public void
		 * run() { computerMove(); } }); } } else if
		 * (board.userCanMove(TKind.black)) JOptionPane.showMessageDialog(this,
		 * "I pass...","Reversi",JOptionPane.INFORMATION_MESSAGE); else
		 * showWinner();
		 */
	}

	public void mouseClicked(MouseEvent e) {
		if (inputEnabled) {
			hint = null;
			int i = e.getX() / OthelloGUI.Square_L;
			int j = e.getY() / OthelloGUI.Square_L;
			if ((i < 8) && (j < 8) && (board.at(i, j) == '0')) {
				try {
					board = new OthelloAction(false, (byte)i, (byte)j).applyTo(board);
				} catch (InvalidActionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				/*
				 * score_black.setText(Integer.toString(board.getCounter(TKind.black
				 * )));
				 * score_white.setText(Integer.toString(board.getCounter(TKind
				 * .white)));
				 */
				repaint();
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						Cursor savedCursor = getCursor();
						setCursor(Cursor
								.getPredefinedCursor(Cursor.WAIT_CURSOR));
						computerMove();
						setCursor(savedCursor);
					}
				});
			} else
				JOptionPane.showMessageDialog(this, "Illegal move", "Reversi",
						JOptionPane.ERROR_MESSAGE);
		}
	}

	public void mouseEntered(MouseEvent e) {
		// generato quando il mouse entra nella finestra
	}

	public void mouseExited(MouseEvent e) {
		// generato quando il mouse esce dalla finestra
	}

	public void mousePressed(MouseEvent e) {
		// generato nell'istante in cui il mouse viene premuto
	}

	public void mouseReleased(MouseEvent e) {
		// generato quando il mouse viene rilasciato, anche a seguito di click
	}

};

public class OthelloGUI extends JFrame implements ActionListener {

	JEditorPane editorPane;

	static final String WindowTitle = "Reversi";
	static final String ABOUTMSG = WindowTitle + "\n\n26-12-2006\njavalc6";

	static GPanel gpanel;
	static JMenuItem hint;
	static boolean helpActive = false;

	static final int Square_L = 33; // length in pixel of a square in the grid
	static final int Width = 8 * Square_L; // Width of the game board
	static final int Height = 8 * Square_L; // Width of the game board

	OthelloState board;
	Decider playerOne, playerTwo;
	static JLabel score_black, score_white;
	JMenu level, theme;

	public OthelloGUI() {
		super(WindowTitle);

		score_black = new JLabel("2"); // the game start with 2 black pieces
		score_black.setForeground(Color.blue);
		score_black.setFont(new Font("Dialog", Font.BOLD, 16));
		score_white = new JLabel("2"); // the game start with 2 white pieces
		score_white.setForeground(Color.red);
		score_white.setFont(new Font("Dialog", Font.BOLD, 16));
		board = new OthelloState();
		playerOne = new NegaMaxDecider(true, 6);
		playerTwo = new OthelloPlayer(false);
		gpanel = new GPanel(board, score_black, score_white, "", 3);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setupMenuBar();
		gpanel
				.setMinimumSize(new Dimension(OthelloGUI.Width,
						OthelloGUI.Height));

		JPanel status = new JPanel();
		status.setLayout(new BorderLayout());
		status.add(score_black, BorderLayout.WEST);
		status.add(score_white, BorderLayout.EAST);
		// status.setMinimumSize(new Dimension(100, 30));
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				gpanel, status);
		splitPane.setOneTouchExpandable(false);
		getContentPane().add(splitPane);

		pack();
		setVisible(true);
		setResizable(false);
	}

	// voci del menu di primo livello
	// File Edit Help
	//
	void setupMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(buildGameMenu());
		menuBar.add(buildHelpMenu());
		setJMenuBar(menuBar);
	}

	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String action = source.getText();
		if (action.equals("Classic"))
			gpanel.setTheme(action);
		else if (action.equals("Electric"))
			gpanel.setTheme(action);
		else if (action.equals("Flat"))
			gpanel.setTheme(action);
	}

	protected JMenu buildGameMenu() {
		JMenu game = new JMenu("Game");
		JMenuItem newWin = new JMenuItem("New");
		level = new JMenu("Level");
		theme = new JMenu("Theme");
		JMenuItem undo = new JMenuItem("Undo");
		hint = new JMenuItem("Hint");
		undo.setEnabled(false);
		JMenuItem quit = new JMenuItem("Quit");

		// build level sub-menu
		ActionListener newLevel = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JMenuItem source = (JMenuItem) (e.getSource());
				gpanel.setLevel(Integer.parseInt(source.getText()));
			}
		};
		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem("2");
		group.add(rbMenuItem);
		level.add(rbMenuItem).addActionListener(newLevel);
		rbMenuItem = new JRadioButtonMenuItem("3", true);
		group.add(rbMenuItem);
		level.add(rbMenuItem).addActionListener(newLevel);
		rbMenuItem = new JRadioButtonMenuItem("4");
		group.add(rbMenuItem);
		level.add(rbMenuItem).addActionListener(newLevel);
		rbMenuItem = new JRadioButtonMenuItem("5");
		group.add(rbMenuItem);
		level.add(rbMenuItem).addActionListener(newLevel);
		rbMenuItem = new JRadioButtonMenuItem("6");
		group.add(rbMenuItem);
		level.add(rbMenuItem).addActionListener(newLevel);

		// build theme sub-menu
		group = new ButtonGroup();
		rbMenuItem = new JRadioButtonMenuItem("Classic");
		group.add(rbMenuItem);
		theme.add(rbMenuItem);
		rbMenuItem.addActionListener(this);
		rbMenuItem = new JRadioButtonMenuItem("Electric", true);
		group.add(rbMenuItem);
		theme.add(rbMenuItem);
		rbMenuItem.addActionListener(this);
		rbMenuItem = new JRadioButtonMenuItem("Flat");
		group.add(rbMenuItem);
		theme.add(rbMenuItem);
		rbMenuItem.addActionListener(this);

		// Begin "New"
		newWin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gpanel.clear();
				hint.setEnabled(true);
				repaint();
			}
		});
		// End "New"

		// Begin "Quit"
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		// End "Quit"

		game.add(newWin);
		game.addSeparator();
		game.add(undo);
		game.add(hint);
		game.addSeparator();
		game.add(level);
		game.add(theme);
		game.addSeparator();
		game.add(quit);
		return game;
	}

	protected JMenu buildHelpMenu() {
		JMenu help = new JMenu("Help");
		JMenuItem about = new JMenuItem("About " + WindowTitle + "...");
		JMenuItem openHelp = new JMenuItem("Help Topics...");

		// Begin "Help"
		openHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createEditorPane();
			}
		});
		// End "Help"

		// Begin "About"
		about.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ImageIcon icon = new ImageIcon(OthelloGUI.class
						.getResource("reversi.jpg"));
				JOptionPane.showMessageDialog(null, ABOUTMSG, "About "
						+ WindowTitle, JOptionPane.PLAIN_MESSAGE, icon);
			}
		});
		// End "About"

		help.add(openHelp);
		help.add(about);

		return help;
	}

	protected void createEditorPane() {
		if (helpActive)
			return;
		editorPane = new JEditorPane();
		editorPane.setEditable(false);
		editorPane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (e instanceof HTMLFrameHyperlinkEvent) {
						((HTMLDocument) editorPane.getDocument())
								.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent) e);
					} else {
						try {
							editorPane.setPage(e.getURL());
						} catch (java.io.IOException ioe) {
							System.out.println("IOE: " + ioe);
						}
					}
				}
			}
		});
		java.net.URL helpURL = OthelloGUI.class.getResource("HelpFile.html");
		if (helpURL != null) {
			try {
				editorPane.setPage(helpURL);
				new HelpWindow(editorPane);
			} catch (java.io.IOException e) {
				System.err.println("Attempted to read a bad URL: " + helpURL);
			}
		} else {
			System.err.println("Couldn't find file: HelpFile.html");
		}

		return;
	}

	public class HelpWindow extends JFrame {

		public HelpWindow(JEditorPane editorPane) {
			super("Help Window");
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					OthelloGUI.helpActive = false;
					setVisible(false);
				}
			});

			JScrollPane editorScrollPane = new JScrollPane(editorPane);
			editorScrollPane
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			getContentPane().add(editorScrollPane);
			setSize(600, 400);
			setVisible(true);
			helpActive = true;
		}
	}

	public HyperlinkListener createHyperLinkListener1() {
		return new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (e instanceof HTMLFrameHyperlinkEvent) {
						((HTMLDocument) editorPane.getDocument())
								.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent) e);
					} else {
						try {
							editorPane.setPage(e.getURL());
						} catch (java.io.IOException ioe) {
							System.out.println("IOE: " + ioe);
						}
					}
				}
			}
		};
	}

	public static void main(String[] args) {
		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
		}
		OthelloGUI app = new OthelloGUI();
	}

}
