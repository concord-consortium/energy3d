package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangeBuildingColorCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private ReadOnlyColorRGBA oldColor, newColor;
	private HousePart part;
	private Foundation foundation;

	public ChangeBuildingColorCommand(HousePart part) {
		this.part = part;
		foundation = part instanceof Foundation ? (Foundation) part : part.getTopContainer();
		oldColor = part.getColor();
	}

	public HousePart getPart() {
		return part;
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newColor = part.getColor();
		Scene.getInstance().setPartColorOfBuilding(part, oldColor);
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setPartColorOfBuilding(part, newColor);
		Scene.getInstance().redrawAll();
	}

	@Override
	public String getPresentationName() {
		return "Color Change for Whole Building";
	}

}
