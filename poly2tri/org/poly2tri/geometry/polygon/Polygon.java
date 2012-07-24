package org.poly2tri.geometry.polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.poly2tri.triangulation.Triangulatable;
import org.poly2tri.triangulation.TriangulationContext;
import org.poly2tri.triangulation.TriangulationMode;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

public class Polygon implements Triangulatable
{

    protected ArrayList<TriangulationPoint> _points = new ArrayList<TriangulationPoint>();
    protected ArrayList<TriangulationPoint> _steinerPoints;
    protected ArrayList<Polygon> _holes;

    protected List<DelaunayTriangle> m_triangles;

    protected PolygonPoint _last;

    /**
     * To create a polygon we need atleast 3 separate points
     *
     * @param p1
     * @param p2
     * @param p3
     */
    public Polygon( final PolygonPoint p1, final PolygonPoint p2, final PolygonPoint p3 )
    {
        p1._next = p2;
        p2._next = p3;
        p3._next = p1;
        p1._previous = p3;
        p2._previous = p1;
        p3._previous = p2;
        _points.add( p1 );
        _points.add( p2 );
        _points.add( p3 );
    }

    /**
     * Requires atleast 3 points
     * @param points - ordered list of points forming the polygon.
     *                 No duplicates are allowed
     */
    public Polygon( final List<PolygonPoint> points )
    {
        // Lets do one sanity check that first and last point hasn't got same position
        // Its something that often happen when importing polygon data from other formats
        if( points.get(0).equals( points.get(points.size()-1) ) )
        {
            points.remove( points.size()-1 );
        }
        _points.addAll( points );
    }

    /**
     * Requires atleast 3 points
     *
     * @param points
     */
    public Polygon( final PolygonPoint[] points )
    {
        this( Arrays.asList( points ) );
    }

    @Override
	public TriangulationMode getTriangulationMode()
    {
        return TriangulationMode.POLYGON;
    }

    public int pointCount()
    {
        int count = _points.size();
        if( _steinerPoints != null )
        {
            count += _steinerPoints.size();
        }
        return count;
    }

    public void addSteinerPoint( final TriangulationPoint point )
    {
        if( _steinerPoints == null )
        {
            _steinerPoints = new ArrayList<TriangulationPoint>();
        }
        _steinerPoints.add( point );
    }

    public void addSteinerPoints( final List<TriangulationPoint> points )
    {
        if( _steinerPoints == null )
        {
            _steinerPoints = new ArrayList<TriangulationPoint>();
        }
        _steinerPoints.addAll( points );
    }

    public void clearSteinerPoints()
    {
        if( _steinerPoints != null )
        {
            _steinerPoints.clear();
        }
    }

    /**
     * Assumes: that given polygon is fully inside the current polygon
     * @param poly - a subtraction polygon
     */
    public void addHole( final Polygon poly )
    {
        if( _holes == null )
        {
            _holes = new ArrayList<Polygon>();
        }
        _holes.add( poly );
        // XXX: tests could be made here to be sure it is fully inside
//        addSubtraction( poly.getPoints() );
    }

    /**
     * Will insert a point in the polygon after given point
     *
     * @param a
     * @param b
     * @param p
     */
    public void insertPointAfter( final PolygonPoint a, final PolygonPoint newPoint )
    {
        // Validate that
        final int index = _points.indexOf( a );
        if( index != -1 )
        {
            newPoint.setNext( a.getNext() );
            newPoint.setPrevious( a );
            a.getNext().setPrevious( newPoint );
            a.setNext( newPoint );
            _points.add( index+1, newPoint );
        }
        else
        {
            throw new RuntimeException( "Tried to insert a point into a Polygon after a point not belonging to the Polygon" );
        }
    }

    public void addPoints( final List<PolygonPoint> list )
    {
        PolygonPoint first;
        for( final PolygonPoint p : list )
        {
            p.setPrevious( _last );
            if( _last != null )
            {
                p.setNext( _last.getNext() );
                _last.setNext( p );
            }
            _last = p;
            _points.add( p );
        }
        first = (PolygonPoint)_points.get(0);
        _last.setNext( first );
        first.setPrevious( _last );
    }

    /**
     * Will add a point after the last point added
     *
     * @param p
     */
    public void addPoint(final PolygonPoint p )
    {
        p.setPrevious( _last );
        p.setNext( _last.getNext() );
        _last.setNext( p );
        _points.add( p );
    }

    public void removePoint( final PolygonPoint p )
    {
        PolygonPoint next, prev;

        next = p.getNext();
        prev = p.getPrevious();
        prev.setNext( next );
        next.setPrevious( prev );
        _points.remove( p );
    }

    public PolygonPoint getPoint()
    {
        return _last;
    }

    @Override
	public List<TriangulationPoint> getPoints()
    {
        return _points;
    }

    @Override
	public List<DelaunayTriangle> getTriangles()
    {
        return m_triangles;
    }

    @Override
	public void addTriangle( final DelaunayTriangle t )
    {
        m_triangles.add( t );
    }

    @Override
	public void addTriangles( final List<DelaunayTriangle> list )
    {
        m_triangles.addAll( list );
    }

    @Override
	public void clearTriangulation()
    {
        if( m_triangles != null )
        {
            m_triangles.clear();
        }
    }

    /**
     * Creates constraints and populates the context with points
     */
    @Override
	public void prepareTriangulation( final TriangulationContext<?> tcx )
    {
        if( m_triangles == null )
        {
            m_triangles = new ArrayList<DelaunayTriangle>( _points.size() );
        }
        else
        {
            m_triangles.clear();
        }

        // Outer constraints
        for( int i = 0; i < _points.size()-1 ; i++ )
        {
            tcx.newConstraint( _points.get( i ), _points.get( i+1 ) );
        }
        tcx.newConstraint( _points.get( 0 ), _points.get( _points.size()-1 ) );
        tcx.addPoints( _points );

        // Hole constraints
        if( _holes != null )
        {
            for( final Polygon p : _holes )
            {
                for( int i = 0; i < p._points.size()-1 ; i++ )
                {
                    tcx.newConstraint( p._points.get( i ), p._points.get( i+1 ) );
                }
                tcx.newConstraint( p._points.get( 0 ), p._points.get( p._points.size()-1 ) );
                tcx.addPoints( p._points );
            }
        }

        if( _steinerPoints != null )
        {
            tcx.addPoints( _steinerPoints );
        }
    }

	public ArrayList<Polygon> getHoles() {
		return _holes;
	}

}
