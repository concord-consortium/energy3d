package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class RotateSolarPanelsOnAllRacksCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean[] oldValues;
	private boolean[] newValues;
	private final List<Rack> racks;

	public RotateSolarPanelsOnAllRacksCommand() {
		racks = Scene.getInstance().getAllRacks();
		final int n = racks.size();
		oldValues = new boolean[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = racks.get(i).getSolarPanel().isRotated();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = racks.size();
		newValues = new boolean[n];
		for (int i = 0; i < n; i++) {
			final Rack r = racks.get(i);
			newValues[i] = r.getSolarPanel().isRotated();
			r.getSolarPanel().setRotated(oldValues[i]);
			r.ensureFullSolarPanels(false);
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
			r.getSolarPanel().setRotated(newValues[i]);
			r.ensureFullSolarPanels(false);
			r.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Rotate Solar Panels for All Racks";
	}

}
