package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.SolarPanel;

public class SetShadeToleranceForSolarPanelsOnFoundationCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final int[] oldValues;
	private int[] newValues;
	private final Foundation foundation;
	private final List<SolarPanel> solarPanels;

	public SetShadeToleranceForSolarPanelsOnFoundationCommand(final Foundation foundation) {
		this.foundation = foundation;
		solarPanels = foundation.getSolarPanels();
		final int n = solarPanels.size();
		oldValues = new int[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = solarPanels.get(i).getShadeTolerance();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = solarPanels.size();
		newValues = new int[n];
		for (int i = 0; i < n; i++) {
			final SolarPanel s = solarPanels.get(i);
			newValues[i] = s.getShadeTolerance();
			s.setShadeTolerance(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = solarPanels.size();
		for (int i = 0; i < n; i++) {
			solarPanels.get(i).setShadeTolerance(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		switch (solarPanels.get(0).getShadeTolerance()) {
		case SolarPanel.NO_SHADE_TOLERANCE:
			return "Choose No Shade Tolerance for All Solar Panels on Selected Foundation";
		case SolarPanel.HIGH_SHADE_TOLERANCE:
			return "Choose High Shade Tolerance for All Solar Panels on Selected Foundation";
		default:
			return "Choose Partial Shade Tolerance for All Solar Panels on Selected Foundation";
		}
	}

}
