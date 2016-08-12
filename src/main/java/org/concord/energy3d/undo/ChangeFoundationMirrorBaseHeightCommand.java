package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFoundationMirrorBaseHeightCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double[] oldValues, newValues;
	private Foundation foundation;
	private List<Mirror> mirrors;

	public ChangeFoundationMirrorBaseHeightCommand(Foundation foundation) {
		this.foundation = foundation;
		mirrors = foundation.getMirrors();
		int n = mirrors.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = mirrors.get(i).getBaseHeight();
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
			Mirror m = mirrors.get(i);
			newValues[i] = m.getBaseHeight();
			m.setBaseHeight(oldValues[i]);
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
			m.setBaseHeight(newValues[i]);
			m.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Base Height for All Mirrors on Selected Foundation";
	}

}
