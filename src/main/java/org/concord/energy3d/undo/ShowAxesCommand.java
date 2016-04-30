package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

public class ShowAxesCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;

	public ShowAxesCommand() {
		oldValue = SceneManager.getInstance().areAxesVisible();
		newValue = !oldValue;
	}

	public boolean getNewValue() {
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		SceneManager.getInstance().setAxesVisible(oldValue);
		Util.selectSilently(MainFrame.getInstance().getAxesMenuItem(), oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		SceneManager.getInstance().setAxesVisible(newValue);
		Util.selectSilently(MainFrame.getInstance().getAxesMenuItem(), newValue);
	}

	@Override
	public String getPresentationName() {
		return "Show Axes";
	}

}
