package org.concord.energy3d.gui;

import java.security.AllPermission;

import javax.swing.UIManager;

import org.concord.energy3d.scene.SceneManager;

public class MainApplication {

	public static void main(String[] args) {
		if (System.getProperty("javawebstart.version", null) != null)
			System.out.println("Application is lauched by webstart, therefore skiping library path setup.");
		else
			try {
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
	}

	private static void setupLibraryPath() {
		final String orgLibraryPath = System.getProperty("java.library.path");
		final String sep = System.getProperty("file.separator");
		final String joglNativePath = "." + sep + "lib" + sep + "jogl" + sep + "native";
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

		final String newLibraryPath = joglNativePath + sep + OSPath + System.getProperty("path.separator") + orgLibraryPath;
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

	// /* returns 4 for 32bit and 8 for 64bit */
	// private static int detectBits() {
	// try {
	// Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
	// unsafeField.setAccessible(true);
	// Unsafe unsafe = (Unsafe) unsafeField.get(null);
	// System.out.println("Detecting..." + unsafe.addressSize() + " bits");
	// return unsafe.addressSize();
	// } catch (Exception e) {
	// e.printStackTrace();
	// return 0;
	// }
	// }

}
