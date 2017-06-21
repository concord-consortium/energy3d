package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.concurrent.CancellationException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.concord.energy3d.simulation.LocationData;

class GlobalMap extends JDialog {

	private static final long serialVersionUID = 1L;

	private final MapImageView mapImageView;

	public GlobalMap(final JFrame owner) {

		super(owner);
		setTitle("Global Map of Supported Locations (" + LocationData.getInstance().getCities().length + ")");
		setResizable(false);
		mapImageView = new MapImageViewWithLocations();
		mapImageView.setAlignmentX(0.5f);
		mapImageView.setText("Loading...");
		mapImageView.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mapImageView, BorderLayout.CENTER);
		final JPanel bottomPanel = new JPanel();
		final JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				GlobalMap.this.dispose();
			}
		});
		bottomPanel.add(closeButton);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(owner);

		new SwingWorker<BufferedImage, Void>() {

			@Override
			protected BufferedImage doInBackground() throws Exception {
				return ImageIO.read(new URL(MapImageView.getGoogleMapUrl("roadmap", false, 0, 0, 1)));
			}

			@Override
			protected void done() {
				try {
					final BufferedImage mapImage = get();
					final int w = mapImageView.getPreferredSize().width;
					mapImageView.setImage(mapImage.getScaledInstance(w, w, Image.SCALE_DEFAULT));
					mapImageView.repaint();
				} catch (final Exception e) {
					displayError(e);
				} finally {
					mapImageView.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					mapImageView.setText(null);
				}
			}

			protected void displayError(final Exception e) {
				if (e instanceof CancellationException) {
					return;
				}
				e.printStackTrace();
				JOptionPane.showMessageDialog(GlobalMap.this, "Couldn't download map from Google!\nPlease check your internet connection and try again.", getTitle(), JOptionPane.WARNING_MESSAGE);
			}

		}.execute();

	}

}
