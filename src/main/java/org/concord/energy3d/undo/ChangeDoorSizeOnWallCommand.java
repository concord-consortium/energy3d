package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.SceneManager;

public class ChangeDoorSizeOnWallCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldWidths, oldHeights;
	private double[] newWidths, newHeights;
	private final Wall wall;
	private final List<Door> doors;

	public ChangeDoorSizeOnWallCommand(final Wall wall) {
		this.wall = wall;
		doors = wall.getDoors();
		final int n = doors.size();
		oldWidths = new double[n];
		oldHeights = new double[n];
		for (int i = 0; i < n; i++) {
			oldWidths[i] = doors.get(i).getDoorWidth();
			oldHeights[i] = doors.get(i).getDoorHeight();
		}
	}

	public HousePart getContainer() {
		return wall;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = doors.size();
		newWidths = new double[n];
		newHeights = new double[n];
		for (int i = 0; i < n; i++) {
			final Door d = doors.get(i);
			newWidths[i] = d.getDoorWidth();
			newHeights[i] = d.getDoorHeight();
			d.setDoorWidth(oldWidths[i]);
			d.setDoorHeight(oldHeights[i]);
			d.draw();
		}
		wall.draw();
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = doors.size();
		for (int i = 0; i < n; i++) {
			final Door d = doors.get(i);
			d.setDoorWidth(newWidths[i]);
			d.setDoorHeight(newHeights[i]);
			d.draw();
		}
		wall.draw();
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Size Change for All Doors on Wall";
	}

}
