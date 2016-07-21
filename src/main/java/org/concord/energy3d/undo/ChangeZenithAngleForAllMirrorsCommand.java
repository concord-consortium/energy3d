package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeZenithAngleForAllMirrorsCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double[] oldValues, newValues;
	private List<Mirror> mirrors;

	public ChangeZenithAngleForAllMirrorsCommand() {
		mirrors = Scene.getInstance().getAllMirrors();
		int n = mirrors.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = mirrors.get(i).getZenith();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		int n = mirrors.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			Mirror m = mirrors.get(i);
			newValues[i] = m.getZenith();
			m.setZenith(oldValues[i]);
			m.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		int n = mirrors.size();
		for (int i = 0; i < n; i++) {
			Mirror m = mirrors.get(i);
			m.setZenith(newValues[i]);
			m.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Zenith Angle for All Mirrors";
	}

}