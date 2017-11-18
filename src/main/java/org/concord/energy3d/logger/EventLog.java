package org.concord.energy3d.logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.concord.energy3d.agents.MyEvent;

/**
 * @author Charles Xie
 *
 */
public class EventLog {

	private final List<MyEvent> events;

	public EventLog() {
		events = Collections.synchronizedList(new ArrayList<MyEvent>());
	}

	public void clear() {
		events.clear();
	}

	public List<MyEvent> getEvents() {
		return events;
	}

	public void addEvent(final MyEvent e) {
		events.add(e);
	}

}
