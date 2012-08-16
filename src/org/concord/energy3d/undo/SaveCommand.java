package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;

public class SaveCommand extends AbstractUndoableEdit {
	private static final long serialVersionUID = 1L;
	private static boolean significant = false;

//	@Override
//	public void undo() throws CannotUndoException {
//		super.undo();
//		final UndoManager undoManager = SceneManager.getInstance().getUndoManager();
//		if (undoManager.canUndo()) {
//			undoManager.undo();
//			Scene.getInstance().setEdited(true);
//		} else
//			Scene.getInstance().setEdited(false);
//	}
//
//	@Override
//	public void redo() throws CannotRedoException {
//		super.redo();
//		final UndoManager undoManager = SceneManager.getInstance().getUndoManager();
//		if (undoManager.canRedo()) {
//			SceneManager.getInstance().getUndoManager().redo();
//			Scene.getInstance().setEdited(true);
//		} else
//			Scene.getInstance().setEdited(false);
//	}
//

	public static void setGloabalSignificant(final boolean significant) {
		SaveCommand.significant = significant;
	}

	@Override
	public boolean isSignificant() {
		return significant;
	}

}
