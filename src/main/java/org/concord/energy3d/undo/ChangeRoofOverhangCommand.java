package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.scene.SceneManager;

public class ChangeRoofOverhangCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldValue, newValue;
	private Roof roof;

	public ChangeRoofOverhangCommand(Roof roof) {
		this.roof = roof;
		oldValue = roof.getOverhangLength();
	}

	public Roof getRoof() {
		return roof;
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = roof.getOverhangLength();
		roof.setOverhangLength(oldValue);
		roof.draw();
		// can't just use Roof.draw() as we also need to draw the wall parts
		Foundation f = roof.getTopContainer();
		f.drawChildren();
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		roof.setOverhangLength(newValue);
		roof.draw();
		// can't just use Roof.draw() as we also need to draw the wall parts
		Foundation f = roof.getTopContainer();
		f.drawChildren();
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Overhang Change for Selected Roof";
	}

}
