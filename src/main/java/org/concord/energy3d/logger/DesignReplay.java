package org.concord.energy3d.logger;

import java.awt.EventQueue;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.ViewMode;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
public class DesignReplay extends PlayControl {

	private final static int SLEEP = 250;
	private final static DesignReplay instance = new DesignReplay();

	private File lastFolder;

	private DesignReplay() {
	}

	public static DesignReplay getInstance() {
		return instance;
	}

	public File getLastFolder() {
		return lastFolder;
	}

	public void play(final File[] files) {
		new Thread("Energy3D Design Replay") {
			@Override
			public void run() {
				MainPanel.getInstance().setToolbarEnabledForReplay(false);
				Util.suppressReportError = true;
				Arrays.sort(files, new FileComparator());
				openFolder(files);
				Util.suppressReportError = false;
				MainPanel.getInstance().setToolbarEnabledForReplay(true);
			}
		}.start();
	}

	private void openFolder(final File[] files) {

		active = true;
		final int n = files.length;
		if (n > 0) {
			lastFolder = files[0].getParentFile();
		}
		int i = -1;
		while (i < n) {
			if (!active) {
				break;
			}
			if (replaying) {
				i++;
				if (i == n) {
					break;
				}
				final int slash = files[i].toString().lastIndexOf(System.getProperty("file.separator"));
				final String fileName = files[i].toString().substring(slash + 1).trim();
				System.out.println("Play back " + (i + 1) + " of " + n + ": " + fileName);
				try {
					Scene.openNow(files[i].toURI().toURL());
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							EnergyPanel.getInstance().updateRadiationHeatMap();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									EnergyPanel.getInstance().update();
									EnergyPanel.getInstance().clearAllGraphs();
									final HousePart p = SceneManager.getInstance().getSelectedPart();
									if (p instanceof Foundation) {
										EnergyPanel.getInstance().getBuildingCostGraph().addGraph((Foundation) p);
										EnergyPanel.getInstance().validate();
									}
									if (MainFrame.getInstance().getTopViewCheckBoxMenuItem().isSelected()) { // make sure we exist the 2D top view
										MainFrame.getInstance().getTopViewCheckBoxMenuItem().setSelected(false);
										SceneManager.getInstance().resetCamera(ViewMode.NORMAL);
										SceneManager.getInstance().resetCamera();
									}
								}
							});
							return null;
						}
					});
					Thread.sleep(SLEEP);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			} else {
				if (backward) {
					if (i > 0) {
						i--;
						System.out.println("Play back " + (i + 1) + " of " + n);
						try {
							Scene.open(files[i].toURI().toURL());
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
					backward = false;
				} else if (forward) {
					if (i < n - 1) {
						i++;
						System.out.println("Play back " + (i + 1) + " of " + n);
						try {
							Scene.open(files[i].toURI().toURL());
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
					forward = false;
					if (i >= n - 1) { // don't break out from the loop, keep it alive so that the user can go back; for exiting the loop, user should press the space key
						i = n - 1;
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "This is the end of the replay. Press the left arrow key to go back or the space key to exit.", "End of Design Replay", JOptionPane.INFORMATION_MESSAGE);
							}
						});
					}
				}
			}
		}
		active = false;

	}

}
