package org.concord.energy3d.geneticalgorithms;

/**
 * @author Charles Xie
 *
 */
public abstract class ObjectiveFunction {

	public final static int DAILY = 0;
	public final static int ANNUAl = 1;

	protected int type = DAILY;

	protected volatile boolean cancelled;

	public void cancel() {
		cancelled = true;
	}

	public int getType() {
		return type;
	}

	public abstract double compute();

}
