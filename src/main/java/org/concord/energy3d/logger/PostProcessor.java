package org.concord.energy3d.logger;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

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
			public void run() {
				final int n = files.length;
				PrintWriter logWriter = null;
				try {
					logWriter = new PrintWriter(output);
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
				final PrintWriter pw = logWriter;
				int i = -1;
				Date date0 = null;
				int total0 = -1;
				int wallCount0 = -1;
				int windowCount0 = -1;
				int foundationCount0 = -1;
				int roofCount0 = -1;
				int floorCount0 = -1;
				long timestamp = -1;
				while (i < n - 1) {
					if (replaying) {
						i++;
						// if (i == n) i = 0;
						int slash = files[i].toString().lastIndexOf(System.getProperty("file.separator"));
						String fileName = files[i].toString().substring(slash + 1).trim();
						String[] ss = fileName.substring(0, fileName.length() - 4).split("[\\s]+"); // get time stamp
						String[] day = ss[0].split("-");
						String[] time = ss[1].split("-");
						Calendar c = Calendar.getInstance();
						c.set(Integer.parseInt(day[0]), Integer.parseInt(day[1]) - 1, Integer.parseInt(day[2]), Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]));
						if (date0 == null)
							date0 = c.getTime();
						timestamp = Math.round((c.getTime().getTime() - date0.getTime()) * 0.001);
						System.out.println("Play back " + i + " of " + n + ": " + fileName);
						try {
							Scene.open(files[i].toURI().toURL());
							update.run();
							sleep(SLEEP);
						} catch (final Exception e) {
							e.printStackTrace();
						}
						final ArrayList<HousePart> parts = Scene.getInstance().getParts();
						int wallCount = 0;
						int windowCount = 0;
						int foundationCount = 0;
						int roofCount = 0;
						int floorCount = 0;
						for (HousePart x : parts) {
							if (x instanceof Wall)
								wallCount++;
							else if (x instanceof Window)
								windowCount++;
							else if (x instanceof Foundation)
								foundationCount++;
							else if (x instanceof Roof)
								roofCount++;
							else if (x instanceof Floor)
								floorCount++;
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
						pw.print(fileName);
						pw.print("  Timestamp=" + FORMAT_TIME_COUNT.format(timestamp));
						pw.print("  Total=" + FORMAT_PART_COUNT.format(parts.size() - total0));
						pw.print("  Wall=" + FORMAT_PART_COUNT.format(wallCount - wallCount0));
						pw.print("  Window=" + FORMAT_PART_COUNT.format(windowCount - windowCount0));
						pw.print("  Foundation=" + FORMAT_PART_COUNT.format(foundationCount - foundationCount0));
						pw.print("  Roof=" + FORMAT_PART_COUNT.format(roofCount - roofCount0));
						pw.print("  Floor=" + FORMAT_PART_COUNT.format(floorCount - floorCount0));
						pw.println("");
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
						}
					}
				}
				pw.close();
			}
		}.start();
	}

}
