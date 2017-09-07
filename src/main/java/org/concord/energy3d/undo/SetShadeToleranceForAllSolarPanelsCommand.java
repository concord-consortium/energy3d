package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;

public class SetShadeToleranceForAllSolarPanelsCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int[] oldValues;
	private int[] newValues;
	private final List<SolarPanel> panels;

	public SetShadeToleranceForAllSolarPanelsCommand() {
		panels = Scene.getInstance().getAllSolarPanels();
		final int n = panels.size();
		oldValues = new int[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = panels.get(i).getShadeTolerance();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = panels.size();
		newValues = new int[n];
		for (int i = 0; i < n; i++) {
			final SolarPanel p = panels.get(i);
			newValues[i] = p.getShadeTolerance();
			p.setShadeTolerance(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = panels.size();
		for (int i = 0; i < n; i++) {
			panels.get(i).setShadeTolerance(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		switch (panels.get(0).getShadeTolerance()) {
		case SolarPanel.NO_SHADE_TOLERANCE:
			return "Choose No Shade Tolerance for All Solar Panels";
		case SolarPanel.HIGH_SHADE_TOLERANCE:
			return "Choose High Shade Tolerance for All Solar Panels";
		default:
			return "Choose Partial Shade Tolerance for All Solar Panels";
		}
	}

}
