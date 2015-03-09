package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.ViewMode;
import org.concord.energy3d.util.Util;

public class TopViewCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private ViewMode oldValue, newValue;

	public TopViewCommand() {
		oldValue = SceneManager.getInstance().getViewMode();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = SceneManager.getInstance().getViewMode();
		SceneManager.getInstance().resetCamera(oldValue);
		Util.selectSilently(MainFrame.getInstance().getTopViewCheckBoxMenuItem(), oldValue == ViewMode.TOP_VIEW);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		SceneManager.getInstance().resetCamera(newValue);
		Util.selectSilently(MainFrame.getInstance().getTopViewCheckBoxMenuItem(), newValue == ViewMode.TOP_VIEW);
	}

	@Override
	public String getPresentationName() {
		return "Top View";
	}

}
