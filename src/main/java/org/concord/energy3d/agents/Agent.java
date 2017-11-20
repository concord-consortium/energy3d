package org.concord.energy3d.agents;

/**
 * @author Charles Xie
 *
 */
public interface Agent {

	public void sense(MyEvent event);

	public void actuate();

	public String getName();

}
