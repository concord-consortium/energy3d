package org.concord.energy3d.logger;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.EnergyPanel.UpdateRadiation;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

/**
 * @author Charles Xie
 * 
 */
public class PostProcessor {

	public static boolean replaying = true;
	public static boolean backward, forward;

	private final static int SLEEP = 200;
	private final static DecimalFormat FORMAT_TIME_COUNT = new DecimalFormat("000000000");
	private final static DecimalFormat FORMAT_PART_COUNT = new DecimalFormat("000");

	private PostProcessor() {

	}

	public static void process(final File[] files, final File output, final Runnable update) {

		new Thread() {
			@Override
			public void run() {
				EnergyPanel.getInstance().setComputeEnabled(false);
				final int n = files.length;
				PrintWriter logWriter = null;
				try {
					logWriter = new PrintWriter(output);
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
				final PrintWriter pw = logWriter;
				int i = -1;
				int total0 = -1;
				int wallCount0 = -1;
				int windowCount0 = -1;
				int foundationCount0 = -1;
				int roofCount0 = -1;
				int floorCount0 = -1;
				int doorCount0 = -1;
				long timestamp = -1;
				Date date0 = null;
				ArrayList<Building> buildings0 = null;
				while (i < n - 1) {
					if (replaying) {
						i++;
						final int slash = files[i].toString().lastIndexOf(System.getProperty("file.separator"));
						final String fileName = files[i].toString().substring(slash + 1).trim();
						final String[] ss = fileName.substring(0, fileName.length() - 4).split("[\\s]+"); // get time stamp
						if (ss.length >= 2) {
							final String[] day = ss[0].split("-");
							final String[] time = ss[1].split("-");
							final Calendar c = Calendar.getInstance();
							c.set(Integer.parseInt(day[0]), Integer.parseInt(day[1]) - 1, Integer.parseInt(day[2]), Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]));
							if (date0 == null)
								date0 = c.getTime();
							timestamp = Math.round((c.getTime().getTime() - date0.getTime()) * 0.001);
						} else {
							System.err.println("File timestamp error");
							timestamp = 0;
						}
						System.out.println("Play back " + i + " of " + n + ": " + fileName);						
						try {
							Scene.openNow(files[i].toURI().toURL());
							Scene.initSceneNow();
							Scene.initEnergy();
							EnergyPanel.getInstance().computeNow(UpdateRadiation.ALWAYS);
							SceneManager.getInstance().refresh();
							update.run();
							sleep(SLEEP);
						} catch (final Exception e) {
							e.printStackTrace();
						}
						final ArrayList<HousePart> parts = Scene.getInstance().getParts();
						final ArrayList<Building> buildings = new ArrayList<Building>();
						int wallCount = 0;
						int windowCount = 0;
						int foundationCount = 0;
						int roofCount = 0;
						int floorCount = 0;
						int doorCount = 0;
						if (buildings0 == null) {
							buildings0 = new ArrayList<Building>();
							for (final HousePart x : parts) {
								final int bid = ((Long) LoggerUtil.getBuildingId(x, false)).intValue();
								final Building b = new Building(bid);
								if (!buildings0.contains(b))
									buildings0.add(b);
							}
						}
						for (final HousePart x : parts) {
							// count the pieces by categories
							if (x instanceof Window)
								windowCount++;
							else if (x instanceof Foundation)
								foundationCount++;
							else if (x instanceof Roof)
								roofCount++;
							else if (x instanceof Floor)
								floorCount++;
							else if (x instanceof Door)
								doorCount++;
							else if (x instanceof Wall) {
								wallCount++;
								final int bid = ((Long) LoggerUtil.getBuildingId(x, false)).intValue();
								final Building b = new Building(bid);
								if (!buildings.contains(b) && !buildings0.contains(b))
									buildings.add(b);
							}
						}
						// scan again to compute building properties
						for (final HousePart x : parts) {
							final int bid = ((Long) LoggerUtil.getBuildingId(x, false)).intValue();
							final Building b = getBuilding(buildings, bid);
							if (b != null) {
								if (x instanceof Window)
									b.windowCount++;
								else if (x instanceof Wall)
									b.addWall((Wall) x);
							}
						}
						if (total0 == -1)
							total0 = parts.size();
						if (wallCount0 == -1)
							wallCount0 = wallCount;
						if (windowCount0 == -1)
							windowCount0 = windowCount;
						if (foundationCount0 == -1)
							foundationCount0 = foundationCount;
						if (roofCount0 == -1)
							roofCount0 = roofCount;
						if (floorCount0 == -1)
							floorCount0 = floorCount;
						if (doorCount0 == -1)
							doorCount0 = doorCount;
						pw.print(fileName);
						pw.print("  Timestamp=" + FORMAT_TIME_COUNT.format(timestamp));
						pw.print("  #Total=" + FORMAT_PART_COUNT.format(parts.size() - total0));
						pw.print("  #Wall=" + FORMAT_PART_COUNT.format(wallCount - wallCount0));
						pw.print("  #Window=" + FORMAT_PART_COUNT.format(windowCount - windowCount0));
						pw.print("  #Foundation=" + FORMAT_PART_COUNT.format(foundationCount - foundationCount0));
						pw.print("  #Roof=" + FORMAT_PART_COUNT.format(roofCount - roofCount0));
						pw.print("  #Floor=" + FORMAT_PART_COUNT.format(floorCount - floorCount0));
						pw.print("  #Door=" + FORMAT_PART_COUNT.format(doorCount - doorCount0));
						pw.print("  " + buildings);
						pw.println("");
						// if (i == n - 1) i = 0;
					} else {
						if (backward) {
							if (i > 0) {
								i--;
								System.out.println("Play back " + i + " of " + n);
								try {
									Scene.open(files[i].toURI().toURL());
									update.run();
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
				pw.close();
				EnergyPanel.getInstance().setComputeEnabled(true);
			}
		}.start();
	}

	private static Building getBuilding(final ArrayList<Building> buildings, final int id) {
		for (final Building x : buildings) {
			if (x.id == id)
				return x;
		}
		return null;
	}

}
