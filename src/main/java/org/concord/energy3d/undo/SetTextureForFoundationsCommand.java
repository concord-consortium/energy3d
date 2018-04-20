package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.SceneManager;

public class SetTextureForFoundationsCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int[] oldTextureTypes;
	private int[] newTextureTypes;
	private final List<Foundation> foundations;

	public SetTextureForFoundationsCommand(final List<Foundation> foundations) {
		this.foundations = foundations;
		final int n = foundations.size();
		oldTextureTypes = new int[n];
		for (int i = 0; i < n; i++) {
			oldTextureTypes[i] = foundations.get(i).getTextureType();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = foundations.size();
		newTextureTypes = new int[n];
		for (int i = 0; i < n; i++) {
			final Foundation f = foundations.get(i);
			newTextureTypes[i] = f.getTextureType();
			f.setTextureType(oldTextureTypes[i]);
			f.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = foundations.size();
		for (int i = 0; i < n; i++) {
			final Foundation f = foundations.get(i);
			f.setTextureType(newTextureTypes[i]);
			f.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Set Texture for Foundations";
	}

}
