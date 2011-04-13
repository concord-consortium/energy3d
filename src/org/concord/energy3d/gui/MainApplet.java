package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Config;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.FrameHandler;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.JoglAwtCanvas;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.util.Timer;

import javax.swing.JButton;

public class MainApplet extends JApplet {
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private MainPanel mainPanel = null;
	private boolean isStarted = false;
	/**
	 * This is the xxx default constructor
	 */
	public MainApplet() {
		super();
		Config.setApplet(this);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	public void init() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception err) {
			err.printStackTrace();
		}
		this.setSize(300, 200);
//		this.setContentPane(getJContentPane());
		this.setContentPane(getMainPanel());
		
//        final JoglCanvasRenderer canvasRenderer = new JoglCanvasRenderer(SceneManager.getInstance());

//        final DisplaySettings settings = new DisplaySettings(400, 300, 24, 0, 0, 16, 0, 0, false, false);
//        final JoglAwtCanvas theCanvas = new JoglAwtCanvas(settings, canvasRenderer);

//        this.getMainPanel().add((Component)SceneManager.getInstance().getCanvas());

//        final Timer timer = new Timer();
//        final FrameHandler frameWork = new FrameHandler(timer);        
//        frameWork.addCanvas(theCanvas);
        
//        setVisible(true);

//        frameWork.init();
//        while (true) {
//            frameWork.updateFrame();
//            Thread.yield();
//        }        
		
		
//		SceneManager.getInstance();
//		new Thread(SceneManager.getInstance(), "Energy 3D Application").start();
		
//		SceneManager.getInstance().frameHandler.init();
//		while (true) {			
//			SceneManager.getInstance().frameHandler.updateFrame();		
//////			SceneManager.getInstance().renderUnto(SceneManager.getInstance().getCanvas().getCanvasRenderer().getRenderer());
//			Thread.yield();
//		}
		
		
		
//		final SceneManager scene = SceneManager.getInstance();
//		MainFrame.getInstance().setVisible(true);
//		new Thread(SceneManager.getInstance(), "Energy 3D Application").start();		
	}	

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
//	private JPanel getJContentPane() {
//		if (jContentPane == null) {
//			jContentPane = new JPanel();
//			jContentPane.setLayout(new BorderLayout());
//			jContentPane.add(getMainPanel(), BorderLayout.CENTER);
//		}
//		return jContentPane;
//	}

	/**
	 * This method initializes mainPanel	
	 * 	
	 * @return org.concord.energy3d.gui.MainPanel	
	 */
	private MainPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = MainPanel.getInstance();
		}
		return mainPanel;
	}

	@Override
	public void start() {
//		SceneManager.getInstance();
		new Thread(SceneManager.getInstance(), "Energy 3D Application").start();
//		SceneManager.getInstance().renderUnto(null);
	}

	@Override
	public void stop() {
		SceneManager.getInstance().exit();		
	}
	
//	@Override
//	public void paint(Graphics g) {
//		if (!isStarted ) {
//			new Thread(SceneManager.getInstance(), "Energy 3D Application").start();
//			isStarted = true;
//		}
//		super.paint(g);
//	}
}
