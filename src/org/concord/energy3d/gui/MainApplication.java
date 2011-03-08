package org.concord.energy3d.gui;

import javax.swing.UIManager;

import org.concord.energy3d.scene.SceneManager;


public class MainApplication {

	public static void main(String[] args){
//		System.out.println("\nOld:\n" + System.getProperty("java.library.path"));
		final String path = System.getProperty("java.library.path");
		System.setProperty("java.library.path", "." + System.getProperty("file.separator") + "bin" + System.getProperty("path.separator") + path);
		System.out.println("Path = " + System.getProperty("java.library.path"));
		java.lang.reflect.Field fieldSysPath;
		try {
			fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
			fieldSysPath.setAccessible( true );
			fieldSysPath.set( null, null );		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		final SceneManager scene = SceneManager.getInstance();
		MainFrame.getInstance().setVisible(true);
		new Thread(scene, "Energy 3D Application").start();
	}

}
