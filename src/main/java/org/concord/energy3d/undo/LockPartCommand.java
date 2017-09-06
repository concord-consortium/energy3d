package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class LockPartCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldValue;
	private boolean newValue;
	private final HousePart part;

	public LockPartCommand(final HousePart part) {
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
				if (p.getTopContainer() == part) {
					p.setFreeze(oldValue);
					p.draw();
				}
			}
		}
		if (oldValue) {
			SceneManager.getInstance().hideAllEditPoints();
		}
		part.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		part.setFreeze(newValue);
		if (part instanceof Foundation) {
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p.getTopContainer() == part) {
					p.setFreeze(newValue);
					p.draw();
				}
			}
		}
		if (newValue) {
			SceneManager.getInstance().hideAllEditPoints();
		}
		part.draw();
	}

	@Override
	public String getPresentationName() {
		return "Lock";
	}

}
