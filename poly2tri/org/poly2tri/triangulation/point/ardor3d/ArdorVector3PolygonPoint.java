package org.poly2tri.triangulation.point.ardor3d;

import java.util.ArrayList;
import java.util.List;

import org.poly2tri.geometry.polygon.PolygonPoint;

import com.ardor3d.math.Vector3;

public class ArdorVector3PolygonPoint extends PolygonPoint
{
    private final Vector3 _p;

    public ArdorVector3PolygonPoint( final Vector3 point )
    {
        super( point.getX(), point.getY() );
        _p = point.clone();
    }

    @Override
	public final double getX()
    {
        return _p.getX();
    }
    @Override
	public final double getY()
    {
        return _p.getY();
    }
    @Override
	public final double getZ()
    {
        return _p.getZ();
    }

    @Override
	public final float getXf()
    {
        return _p.getXf();
    }
    @Override
	public final float getYf()
    {
        return _p.getYf();
    }
    @Override
	public final float getZf()
    {
        return _p.getZf();
    }

    public static List<PolygonPoint> toPoints( final Vector3[] vpoints )
    {
        final ArrayList<PolygonPoint> points = new ArrayList<PolygonPoint>(vpoints.length);
        for( int i=0; i<vpoints.length; i++ )
        {
            points.add( new ArdorVector3PolygonPoint(vpoints[i]) );
        }
        return points;
    }

    public static List<PolygonPoint> toPoints( final ArrayList<Vector3> vpoints )
    {
        final int i=0;
        final ArrayList<PolygonPoint> points = new ArrayList<PolygonPoint>(vpoints.size());
        for( final Vector3 point : vpoints )
        {
            points.add( new ArdorVector3PolygonPoint(point) );
        }
        return points;
    }

    @Override
    public void set(final double x, final double y, final double z) {
    	_p.set(x, y, z);
    }
}
