package org.concord.energy3d.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.logger.SnapshotLogger;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

/**
 * @author Charles Xie
 *
 */
public class BugReporter {

	public static void report(final Throwable e) {
		report(e, "");
	}

	public static void report(final Throwable e, final String header) {
		if (Util.suppressReportError) {
			return;
		}
		e.printStackTrace();
		final StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		final String msg = sw.toString();
		if (msg.indexOf("java.lang.OutOfMemoryError") != -1) { // in this case, we may not have enough resource to send error report. just advise user to restart
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Energy3D has temporarily run out of memory. If this message<br>persists, please restart the software.</html>", "Out of Memory", JOptionPane.ERROR_MESSAGE);
					System.gc();
				}
			});
		} else if (msg.indexOf("Error in opengl: invalid enumerant") != -1) { // Intel driver bug reminder
			if (Config.isWindows()) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Oops, Energy3D will not work with the current graphics card driver.</html>", "Graphics Driver Problem", JOptionPane.ERROR_MESSAGE);
					}
				});
			}
		} else {
			final String text = header + "\n" + msg;
			File file;
			try {
				file = SnapshotLogger.getInstance().saveSnapshot("error");
			} catch (final Exception ex) {
				ex.printStackTrace();
				file = null;
			}
			final File currentFile = file;
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					final JPanel panel = new JPanel(new BorderLayout(10, 10));
					final JScrollPane scrollPane = new JScrollPane(new JTextArea(msg));
					scrollPane.setPreferredSize(new Dimension(400, 400));
					panel.add(scrollPane, BorderLayout.CENTER);
					final boolean corrupted = msg.indexOf("java.io.EOFException") != -1;
					panel.add(new JLabel("<html><b>" + (corrupted ? "Your file is corrupted. Please use <i>Recover from Log</i> under the File Menu to restore it.<br>" : "") + "Report the above error message to the developers?</b></html>"), BorderLayout.SOUTH);
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), panel, "Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION) {
						new Uploader(text, currentFile).execute();
					}
				}
			});
		}
	}

	private static class Uploader extends SwingWorker<String, Void> {

		private final String text;
		private final File currentFile;

		Uploader(final String text, final File currentFile) {
			super();
			this.text = text;
			this.currentFile = currentFile;
		}

		@Override
		protected String doInBackground() throws Exception {
			return upload(text, currentFile);
		}

		@Override
		protected void done() {
			try {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), get(), "Notice", JOptionPane.INFORMATION_MESSAGE);
			} catch (final Exception e) { // backup solution
				e.printStackTrace();
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html><h1>Error message copied</h1>Please paste it in your email and send it to qxie@concord.org.<br>Thanks for your help for this open-source project!</html>", "Noficiation", JOptionPane.INFORMATION_MESSAGE);
			}
			// attempt to fix problems
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() {
					Scene.getInstance().fixProblems(true);
					return null;
				}
			});
		}
	}

	public static String upload(final String msg, final File currentFile) throws Exception {
		final MultipartUtility multipart = new MultipartUtility("http://energy3d.concord.org/errors/error.php", "UTF-8");
		multipart.addFormField("ip_address", InetAddress.getLocalHost().getHostAddress());
		multipart.addFormField("os_name", System.getProperty("os.name"));
		multipart.addFormField("user_name", System.getProperty("user.name"));
		multipart.addFormField("os_version", System.getProperty("os.version"));
		multipart.addFormField("energy3d_version", MainApplication.VERSION);
		multipart.addFormField("error_message", msg);
		if (!Scene.isInternalFile()) {
			final URL url = Scene.getURL();
			if (url != null) {
				final File f = new File(url.toURI());
				if (f.exists()) {
					multipart.addFilePart("model_lastsaved", f);
				}
			}
			final File file = SnapshotLogger.getInstance().getLatestSnapshot();
			if (file != null) {
				multipart.addFilePart("model_snapshot", file);
			}
			if (currentFile != null) {
				multipart.addFilePart("model_current", currentFile);
			}
		}
		return multipart.finish();
	}

}
