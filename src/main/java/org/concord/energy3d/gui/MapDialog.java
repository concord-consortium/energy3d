package org.concord.energy3d.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.image.Image;
import com.ardor3d.image.Texture.MinificationFilter;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.util.awt.AWTImageLoader;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.TextureKey;

public class MapDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static MapDialog instance;
	final private JSpinner latitudeSpinner = new JSpinner(new SpinnerNumberModel(42.45661, -90, 90, 0.00001));
	final private JSpinner longitudeSpinner = new JSpinner(new SpinnerNumberModel(-71.35823, -90, 90, 0.00001));
	final private JSpinner zoomSpinner = new JSpinner(new SpinnerNumberModel(20, 17, 21, 1));
	final private JLabel mapLabel = new JLabel();

	public static MapDialog getInstance() {
		if (instance == null)
			instance = new MapDialog(MainFrame.getInstance());
		return instance;
	}

	private MapDialog(final JFrame owner) {
		super(owner);
		setSize(800, 400);
		mapLabel.setAlignmentX(0.5f);
		latitudeSpinner.setEditor(new JSpinner.NumberEditor(latitudeSpinner, "0.00000"));
		longitudeSpinner.setEditor(new JSpinner.NumberEditor(longitudeSpinner, "0.00000"));
		final ChangeListener changeListener = new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent event) {
				updateMap();
				pack();
			}
		};
		latitudeSpinner.addChangeListener(changeListener);
		longitudeSpinner.addChangeListener(changeListener);
		zoomSpinner.addChangeListener(changeListener);
		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		final JPanel panel = new JPanel();
		panel.add(new JLabel("Latitude:"));
		panel.add(latitudeSpinner);
		panel.add(new JLabel("Longitude:"));
		panel.add(longitudeSpinner);
		panel.add(new JLabel("Zoom:"));
		panel.add(zoomSpinner);
		this.getContentPane().add(panel);
		this.getContentPane().add(mapLabel);
		final JPanel bottomPanel = new JPanel();
		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final int zoom = (Integer) zoomSpinner.getValue();
				final double scale;
				if (zoom == 21)
					scale = 0.5;
				else if (zoom == 20)
					scale = 1;
				else
					scale = (Math.pow(2, 20 - zoom));

				final BufferedImage googleMapImage = getGoogleMapImage(true);
				if (googleMapImage != null) {
					SceneManager.getInstance().resizeMapLand(scale);
					final Image image = AWTImageLoader.makeArdor3dImage(googleMapImage, true);
					final Texture2D texture = new Texture2D();
					texture.setTextureKey(TextureKey.getRTTKey(MinificationFilter.NearestNeighborNoMipMaps));
					texture.setImage(image);
					final TextureState textureState = new TextureState();
					textureState.setTexture(texture);
					final Mesh mesh = SceneManager.getInstance().getMapLand();
					mesh.setRenderState(textureState);
					mesh.setVisible(true);
					setFoundationsVisible(false);
					setVisible(false);
				}
			}
		});
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				SceneManager.getInstance().getMapLand().setVisible(false);
				setFoundationsVisible(true);
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
		if (mapImage != null)
			mapLabel.setIcon(new ImageIcon(mapImage));
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
			JOptionPane.showMessageDialog(this, "Could not retrieve map from google!\nPlease check your internet connection and try again.", "Error", JOptionPane.WARNING_MESSAGE);
		}
		return mapImage;
	}

	private void setFoundationsVisible(final boolean visible) {
		for (final HousePart part : Scene.getInstance().getParts())
			if (part instanceof Foundation)
				part.getMesh().setVisible(visible);
		SceneManager.getInstance().refresh();
	}

	// public boolean isMapMode() {
	// return isMapMode;
	// }

	// public com.ardor3d.image.Image getMapImage() {
	// return image;
	// }

}
