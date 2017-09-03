package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;

public class ChangeFoundationFresnelReflectorAbsorptanceCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final Foundation foundation;
	private final List<FresnelReflector> reflectors;

	public ChangeFoundationFresnelReflectorAbsorptanceCommand(final Foundation foundation) {
		this.foundation = foundation;
		reflectors = foundation.getFresnelReflectors();
		final int n = reflectors.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = reflectors.get(i).getAbsorptance();
		}
	}

	public Foundation getFoundation() {
		return foundation;
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
		return "Absorptance Change for All Fresnel Reflectors on Selected Foundation";
	}

}
