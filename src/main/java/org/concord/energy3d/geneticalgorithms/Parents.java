package org.concord.energy3d.geneticalgorithms;

import java.util.Objects;

/**
 * @author Charles Xie
 *
 */
public class Parents {

	public final Individual dad, mom;

	public Parents(final Individual dad, final Individual mom) {
		this.dad = dad;
		this.mom = mom;
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof Parents) {
			final Parents p = (Parents) o;
			return (p.dad == dad && p.mom == mom) || (p.dad == mom && p.mom == dad);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(dad, mom);
	}

}
