package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.concord.energy3d.simulation.LocationData;

class GlobalMap extends JDialog {

	private static final long serialVersionUID = 1L;

	private final MapImageView mapImageView;

	public GlobalMap(final JFrame owner) {

		super(owner);
		setTitle((LocationData.getInstance().getCities().length - 1) + " Supported Locations");
		setResizable(false);
		mapImageView = new MapImageViewWithLocations();
		final int m = 5;
		mapImageView.setMargin(m);
		mapImageView.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(m, m, m, m), BorderFactory.createLineBorder(Color.GRAY)));
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

		final Map<String, ArrayList<String>> countries = new TreeMap<String, ArrayList<String>>();
		for (final String s : LocationData.getInstance().getCities()) {
			if (!s.equals("")) {
				final String[] t = s.split(",");
				t[0] = t[0].trim();
				if (t.length == 1) {
					final ArrayList<String> list = new ArrayList<String>();
					list.add(t[0]);
					countries.put(t[0], list);
				} else {
					t[1] = t[1].trim();
					if (t[1].length() == 2) {
						if (!countries.keySet().contains("United States")) {
							countries.put("United States", new ArrayList<String>());
						}
						final ArrayList<String> list = countries.get("United States");
						list.add(s);
					} else {
						if (!countries.keySet().contains((t[1]))) {
							countries.put(t[1], new ArrayList<String>());
						}
						final ArrayList<String> list = countries.get(t[1]);
						list.add(t[0]);
					}
				}
			}
		}

		final JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		getContentPane().add(topPanel, BorderLayout.NORTH);

		final JComboBox<String> locationsComboBox = new JComboBox<String>();
		final JComboBox<String> countriesComboBox = new JComboBox<String>();
		final JLabel locationsInCountryLabel = new JLabel();

		topPanel.add(new JLabel(countries.size() + " Countries:"));
		for (final String s : countries.keySet()) {
			countriesComboBox.addItem(s);
		}
		countriesComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				locationsComboBox.removeAllItems();
				final ArrayList<String> locations = countries.get(countriesComboBox.getSelectedItem());
				for (final String s : locations) {
					locationsComboBox.addItem(s);
				}
				locationsInCountryLabel.setText(locations.size() + " locations found");
			}
		});
		topPanel.add(countriesComboBox);

		topPanel.add(new JLabel("Areas:"));
		final ArrayList<String> locations = countries.get(countriesComboBox.getSelectedItem());
		for (final String s : locations) {
			locationsComboBox.addItem(s);
		}
		locationsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String s = (String) countriesComboBox.getSelectedItem();
				if (s.equals("United States")) {
					if (locationsComboBox.getSelectedItem() != null) {
						EnergyPanel.getInstance().getCityComboBox().setSelectedItem(locationsComboBox.getSelectedItem());
					}
				}
				mapImageView.repaint();
			}
		});
		topPanel.add(locationsComboBox);
		topPanel.add(locationsInCountryLabel);

		final String current = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
		if (!current.equals("")) {
			final String[] t = current.split(",");
			t[0] = t[0].trim();
			if (t.length == 1) {
				countriesComboBox.setSelectedItem(t[0]);
				locationsComboBox.setSelectedItem(t[0]);
			} else {
				t[1] = t[1].trim();
				if (t[1].length() == 2) {
					countriesComboBox.setSelectedItem("United States");
					locationsComboBox.setSelectedItem(current);
				} else {
					countriesComboBox.setSelectedItem(t[1]);
					locationsComboBox.setSelectedItem(t[0]);
				}
			}
		}

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
					final int w = mapImageView.getPreferredSize().width - mapImageView.getMargin() * 2;
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
