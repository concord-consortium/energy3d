package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.SceneManager;

public class ChangeDoorTextureOnWallCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int[] oldTextureTypes;
	private int[] newTextureTypes;
	private final Wall wall;
	private final List<Door> doors;

	public ChangeDoorTextureOnWallCommand(final Wall wall) {
		this.wall = wall;
		doors = wall.getDoors();
		final int n = doors.size();
		oldTextureTypes = new int[n];
		for (int i = 0; i < n; i++) {
			oldTextureTypes[i] = doors.get(i).getTextureType();
		}
	}

	public HousePart getContainer() {
		return wall;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = doors.size();
		newTextureTypes = new int[n];
		for (int i = 0; i < n; i++) {
			final Door d = doors.get(i);
			newTextureTypes[i] = d.getTextureType();
			d.setTextureType(oldTextureTypes[i]);
			d.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = doors.size();
		for (int i = 0; i < n; i++) {
			final Door d = doors.get(i);
			d.setTextureType(newTextureTypes[i]);
			d.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Texture Change for All Doors on Wall";
	}

}
