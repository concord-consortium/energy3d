package org.concord.energy3d.util;

/**
 * @author Charles Xie
 *
 */
public class Pair {

	private final int i, j;

	public Pair(final int i, final int j) {
		if (i == j) {
			throw new IllegalArgumentException("i and j must be different");
		} else if (i < j) {
			this.i = i;
			this.j = j;
		} else {
			this.i = j;
			this.j = i;
		}
	}

	public int i() {
		return i;
	}

	public int j() {
		return j;
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof Pair) {
			final Pair p = (Pair) o;
			return i == p.i && j == p.j;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int code = 17;
		code = 31 * code + i;
		code = 31 * code + j;
		return code;
	}

	@Override
	public String toString() {
		return "(" + i + ", " + j + ")";
	}

}
