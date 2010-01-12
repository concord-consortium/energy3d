package cc.househeat;

import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import javax.swing.JToggleButton;

import cc.househeat.model.SceneManager;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JMenuBar appMenuBar = null;
	private JMenu fileMenu = null;
	private JToolBar appToolbar = null;
	private JToggleButton selectButton = null;
	private JToggleButton lineButton = null;
	private JToggleButton rectangleButton = null;

	/**
	 * This method initializes appMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getAppMenuBar() {
		if (appMenuBar == null) {
			appMenuBar = new JMenuBar();
			appMenuBar.add(getFileMenu());
		}
		return appMenuBar;
	}

	/**
	 * This method initializes fileMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu();
			fileMenu.setText("File");
		}
		return fileMenu;
	}

	/**
	 * This method initializes appToolbar	
	 * 	
	 * @return javax.swing.JToolBar	
	 */
	private JToolBar getAppToolbar() {
		if (appToolbar == null) {
			appToolbar = new JToolBar();
			appToolbar.add(getSelectButton());
			appToolbar.addSeparator();
			appToolbar.add(getLineButton());
			appToolbar.add(getRectangleButton());
			
			ButtonGroup bg = new ButtonGroup();
			bg.add(selectButton);
			bg.add(lineButton);
			bg.add(rectangleButton);
		}
		return appToolbar;
	}

	/**
	 * This method initializes selectButton	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getSelectButton() {
		if (selectButton == null) {
			selectButton = new JToggleButton();
			selectButton.setText("Select");
			selectButton.setSelected(true);
			selectButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.SELECT);
				}
			});
		}
		return selectButton;
	}

	/**
	 * This method initializes lineButton	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getLineButton() {
		if (lineButton == null) {
			lineButton = new JToggleButton();
			lineButton.setText("Line");
			lineButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.DRAW_LINES);
				}
			});
		}
		return lineButton;
	}

	/**
	 * This method initializes rectangleButton	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getRectangleButton() {
		if (rectangleButton == null) {
			rectangleButton = new JToggleButton();
			rectangleButton.setText("Rectangle");
			rectangleButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.DRAW_RECTANGLE);
				}
			});
		}
		return rectangleButton;
	}

	/**
		 * @param args
		 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainFrame thisClass = new MainFrame();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}

	/**
	 * This is the default constructor
	 */
	public MainFrame() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(800, 600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setJMenuBar(getAppMenuBar());
		this.setContentPane(getJContentPane());
		this.setTitle("Solar House Simulation");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.setVisible(false);
			jContentPane.add(getAppToolbar(), BorderLayout.NORTH);
		}
		return jContentPane;
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		jContentPane.setVisible(visible);
	}
}
