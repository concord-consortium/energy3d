/**
 * Copyright (c) 2008-2010 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package org.concord.energy3d.gui;

import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.JoglCanvas;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Ray3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.Timer;

/**
 * <p>
 * This jogl-based example is meant to show how to use Ardor3D at the most primitive level, forsaking the use of
 * ExampleBase and much of our framework classes and interfaces.
 * </p>
 * 
 * <p>
 * Also of note, this example does not allow choosing of properties on launch. It also does not handle input or show any
 * special debugging. This is to simplify the example to the basic essentials.
 * </p>
 */
public class JoglBasicExample implements Scene {

    // Our native window, not the gl surface itself.
    private final JoglCanvas _canvas;

    // Our timer.
    private final Timer _timer = new Timer();

    // A boolean allowing us to "pull the plug" from anywhere.
    private boolean _exit = false;

    // The root of our scene
    private final Node _root = new Node();

    public static void main(final String[] args) {
        final JoglBasicExample example = new JoglBasicExample();
        example.start();
    }

    /**
     * Constructs the example class, also creating the native window and GL surface.
     */
    public JoglBasicExample() {
        _canvas = initJogl();
        _canvas.init();
    }

    /**
     * Kicks off the example logic, first setting up the scene, then continuously updating and rendering it until exit
     * is flagged. Afterwards, the scene and gl surface are cleaned up.
     */
    private void start() {
        initExample();

        // Run in this same thread.
        while (!_exit) {
            updateExample();
            _canvas.draw(null);
            Thread.yield();
        }
        _canvas.getCanvasRenderer().makeCurrentContext();

        // Done, do cleanup
        ContextGarbageCollector.doFinalCleanup(_canvas.getCanvasRenderer().getRenderer());
        _canvas.close();

        _canvas.getCanvasRenderer().releaseCurrentContext();
    }

    /**
     * Setup a jogl canvas and canvas renderer.
     * 
     * @return the canvas.
     */
    private JoglCanvas initJogl() {
        final JoglCanvasRenderer canvasRenderer = new JoglCanvasRenderer(this);
        final DisplaySettings settings = new DisplaySettings(800, 600, 24, 0, 0, 8, 0, 0, false, false);
        return new JoglCanvas(canvasRenderer, settings);
    }

    /**
     * Initialize our scene.
     */
    protected void initExample() {
        PolygonPoint p[] = new PolygonPoint[4];
        p[0] = new PolygonPoint(-2, -2, 0);
        p[1] = new PolygonPoint(2, -2, 0);
        p[2] = new PolygonPoint(2, 2, 0);
        p[3] = new PolygonPoint(-2, 2, 0);
        Polygon polygon = new Polygon(p);

        double data[] = new double[] {0, 0, 0, 2.0, 0, 0, 2.0, 0.5, 0, 0, 0.5, 0};

        for (int i = 0; i < data.length; i += 12) {
           p = new PolygonPoint[] { new PolygonPoint(data[i], data[i + 1], data[i + 2]), new PolygonPoint(data[i + 3], data[i + 4], data[i + 5]), new PolygonPoint(data[i + 6], data[i + 7], data[i + 8]), new PolygonPoint(data[i + 9], data[i + 10], data[i + 11]) };
           Polygon hole = new Polygon(p);
           polygon.addHole(hole);
        }

        Mesh mesh = new Mesh();
        mesh.setDefaultColor(ColorRGBA.BLUE);
        _root.attachChild(mesh);

        Poly2Tri.triangulate(polygon);
        ArdorMeshMapper.updateTriangleMesh(mesh, polygon);
     }
    
    /**
     * Update our scene... Check if the window is closing. Then update our timer and finally update the geometric state
     * of the root and its children.
     */
    private void updateExample() {
        if (_canvas.isClosing()) {
            _exit = true;
            return;
        }

        _timer.update();

        // Update controllers/render states/transforms/bounds for rootNode.
        _root.updateGeometricState(_timer.getTimePerFrame(), true);
    }

    // ------ Scene methods ------

    public boolean renderUnto(final Renderer renderer) {
        if (!_canvas.isClosing()) {

            // Draw the root and all its children.
            renderer.draw(_root);

            return true;
        }
        return false;
    }

    public PickResults doPick(final Ray3 pickRay) {
        // Ignore
        return null;
    }
}
