package org.concord.energy3d.logger;

import java.awt.EventQueue;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.gui.EnergyPanel.UpdateRadiation;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
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
		new Thread() {
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
		if (n > 0)
			lastFolder = files[0].getParentFile();
		int i = -1;
		while (i < n) {
			if (replaying) {
				i++;
				if (i == n)
					break;
				final int slash = files[i].toString().lastIndexOf(System.getProperty("file.separator"));
				final String fileName = files[i].toString().substring(slash + 1).trim();
				System.out.println("Play back " + (i + 1) + " of " + n + ": " + fileName);
				try {
					Scene.openNow(files[i].toURI().toURL());
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.initSceneNow();
							Scene.getInstance().redrawAllNow();
							Scene.initEnergy();
							EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
							EnergyPanel.getInstance().update();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									EnergyPanel.getInstance().clearAllGraphs();
									HousePart p = SceneManager.getInstance().getSelectedPart();
									if (p instanceof Foundation) {
										EnergyPanel.getInstance().getConstructionCostGraph().addGraph((Foundation) p);
										EnergyPanel.getInstance().validate();
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
