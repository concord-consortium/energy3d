package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangeLandColorCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final ReadOnlyColorRGBA oldColor;
	private ReadOnlyColorRGBA newColor;

	public ChangeLandColorCommand() {
		oldColor = Scene.getInstance().getLandColor();
	}

	public ReadOnlyColorRGBA getOldColor() {
		return oldColor;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newColor = Scene.getInstance().getLandColor();
		Scene.getInstance().setLandColor(oldColor);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setLandColor(newColor);
	}

	@Override
	public String getPresentationName() {
		return "Land Color Change";
	}

}
