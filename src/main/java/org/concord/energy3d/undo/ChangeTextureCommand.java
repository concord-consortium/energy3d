package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.SceneManager;

public class ChangeTextureCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;
	private final HousePart part;

	public ChangeTextureCommand(final HousePart part) {
		this.part = part;
		oldValue = part.getTextureType();
	}

	public int getOldValue() {
		return oldValue;
	}

	public HousePart getPart() {
		return part;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = part.getTextureType();
		part.setTextureType(oldValue);
		part.draw();
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		part.setTextureType(newValue);
		part.draw();
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Texture Change";
	}

}
