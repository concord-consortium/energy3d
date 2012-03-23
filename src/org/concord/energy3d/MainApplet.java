package org.concord.energy3d;

import javax.swing.JApplet;
import javax.swing.UIManager;

import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
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
			final String color = getParameter("color");
			if (color != null)
				HousePart.setDefaultColor(ColorRGBA.parseColor(color, null));
		} catch (final Exception e) {
			e.printStackTrace();
		}
		Scene.getInstance().setTextureEnabled(!"false".equalsIgnoreCase(getParameter("texture")));
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
