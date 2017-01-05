package org.concord.energy3d.gui;

import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLKeyException;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.concord.energy3d.scene.Scene;

import com.ardor3d.math.MathUtils;

public class MapDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final int zoomMin = 0;
	private static final int zoomMax = 21;
	private static final RuntimeException missingExtensionPathException = new RuntimeException();
	private final JTextField addressField = new JTextField("25 Love lane, Concord, MA, USA");
	private final JSpinner latitudeSpinner = new JSpinner(new SpinnerNumberModel(42.45661, -90, 90, 0.00001));
	private final JSpinner longitudeSpinner = new JSpinner(new SpinnerNumberModel(-71.35823, -90, 90, 0.00001));
	private final JSpinner zoomSpinner = new JSpinner(new SpinnerNumberModel(20, zoomMin, zoomMax, 1));
	private final JLabel mapLabel = new JLabel();
	private static MapDialog instance;
	private boolean lock = false;

	public static void showDialog() {
		if (instance == null) {
			try {
				instance = new MapDialog(MainFrame.getInstance());
			} catch (final RuntimeException e) {
				if (e == missingExtensionPathException) {
					return;
				} else {
					e.printStackTrace();
				}
			}
		}
		instance.setVisible(true);
	}

	private MapDialog(final JFrame owner) {
		super(owner);
		setTitle("Map");
		final JSpinner.NumberEditor latEditor = new JSpinner.NumberEditor(latitudeSpinner, "0.00000");
		final JSpinner.NumberEditor lngEditor = new JSpinner.NumberEditor(longitudeSpinner, "0.00000");
		latEditor.getTextField().setColumns(6);
		lngEditor.getTextField().setColumns(6);
		latitudeSpinner.setEditor(latEditor);
		longitudeSpinner.setEditor(lngEditor);
		mapLabel.setAlignmentX(0.5f);
		final Point point = new Point();
		mapLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				point.setLocation(e.getPoint());
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				point.setLocation(0, 0);
			}
		});
		mapLabel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(final MouseEvent e) {
				if (point.getX() != 0 && point.getY() != 0) {
					final double dx = e.getX() - point.getX();
					final double dy = e.getY() - point.getY();
					final Double lat = (Double) latitudeSpinner.getValue();
					final Double lng = (Double) longitudeSpinner.getValue();
					lock = true;
					final double scale = getScale();
					latitudeSpinner.setValue(lat + dy / 1000000.0 * scale);
					longitudeSpinner.setValue(lng - dx / 750000.0 * scale);
					lock = false;
					updateMap();
				}
				point.setLocation(e.getPoint());
			}
		});
		mapLabel.addMouseWheelListener(new MouseWheelListener() {
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
		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
		panel1.setBorder(new EmptyBorder(5, 5, 0, 5));
		panel1.add(new JLabel("Address:"));
		panel1.add(addressField);
		this.getContentPane().add(panel1);
		final JPanel panel2 = new JPanel();
		panel2.add(new JLabel("Latitude:"));
		panel2.add(latitudeSpinner);
		panel2.add(new JLabel("Longitude:"));
		panel2.add(longitudeSpinner);
		panel2.add(new JLabel("Zoom:"));
		panel2.add(zoomSpinner);
		this.getContentPane().add(panel2);
		this.getContentPane().add(mapLabel);
		final JPanel bottomPanel = new JPanel();
		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if ((Integer) zoomSpinner.getValue() < 16) {
					JOptionPane.showMessageDialog(MapDialog.this, "The selected region is too large. Please zoom in and try again.", MapDialog.this.getTitle(), JOptionPane.WARNING_MESSAGE);
					return;
				}
				final BufferedImage mapImage = getGoogleMapImage(true);
				if (mapImage != null) {
					Scene.getInstance().setGroundImage(mapImage, getScale());
					Scene.getInstance().setGroundImageEarthView(true);
					Scene.getInstance().setEdited(true);
					setVisible(false);
				}
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
		this.getContentPane().add(bottomPanel);
		updateMap();
		this.pack();
		this.setLocationRelativeTo(owner);
	}

	private void updateMap() {
		final BufferedImage mapImage = getGoogleMapImage(false);
		if (mapImage != null) {
			final int w = this.getContentPane().getPreferredSize().width;
			mapLabel.setIcon(new ImageIcon(mapImage.getScaledInstance(w, w, Image.SCALE_DEFAULT)));
		}
	}

	private BufferedImage getGoogleMapImage(final boolean highResolution) {
		final double x = (Double) latitudeSpinner.getValue();
		final double y = (Double) longitudeSpinner.getValue();
		final int zoom = (Integer) zoomSpinner.getValue();
		final int scale = highResolution & zoom <= 20 ? 2 : 1;
		BufferedImage mapImage = null;
		try {
			final URL url = new URL("https://maps.googleapis.com/maps/api/staticmap?maptype=satellite&center=" + x + "," + y + "&zoom=" + zoom + "&size=640x640&scale=" + scale + "&key=AIzaSyBEGiCg33CccHloDdPENWk1JDhwTEQaZQ0");
			mapImage = ImageIO.read(url);
		} catch (final IOException e) {
			e.printStackTrace();
			if (e.getCause() instanceof SSLKeyException) {
				JOptionPane.showMessageDialog(this, "Missing feature! To use this feature you need to download and install the latest version of Energy3D.", this.getTitle(), JOptionPane.ERROR_MESSAGE);
				throw missingExtensionPathException;
			} else {
				JOptionPane.showMessageDialog(this, "Could not retrieve map from google!\nPlease check your internet connection and try again.", this.getTitle(), JOptionPane.WARNING_MESSAGE);
			}
		}
		return mapImage;
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
