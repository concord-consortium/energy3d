package org.concord.energy3d.logger;

import java.awt.EventQueue;
import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Building;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;

/**
 * @author Charles Xie
 *
 */
public class PostProcessor extends PlayControl {

	private final static int SLEEP = 200;
	private final static DecimalFormat FORMAT_TWO_DIGITS = new DecimalFormat("00");
	private final static DecimalFormat FORMAT_THREE_DIGITS = new DecimalFormat("000");
	private final static DecimalFormat FORMAT_TIME_COUNT = new DecimalFormat("000000000");
	private final static PostProcessor instance = new PostProcessor();

	private PostProcessor() {
	}

	public static PostProcessor getInstance() {
		return instance;
	}

	public void analyze(final File[] files, final File output, final Runnable update) {

		new Thread() {
			@Override
			public void run() {
				active = true;
				Arrays.sort(files, new FileComparator());
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
							try {
								c.set(Integer.parseInt(day[0]), Integer.parseInt(day[1]) - 1, Integer.parseInt(day[2]), Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]));
							} catch (final Exception e) {
								e.printStackTrace();
							}
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
							// Scene.initSceneNow();
							// Scene.initEnergy();
							EnergyPanel.getInstance().computeNow();
							SceneManager.getInstance().refresh();
							update.run();
							EventQueue.invokeLater(new Runnable(){
								@Override
								public void run() {
									EnergyPanel.getInstance().update();
								}
							});
							sleep(SLEEP);
						} catch (final Exception e) {
							e.printStackTrace();
						}
						final List<HousePart> parts = Scene.getInstance().getParts();
						final ArrayList<Building> buildings = new ArrayList<Building>();
						int wallCount = 0;
						int windowCount = 0;
						int foundationCount = 0;
						int roofCount = 0;
						int floorCount = 0;
						int doorCount = 0;
						if (buildings0 == null) {
							buildings0 = new ArrayList<Building>();
							synchronized (parts) {
								for (final HousePart x : parts) {
									final Building b = new Building(Building.getBuildingFoundation(x));
									if (!buildings0.contains(b))
										buildings0.add(b);
								}
							}
						}
						synchronized (parts) {
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
									final Building b = new Building(Building.getBuildingFoundation(x));
									if (!buildings.contains(b) && !buildings0.contains(b))
										buildings.add(b);
								}
							}
						}
						// scan again to compute building properties
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
						final Calendar heliodonCalendar = Heliodon.getInstance().getCalender();
						String heliodonTime = FORMAT_TWO_DIGITS.format(heliodonCalendar.get(Calendar.MONTH) + 1);
						heliodonTime += "/" + FORMAT_TWO_DIGITS.format(heliodonCalendar.get(Calendar.DAY_OF_MONTH));
						heliodonTime += ":" + FORMAT_TWO_DIGITS.format(heliodonCalendar.get(Calendar.HOUR_OF_DAY));
						pw.print(fileName);
						pw.print("  Timestamp=" + FORMAT_TIME_COUNT.format(timestamp));
						pw.print("  Heliodon=" + heliodonTime);
						pw.print("  Latitude=" + FORMAT_THREE_DIGITS.format(Math.round(180 * Heliodon.getInstance().getLatitude() / Math.PI)));
						pw.print("  #Total=" + FORMAT_THREE_DIGITS.format(parts.size() - total0));
						pw.print("  #Wall=" + FORMAT_THREE_DIGITS.format(wallCount - wallCount0));
						pw.print("  #Window=" + FORMAT_THREE_DIGITS.format(windowCount - windowCount0));
						pw.print("  #Foundation=" + FORMAT_THREE_DIGITS.format(foundationCount - foundationCount0));
						pw.print("  #Roof=" + FORMAT_THREE_DIGITS.format(roofCount - roofCount0));
						pw.print("  #Floor=" + FORMAT_THREE_DIGITS.format(floorCount - floorCount0));
						pw.print("  #Door=" + FORMAT_THREE_DIGITS.format(doorCount - doorCount0));
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
				active = false;
			}
		}.start();
	}

}
