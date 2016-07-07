package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeHeliostatForAllMirrorsCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private int[] oldValues, newValues;
	private List<Mirror> mirrors;

	public ChangeHeliostatForAllMirrorsCommand() {
		mirrors = Scene.getInstance().getAllMirrors();
		int n = mirrors.size();
		oldValues = new int[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = mirrors.get(i).getHeliostatType();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		int n = mirrors.size();
		newValues = new int[n];
		for (int i = 0; i < n; i++) {
			Mirror m = mirrors.get(i);
			newValues[i] = m.getHeliostatType();
			m.setHeliostatType(oldValues[i]);
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
			m.setHeliostatType(newValues[i]);
			m.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Heliostat for All Mirrors";
	}

}
