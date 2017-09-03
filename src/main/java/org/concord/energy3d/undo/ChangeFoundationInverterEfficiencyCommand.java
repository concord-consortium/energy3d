package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.SolarPanel;

public class ChangeFoundationInverterEfficiencyCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final Foundation foundation;
	private final List<SolarPanel> panels;

	public ChangeFoundationInverterEfficiencyCommand(final Foundation foundation) {
		this.foundation = foundation;
		panels = foundation.getSolarPanels();
		final int n = panels.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = panels.get(i).getInverterEfficiency();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = panels.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			newValues[i] = panels.get(i).getInverterEfficiency();
			panels.get(i).setInverterEfficiency(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = panels.size();
		for (int i = 0; i < n; i++) {
			panels.get(i).setInverterEfficiency(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "Inverter Efficiency Change for All Solar Panels on Selected Foundation";
	}

}
