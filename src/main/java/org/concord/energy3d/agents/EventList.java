package org.concord.energy3d.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.MyAbstractUndoableEdit;

/**
 * @author Charles Xie
 *
 */
public class EventList {

	private final Map<String, List<MyEvent>> events;

	public EventList() {
		events = new HashMap<String, List<MyEvent>>();
		for (final UndoableEdit e : SceneManager.getInstance().getUndoManager().getEdits()) {
			if (e instanceof MyAbstractUndoableEdit) {
				final MyAbstractUndoableEdit e2 = (MyAbstractUndoableEdit) e;
				List<MyEvent> list = events.get(e2.getName());
				if (list == null) {
					list = new ArrayList<MyEvent>();
					events.put(e2.getName(), list);
				}
				list.add(e2);
			}
		}
		for (final MyEvent e : MainApplication.getEventLog().getEvents()) {
			List<MyEvent> list = events.get(e.getName());
			if (list == null) {
				list = new ArrayList<MyEvent>();
				events.put(e.getName(), list);
			}
			list.add(e);
		}
	}

	public Map<String, List<MyEvent>> getEvents() {
		return events;
	}

}
