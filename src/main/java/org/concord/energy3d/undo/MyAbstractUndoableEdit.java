package org.concord.energy3d.undo;

import java.net.URL;

import javax.swing.undo.AbstractUndoableEdit;

import org.concord.energy3d.agents.UndoableEvent;
import org.concord.energy3d.scene.Scene;

/**
 * Store the timestamp and other information for an undoable edit in order to analyze it
 * 
 * @author Charles Xie
 *
 */
public abstract class MyAbstractUndoableEdit extends AbstractUndoableEdit implements UndoableEvent {

	private static final long serialVersionUID = 1L;
	protected long timestamp;
	protected URL file;

	public MyAbstractUndoableEdit() {
		super();
		timestamp = System.currentTimeMillis();
		file = Scene.getURL();
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public String getName() {
		return getPresentationName();
	}

	@Override
	public char getOneLetterCode() {
		return getName().charAt(0);
	}

	@Override
	public URL getFile() {
		return file;
	}

}
