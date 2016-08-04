package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFoundationMirrorTargetCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private Foundation[] oldValues, newValues;
	private Foundation foundation;
	private List<Mirror> mirrors;

	public ChangeFoundationMirrorTargetCommand(Foundation foundation) {
		this.foundation = foundation;
		mirrors = foundation.getMirrors();
		int n = mirrors.size();
		oldValues = new Foundation[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = mirrors.get(i).getHeliostatTarget();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		int n = mirrors.size();
		newValues = new Foundation[n];
		for (int i = 0; i < n; i++) {
			Mirror m = mirrors.get(i);
			newValues[i] = m.getHeliostatTarget();
			m.setHeliostatTarget(oldValues[i]);
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
			m.setHeliostatTarget(newValues[i]);
			m.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Target for All Mirrors on Selected Foundation";
	}

}
