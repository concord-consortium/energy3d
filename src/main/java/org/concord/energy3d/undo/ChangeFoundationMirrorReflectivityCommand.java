package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.Scene;

public class ChangeFoundationMirrorReflectivityCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double[] oldValues, newValues;
	private Foundation foundation;
	private List<Mirror> mirrors;

	public ChangeFoundationMirrorReflectivityCommand(Foundation foundation) {
		this.foundation = foundation;
		mirrors = Scene.getInstance().getMirrorsOfFoundation(foundation);
		int n = mirrors.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = mirrors.get(i).getReflectivity();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		int n = mirrors.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			newValues[i] = mirrors.get(i).getReflectivity();
			mirrors.get(i).setReflectivity(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		int n = mirrors.size();
		for (int i = 0; i < n; i++) {
			mirrors.get(i).setReflectivity(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "Reflectivity Change for All Mirrors on Selected Foundation";
	}

}
