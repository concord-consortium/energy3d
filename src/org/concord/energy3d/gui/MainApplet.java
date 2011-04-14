package org.concord.energy3d.gui;

import javax.swing.JApplet;
import javax.swing.UIManager;

import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Config;

public class MainApplet extends JApplet {
	private static final long serialVersionUID = 1L;
	private MainPanel mainPanel = null;

	public MainApplet() {
		super();
		Config.setApplet(this);
	}

	public void init() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception err) {
			err.printStackTrace();
		}
//		this.setSize(300, 200);
		this.setContentPane(getMainPanel());
		SceneManager.getInstance();
	}	

	private MainPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = MainPanel.getInstance();
		}
		return mainPanel;
	}

	@Override
	public void start() {
		new Thread(SceneManager.getInstance(), "Energy 3D Application").start();
	}

	@Override
	public void stop() {
		SceneManager.getInstance().exit();		
	}
	
}
