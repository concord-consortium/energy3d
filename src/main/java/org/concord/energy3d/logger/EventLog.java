package org.concord.energy3d.logger;

import java.util.Vector;

import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.scene.SceneManager;

/**
 * @author Charles Xie
 *
 */
public class EventLog {

	private final Vector<UndoableEdit> edits;

	public EventLog() {
		edits = SceneManager.getInstance().getUndoManager().getEdits();
	}

}
