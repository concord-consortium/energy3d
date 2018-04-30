package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangeBuildingColorCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final ReadOnlyColorRGBA[] oldColors;
	private ReadOnlyColorRGBA[] newColors;
	private final int[] oldTextures;
	private final HousePart part;
	private final Foundation foundation;
	private final List<HousePart> parts;

	public ChangeBuildingColorCommand(final HousePart part) {
		this.part = part;
		foundation = part instanceof Foundation ? (Foundation) part : part.getTopContainer();
		parts = Scene.getInstance().getPartsOfSameTypeInBuilding(part);
		final int n = parts.size();
		oldColors = new ReadOnlyColorRGBA[n];
		oldTextures = new int[n];
		for (int i = 0; i < n; i++) {
			oldColors[i] = parts.get(i).getColor();
			oldTextures[i] = parts.get(i).getTextureType();
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
		final int n = parts.size();
		newColors = new ReadOnlyColorRGBA[n];
		for (int i = 0; i < n; i++) {
			final HousePart p = parts.get(i);
			newColors[i] = p.getColor();
			p.setColor(oldColors[i]);
			p.setTextureType(oldTextures[i]);
			p.draw();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = parts.size();
		for (int i = 0; i < n; i++) {
			final HousePart p = parts.get(i);
			p.setColor(newColors[i]);
			p.setTextureType(HousePart.TEXTURE_NONE);
			p.draw();
		}
	}

	@Override
	public String getPresentationName() {
		return "Color Change for Whole Building";
	}

}
