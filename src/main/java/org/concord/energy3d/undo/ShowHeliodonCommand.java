package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

public class ShowHeliodonCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final boolean oldValue, newValue;

	public ShowHeliodonCommand() {
		oldValue = SceneManager.getInstance().isHeliodonVisible();
		newValue = !oldValue;
	}

	public boolean getNewValue() {
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		SceneManager.getInstance().setHeliodonVisible(oldValue);
		Util.selectSilently(MainPanel.getInstance().getHeliodonButton(), oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		SceneManager.getInstance().setHeliodonVisible(newValue);
		Util.selectSilently(MainPanel.getInstance().getHeliodonButton(), newValue);
	}

	@Override
	public String getPresentationName() {
		return "Show Heliodon";
	}

}
