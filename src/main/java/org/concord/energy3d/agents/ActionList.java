package org.concord.energy3d.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.MyAbstractUndoableEdit;

/**
 * @author Charles Xie
 *
 */
public class ActionList {

	private final Vector<UndoableEdit> edits;
	private final Map<String, List<MyAbstractUndoableEdit>> actions;

	public ActionList() {
		edits = SceneManager.getInstance().getUndoManager().getEdits();
		actions = new HashMap<String, List<MyAbstractUndoableEdit>>();
		for (final UndoableEdit e : edits) {
			if (e instanceof MyAbstractUndoableEdit) {
				final MyAbstractUndoableEdit e2 = (MyAbstractUndoableEdit) e;
				List<MyAbstractUndoableEdit> list = actions.get(e2.getPresentationName());
				if (list == null) {
					list = new ArrayList<MyAbstractUndoableEdit>();
					actions.put(e2.getPresentationName(), list);
				}
				list.add(e2);
			}
		}
	}

	public Map<String, List<MyAbstractUndoableEdit>> getActions() {
		return actions;
	}

}
