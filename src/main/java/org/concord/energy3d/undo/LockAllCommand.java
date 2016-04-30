package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class LockAllCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean[] oldValues;

	public LockAllCommand() {
		int n = Scene.getInstance().getParts().size();
		oldValues = new boolean[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = Scene.getInstance().getParts().get(i).isFrozen();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		int n = Scene.getInstance().getParts().size();
		boolean b = false;
		for (int i = 0; i < n; i++) {
			Scene.getInstance().getParts().get(i).setFreeze(oldValues[i]);
			if (oldValues[i])
				b = true;
		}
		if (b)
			SceneManager.getInstance().hideAllEditPoints();
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		int n = Scene.getInstance().getParts().size();
		for (int i = 0; i < n; i++) {
			Scene.getInstance().getParts().get(i).setFreeze(true);
		}
		SceneManager.getInstance().hideAllEditPoints();
		Scene.getInstance().redrawAll();
	}

	@Override
	public String getPresentationName() {
		return "Lock All";
	}

}
