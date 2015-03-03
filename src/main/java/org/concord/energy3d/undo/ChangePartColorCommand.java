package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangePartColorCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private ReadOnlyColorRGBA orgColor, newColor;
	private HousePart selectedPart;

	public ChangePartColorCommand() {
		selectedPart = SceneManager.getInstance().getSelectedPart();
		orgColor = selectedPart == null ? null : selectedPart.getColor();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newColor = selectedPart == null ? null : selectedPart.getColor();
		if (selectedPart != null) {
			selectedPart.setColor(orgColor);
			Scene.getInstance().redrawAll();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (selectedPart != null) {
			selectedPart.setColor(newColor);
			Scene.getInstance().redrawAll();
		}
	}

	// for action logging
	public HousePart getHousePart() {
		return selectedPart;
	}

	@Override
	public String getPresentationName() {
		return "Color Change for Selected Part";
	}

}
