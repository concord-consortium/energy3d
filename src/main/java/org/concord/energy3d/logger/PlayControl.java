package org.concord.energy3d.logger;

/**
 * @author Charles Xie
 * 
 */
public abstract class PlayControl {

	public static volatile boolean active;
	public static volatile boolean replaying = true;
	public static volatile boolean backward;
	public static volatile boolean forward;

}
