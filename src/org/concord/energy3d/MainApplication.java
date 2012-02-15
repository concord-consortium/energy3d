package org.concord.energy3d;

import java.io.IOException;

import javax.swing.UIManager;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Config;

import com.sun.deploy.association.Action;
import com.sun.deploy.association.Association;
import com.sun.deploy.association.AssociationAlreadyRegisteredException;
import com.sun.deploy.association.AssociationNotRegisteredException;
import com.sun.deploy.association.AssociationService;
import com.sun.deploy.association.RegisterFailedException;

public class MainApplication {	
	public static void main(String[] args) {
//		try {
//			System.in.read();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		final long s = System.nanoTime();
		Config.setWebStart(System.getProperty("javawebstart.version", null) != null);
		if (Config.isWebStart()) {
			System.out.println("Application is lauched by webstart.");
		} else
			try {
//				setupFileAssociations();
				setupLibraryPath();
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
//		System.out.println(System.nanoTime() - s);
	}

	private static void setupLibraryPath() {
		final String orgLibraryPath = System.getProperty("java.library.path");
		final String sep = System.getProperty("file.separator");
		final String rendererNativePath = "." + sep + "lib" + sep + (Config.JOGL ? "jogl" : "lwjgl") + sep + "native";
		final String OSPath;
		final String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("windows")) {
			final String sunArch = System.getProperty("sun.arch.data.model");
			if (sunArch != null && sunArch.startsWith("64"))
				OSPath = "windows-64";
			else
				OSPath = "windows-32";
		} else if (os.startsWith("mac")) {
			OSPath = "mac-universal";
		} else if (os.startsWith("linux")) {
			final String sunArch = System.getProperty("sun.arch.data.model");
			if (sunArch != null && sunArch.startsWith("64"))
				OSPath = "linux-64";
			else
				OSPath = "linux-32";
		} else
			throw new RuntimeException("Unknown OS: " + os);

		final String newLibraryPath = rendererNativePath + sep + OSPath + System.getProperty("path.separator") + orgLibraryPath;
		System.setProperty("java.library.path", newLibraryPath);
		System.out.println("Path = " + System.getProperty("java.library.path"));
		// The following code is to empty the library path cache in order to force JVM to use the new library path above
		java.lang.reflect.Field fieldSysPath;
		try {
			fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static void setupFileAssociations() {
		AssociationService serv = new AssociationService();
		Association logassoc = new Association();

		// Adds the .log type to the Association object.

		logassoc.addFileExtension("eg3"); 
		           
		// Adds an Action to the Association object that will 
		// open a .log file with Windows Notepad. 

		logassoc.addAction(
		  new Action(
		    "open",
		    "C:\\WINDOWS\\system32\\NOTEPAD.EXE %1"));
		                 
		try {

		   // Adds the .log Association to the file types' table 
		   // at the user level using an AssociationService object.

		   serv.registerUserAssociation(logassoc);

		} 
		catch (java.lang.IllegalArgumentException e) {

		   // This exception will be caught if the given Association is not valid 
		   // to be added to the table of file types.

		   System.err.println(e);

		}
		catch (AssociationAlreadyRegisteredException e) {

		   // This exception will be caught if the Association already
		   // exists in the table of file types.

		   System.err.println(e);

		}
		catch (RegisterFailedException e) {

		   // This exception will be caught if the Association was
		   // unable to be added to the table of file types.

		   System.err.println(e);

		}
	}
	
	private static void removeFileAssociations() {
		AssociationService serv = new AssociationService();
		
		// This uses an AssociationService to search the table of file 
		// types for the .log extension. If the .log file is found, 
		// an Association object representing the .log file type 
		// will be returned. Otherwise, null is returned. 

		Association logassoc = serv.getFileExtensionAssociation("LOG");

		try {

		   // The AssociationService will remove the .log file type from 
		   // the table of file types.

		   serv.unregisterUserAssociation(logassoc);

		} catch (java.lang.IllegalArgumentException e) {

		   // This exception will be caught if the given Association is not valid 
		   // to be removed from the table of file types.

		   System.err.println(e);

		} catch (AssociationNotRegisteredException e) {

		   // This exception will be caught if the Association does not already  
		   // exist in the table of file types.

		   System.err.println(e);

		} catch (RegisterFailedException e) {

		   // This exception will be caughtif the association was unable to be 
		   // removed from the table of file types.

		   System.err.println(e);
		}
	}
}
