package org.concord.energy3d.kinect;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class HandTrackerApplication {

    /**
	 *
	 */
	public HandTracker viewer;
	private boolean shouldRun = true;
	private final JFrame frame;

    public HandTrackerApplication (final JFrame frame)
    {
    	this.frame = frame;
    	frame.addKeyListener(new KeyListener()
		{
			@Override
			public void keyTyped(final KeyEvent arg0) {}
			@Override
			public void keyReleased(final KeyEvent arg0) {}
			@Override
			public void keyPressed(final KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					shouldRun = false;
				}
			}
		});
    }

    public void buildUI()
    {
        if (viewer == null)
        {
            viewer = new HandTracker();
        }
        viewer.updateDepth();
        viewer.repaint();
    }

    public static void main(final String s[])
    {
        final JFrame f = new JFrame("OpenNI Hand Tracker");
        f.addWindowListener(new WindowAdapter() {
            @Override
			public void windowClosing(final WindowEvent e) {System.exit(0);}
        });
        final HandTrackerApplication app = new HandTrackerApplication(f);

        app.viewer = new HandTracker();
        f.add("Center", app.viewer);
        f.pack();
        f.setVisible(true);
        app.run();
    }

    void run()
    {
        while(shouldRun) {
            viewer.updateDepth();
            viewer.repaint();
        }
        frame.dispose();
    }

}