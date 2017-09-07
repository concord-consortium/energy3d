package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

public class AnimateSunCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldValue, newValue;

	public AnimateSunCommand() {
		oldValue = SceneManager.getInstance().isSunAnimation();
		newValue = !oldValue;
	}

	public boolean getNewValue() {
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		SceneManager.getInstance().setSunAnimation(oldValue);
		Util.selectSilently(MainPanel.getInstance().getSunAnimationButton(), oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		SceneManager.getInstance().setSunAnimation(newValue);
		Util.selectSilently(MainPanel.getInstance().getSunAnimationButton(), newValue);
	}

	@Override
	public String getPresentationName() {
		return "Animate Sun";
	}

}
