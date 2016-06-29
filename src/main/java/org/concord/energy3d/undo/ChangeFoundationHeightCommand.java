package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFoundationHeightCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldValue, newValue;
	private Foundation foundation;

	public ChangeFoundationHeightCommand(Foundation roof) {
		this.foundation = roof;
		oldValue = roof.getHeight();
	}

	public Foundation getFoundation() {
		return foundation;
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = foundation.getHeight();
		foundation.setHeight(oldValue);
		foundation.draw();
		foundation.drawChildren();
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		foundation.setHeight(newValue);
		foundation.draw();
		foundation.drawChildren();
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Height Change for Selected Foundation";
	}

}
