package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.concord.energy3d.logger.SnapshotLogger;

/**
 * @author Charles Xie
 * 
 */
class LogZipper implements PropertyChangeListener {

	private File zipFile;
	private JProgressBar progressBar;

	LogZipper(File zipFile) {
		this.zipFile = zipFile;
	}

	void createDialog() {

		final JDialog dialog = new JDialog(MainFrame.getInstance(), true);
		dialog.setTitle("Export log as a zip file");
		dialog.setUndecorated(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JPanel panel = new JPanel(new BorderLayout(20, 20));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setPreferredSize(new Dimension(400, 100));
		dialog.setContentPane(panel);

		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setBorderPainted(true);
		panel.add(progressBar, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		panel.add(buttonPanel, BorderLayout.SOUTH);
		final JButton closeButton = new JButton("Close");
		closeButton.setEnabled(false);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		buttonPanel.add(closeButton);
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {

				ZipOutputStream zos = null;
				try {
					zos = new ZipOutputStream(new FileOutputStream(zipFile, false));
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}

				FileInputStream in = null;
				int c;
				int count = 0;
				try {
					File[] files = SnapshotLogger.getLogFolder().listFiles();
					for (File f : files) {
						zos.putNextEntry(new ZipEntry(f.getName()));
						in = new FileInputStream(f);
						while ((c = in.read()) != -1)
							zos.write(c);
						in.close();
						zos.flush();
						zos.closeEntry();
						count++;
						setProgress(Math.round((float) count / (float) files.length * 100f));
					}
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				} finally {
					try {
						zos.close();
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
							return null;
						}
					}
				}

				return null;

			}

			@Override
			protected void done() {
				Toolkit.getDefaultToolkit().beep();
				closeButton.setEnabled(true);
			}
		};
		worker.addPropertyChangeListener(this);
		worker.execute();
		dialog.pack();
		dialog.setLocationRelativeTo(MainFrame.getInstance());
		dialog.setVisible(true);

	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if ("progress" == e.getPropertyName()) {
			int progress = (Integer) e.getNewValue();
			progressBar.setValue(progress);
			System.out.println(progress);
		}
	}

}
