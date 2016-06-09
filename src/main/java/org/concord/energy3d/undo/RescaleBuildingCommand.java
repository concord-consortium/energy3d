package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.Scene;

public class RescaleBuildingCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldXLength, newXLength;
	private double oldYLength, newYLength;
	private double oldZLength, newZLength;
	private Foundation foundation;

	public RescaleBuildingCommand(Foundation foundation, double oldXLength, double newXLength, double oldYLength, double newYLength, double oldZLength, double newZLength) {
		this.foundation = foundation;
		this.oldXLength = oldXLength;
		this.newXLength = newXLength;
		this.oldYLength = oldYLength;
		this.newYLength = newYLength;
		this.oldZLength = oldZLength;
		this.newZLength = newZLength;
	}

	public Foundation getFoundation() {
		return foundation;
	}

	public double getOldXLength() {
		return oldXLength;
	}

	public double getNewXLength() {
		return newXLength;
	}

	public double getOldYLength() {
		return oldYLength;
	}

	public double getNewYLength() {
		return newYLength;
	}

	public double getOldZLength() {
		return oldZLength;
	}

	public double getNewZLength() {
		return newZLength;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		foundation.rescale(oldXLength / newXLength, oldYLength / newYLength, oldZLength / newZLength);
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		foundation.rescale(newXLength / oldXLength, newYLength / oldYLength, newZLength / oldZLength);
		Scene.getInstance().redrawAll();
	}

	@Override
	public String getPresentationName() {
		return "Rescale Building";
	}

}
