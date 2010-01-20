package cc.energy3d;

import cc.energy3d.model.SceneManager;

public class MainApplication {

	public static void main(String[] args) {
//		HouseScene.start(HouseScene.class);
		MainFrame frame = new MainFrame();
//		frame.getContentPane().add(scene.getCanvas(), "Center");
		
		SceneManager scene = new SceneManager(frame.getContentPane());		
//		MainFrame frame = new MainFrame();
//		frame.getContentPane().add(scene.getCanvas(), "Center");
		frame.setVisible(true);
		
		new Thread(scene).start();
        // Create our example this LWJGL renderer
//        Scene exp = new Scene((JPanel)frame.getContentPane());
//        frame.getContentPane().add(exp.getCanvas(), "Center");
//        exp.getCanvas().ini
        //Start main loop
//        exp.mainLoop();
		
		
	}

}
