package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangePartColorCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private ReadOnlyColorRGBA oldColor, newColor;
	private HousePart part;

	public ChangePartColorCommand(HousePart part) {
		this.part = part;
		oldColor = part.getColor();
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
		part.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		part.setColor(newColor);
		part.draw();
	}

	@Override
	public String getPresentationName() {
		return "Color Change for Selected Part";
	}

}
