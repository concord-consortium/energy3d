package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;
import org.concord.energy3d.scene.Scene;

public class SetSolarPanelShadeToleranceForAllRacksCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int[] oldValues;
	private int[] newValues;
	private final List<Rack> racks;

	public SetSolarPanelShadeToleranceForAllRacksCommand() {
		racks = Scene.getInstance().getAllRacks();
		final int n = racks.size();
		oldValues = new int[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = racks.get(i).getSolarPanel().getShadeTolerance();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = racks.size();
		newValues = new int[n];
		for (int i = 0; i < n; i++) {
			final Rack r = racks.get(i);
			newValues[i] = r.getSolarPanel().getShadeTolerance();
			r.getSolarPanel().setShadeTolerance(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = racks.size();
		for (int i = 0; i < n; i++) {
			racks.get(i).getSolarPanel().setShadeTolerance(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "Set Solar Panel Shade Tolerance for All Racks";
	}

}