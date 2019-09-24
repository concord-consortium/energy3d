package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.Heliodon;

public class ShowZenithAngleCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldValue, newValue;

	public ShowZenithAngleCommand() {
		oldValue = Scene.getInstance().isZenithAngleVisible();
		newValue = !oldValue;
	}

	public boolean getNewValue() {
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		Scene.getInstance().setZenithAngleVisible(oldValue);
		Heliodon.getInstance().drawSunTriangle();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setZenithAngleVisible(newValue);
		Heliodon.getInstance().drawSunTriangle();
	}

	@Override
	public String getPresentationName() {
		return "Show Zenith Angle";
	}

}