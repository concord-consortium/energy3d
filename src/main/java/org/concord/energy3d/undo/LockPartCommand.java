package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class LockPartCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;
	private HousePart part;

	public LockPartCommand(HousePart part) {
		this.part = part;
		oldValue = part.isFrozen();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = part.isFrozen();
		part.setFreeze(oldValue);
		if (part instanceof Foundation) {
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p.getTopContainer() == part)
					p.setFreeze(oldValue);
			}
		}
		if (oldValue)
			SceneManager.getInstance().hideAllEditPoints();
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		part.setFreeze(newValue);
		if (part instanceof Foundation) {
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p.getTopContainer() == part)
					p.setFreeze(newValue);
			}
		}
		if (newValue)
			SceneManager.getInstance().hideAllEditPoints();
		Scene.getInstance().redrawAll();
	}

	@Override
	public String getPresentationName() {
		return "Lock";
	}

}
