package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;

public class SetGroupMasterCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldValue;
	private boolean newValue;
	private final Foundation foundation;

	public SetGroupMasterCommand(final Foundation foundation) {
		this.foundation = foundation;
		oldValue = foundation.isGroupMaster();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = foundation.isGroupMaster();
		foundation.setGroupMaster(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		foundation.setGroupMaster(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Set Group Master";
	}

}
