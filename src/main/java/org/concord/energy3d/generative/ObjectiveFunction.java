package org.concord.energy3d.generative;

/**
 * @author Charles Xie
 *
 */
public abstract class ObjectiveFunction {

	volatile boolean cancelled;

	public void cancel() {
		cancelled = true;
	}

	public abstract double compute();

}
