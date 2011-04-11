package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Graphics;

import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.concord.energy3d.scene.SceneManager;

public class MainApplet extends JApplet {
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private MainPanel mainPanel = null;
	private boolean isStarted = false;
	/**
	 * This is the xxx default constructor
	 */
	public MainApplet() {
		super();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	public void init() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception err) {
			err.printStackTrace();
		}
		this.setSize(300, 200);
		this.setContentPane(getJContentPane());		
		
//		SceneManager.getInstance();
//		new Thread(SceneManager.getInstance(), "Energy 3D Application").start();
		
//		SceneManager.getInstance().frameHandler.init();
//		while (!exit) {			
//		SceneManager.getInstance().frameHandler.updateFrame();
		
//		SceneManager.getInstance().renderUnto(SceneManager.getInstance().getCanvas().getCanvasRenderer().getRenderer());
		
		
		
//		final SceneManager scene = SceneManager.getInstance();
//		MainFrame.getInstance().setVisible(true);
//		new Thread(SceneManager.getInstance(), "Energy 3D Application").start();		
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
			jContentPane.add(getMainPanel(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes mainPanel	
	 * 	
	 * @return org.concord.energy3d.gui.MainPanel	
	 */
	private MainPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = MainPanel.getInstance();
		}
		return mainPanel;
	}

	@Override
	public void start() {
//		SceneManager.getInstance();
//		new Thread(SceneManager.getInstance(), "Energy 3D Application").start();
//		SceneManager.getInstance().renderUnto(null);
	}

	@Override
	public void stop() {
//		SceneManager.getInstance().exit();		
	}
	
//	@Override
//	public void paint(Graphics g) {
//		if (!isStarted ) {
//			new Thread(SceneManager.getInstance(), "Energy 3D Application").start();
//			isStarted = true;
//		}
//		super.paint(g);
//	}
}
