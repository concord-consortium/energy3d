package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.Operation;

@SuppressWarnings("serial")
public class EditFoundationCommand extends EditHousePartCommand {
	private final Foundation foundation;
	private final boolean isResizeMode;

	public EditFoundationCommand(final Foundation foundation) {
		super(foundation);
		this.foundation = foundation;
		this.isResizeMode = SceneManager.getInstance().getOperation() == Operation.RESIZE;
	}

	@Override
	public void undo() throws CannotUndoException {
		foundation.setResizeHouseMode(isResizeMode);
		if (!isResizeMode)
			foundation.prepareForNotResizing();
		super.undo();
		foundation.complete();
		foundation.setResizeHouseMode(SceneManager.getInstance().getOperation() == Operation.RESIZE);
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		foundation.setResizeHouseMode(isResizeMode);
		if (!isResizeMode)
			foundation.prepareForNotResizing();
		super.redo();
		foundation.complete();
		foundation.setResizeHouseMode(SceneManager.getInstance().getOperation() == Operation.RESIZE);
		Scene.getInstance().redrawAll();
	}
}
