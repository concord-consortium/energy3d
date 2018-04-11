package org.concord.energy3d.util;

import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;

/**
 * This is a file chooser that remembers the latest path and files.
 *
 * @author Charles Xie
 */

public class FileChooser {

	public static final ExtensionFileFilter pngFilter = new ExtensionFileFilter("Image (*.png)", "png");
	public static final ExtensionFileFilter daeFilter = new ExtensionFileFilter("Collada (*.dae)", "dae");
	public static final ExtensionFileFilter objFilter = new ExtensionFileFilter("Wavefront (*.obj)", "obj");
	public static final ExtensionFileFilter ng3Filter = new ExtensionFileFilter("Energy3D (*.ng3)", "ng3");
	public static final ExtensionFileFilter zipFilter = new ExtensionFileFilter("Zip (*.zip)", "zip");

	private static final int MAX = 4;
	private static FileChooser instance;
	private final List<String> recentFiles = new ArrayList<String>();
	private final JFileChooser fileChooser;
	private FileDialog fileDialog;

	public static FileChooser getInstance() {
		if (instance == null) {
			instance = new FileChooser();
		}
		return instance;
	}

	public FileChooser() {
		String directoryPath = null;
		if (!MainApplication.runFromOnlyJar()) {
			final Preferences pref = Preferences.userNodeForPackage(MainApplication.class);
			addRecentFile(pref.get("Recent File 0", null));
			addRecentFile(pref.get("Recent File 1", null));
			addRecentFile(pref.get("Recent File 2", null));
			addRecentFile(pref.get("Recent File 3", null));
			directoryPath = pref.get("dir", null);
		}
		if (!Config.isWebStart() && directoryPath == null) {
			directoryPath = System.getProperties().getProperty("user.dir");
		}
		fileChooser = new JFileChooser(directoryPath);
		fileChooser.setAcceptAllFileFilterUsed(false);
		if (Config.isMac()) {
			fileDialog = new FileDialog(MainFrame.getInstance());
			fileDialog.setDirectory(directoryPath);
		}
	}

	public void setCurrentDirectory(final File dir) {
		fileChooser.setCurrentDirectory(dir);
	}

	public void rememberFile(final String fileName) {
		if (fileName == null) {
			return;
		}
		if (recentFiles.contains(fileName)) {
			recentFiles.remove(fileName);
		} else {
			if (recentFiles.size() >= MAX) {
				recentFiles.remove(0);
			}
		}
		recentFiles.add(fileName);
	}

	public void addRecentFile(final String fileName) {
		if (fileName != null) {
			recentFiles.add(fileName);
		}
	}

	public String[] getRecentFiles() {
		final int n = recentFiles.size();
		if (n == 0) {
			return new String[] {};
		}
		final String[] s = new String[n];
		for (int i = 0; i < n; i++) {
			s[n - 1 - i] = recentFiles.get(i);
		}
		return s;
	}

	public File showDialog(final String dotExtension, final FileFilter filter, final boolean isSaveDialog) {
		if (Config.isMac() && filter != null) {
			fileDialog.setMode(isSaveDialog ? FileDialog.SAVE : FileDialog.LOAD);
			fileDialog.setTitle(isSaveDialog ? "Save" : "Open");
			fileDialog.setFilenameFilter(new FilenameFilter() {
				@Override
				public boolean accept(final File dir, final String name) {
					return name.toLowerCase().endsWith(dotExtension);
				}
			});

			if (isSaveDialog && dotExtension.equals(".ng3") && Scene.getURL() != null) {
				fileDialog.setFile(Scene.getURL().getFile());
			} else {
				fileDialog.setFile(null);
			}

			fileDialog.setVisible(true);
			final String filename = fileDialog.getFile();
			if (filename == null) {
				System.out.println("cancelled.");
				return null;
			} else {
				final String filenameFull = fileDialog.getDirectory() + filename + (filename.toLowerCase().endsWith(dotExtension) ? "" : dotExtension);
				final File file = new File(filenameFull);
				if (!MainApplication.runFromOnlyJar()) {
					Preferences.userNodeForPackage(MainApplication.class).put("dir", fileDialog.getDirectory());
				}
				return file;
			}
		} else {
			fileChooser.setFileSelectionMode(filter == null ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
			fileChooser.resetChoosableFileFilters();
			if (filter != null) {
				fileChooser.addChoosableFileFilter(filter);
				fileChooser.setFileFilter(filter);
			}

			if (isSaveDialog && dotExtension.equals(".ng3") && Scene.getURL() != null) {
				fileChooser.setSelectedFile(new File(Scene.getURL().getFile()));
			} else {
				fileChooser.setSelectedFile(new File(""));
			}

			while (true) {
				final MainFrame parent = MainFrame.getInstance();
				if (isSaveDialog) {
					if (fileChooser.showSaveDialog(parent) == JFileChooser.CANCEL_OPTION) {
						return null;
					}
				} else if (fileChooser.showOpenDialog(parent) == JFileChooser.CANCEL_OPTION) {
					return null;
				}
				File file = fileChooser.getSelectedFile();
				if (!file.toString().toLowerCase().endsWith(dotExtension)) {
					file = new File(file.getParentFile(), Util.getFileName(file.toString()) + dotExtension);
				}
				if (!isSaveDialog || !file.exists() || JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(parent, "File \"" + file.getName() + "\" already exists. Overwrite?", "Save File", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
					if (!MainApplication.runFromOnlyJar()) {
						Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getCurrentDirectory().toString());
					}
					return file;
				}
			}
		}
	}

}
