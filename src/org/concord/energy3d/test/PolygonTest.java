package org.concord.energy3d.test;

import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;

public class PolygonTest {

	public static void main(final String[] args) {
//		final PolygonPoint[] points = {
//				new PolygonPoint(0.018554450520199106, -0.00311995148021188, 0.17662504900428785),
//				new PolygonPoint(1.2883457542888057, -0.21663677071666834, 0.17662504900428785),
//				new PolygonPoint(1.1225226095131287, -1.2027922780565123, 0.17662504900428785),
//				new PolygonPoint(-0.14726869425547795, -0.9892754588200559, 0.17662504900428785)
//		};
//		final Polygon polygon = new Polygon(points);

			final Polygon polygon = new Polygon(new PolygonPoint[] {
				new PolygonPoint(6.699999999999999, 1.1, 1.7000000000000002),
				new PolygonPoint(0.1, 1.1, 1.7000000000000002),
				new PolygonPoint(0.1, -1.1, 1.7000000000000002),
				new PolygonPoint(6.699999999999999, -1.1, 1.7000000000000002),
			});
			Polygon hole;
			hole = new Polygon(new PolygonPoint[] {
				new PolygonPoint(5.6, -0.19999999999999907, 1.7000000000000002),
				new PolygonPoint(6.4, -0.19999999999999907, 1.7000000000000002),
				new PolygonPoint(6.4, -0.8999999999999999, 1.7000000000000002),
				new PolygonPoint(5.6, -0.8999999999999999, 1.7000000000000002),
			});
			polygon.addHole(hole);
			hole = new Polygon(new PolygonPoint[] {
				new PolygonPoint(4.6, -0.19999999999999907, 1.7000000000000002),
				new PolygonPoint(5.1, -0.19999999999999907, 1.7000000000000002),
				new PolygonPoint(5.1, -0.8999999999999995, 1.7000000000000002),
				new PolygonPoint(4.6, -0.8999999999999995, 1.7000000000000002),
			});
			polygon.addHole(hole);
			hole = new Polygon(new PolygonPoint[] {
				new PolygonPoint(1.5999999999999999, 0.8000000000000052, 1.7000000000000002),
				new PolygonPoint(2.3000000000000003, 0.8000000000000052, 1.7000000000000002),
				new PolygonPoint(2.3000000000000003, 0.3000000000000015, 1.7000000000000002),
				new PolygonPoint(1.5999999999999999, 0.3000000000000015, 1.7000000000000002),
			});
			polygon.addHole(hole);


			Poly2Tri.triangulate(polygon);
	}

}
