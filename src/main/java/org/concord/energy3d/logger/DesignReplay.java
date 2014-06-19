package org.concord.energy3d.logger;

import java.io.File;
import java.util.concurrent.Callable;

import org.concord.energy3d.gui.EnergyPanel;
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

	private DesignReplay() {

	}

	public static void play(final File[] files, final Runnable update) {
		new Thread() {
			@Override
			public void run() {
				Util.suppressReportError = true;
				openFolder(files, update);
				Util.suppressReportError = false;
			}
		}.start();
	}

	private static void updateEnergyPanel() {
		for (HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Foundation) {
				SceneManager.getInstance().setSelectedPart(p);
				break;
			}
		}
		EnergyPanel.getInstance().updateCost();
		EnergyPanel.getInstance().updateBudgetBar();
		EnergyPanel.getInstance().updateAreaBar();
		EnergyPanel.getInstance().updateHeightBar();
		EnergyPanel.getInstance().updatePartEnergy();
	}

	private static void openFolder(final File[] files, final Runnable update) {

		final int n = files.length;
		int i = -1;
		while (i < n - 1) {
			if (replaying) {
				i++;
				final int slash = files[i].toString().lastIndexOf(System.getProperty("file.separator"));
				final String fileName = files[i].toString().substring(slash + 1).trim();
				System.out.println("Play back " + i + " of " + n + ": " + fileName);
				try {
					Scene.openNow(files[i].toURI().toURL());
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.initSceneNow();
							Scene.initEnergy();
							EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
							return null;
						}
					});
					update.run();
					updateEnergyPanel();
					Thread.sleep(SLEEP);
				} catch (final Exception e) {
					e.printStackTrace();
				}
				// if (i == n - 1) i = 0;
			} else {
				if (backward) {
					if (i > 0) {
						i--;
						System.out.println("Play back " + i + " of " + n);
						try {
							Scene.open(files[i].toURI().toURL());
							update.run();
							updateEnergyPanel();
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
					backward = false;
				} else if (forward) {
					if (i < n - 1) {
						i++;
						System.out.println("Play back " + i + " of " + n);
						try {
							Scene.open(files[i].toURI().toURL());
							update.run();
							updateEnergyPanel();
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
					forward = false;
					if (i == n - 1)
						i = n - 2;
				}
			}
		}

	}

}
