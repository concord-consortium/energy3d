package org.concord.energy3d.test;

import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;

public class PolygonTest {

	public static void main(String[] args) {
		PolygonPoint[] points = {
				new PolygonPoint(0.018554450520199106, -0.00311995148021188, 0.17662504900428785),
				new PolygonPoint(1.2883457542888057, -0.21663677071666834, 0.17662504900428785),
				new PolygonPoint(1.1225226095131287, -1.2027922780565123, 0.17662504900428785),
				new PolygonPoint(-0.14726869425547795, -0.9892754588200559, 0.17662504900428785)		
		};
		Polygon polygon = new Polygon(points);
		Poly2Tri.triangulate(polygon);
	}

}
