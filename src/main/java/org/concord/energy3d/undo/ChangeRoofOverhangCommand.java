package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Roof;
import org.concord.energy3d.scene.Scene;

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
		Scene.getInstance().redrawAll(); // can't just use Roof.draw() as we also need to draw the wall parts
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		roof.setOverhangLength(newValue);
		Scene.getInstance().redrawAll(); // can't just use Roof.draw() as we also need to draw the wall parts
	}

	@Override
	public String getPresentationName() {
		return "Overhang Change for Selected Roof";
	}

}
