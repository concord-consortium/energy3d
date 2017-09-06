package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;

/**
 * Store the timestamp and other information for an undoable edit in order to analyze it
 * 
 * @author Charles Xie
 *
 */
public abstract class MyAbstractUndoableEdit extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	protected long timestamp;

	public MyAbstractUndoableEdit() {
		super();
		timestamp = System.currentTimeMillis();
	}

	public long getTimestamp() {
		return timestamp;
	}

}
