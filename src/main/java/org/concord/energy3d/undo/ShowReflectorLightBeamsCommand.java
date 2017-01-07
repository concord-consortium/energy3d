package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;

public class ShowReflectorLightBeamsCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldValue, newValue;

	public ShowReflectorLightBeamsCommand() {
		oldValue = Scene.getInstance().areLightBeamsVisible();
		newValue = !oldValue;
	}

	public boolean getNewValue() {
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		Scene.getInstance().setLightBeamsVisible(oldValue);
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setLightBeamsVisible(newValue);
		Scene.getInstance().redrawAll();
	}

	@Override
	public String getPresentationName() {
		return "Show Reflector Light Beams";
	}

}
