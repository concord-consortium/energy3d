package org.concord.energy3d.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.JApplet;
import javax.swing.UIManager;

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

	public void init() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception err) {
			err.printStackTrace();
		}
		
		Component parent = this;
        while (parent.getParent() != null)
            parent = parent.getParent();
        if (parent instanceof Frame) {
            final Frame frame = (Frame) parent;
			if (!frame.isResizable()) {
                frame.setResizable(true);
                frame.setLayout(new GridLayout());
        		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        		frame.setLocation((int) (screenSize.getWidth() - frame.getSize().getWidth()) / 2, (int) (screenSize.getHeight() - frame.getSize().getHeight()) / 2);                
            }
        }		
		this.setContentPane(getMainPanel());
		SceneManager.getInstance();
		
		try {
			HousePart.setDefaultColor(ColorRGBA.parseColor(this.getParameter("color"), null));
		} catch (Exception e) {
			e.printStackTrace();
		}		
		Scene.getInstance().setTextureEnabled(!"false".equalsIgnoreCase(this.getParameter("texture")));
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
