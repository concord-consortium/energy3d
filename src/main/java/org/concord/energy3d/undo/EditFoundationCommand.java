package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.Operation;

@SuppressWarnings("serial")
public class EditFoundationCommand extends EditHousePartCommand {
	private final Foundation foundation;
	private final boolean isResizeMode;

	public EditFoundationCommand(final Foundation foundation) {
		super(foundation);
		this.foundation = foundation;
		isResizeMode = SceneManager.getInstance().getOperation() == Operation.RESIZE;
	}

	@Override
	public void undo() throws CannotUndoException {
		foundation.setResizeHouseMode(isResizeMode);
		if (!isResizeMode)
			foundation.prepareForNotResizing();
		super.undo();
		try {
			foundation.complete();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		foundation.setResizeHouseMode(SceneManager.getInstance().getOperation() == Operation.RESIZE);
	}

	@Override
	public void redo() throws CannotRedoException {
		foundation.setResizeHouseMode(isResizeMode);
		if (!isResizeMode)
			foundation.prepareForNotResizing();
		super.redo();
		try {
			foundation.complete();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		foundation.setResizeHouseMode(SceneManager.getInstance().getOperation() == Operation.RESIZE);
	}
}