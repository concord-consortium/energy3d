package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

public class SpinViewCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;

	public SpinViewCommand() {
		oldValue = SceneManager.getInstance().getSpinView();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = SceneManager.getInstance().getSpinView();
		SceneManager.getInstance().setSpinView(oldValue);
		Util.selectSilently(MainPanel.getInstance().getSpinViewButton(), oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		SceneManager.getInstance().setSpinView(newValue);
		Util.selectSilently(MainPanel.getInstance().getSpinViewButton(), newValue);
	}

	@Override
	public String getPresentationName() {
		return "Spin View";
	}

}
