package org.concord.energy3d.logger;

import java.io.File;
import java.io.PrintWriter;
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
import org.concord.energy3d.util.Config;

/**
 * @author Charles Xie
 * 
 */
public class PostProcessor {

	private final static int SLEEP = 200;

	private PostProcessor() {

	}

	public static void process(final File[] files, final File output, final Runnable update) {

		final int n = files.length;
		PrintWriter logWriter = null;
		try {
			logWriter = new PrintWriter(output);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		final PrintWriter pw = logWriter;
		new Thread() {
			@Override
			public void run() {
				int i = -1;
				Date zero = null;
				long timestamp = -1;
				while (i < n - 1) {
					if (Config.replaying) {
						i++;
						// if (i == n) i = 0;
						int slash = files[i].toString().lastIndexOf(System.getProperty("file.separator"));
						String fileName = files[i].toString().substring(slash + 1).trim();
						fileName = fileName.substring(0, fileName.length() - 4);
						String[] ss = fileName.split("[\\s]+"); // get time stamp
						String[] day = ss[0].split("-");
						String[] time = ss[1].split("-");
						Calendar c = Calendar.getInstance();
						c.set(Integer.parseInt(day[0]), Integer.parseInt(day[1]) - 1, Integer.parseInt(day[2]), Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]));
						if (zero == null)
							zero = c.getTime();
						timestamp = Math.round((c.getTime().getTime() - zero.getTime()) * 0.001);
						System.out.println("Play back " + i + " of " + n + ": " + fileName + " -- " + timestamp);
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
						pw.print(timestamp + "  Total=" + parts.size());
						pw.print("  Wall=" + wallCount);
						pw.print("  Window=" + windowCount);
						pw.print("  Foundation=" + foundationCount);
						pw.print("  Roof=" + roofCount);
						pw.println("  Floor=" + floorCount);
					} else {
						if (Config.backward) {
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
							Config.backward = false;
						} else if (Config.forward) {
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
							Config.forward = false;
						}
					}
				}
				pw.close();
			}
		}.start();
	}

}
