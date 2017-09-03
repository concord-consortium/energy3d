package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.Scene;

public class ChangeReflectanceForAllMirrorsCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final List<Mirror> mirrors;

	public ChangeReflectanceForAllMirrorsCommand() {
		mirrors = Scene.getInstance().getAllMirrors();
		final int n = mirrors.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = mirrors.get(i).getReflectance();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = mirrors.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			newValues[i] = mirrors.get(i).getReflectance();
			mirrors.get(i).setReflectance(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = mirrors.size();
		for (int i = 0; i < n; i++) {
			mirrors.get(i).setReflectance(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "Reflectance Change for All Mirrors";
	}

}
