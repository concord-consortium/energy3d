package org.concord.energy3d.util;

import java.util.ArrayList;
import java.util.List;

import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;


public class PolygonWithHoles extends Polygon {

    public PolygonWithHoles(final List<PolygonPoint> points) {
        super(points);
    }

    public ArrayList<Polygon> getHoles() {
        return _holes;
    }

}