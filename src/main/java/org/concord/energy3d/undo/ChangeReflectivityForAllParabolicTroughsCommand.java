package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.scene.Scene;

public class ChangeReflectivityForAllParabolicTroughsCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final List<ParabolicTrough> troughs;

	public ChangeReflectivityForAllParabolicTroughsCommand() {
		troughs = Scene.getInstance().getAllParabolicTroughs();
		final int n = troughs.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = troughs.get(i).getReflectivity();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = troughs.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			newValues[i] = troughs.get(i).getReflectivity();
			troughs.get(i).setReflectivity(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = troughs.size();
		for (int i = 0; i < n; i++) {
			troughs.get(i).setReflectivity(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "Reflectivity Change for All Parabolic Troughs";
	}

}
