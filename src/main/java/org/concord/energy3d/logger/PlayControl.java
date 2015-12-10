package org.concord.energy3d.logger;

/**
 * @author Charles Xie
 * 
 */
public abstract class PlayControl {

	public static volatile boolean replaying = true;
	public static volatile boolean backward, forward;

	boolean active;

	public boolean isActive() {
		return active;
	}

}
