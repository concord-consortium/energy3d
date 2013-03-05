package org.concord.energy3d;

import javax.swing.JApplet;
import javax.swing.UIManager;

import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Config;

import com.ardor3d.math.ColorRGBA;

public class MainApplet extends JApplet {
	private static final long serialVersionUID = 1L;
	private MainPanel mainPanel = null;

	public MainApplet() {
		super();
		Config.setApplet(this);
	}

	@Override
	public void init() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception err) {
			err.printStackTrace();
		}

		setContentPane(getMainPanel());
		SceneManager.getInstance();

		try {
			final String color = getParameter("foundation-color");
			if (color != null)
				Scene.getInstance().setFoundationColor(ColorRGBA.parseColor(color, null));
		} catch (final Exception e) {
			e.printStackTrace();
		}
		try {
			final String color = getParameter("wall-color");
			if (color != null)
				Scene.getInstance().setWallColor(ColorRGBA.parseColor(color, null));
		} catch (final Exception e) {
			e.printStackTrace();
		}
		try {
			final String color = getParameter("door-color");
			if (color != null)
				Scene.getInstance().setDoorColor(ColorRGBA.parseColor(color, null));
		} catch (final Exception e) {
			e.printStackTrace();
		}
		try {
			final String color = getParameter("floor-color");
			if (color != null)
				Scene.getInstance().setFloorColor(ColorRGBA.parseColor(color, null));
		} catch (final Exception e) {
			e.printStackTrace();
		}
		try {
			final String color = getParameter("roof-color");
			if (color != null)
				Scene.getInstance().setRoofColor(ColorRGBA.parseColor(color, null));
		} catch (final Exception e) {
			e.printStackTrace();
		}

		if ("none".equalsIgnoreCase(getParameter("texture")))
			Scene.getInstance().setTextureMode(TextureMode.None);
		else if ("simple".equalsIgnoreCase(getParameter("texture")))
			Scene.getInstance().setTextureMode(TextureMode.Simple);
		else
			Scene.getInstance().setTextureMode(TextureMode.Full);
	}

	private MainPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = MainPanel.getInstance();
		}
		return mainPanel;
	}

	@Override
	public void start() {
		new Thread(SceneManager.getInstance(), "Energy 3D Application").start();
	}

	@Override
	public void stop() {
		SceneManager.getInstance().exit();
	}
}
