package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangeBuildingColorCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private ReadOnlyColorRGBA[] oldColors, newColors;
	private HousePart part;
	private Foundation foundation;
	private List<HousePart> parts;

	public ChangeBuildingColorCommand(HousePart part) {
		this.part = part;
		foundation = part instanceof Foundation ? (Foundation) part : part.getTopContainer();
		parts = Scene.getInstance().getHousePartsOfSameTypeInBuilding(part);
		int n = parts.size();
		oldColors = new ReadOnlyColorRGBA[n];
		for (int i = 0; i < n; i++) {
			oldColors[i] = parts.get(i).getColor();
		}
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
		int n = parts.size();
		newColors = new ReadOnlyColorRGBA[n];
		for (int i = 0; i < n; i++) {
			newColors[i] = parts.get(i).getColor();
			parts.get(i).setColor(oldColors[i]);
		}
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		int n = parts.size();
		for (int i = 0; i < n; i++) {
			parts.get(i).setColor(newColors[i]);
		}
		Scene.getInstance().redrawAll();
	}

	@Override
	public String getPresentationName() {
		return "Color Change for Whole Building";
	}

}
