package org.concord.energy3d;

import java.awt.Component;
import java.net.URISyntaxException;

import javax.swing.JFrame;

import org.lwjgl.LWJGLException;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.example.ExampleBase;
import com.ardor3d.example.basic.LwjglBasicExample;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.JoglAwtCanvas;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.framework.lwjgl.LwjglAwtCanvas;
import com.ardor3d.framework.lwjgl.LwjglCanvasRenderer;
import com.ardor3d.image.Texture;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;

public class LwjglAwtExample implements Scene {	
	private static final boolean JOGL = false;
	private final Canvas canvas;
	private final Node root = new Node();
	
	public static void main(String args[]) {
		try {
			LwjglAwtExample example = new LwjglAwtExample();
			example.start();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}

	private void start() {
        initExample();

        // Run in this same thread.
//        while (!_exit) {
        while (true) {
            updateExample();
            canvas.draw(null);
            Thread.yield();
        }

//        // Done, do cleanup
//        ContextGarbageCollector.doFinalCleanup(_canvas.getCanvasRenderer().getRenderer());
//        _canvas.close();		
	}


	public LwjglAwtExample() throws LWJGLException {
		final DisplaySettings settings = new DisplaySettings(800, 600, 32, 60, 0, 8, 0, 8, false, false);
		if (JOGL)
			canvas = new JoglAwtCanvas(settings, new JoglCanvasRenderer(this));
		else
			canvas = new LwjglAwtCanvas(settings, new LwjglCanvasRenderer(this));
		
		JFrame frame = new JFrame("Swing JFrame");				
		frame.add((Component)canvas, "Center");		
	}
	
    private void initExample() {
        final Box _box = new Box("Box", Vector3.ZERO, 5, 5, 5);
        _box.setRandomColors();
        _box.setModelBound(new BoundingBox());
        _box.setTranslation(new Vector3(0, 0, -15));
        root.attachChild(_box);
    }	

    private void updateExample() {
    	root.updateGeometricState(0, true);
    }
    
	@Override
	public boolean renderUnto(Renderer renderer) {
		renderer.draw(root);
		return true;
	}

	@Override
	public PickResults doPick(Ray3 pickRay) {
		return null;
	}	
}
