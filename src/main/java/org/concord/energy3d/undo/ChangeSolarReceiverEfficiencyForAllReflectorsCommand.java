package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.SolarReflector;
import org.concord.energy3d.scene.Scene;

public class ChangeSolarReceiverEfficiencyForAllReflectorsCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final List<SolarReflector> reflectors;

	public ChangeSolarReceiverEfficiencyForAllReflectorsCommand(final Class<?> c) {
		reflectors = Scene.getInstance().getAllSolarReflectors(c);
		final int n = reflectors.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			final SolarReflector r = reflectors.get(i);
			if (r instanceof HousePart) {
				oldValues[i] = (((HousePart) r).getTopContainer()).getSolarReceiverEfficiency();
			}
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = reflectors.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			final SolarReflector r = reflectors.get(i);
			if (r instanceof HousePart) {
				final Foundation f = ((HousePart) r).getTopContainer();
				newValues[i] = f.getSolarReceiverEfficiency();
				f.setSolarReceiverEfficiency(oldValues[i]);
			}
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = reflectors.size();
		for (int i = 0; i < n; i++) {
			final SolarReflector r = reflectors.get(i);
			if (r instanceof HousePart) {
				final Foundation f = ((HousePart) r).getTopContainer();
				f.setSolarReceiverEfficiency(newValues[i]);
			}
		}
	}

	@Override
	public String getPresentationName() {
		return "Solar Receiver Efficiency Change for All Reflectors";
	}

}
