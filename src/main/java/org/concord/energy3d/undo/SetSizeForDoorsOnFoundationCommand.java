package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.SceneManager;

public class SetSizeForDoorsOnFoundationCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldWidths;
	private double[] newWidths;
	private final double[] oldHeights;
	private double[] newHeights;
	private final Foundation foundation;
	private final List<Door> doors;

	public SetSizeForDoorsOnFoundationCommand(final Foundation foundation) {
		this.foundation = foundation;
		doors = foundation.getDoors();
		final int n = doors.size();
		oldWidths = new double[n];
		oldHeights = new double[n];
		for (int i = 0; i < n; i++) {
			final Door d = doors.get(i);
			oldWidths[i] = d.getDoorWidth();
			oldHeights[i] = d.getDoorHeight();
		}
	}

	public Foundation getFoundation() {
		return foundation;
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
			d.getContainer().draw();
		}
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
			d.getContainer().draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Set Size for All Doors on Selected Foundation";
	}

}
