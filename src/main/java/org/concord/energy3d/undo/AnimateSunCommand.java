package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

public class AnimateSunCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;

	public AnimateSunCommand() {
		oldValue = SceneManager.getInstance().isSunAnimation();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = SceneManager.getInstance().isSunAnimation();
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
