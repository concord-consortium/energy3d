package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
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
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

import org.concord.energy3d.simulation.LocationData;

class GlobalMap extends JDialog {

	private static final long serialVersionUID = 1L;

	private final MapImageView mapImageView;

	public GlobalMap(final JFrame owner) {

		super(owner);
		setTitle("Total Supported Regions: " + (LocationData.getInstance().getCities().length - 1));
		setResizable(false);
		mapImageView = new MapImageViewWithLocations();
		final int m = 1;
		mapImageView.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(m, m, m, m), BorderFactory.createLineBorder(Color.LIGHT_GRAY)));
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

		final JComboBox<String> regionsComboBox = new JComboBox<String>();
		final JComboBox<String> countriesComboBox = new JComboBox<String>();
		final JLabel regionsLabel = new JLabel("Regions:");
		final ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (regionsComboBox.getSelectedItem() != null) {
					if ("United States".equals(countriesComboBox.getSelectedItem())) {
						EnergyPanel.getInstance().getCityComboBox().setSelectedItem(regionsComboBox.getSelectedItem());
					} else {
						if (regionsComboBox.getSelectedItem().equals(countriesComboBox.getSelectedItem())) {
							EnergyPanel.getInstance().getCityComboBox().setSelectedItem(regionsComboBox.getSelectedItem());
						} else {
							EnergyPanel.getInstance().getCityComboBox().setSelectedItem(regionsComboBox.getSelectedItem() + ", " + countriesComboBox.getSelectedItem());
						}
					}
				}
				mapImageView.repaint();
			}
		};

		topPanel.add(new JLabel(countries.size() + " Countries:"));
		for (final String s : countries.keySet()) {
			countriesComboBox.addItem(s);
		}
		countriesComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				regionsComboBox.removeAllItems();
				final ArrayList<String> locationsInCountry = countries.get(countriesComboBox.getSelectedItem());
				for (final String s : locationsInCountry) {
					regionsComboBox.addItem(s);
				}
				regionsLabel.setText("Regions (" + locationsInCountry.size() + "):");
			}
		});
		topPanel.add(countriesComboBox);

		topPanel.add(regionsLabel);
		regionsComboBox.addActionListener(listener);
		topPanel.add(regionsComboBox);

		final String current = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
		if (current.equals("")) {
			final ArrayList<String> regionsInCountries = countries.get(countriesComboBox.getSelectedItem());
			for (final String s : regionsInCountries) {
				regionsComboBox.addItem(s);
			}
		} else {
			final String[] t = current.split(",");
			t[0] = t[0].trim();
			if (t.length == 1) {
				countriesComboBox.setSelectedItem(t[0]);
				regionsComboBox.setSelectedItem(t[0]);
			} else {
				t[1] = t[1].trim();
				if (t[1].length() == 2) {
					countriesComboBox.setSelectedItem("United States");
					regionsComboBox.setSelectedItem(current);
				} else {
					countriesComboBox.setSelectedItem(t[1]);
					regionsComboBox.setSelectedItem(t[0]);
				}
			}
		}

		final JButton listButton = new JButton("List");
		listButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				String s = "<html><body><table>";
				int i = 0;
				for (final String country : countries.keySet()) {
					switch (i % 3) {
					case 0:
						s += "<tr>";
						s += "<td bgcolor=87CEEB><font size=3>" + country + "</td><td width=40 bgcolor=98FB98><font size=3>" + countries.get(country).size() + "</td>";
						break;
					case 1:
						s += "<td bgcolor=87CEEB><font size=3>" + country + "</td><td width=40 bgcolor=98FB98><font size=3>" + countries.get(country).size() + "</td>";
						break;
					case 2:
						s += "<td bgcolor=87CEEB><font size=3>" + country + "</td><td width=40 bgcolor=98FB98><font size=3>" + countries.get(country).size() + "</td>";
						s += "</tr>";
						break;
					}
					i++;
				}
				s += "</table></body></html>";
				final JTextPane textPane = new JTextPane();
				textPane.setContentType("text/html");
				textPane.setText(s);
				final JScrollPane scroller = new JScrollPane(textPane);
				scroller.setPreferredSize(new Dimension(400, 400));
				JOptionPane.showMessageDialog(GlobalMap.this, scroller, "List of Countries and Numbers of Regions", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		topPanel.add(listButton);

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
