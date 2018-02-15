package org.concord.energy3d.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;
import java.net.InetAddress;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.logger.SnapshotLogger;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.GeoLocation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.simulation.AnnualGraph;

/**
 * @author Charles Xie
 *
 */
public class VsgSubmitter {

	public static void submit() {

		final GeoLocation geo = Scene.getInstance().getGeoLocation();
		if (geo == null) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "No geolocation is set for this model. It cannot be submitted.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String s = "{\n";
		s += "\t\"lat\": " + EnergyPanel.FIVE_DECIMALS.format(geo.getLatitude()) + ",\n";
		s += "\t\"lng\": " + EnergyPanel.FIVE_DECIMALS.format(geo.getLongitude()) + ",\n";
		s += "\t\"address\": \"" + geo.getAddress() + "\",\n";
		switch (Scene.getInstance().getProjectType()) {
		case Foundation.TYPE_PV_PROJECT:
			s += "\t\"type\": \"PV\",\n";
			break;
		case Foundation.TYPE_CSP_PROJECT:
			s += "\t\"type\": \"CSP\",\n";
			break;
		}
		if (Scene.getInstance().getDesigner() != null) {
			s += "\t\"author\": \"" + Scene.getInstance().getDesigner().getName() + "\",\n";
		}
		if (Scene.getInstance().getProjectName() != null) {
			s += "\t\"label\": \"" + Scene.getInstance().getProjectName() + "\",\n";
		}
		final double[][] solarResults = Scene.getInstance().getSolarResults();
		if (solarResults != null) {
			for (int i = 0; i < solarResults.length; i++) {
				s += "\t\"" + AnnualGraph.THREE_LETTER_MONTH[i] + "\": \"";
				for (int j = 0; j < solarResults[i].length; j++) {
					s += EnergyPanel.FIVE_DECIMALS.format(solarResults[i][j]).replaceAll(",", "") + " ";
				}
				s = s.trim() + "\",\n";
			}
		}
		s += "\t\"url\": \"tbd\"\n";
		s += "}";

		File file;
		try {
			file = SnapshotLogger.getInstance().saveSnapshot("vsg_model");
		} catch (final Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Failed in saving a snapshot of your current design.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		final File currentFile = file;
		final String info = s;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final JTextArea textArea = new JTextArea(info);
				textArea.setEditable(false);
				final JPanel panel = new JPanel(new BorderLayout(10, 10));
				final JScrollPane scrollPane = new JScrollPane(textArea);
				scrollPane.setPreferredSize(new Dimension(400, 300));
				panel.add(scrollPane, BorderLayout.CENTER);
				String s = "<html><font size=2>";
				s += "By pressing the Yes button below, you will contribute your model to the Virtual Solar Grid, a publicly<br>";
				s += "accessible site that houses many virtual solar power systems. Your model will be reviewed by experts<br>";
				s += "before it can be published. However, there is no guarantee that it will be accepted. If you agree on<br>";
				s += "this term, please continue. Otherwise, please click the No button.</font><br><br>";
				s += "<b>Do you want to submit your model to the Virtual Solar Grid now?";
				s += "</b></html>";
				panel.add(new JLabel(s), BorderLayout.SOUTH);
				if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), panel, "Virtual Solar Grid", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.YES_OPTION) {
					new Uploader(info, currentFile).execute();
				}
			}
		});

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
			} catch (final Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Failed in uploading the file...<hr>" + e.getMessage() + "</html>", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private static String upload(final String info, final File currentFile) throws Exception {
		final MultipartUtility multipart = new MultipartUtility("http://energy3d.concord.org/vsg/data.php", "UTF-8");
		multipart.addFormField("ip_address", InetAddress.getLocalHost().getHostAddress());
		multipart.addFormField("os_name", System.getProperty("os.name"));
		multipart.addFormField("user_name", System.getProperty("user.name"));
		multipart.addFormField("os_version", System.getProperty("os.version"));
		multipart.addFormField("energy3d_version", MainApplication.VERSION);
		multipart.addFormField("data", info);
		if (!Scene.isInternalFile() && currentFile != null) {
			multipart.addFilePart("model", currentFile);
		}
		return multipart.finish();
	}

}
