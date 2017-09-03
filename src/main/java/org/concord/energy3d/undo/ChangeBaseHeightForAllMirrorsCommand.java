package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeBaseHeightForAllMirrorsCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final List<Mirror> mirrors;

	public ChangeBaseHeightForAllMirrorsCommand() {
		mirrors = Scene.getInstance().getAllMirrors();
		final int n = mirrors.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = mirrors.get(i).getBaseHeight();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = mirrors.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			final Mirror m = mirrors.get(i);
			newValues[i] = m.getBaseHeight();
			m.setBaseHeight(oldValues[i]);
			m.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = mirrors.size();
		for (int i = 0; i < n; i++) {
			final Mirror m = mirrors.get(i);
			m.setBaseHeight(newValues[i]);
			m.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Base Height for All Mirrors";
	}

}
