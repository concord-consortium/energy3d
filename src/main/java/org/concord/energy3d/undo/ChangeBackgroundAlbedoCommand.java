package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;

public class ChangeBackgroundAlbedoCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double orgAlbedo, newAlbedo;

	public ChangeBackgroundAlbedoCommand() {
		orgAlbedo = Scene.getInstance().getGround().getAlbedo();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newAlbedo = Scene.getInstance().getGround().getAlbedo();
		Scene.getInstance().getGround().setAlbedo(orgAlbedo);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().getGround().setAlbedo(newAlbedo);
	}

	@Override
	public String getPresentationName() {
		return "Change Ground Albedo";
	}

}
