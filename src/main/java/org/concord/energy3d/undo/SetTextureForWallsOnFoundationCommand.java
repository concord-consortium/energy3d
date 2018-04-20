package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.SceneManager;

public class SetTextureForWallsOnFoundationCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int[] oldTextureTypes;
	private int[] newTextureTypes;
	private final Foundation foundation;
	private final List<Wall> walls;

	public SetTextureForWallsOnFoundationCommand(final Foundation foundation) {
		this.foundation = foundation;
		walls = foundation.getWalls();
		final int n = walls.size();
		oldTextureTypes = new int[n];
		for (int i = 0; i < n; i++) {
			oldTextureTypes[i] = walls.get(i).getTextureType();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = walls.size();
		newTextureTypes = new int[n];
		for (int i = 0; i < n; i++) {
			final Wall w = walls.get(i);
			newTextureTypes[i] = w.getTextureType();
			w.setTextureType(oldTextureTypes[i]);
			w.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = walls.size();
		for (int i = 0; i < n; i++) {
			final Wall w = walls.get(i);
			w.setTextureType(newTextureTypes[i]);
			w.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Set Texture for All Walls on Selected Foundation";
	}

}
