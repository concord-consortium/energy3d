package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangePoleSettingsForAllRacksCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldDxs;
	private double[] newDxs;
	private final double[] oldDys;
	private double[] newDys;
	private final boolean[] oldVisibles;
	private boolean[] newVisibles;
	private final List<Rack> racks;

	public ChangePoleSettingsForAllRacksCommand() {
		racks = Scene.getInstance().getAllRacks();
		final int n = racks.size();
		oldDxs = new double[n];
		oldDys = new double[n];
		oldVisibles = new boolean[n];
		for (int i = 0; i < n; i++) {
			final Rack r = racks.get(i);
			oldDxs[i] = r.getPoleDistanceX();
			oldDys[i] = r.getPoleDistanceY();
			oldVisibles[i] = r.isPoleVisible();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = racks.size();
		newDxs = new double[n];
		newDys = new double[n];
		newVisibles = new boolean[n];
		for (int i = 0; i < n; i++) {
			final Rack r = racks.get(i);
			newDxs[i] = r.getPoleDistanceX();
			r.setPoleDistanceX(oldDxs[i]);
			newDys[i] = r.getPoleDistanceY();
			r.setPoleDistanceY(oldDys[i]);
			newVisibles[i] = r.isPoleVisible();
			r.setPoleVisible(oldVisibles[i]);
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
			r.setPoleDistanceX(newDxs[i]);
			r.setPoleDistanceY(newDys[i]);
			r.setPoleVisible(newVisibles[i]);
			r.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Pole Settings for All Racks";
	}

}
