package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.SceneManager;

public class SetTextureForPartsCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int[] oldTextureTypes;
	private int[] newTextureTypes;
	private final List<HousePart> parts;

	public SetTextureForPartsCommand(final List<HousePart> parts) {
		this.parts = parts;
		final int n = parts.size();
		oldTextureTypes = new int[n];
		for (int i = 0; i < n; i++) {
			oldTextureTypes[i] = parts.get(i).getTextureType();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = parts.size();
		newTextureTypes = new int[n];
		for (int i = 0; i < n; i++) {
			final HousePart p = parts.get(i);
			newTextureTypes[i] = p.getTextureType();
			p.setTextureType(oldTextureTypes[i]);
			p.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = parts.size();
		for (int i = 0; i < n; i++) {
			final HousePart p = parts.get(i);
			p.setTextureType(newTextureTypes[i]);
			p.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Set Texture for Foundations";
	}

}
