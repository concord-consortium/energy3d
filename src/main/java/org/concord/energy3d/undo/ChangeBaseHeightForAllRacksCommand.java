package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeBaseHeightForAllRacksCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final List<Rack> racks;

	public ChangeBaseHeightForAllRacksCommand() {
		racks = Scene.getInstance().getAllRacks();
		final int n = racks.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = racks.get(i).getBaseHeight();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = racks.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			final Rack r = racks.get(i);
			newValues[i] = r.getBaseHeight();
			r.setBaseHeight(oldValues[i]);
			r.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = racks.size();
		for (int i = 0; i < n; i++) {
			final Rack r = racks.get(i);
			r.setBaseHeight(newValues[i]);
			r.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Base Height for All Racks";
	}

}
