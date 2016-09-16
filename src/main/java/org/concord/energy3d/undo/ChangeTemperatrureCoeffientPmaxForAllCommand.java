package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;

public class ChangeTemperatrureCoeffientPmaxForAllCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final List<SolarPanel> panels;

	public ChangeTemperatrureCoeffientPmaxForAllCommand() {
		panels = Scene.getInstance().getAllSolarPanels();
		final int n = panels.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = panels.get(i).getTemperatureCoefficientPmax();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = panels.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			newValues[i] = panels.get(i).getTemperatureCoefficientPmax();
			panels.get(i).setTemperatureCoefficientPmax(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = panels.size();
		for (int i = 0; i < n; i++) {
			panels.get(i).setTemperatureCoefficientPmax(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "Temperature Coefficient of Pmax Change for All Solar Panels";
	}

}
