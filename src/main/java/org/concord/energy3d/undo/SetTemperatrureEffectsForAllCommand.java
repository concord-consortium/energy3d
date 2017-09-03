package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;

public class SetTemperatrureEffectsForAllCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double[] oldPmaxs;
	private double[] newPmaxs;
	private final double[] oldNocts;
	private double[] newNocts;
	private final List<SolarPanel> panels;

	public SetTemperatrureEffectsForAllCommand() {
		panels = Scene.getInstance().getAllSolarPanels();
		final int n = panels.size();
		oldPmaxs = new double[n];
		oldNocts = new double[n];
		SolarPanel p;
		for (int i = 0; i < n; i++) {
			p = panels.get(i);
			oldPmaxs[i] = p.getTemperatureCoefficientPmax();
			oldNocts[i] = p.getNominalOperatingCellTemperature();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = panels.size();
		newPmaxs = new double[n];
		newNocts = new double[n];
		SolarPanel p;
		for (int i = 0; i < n; i++) {
			p = panels.get(i);
			newPmaxs[i] = p.getTemperatureCoefficientPmax();
			p.setTemperatureCoefficientPmax(oldPmaxs[i]);
			newNocts[i] = p.getNominalOperatingCellTemperature();
			p.setNominalOperatingCellTemperature(oldNocts[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = panels.size();
		SolarPanel p;
		for (int i = 0; i < n; i++) {
			p = panels.get(i);
			p.setTemperatureCoefficientPmax(newPmaxs[i]);
			p.setNominalOperatingCellTemperature(newNocts[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "Temperature Effects Change for All Solar Panels";
	}

}
