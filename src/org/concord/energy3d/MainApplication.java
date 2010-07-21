package org.concord.energy3d;

import org.concord.energy3d.scene.SceneManager;

public class MainApplication {

	public static void main(String[] args) {
//		MainFrame frame = new MainFrame();
		MainFrame frame = MainFrame.getInstance();
		SceneManager scene = new SceneManager(frame.getContentPane());		
		frame.setVisible(true);
		new Thread(scene, "Energy 3D Application").start();
	}

}
