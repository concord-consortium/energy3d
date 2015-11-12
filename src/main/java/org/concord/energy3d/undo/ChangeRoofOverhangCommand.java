package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Roof;
import org.concord.energy3d.scene.Scene;

public class ChangeRoofOverhangCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double orgOverhangLength, newOverhangLength;
	private Roof selectedRoof;

	public ChangeRoofOverhangCommand(Roof selectedRoof) {
		this.selectedRoof = selectedRoof;
		orgOverhangLength = selectedRoof.getOverhangLength();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newOverhangLength = selectedRoof.getOverhangLength();
		selectedRoof.setOverhangLength(orgOverhangLength);
		Scene.getInstance().redrawAll(); // can't just use Roof.draw() as we also need to draw the wall parts
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		selectedRoof.setOverhangLength(newOverhangLength);
		Scene.getInstance().redrawAll(); // can't just use Roof.draw() as we also need to draw the wall parts
	}

	// for action logging
	public Roof getRoof() {
		return selectedRoof;
	}

	@Override
	public String getPresentationName() {
		return "Overhang Change for Selected Roof";
	}

}
