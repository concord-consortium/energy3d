package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFoundationHeliostatTargetCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final Foundation[] oldValues;
	private Foundation[] newValues;
	private final Foundation foundation;
	private final List<Mirror> mirrors;

	public ChangeFoundationHeliostatTargetCommand(final Foundation foundation) {
		this.foundation = foundation;
		mirrors = foundation.getHeliostats();
		final int n = mirrors.size();
		oldValues = new Foundation[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = mirrors.get(i).getReceiver();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = mirrors.size();
		newValues = new Foundation[n];
		for (int i = 0; i < n; i++) {
			final Mirror m = mirrors.get(i);
			newValues[i] = m.getReceiver();
			m.setReceiver(oldValues[i]);
			m.draw();
			if (oldValues[i] != null) {
				oldValues[i].drawSolarReceiver();
			}
			if (newValues[i] != null) {
				newValues[i].drawSolarReceiver();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = mirrors.size();
		for (int i = 0; i < n; i++) {
			final Mirror m = mirrors.get(i);
			m.setReceiver(newValues[i]);
			m.draw();
			if (oldValues[i] != null) {
				oldValues[i].drawSolarReceiver();
			}
			if (newValues[i] != null) {
				newValues[i].drawSolarReceiver();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Target for All Mirrors on Selected Foundation";
	}

}
