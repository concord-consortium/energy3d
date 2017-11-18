package org.concord.energy3d.agents;

import java.net.URL;

/**
 * @author Charles Xie
 *
 */
public interface MyEvent {

	public long getTimestamp();

	public String getName();

	public URL getFile();

}
