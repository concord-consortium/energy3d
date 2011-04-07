package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.UIManager;

import org.concord.energy3d.scene.SceneManager;

public class MainApplet extends JApplet {
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JButton startButton = null;

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
		this.setSize(300, 200);
		this.setContentPane(getJContentPane());
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
			jContentPane.add(getStartButton(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes startButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getStartButton() {
		if (startButton == null) {
			startButton = new JButton();
			startButton.setText("Start");
			startButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (Exception err) {
						err.printStackTrace();
					}
					final SceneManager scene = SceneManager.getInstance();
					MainFrame.getInstance().setVisible(true);
					new Thread(scene, "Energy 3D Application").start();
				}
			});
		}
		return startButton;
	}

}
