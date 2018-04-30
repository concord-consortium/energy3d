package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangePartColorCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final ReadOnlyColorRGBA oldColor;
	private ReadOnlyColorRGBA newColor;
	private final int oldTexture;
	private final HousePart part;

	public ChangePartColorCommand(final HousePart part) {
		this.part = part;
		oldColor = part.getColor();
		oldTexture = part.getTextureType();
	}

	public HousePart getPart() {
		return part;
	}

	public ReadOnlyColorRGBA getOldColor() {
		return oldColor;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newColor = part.getColor();
		part.setColor(oldColor);
		part.setTextureType(oldTexture);
		part.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		part.setColor(newColor);
		part.setTextureType(HousePart.TEXTURE_NONE);
		part.draw();
	}

	@Override
	public char getOneLetterCode() {
		return 'L';
	}

	@Override
	public String getPresentationName() {
		return "Color Change for Selected Part";
	}

}
