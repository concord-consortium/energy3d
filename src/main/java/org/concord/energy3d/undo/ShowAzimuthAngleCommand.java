package org.concord.energy3d.undo;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.Heliodon;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class ShowAzimuthAngleCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldValue, newValue;

	public ShowAzimuthAngleCommand() {
		oldValue = Scene.getInstance().isAzimuthAngleVisible();
		newValue = !oldValue;
	}

	public boolean getNewValue() {
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		Scene.getInstance().setAzimuthAngleVisible(oldValue);
		Heliodon.getInstance().drawSunTriangle();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setAzimuthAngleVisible(newValue);
		Heliodon.getInstance().drawSunTriangle();
	}

	@Override
	public String getPresentationName() {
		return "Show Azimuth Angle";
	}

}