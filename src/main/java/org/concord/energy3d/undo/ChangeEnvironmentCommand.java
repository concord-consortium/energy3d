package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangeEnvironmentCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;
	private final ReadOnlyColorRGBA oldLandColor;
	private ReadOnlyColorRGBA newLandColor;

	public ChangeEnvironmentCommand() {
		oldValue = Scene.getInstance().getEnvironment();
		oldLandColor = Scene.getInstance().getLandColor();
	}

	public int getOldValue() {
		return oldValue;
	}

	public ReadOnlyColorRGBA getOldLandColor() {
		return oldLandColor;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = Scene.getInstance().getEnvironment();
		newLandColor = Scene.getInstance().getLandColor();
		Scene.getInstance().setEnvironment(oldValue);
		Scene.getInstance().setLandColor(oldLandColor);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setEnvironment(newValue);
		Scene.getInstance().setLandColor(newLandColor);
	}

	@Override
	public String getPresentationName() {
		return "Environment Change";
	}

}
