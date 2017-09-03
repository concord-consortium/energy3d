package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.ParabolicTrough;

public class ChangeFoundationParabolicTroughAbsorptanceCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final Foundation foundation;
	private final List<ParabolicTrough> troughs;

	public ChangeFoundationParabolicTroughAbsorptanceCommand(final Foundation foundation) {
		this.foundation = foundation;
		troughs = foundation.getParabolicTroughs();
		final int n = troughs.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = troughs.get(i).getAbsorptance();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = troughs.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			newValues[i] = troughs.get(i).getAbsorptance();
			troughs.get(i).setAbsorptance(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = troughs.size();
		for (int i = 0; i < n; i++) {
			troughs.get(i).setAbsorptance(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "Absorptance Change for All Parabolic Troughs on Selected Foundation";
	}

}
