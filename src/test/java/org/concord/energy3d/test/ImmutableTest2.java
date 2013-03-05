package org.concord.energy3d.test;

public class ImmutableTest2 {
	    private static final int TEST_COUNT = 1000;

	    public static void main( final String args[] )
	    {
	        System.out.println( "Processing time for " + TEST_COUNT + " Mutable Vector3 operations (using Vector3.addLocal(ONE)):" );
	        testMutable();
	        System.out.println( "Processing time for " + TEST_COUNT + " Immutable Vector3 operations (using Vector3.add(ONE, null)):" );
	        testImmutable();
	    }

	    private static void testMutable()
	    {
	        final long start = System.nanoTime();
	        MVector3 mutable = new MVector3( 0,0,0 );

	        for( int i = 0; i < TEST_COUNT; i++ )
	            mutable.add( 1, 1, 1 );

	        final long end = System.nanoTime();
	        System.out.println( (end - start)*1e-6 + "ms" );
	    }

	    private static void testImmutable()
	    {
	        final long start = System.nanoTime();
	        IVector3 immutable = new IVector3( 0,0,0 );

	        for( int i = 0; i < TEST_COUNT; i++ )
	            immutable = immutable.add( 1, 1, 1 );

	        final long end = System.nanoTime();
	        System.out.println( (end - start)*1e-6 + "ms" );
	    }
	    
	    public final static class MVector3
	    {
	        protected double x,y,z;
	        public MVector3( double x, double y, double z )
	        { this.x = x; this.y = y; this.z = z; }
	        public void add( double x, double y, double z )
	        { this.x += x; this.y += y; this.z += z; }
	    }
	    
	    public final static class IVector3
	    {        
	        public final double x,y,z;
	        public IVector3( double x, double y, double z )
	        { this.x = x; this.y = y; this.z = z; }
	        public IVector3 add( double x, double y, double z )
	        { return new IVector3( this.x + x, this.y + y, this.z + z ); }
	    }
}
