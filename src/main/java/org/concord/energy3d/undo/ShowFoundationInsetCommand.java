package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;

public class ShowFoundationInsetCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldValue;
	private boolean newValue;
	private final Foundation foundation;

	public ShowFoundationInsetCommand(final Foundation foundation) {
		this.foundation = foundation;
		oldValue = foundation.getPolygon().isVisible();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = foundation.getPolygon().isVisible();
		foundation.getPolygon().setVisible(oldValue);
		foundation.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		foundation.getPolygon().setVisible(newValue);
		foundation.draw();
	}

	@Override
	public String getPresentationName() {
		return "Show Foundation Inset";
	}

}
