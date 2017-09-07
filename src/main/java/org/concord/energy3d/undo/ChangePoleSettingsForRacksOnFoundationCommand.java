package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.scene.SceneManager;

public class ChangePoleSettingsForRacksOnFoundationCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldDxs;
	private double[] newDxs;
	private final double[] oldDys;
	private double[] newDys;
	private final boolean[] oldVisibles;
	private boolean[] newVisibles;
	private final Foundation foundation;
	private final List<Rack> racks;

	public ChangePoleSettingsForRacksOnFoundationCommand(final Foundation foundation) {
		this.foundation = foundation;
		racks = foundation.getRacks();
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

	public Foundation getFoundation() {
		return foundation;
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
			newDys[i] = r.getPoleDistanceY();
			newVisibles[i] = r.isPoleVisible();
			r.setPoleDistanceX(oldDxs[i]);
			r.setPoleDistanceY(oldDys[i]);
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
		return "Change Pole Settings for All Racks on Selected Foundation";
	}

}
