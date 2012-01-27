package org.concord.energy3d.test;

import com.ardor3d.math.Vector3;

public class ImmutableTest {
	private static final int TEST_COUNT = 10000;
	
	public static void main(final String args[]) {
		System.out.println("Processing time for " + TEST_COUNT + " Mutable Vector3 operations (using Vector3.addLocal(ONE)):");
		testMutable();
		System.out.println("Processing time for " + TEST_COUNT + " Immutable Vector3 operations (using Vector3.add(ONE, null)):");
		testImmutable();
	}

	private static void testMutable() {
		final long start = System.currentTimeMillis();
		Vector3 mutable = new Vector3();
		
		for (int i = 0; i < TEST_COUNT; i++)
			mutable = mutable.addLocal(Vector3.ONE);
		
		final long end = System.currentTimeMillis();
		System.out.println(end - start + "ms");
	}
	
	private static void testImmutable() {
		final long start = System.currentTimeMillis();
		Vector3 immutable = new Vector3();
		
		for (int i = 0; i < TEST_COUNT; i++)
			immutable = immutable.add(Vector3.ONE, null);
		
		final long end = System.currentTimeMillis();
		System.out.println(end - start + "ms");
	}
}
