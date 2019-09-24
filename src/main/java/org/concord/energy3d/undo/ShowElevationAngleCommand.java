package org.concord.energy3d.undo;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.Heliodon;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class ShowElevationAngleCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldValue, newValue;

	public ShowElevationAngleCommand() {
		oldValue = Scene.getInstance().isElevationAngleVisible();
		newValue = !oldValue;
	}

	public boolean getNewValue() {
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		Scene.getInstance().setElevationAngleVisible(oldValue);
		Heliodon.getInstance().drawSunTriangle();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setElevationAngleVisible(newValue);
		Heliodon.getInstance().drawSunTriangle();
	}

	@Override
	public String getPresentationName() {
		return "Show Elevation Angle";
	}

}