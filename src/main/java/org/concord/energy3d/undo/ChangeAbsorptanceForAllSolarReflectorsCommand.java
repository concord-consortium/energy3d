package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarReflector;
import org.concord.energy3d.scene.Scene;

public class ChangeAbsorptanceForAllSolarReflectorsCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final List<SolarReflector> reflectors;

	public ChangeAbsorptanceForAllSolarReflectorsCommand(final Class<?> c) {
		reflectors = Scene.getInstance().getAllSolarReflectors(c);
		final int n = reflectors.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = reflectors.get(i).getAbsorptance();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = reflectors.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			newValues[i] = reflectors.get(i).getAbsorptance();
			reflectors.get(i).setAbsorptance(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = reflectors.size();
		for (int i = 0; i < n; i++) {
			reflectors.get(i).setAbsorptance(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "Absorptance Change for All " + reflectors.get(0).getClass().getSimpleName() + "s";
	}

}
