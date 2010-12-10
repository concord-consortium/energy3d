package org.concord.energy3d.gui;

import javax.swing.UIManager;

import org.concord.energy3d.scene.SceneManager;

public class MainApplication {

	public static void main(String[] args){ // throws LWJGLException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
//		MainFrame frame = MainFrame.getInstance();
//		SceneManager scene = new SceneManager(frame.getContentPane());		
		final SceneManager scene = SceneManager.getInstance();
		MainFrame.getInstance().setVisible(true);
//		frame.setVisible(true);
		new Thread(scene, "Energy 3D Application").start();
	}

}
