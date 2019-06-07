package org.concord.energy3d.gui;

import com.ardor3d.math.MathUtils;
import org.concord.energy3d.model.GeoLocation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.LocationData;
import org.concord.energy3d.util.ClipImage;
import org.concord.energy3d.util.Util;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLKeyException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CancellationException;

class MapDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final int zoomMin = 0;
    private static final int zoomMax = 21;
    private final JTextField addressField;
    private final JSpinner latitudeSpinner;
    private final JSpinner longitudeSpinner;
    private final JSpinner zoomSpinner;
    private final JComboBox<String> resolutionOptionComboBox;
    private final MapImageView mapImageView;
    private volatile boolean lock;
    private MapLoader mapLoader;
    private int extent;
    private BufferedImage mapImage;
    private JButton okButton;

    class MapLoader extends SwingWorker<BufferedImage, Integer> {

        private final double lat, lng, latWindow, lngWindow;
        private final int zoom, w, h;
        private int extent;

        MapLoader(final int extent) {
            okButton.setEnabled(false);
            if (mapLoader != null) {
                mapLoader.cancel(true);
            }
            this.extent = extent;
            lng = (Double) longitudeSpinner.getValue();
            lat = (Double) latitudeSpinner.getValue();
            zoom = (Integer) zoomSpinner.getValue();
            w = mapImageView.getPreferredSize().width;
            h = mapImageView.getPreferredSize().height;
            lngWindow = 360.0 / Math.pow(2, zoom + 8) * h;
            latWindow = lngWindow * Math.cos(Math.toRadians(lat));
            mapImageView.setText("Loading...");
            mapImageView.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

        @Override
        protected void process(final List<Integer> chunks) {
            final int i = chunks.get(chunks.size() - 1);
            mapImageView.setText("Loading " + i + "%..."); // The last value in this array is all we care about.
            mapImageView.repaint();
            if (okButton.isEnabled()) { // sometimes the OK button can be enabled when a new download process terminates the current one
                okButton.setEnabled(false);
            }
        }

        @Override
        protected BufferedImage doInBackground() throws Exception {
            if (extent <= 0) {
                final String url = MapImageView.getGoogleMapUrl("satellite", lat, lng, zoom, w, h);
                return ImageIO.read(new URL(url));
            }
            final int size = 2 * extent + 1;
            final BufferedImage[] images = new BufferedImage[size * size];
            int index = 0;
            for (int i = -extent; i <= extent; i++) {
                for (int j = -extent; j <= extent; j++) {
                    if (isCancelled()) {
                        return null;
                    }
                    final String url = MapImageView.getGoogleMapUrl("satellite", lat + i * latWindow, lng + j * lngWindow, zoom, w, h);
                    images[index++] = ImageIO.read(new URL(url));
                    publish((int) (100f * index / (size * size)));
                }
            }
            final int patchWidth = images[0].getWidth();
            final int patchHeight = images[0].getHeight();
            final BufferedImage fullImage = new BufferedImage(patchWidth * size, patchHeight * size, images[0].getType());
            final Graphics2D g2 = fullImage.createGraphics();
            index = 0;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (isCancelled()) {
                        return null;
                    }
                    g2.drawImage(images[index++], null, j * patchWidth, (size - i - 1) * patchHeight);
                }
            }
            g2.dispose();
            return fullImage;
        }

        @Override
        protected void done() {
            try {
                mapImage = get();
                final int w = mapImageView.getPreferredSize().width;
                mapImageView.setImage(mapImage.getScaledInstance(w, w, Image.SCALE_DEFAULT));
                mapImageView.repaint();
            } catch (final Exception e) {
                displayError(e);
            } finally {
                mapImageView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                mapImageView.setText(null);
                mapLoader = null;
            }
            okButton.setEnabled(true);
        }

        void displayError(final Exception e) {
            if (e instanceof CancellationException) {
                return;
            }
            e.printStackTrace();
            if (e.getCause() instanceof SSLKeyException) {
                JOptionPane.showMessageDialog(MapDialog.this, "Missing feature! To use this feature you need to download and install the latest version of Energy3D.", getTitle(), JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(MapDialog.this, "Couldn't download map from Google!\nPlease check your internet connection and try again.", getTitle(), JOptionPane.WARNING_MESSAGE);
            }
        }

    }

    MapDialog(final JFrame owner) {

        super(owner);
        setTitle("Earth View");
        setResizable(false);
        extent = Scene.getInstance().getGroundImageExtent();

        mapImageView = new MapImageView();
        mapImageView.setAlignmentX(0.5f);
        int size = Toolkit.getDefaultToolkit().getScreenSize().height > 1000 ? 640 : 480;
        mapImageView.setPreferredSize(new Dimension(size, size));
        mapImageView.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                final Double lat = (Double) latitudeSpinner.getValue();
                final Double lng = (Double) longitudeSpinner.getValue();
                final Integer zoom = (Integer) zoomSpinner.getValue();
                // Google Maps API returns 512 pixels that represent exactly the length of the equator at zoom level 1
                // https://maps.googleapis.com/maps/api/staticmap?center=0,0&zoom=1&size=512x512&maptype=satellite
                // See: https://stackoverflow.com/questions/47106276/converting-pixels-to-latlng-coordinates-from-google-static-image
                // However, we send a 640x640 request. At 640x640, zoom level 1 returns a map image wider than the full equator length
                final double lngWindow = 360.0 / Math.pow(2, zoom + 8) * 640;
                final double latWindow = lngWindow * Math.cos(Math.toRadians(lat));
                final double delta = getScale() / 10000.0;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        lock = true;
                        latitudeSpinner.setValue(lat + (e.isShiftDown() ? latWindow : delta));
                        lock = false;
                        updateMap();
                        break;
                    case KeyEvent.VK_DOWN:
                        lock = true;
                        latitudeSpinner.setValue(lat - (e.isShiftDown() ? latWindow : delta));
                        lock = false;
                        updateMap();
                        break;
                    case KeyEvent.VK_LEFT:
                        lock = true;
                        longitudeSpinner.setValue(lng - (e.isShiftDown() ? lngWindow : delta));
                        lock = false;
                        updateMap();
                        break;
                    case KeyEvent.VK_RIGHT:
                        lock = true;
                        longitudeSpinner.setValue(lng + (e.isShiftDown() ? lngWindow : delta));
                        lock = false;
                        updateMap();
                        break;
                    case KeyEvent.VK_PAGE_UP:
                        lock = true;
                        latitudeSpinner.setValue(lat + latWindow);
                        lock = false;
                        updateMap();
                        break;
                    case KeyEvent.VK_PAGE_DOWN:
                        lock = true;
                        latitudeSpinner.setValue(lat - latWindow);
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

        latitudeSpinner = new JSpinner(new SpinnerNumberModel(42.45661, -90, 90, 0.000001)); // spinner tick must go down to 10^-6 in order to avoid seam lines at zoom level 20
        longitudeSpinner = new JSpinner(new SpinnerNumberModel(-71.35823, -90, 90, 0.000001));
        final JSpinner.NumberEditor latEditor = new JSpinner.NumberEditor(latitudeSpinner, "0.00000");
        final JSpinner.NumberEditor lngEditor = new JSpinner.NumberEditor(longitudeSpinner, "0.00000");
        latEditor.getTextField().setColumns(6);
        lngEditor.getTextField().setColumns(6);
        latitudeSpinner.setEditor(latEditor);
        longitudeSpinner.setEditor(lngEditor);
        zoomSpinner = new JSpinner(new SpinnerNumberModel(20, zoomMin, zoomMax, 1));
        addressField = new JTextField("25 Love lane, Concord, MA, USA");
        addressField.addActionListener(e -> {
            final double[] coordinates = getGoogleMapAddressCoordinates();
            if (coordinates != null) {
                lock = true;
                latitudeSpinner.setValue(coordinates[0]);
                longitudeSpinner.setValue(coordinates[1]);
                zoomSpinner.setValue(20);
                lock = false;
                updateMap();
            }
        });
        setGeoLocation(); // set before hooking up with listener to avoid firing an event
        final ChangeListener changeListener = event -> {
            if (!lock) {
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
        okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            if ((Integer) zoomSpinner.getValue() < 14) {
                JOptionPane.showMessageDialog(MapDialog.this, "The selected region is too large. Please zoom in and try again.", MapDialog.this.getTitle(), JOptionPane.WARNING_MESSAGE);
                return;
            }
            final double lng = (Double) longitudeSpinner.getValue();
            final double lat = (Double) latitudeSpinner.getValue();
            final int zoom = (Integer) zoomSpinner.getValue();
            Scene.getInstance().setGeoLocation(lat, lng, zoom, addressField.getText());
            Scene.getInstance().setGroundImageExtent(extent);
            Scene.getInstance().setSnapToGrids(false);
            SceneManager.getTaskManager().update(() -> {
                Scene.getInstance().setGroundImage(mapImage, getScale() * (2 * extent + 1));
                Scene.getInstance().setGroundImageEarthView(true);
                Scene.getInstance().setEdited(true);
                return null;
            });
            final String closestCity = LocationData.getInstance().getClosestCity(lng, lat);
            if (closestCity != null) {
                EnergyPanel.getInstance().getCityComboBox().setSelectedItem(closestCity);
            }
            dispose();
        });
        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            if (mapLoader != null) {
                mapLoader.cancel(true);
            }
            dispose();
        });
        resolutionOptionComboBox = new JComboBox<>(new String[]{"1\u00D71", "3\u00D73", "5\u00D75", "7\u00D77", "9\u00D79"});
        resolutionOptionComboBox.setSelectedIndex(extent);
        resolutionOptionComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if ((Integer) zoomSpinner.getValue() < 14) {
                    JOptionPane.showMessageDialog(MapDialog.this, "The selected region is too large to apply this option.", MapDialog.this.getTitle(), JOptionPane.WARNING_MESSAGE);
                    Util.selectSilently(resolutionOptionComboBox, 0);
                    return;
                }
                extent = resolutionOptionComboBox.getSelectedIndex();
                updateMap();
            }
        });
        bottomPanel.add(new JLabel("Resolution:"));
        bottomPanel.add(resolutionOptionComboBox);
        final JButton imageButton = new JButton("Copy Image");
        imageButton.addActionListener(e -> new ClipImage().copyImageToClipboard(mapImageView));
        bottomPanel.add(imageButton);
        bottomPanel.add(okButton);
        bottomPanel.add(cancelButton);
        getContentPane().add(bottomPanel);

        mapImage = Scene.getInstance().getGroundImage();
        if (mapImage == null) {
            updateMap();
        } else {
            final int w = mapImageView.getPreferredSize().width;
            mapImageView.setImage(mapImage.getScaledInstance(w, w, Image.SCALE_DEFAULT));
        }

        pack();
        setLocationRelativeTo(owner);
        addressField.requestFocusInWindow();
        addressField.selectAll();

    }

    public void setExtent(final int extent) {
        this.extent = extent;
    }

    public int getExtent() {
        return extent;
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
        mapLoader = new MapLoader(extent);
        mapLoader.execute();
    }

    private double[] getGoogleMapAddressCoordinates() {
        final String address = addressField.getText().replace(' ', '+');
        try {
            final URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=AIzaSyD7MfCQjMAlsdFA3OmfGZ_rzC8ldJPnoHc");
            final Scanner scanner = new Scanner(url.openStream());
            try {
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if (line.contains("formatted_address")) {
                        addressField.setText(line.substring(line.indexOf(':') + 3, line.length() - 2));
                    } else if (line.contains("\"lat\" :")) {
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