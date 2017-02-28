package org.concord.energy3d.gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLKeyException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.concord.energy3d.model.GeoLocation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.LocationData;

import com.ardor3d.math.MathUtils;

public class MapDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final int zoomMin = 0;
	private static final int zoomMax = 21;
	private final JTextField addressField = new JTextField("25 Love lane, Concord, MA, USA");
	private final JSpinner latitudeSpinner = new JSpinner(new SpinnerNumberModel(42.45661, -90, 90, 0.00001));
	private final JSpinner longitudeSpinner = new JSpinner(new SpinnerNumberModel(-71.35823, -90, 90, 0.00001));
	private final JSpinner zoomSpinner = new JSpinner(new SpinnerNumberModel(20, zoomMin, zoomMax, 1));
	private final MapImageView mapImageView = new MapImageView();
	private static MapDialog instance;
	private volatile boolean lock;
	private GoogleMapImageLoader mapImageLoader;

	class GoogleMapImageLoader extends SwingWorker<BufferedImage, Void> {
		private final boolean highResolution;
		private final String googleMapUrl;

		public GoogleMapImageLoader(final boolean highResolution) {
			if (mapImageLoader != null) {
				mapImageLoader.cancel(true);
			}
			this.highResolution = highResolution;
			googleMapUrl = getGoogleMapUrl(highResolution);
			mapImageView.setText("Loading...");
			mapImageView.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}

		@Override
		protected BufferedImage doInBackground() throws Exception {
			return ImageIO.read(new URL(googleMapUrl));

		}

		@Override
		protected void done() {
			try {
				final BufferedImage mapImage = get();
				if (highResolution) {
					final double lat = (Double) latitudeSpinner.getValue();
					final double lon = (Double) longitudeSpinner.getValue();
					Scene.getInstance().setGeoLocation(lat, lon, (Integer) zoomSpinner.getValue(), addressField.getText());
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().setGroundImage(mapImage, getScale());
							Scene.getInstance().setGroundImageEarthView(true);
							Scene.getInstance().setEdited(true);
							return null;
						}
					});
					setVisible(false);
					final String closestCity = LocationData.getInstance().getClosestCity(lon, lat);
					if (closestCity != null) {
						EnergyPanel.getInstance().getCityComboBox().setSelectedItem(closestCity);
					}
				} else {
					final int w = mapImageView.getPreferredSize().width;
					mapImageView.setImage(mapImage.getScaledInstance(w, w, Image.SCALE_DEFAULT));
					mapImageView.repaint();
				}
			} catch (final Exception e) {
				displayError(e);
			} finally {
				mapImageView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				mapImageView.setText(null);
				mapImageLoader = null;
			}
		}

		protected void displayError(final Exception e) {
			if (e instanceof CancellationException) {
				return;
			}
			e.printStackTrace();
			if (e.getCause() instanceof SSLKeyException) {
				JOptionPane.showMessageDialog(MapDialog.this, "Missing feature! To use this feature you need to download and install the latest version of Energy3D.", getTitle(), JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(MapDialog.this, "Could not retrieve map from google!\nPlease check your internet connection and try again.", getTitle(), JOptionPane.WARNING_MESSAGE);
			}
		}

	}

	public static void showDialog() {
		if (instance == null) {
			instance = new MapDialog(MainFrame.getInstance());
		}
		instance.setGeoLocation();
		instance.updateMap();
		instance.setVisible(true);
	}

	private MapDialog(final JFrame owner) {
		super(owner);
		setTitle("Earth View");
		setResizable(false);
		final JSpinner.NumberEditor latEditor = new JSpinner.NumberEditor(latitudeSpinner, "0.00000");
		final JSpinner.NumberEditor lngEditor = new JSpinner.NumberEditor(longitudeSpinner, "0.00000");
		latEditor.getTextField().setColumns(6);
		lngEditor.getTextField().setColumns(6);
		latitudeSpinner.setEditor(latEditor);
		longitudeSpinner.setEditor(lngEditor);
		setGeoLocation();
		mapImageView.setAlignmentX(0.5f);
		mapImageView.setPreferredSize(new Dimension(500, 500));
		mapImageView.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				final double delta = getScale() / 10000.0;
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
					Double lat = (Double) latitudeSpinner.getValue();
					lock = true;
					latitudeSpinner.setValue(lat + delta);
					lock = false;
					updateMap();
					break;
				case KeyEvent.VK_DOWN:
					lat = (Double) latitudeSpinner.getValue();
					lock = true;
					latitudeSpinner.setValue(lat - delta);
					lock = false;
					updateMap();
					break;
				case KeyEvent.VK_LEFT:
					Double lng = (Double) longitudeSpinner.getValue();
					lock = true;
					longitudeSpinner.setValue(lng - delta);
					lock = false;
					updateMap();
					break;
				case KeyEvent.VK_RIGHT:
					lng = (Double) longitudeSpinner.getValue();
					lock = true;
					longitudeSpinner.setValue(lng + delta);
					lock = false;
					updateMap();
					break;
				}
			}
		});
		mapImageView.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(final MouseEvent e) {
				final Point point = mapImageView.getPoint();
				if (point.getX() != 0 && point.getY() != 0) {
					final double dx = e.getX() - point.getX();
					final double dy = e.getY() - point.getY();
					final Double lat = (Double) latitudeSpinner.getValue();
					final Double lng = (Double) longitudeSpinner.getValue();
					lock = true;
					final double scale = getScale();
					latitudeSpinner.setValue(lat + dy / 1000000.0 * scale);
					longitudeSpinner.setValue(lng - dx / 1000000.0 * scale);
					lock = false;
					updateMap();
				}
				point.setLocation(e.getPoint());
			}
		});
		mapImageView.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				lock = true;
				zoomSpinner.setValue(MathUtils.clamp((Integer) zoomSpinner.getValue() - e.getWheelRotation(), zoomMin, zoomMax));
				lock = false;
				updateMap();
			}
		});
		addressField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final double[] coordinates = getGoogleMapAddressCoordinates();
				if (coordinates != null) {
					lock = true;
					latitudeSpinner.setValue(coordinates[0]);
					longitudeSpinner.setValue(coordinates[1]);
					zoomSpinner.setValue(20);
					lock = false;
					updateMap();
				}
			}
		});
		final ChangeListener changeListener = new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent event) {
				if (lock) {
					return;
				}
				updateMap();
			}
		};
		latitudeSpinner.addChangeListener(changeListener);
		longitudeSpinner.addChangeListener(changeListener);
		zoomSpinner.addChangeListener(changeListener);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
		panel1.setBorder(new EmptyBorder(5, 5, 0, 5));
		panel1.add(new JLabel("Address: "));
		panel1.add(addressField);
		final JButton goButton = new JButton("Go");
		goButton.addActionListener(addressField.getActionListeners()[0]);
		panel1.add(goButton);
		getContentPane().add(panel1);
		final JPanel panel2 = new JPanel();
		panel2.add(new JLabel("Latitude:"));
		panel2.add(latitudeSpinner);
		panel2.add(new JLabel("Longitude:"));
		panel2.add(longitudeSpinner);
		panel2.add(new JLabel("Zoom:"));
		panel2.add(zoomSpinner);
		getContentPane().add(panel2);
		getContentPane().add(mapImageView);
		final JPanel bottomPanel = new JPanel();
		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if ((Integer) zoomSpinner.getValue() < 16) {
					JOptionPane.showMessageDialog(MapDialog.this, "The selected region is too large. Please zoom in and try again.", MapDialog.this.getTitle(), JOptionPane.WARNING_MESSAGE);
					return;
				}
				mapImageLoader = new GoogleMapImageLoader(true);
				mapImageLoader.execute();
			}
		});
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				setVisible(false);
			}
		});
		bottomPanel.add(okButton);
		bottomPanel.add(cancelButton);
		getContentPane().add(bottomPanel);
		updateMap();
		pack();
		setLocationRelativeTo(owner);
	}

	private void setGeoLocation() {
		final GeoLocation geoLocation = Scene.getInstance().getGeoLocation();
		if (geoLocation != null) {
			latitudeSpinner.setValue(geoLocation.getLatitude());
			longitudeSpinner.setValue(geoLocation.getLongitude());
			zoomSpinner.setValue(geoLocation.getZoom());
			addressField.setText(geoLocation.getAddress());
		}
	}

	private void updateMap() {
		mapImageLoader = new GoogleMapImageLoader(false);
		mapImageLoader.execute();
	}

	private String getGoogleMapUrl(final boolean highResolution) {
		final double x = (Double) latitudeSpinner.getValue();
		final double y = (Double) longitudeSpinner.getValue();
		final int zoom = (Integer) zoomSpinner.getValue();
		final int scale = highResolution & zoom <= 20 ? 2 : 1;
		return "https://maps.googleapis.com/maps/api/staticmap?maptype=satellite&center=" + x + "," + y + "&zoom=" + zoom + "&size=640x640&scale=" + scale + "&key=AIzaSyBEGiCg33CccHloDdPENWk1JDhwTEQaZQ0";
	}

	private double[] getGoogleMapAddressCoordinates() {
		final String address = addressField.getText().replace(' ', '+');
		try {
			final URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=AIzaSyD7MfCQjMAlsdFA3OmfGZ_rzC8ldJPnoHc");
			final Scanner scanner = new Scanner(url.openStream());
			try {
				while (scanner.hasNext()) {
					String line = scanner.nextLine();
					if (line.indexOf("formatted_address") != -1) {
						addressField.setText(line.substring(line.indexOf(':') + 3, line.length() - 2));
					} else if (line.indexOf("\"lat\" :") != -1) {
						final double[] result = new double[2];
						result[0] = Double.valueOf(line.substring(line.indexOf(':') + 1, line.length() - 1));
						line = scanner.nextLine();
						result[1] = Double.valueOf(line.substring(line.indexOf(':') + 1));
						return result;
					}
				}
			} finally {
				scanner.close();
			}
		} catch (final IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Could not retrieve map from google!\nPlease check your internet connection and try again.", "Error", JOptionPane.WARNING_MESSAGE);
		}
		JOptionPane.showMessageDialog(this, "Could not find the address!", "Error", JOptionPane.WARNING_MESSAGE);
		return null;
	}

	private double getScale() {
		final int zoom = (Integer) zoomSpinner.getValue();
		final double scale;
		if (zoom == 21) {
			scale = 0.5;
		} else if (zoom == 20) {
			scale = 1;
		} else {
			scale = Math.pow(2, 20 - zoom);
		}
		return scale;
	}
}
