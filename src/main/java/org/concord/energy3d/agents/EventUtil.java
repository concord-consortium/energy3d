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
public class EventUtil {

	private EventUtil() {
	}

	public static List<MyEvent> getEvents() {
		final List<MyEvent> events = new ArrayList<MyEvent>(MainApplication.getEventLog().getEvents());
		for (final UndoableEdit x : SceneManager.getInstance().getUndoManager().getEdits()) {
			if (x instanceof MyEvent) {
				events.add((MyEvent) x);
			}
		}
		return events;
	}

	public static Map<String, List<MyEvent>> getEventNameMap() {
		final Map<String, List<MyEvent>> events = new HashMap<String, List<MyEvent>>();
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
		return events;
	}

}
