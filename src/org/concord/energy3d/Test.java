package org.concord.energy3d;

import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.example.ExampleBase;
import com.ardor3d.example.basic.BoxExample;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.scenegraph.Mesh;

public class Test extends ExampleBase {
	
	public Test() {
		System.out.println("Constructor");
	}
	
    public static void main(final String[] args) {
        start(ExampleBase.class);
    }	

	@Override
	protected void initExample() {
	     PolygonPoint p[] = new PolygonPoint[4];
	        p[0] = new PolygonPoint( 1.0,-0.1 );
	        p[1] = new PolygonPoint( -2.5,-0.1 );
	        p[2] = new PolygonPoint( -2.5,-7.1875 );
	        p[3] = new PolygonPoint( 1.0,-7.1875 );
	        Polygon polygon = new Polygon( p );
	        
	        double data[] = new double[]{-2.25,-1.2,-1.5,-1.2,
	            -1.5,-1.65,-2.25,-1.65, 0.6,-1.2,-0.1499999999999999,-1.2,
	            -0.1499999999999999,-1.65,0.6,-1.65,-1.2000000000000002,-1.2,-0.44999999999999996,-1.2,-0.44999999999999996,-1.65,-1.2000000000000002,-1.65,
	            -2.25,-2.1,-1.5,-2.1,-1.5,-2.55,-2.25,-2.55,
	            -1.2000000000000002,-2.1,-0.44999999999999996,-2.1,-0.44999999999999996,-2.55,-1.2000000000000002,-2.55,
	            -0.1499999999999999,-2.1,0.6,-2.1,0.6,-2.55,-0.1499999999999999,-2.55,
	            -2.25,-3.0,-1.5,-3.0,-1.5,-3.4499999999999997,-2.25,-3.4499999999999997,
	            -2.25,-3.9,-1.5,-3.9,-1.5,-4.35,-2.25,-4.35,
	            -2.25,-4.8,-1.5,-4.8,-1.5,-5.25,-2.25,-5.25,
	            -2.25,-5.7,-1.5,-5.7,-1.5,-6.1499999999999995,-2.25,-6.1499999999999995,
	            -1.2000000000000002,-3.0,-0.44999999999999996,-3.0,-0.44999999999999996,-3.4499999999999997,-1.2000000000000002,-3.4499999999999997,
	            -1.2000000000000002,-3.9,-0.44999999999999996,-3.9,-0.44999999999999996,-4.35,-1.2000000000000002,-4.35,
	            -1.2000000000000002,-4.8,-0.44999999999999996,-4.8,-0.44999999999999996,-5.25,-1.2000000000000002,-5.25,
	            -1.2000000000000002,-5.7,-0.44999999999999996,-5.7,-0.44999999999999996,-6.1499999999999995,-1.2000000000000002,-6.1499999999999995,
	            -0.1499999999999999,-3.0,0.6,-3.0,0.6,-3.4499999999999997,-0.1499999999999999,-3.4499999999999997,
	            -0.1499999999999999,-3.9,0.6,-3.9,0.6,-4.35,-0.1499999999999999,-4.35,
	            -0.1499999999999999,-4.8,0.6,-4.8,0.6,-5.25,-0.1499999999999999,-5.25,
	            -0.1499999999999999,-5.7,0.6,-5.7,0.6,-6.1499999999999995,-0.1499999999999999,-6.1499999999999995,
	            -2.25,-6.6,-1.5,-6.6,-1.5,-7.05,-2.25,-7.05,
	            -1.2000000000000002,-6.6,-0.44999999999999996,-6.6,-0.44999999999999996,-7.05,-1.2000000000000002,-7.05,
	            -0.1499999999999999,-6.6,0.6,-6.6,0.6,-7.05,-0.1499999999999999,-7.05};
	        
	        for( int i=0; i<data.length; i+=8 )
	        {
	            p = new PolygonPoint[]{ 
	                new PolygonPoint(data[i],data[i+1]), 
	                new PolygonPoint(data[i+2],data[i+3]), 
	                new PolygonPoint(data[i+4],data[i+5]),
	                new PolygonPoint(data[i+6],data[i+7])};
	            Polygon hole = new Polygon( p );
	            polygon.addHole( hole );
	        }

	        Mesh mesh = new Mesh();
	        mesh.setDefaultColor( ColorRGBA.BLUE );
	        _root.attachChild( mesh );
	  
	        Poly2Tri.triangulate( polygon );
	        ArdorMeshMapper.updateTriangleMesh( mesh, polygon );
	}

}
