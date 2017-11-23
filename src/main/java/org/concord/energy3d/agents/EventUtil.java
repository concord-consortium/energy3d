package org.concord.energy3d.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

	private final static char IDLE_LETTER = '_';
	private final static char BACKGROUND_LETTER = '*';

	private EventUtil() {
	}

	public static String eventsToString(final Class<?>[] selection, final int idleTimeInMillis) {
		String s = "";
		final List<MyEvent> events = getEvents();
		MyEvent lastEvent = null;
		if (selection == null) {
			for (final MyEvent e : events) {
				if (lastEvent != null) {
					if (e.getTimestamp() - lastEvent.getTimestamp() > idleTimeInMillis) {
						s += IDLE_LETTER;
					}
				}
				s += e.getOneLetterCode();
				lastEvent = e;
			}
		} else {
			char x;
			for (final MyEvent e : events) {
				x = BACKGROUND_LETTER;
				for (final Class<?> c : selection) {
					if (c.isInstance(e)) {
						x = e.getOneLetterCode();
						break;
					}
				}
				if (lastEvent != null) {
					if (e.getTimestamp() - lastEvent.getTimestamp() > idleTimeInMillis) {
						s += IDLE_LETTER;
					}
				}
				s += x;
				lastEvent = e;
			}
		}
		return s;
	}

	public static List<MyEvent> getEvents() {
		final List<MyEvent> events = new ArrayList<MyEvent>(MainApplication.getEventLog().getEvents());
		for (final UndoableEdit x : SceneManager.getInstance().getUndoManager().getEdits()) {
			if (x instanceof MyEvent) {
				events.add((MyEvent) x);
			}
		}
		Collections.sort(events, new Comparator<MyEvent>() {
			@Override
			public int compare(final MyEvent e1, final MyEvent e2) {
				return new Long(e1.getTimestamp()).compareTo(new Long(e2.getTimestamp()));
			}
		});
		return events;
	}

	public static List<AnalysisEvent> getAnalysisEvents() {
		final List<AnalysisEvent> list = new ArrayList<AnalysisEvent>();
		for (final MyEvent e : MainApplication.getEventLog().getEvents()) {
			if (e instanceof AnalysisEvent) {
				list.add((AnalysisEvent) e);
			}
		}
		return list;
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
