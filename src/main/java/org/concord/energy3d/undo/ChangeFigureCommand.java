package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Human;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFigureCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;
	private final Human human;

	public ChangeFigureCommand(final Human human) {
		this.human = human;
		oldValue = human.getHumanType();
	}

	public int getOldValue() {
		return oldValue;
	}

	public Human getHuman() {
		return human;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = human.getHumanType();
		human.setHumanType(oldValue);
		human.draw();
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		human.setHumanType(newValue);
		human.draw();
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Human Figure Change";
	}

}
