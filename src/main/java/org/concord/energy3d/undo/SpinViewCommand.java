package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

public class SpinViewCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldValue, newValue;

	public SpinViewCommand() {
		oldValue = SceneManager.getInstance().getSpinView();
		newValue = !oldValue;
	}

	public boolean getNewValue() {
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
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
