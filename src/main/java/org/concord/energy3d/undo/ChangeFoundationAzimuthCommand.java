package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFoundationAzimuthCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldAzimuth;
	private double newAzimuth;
	private final Foundation foundation;

	public ChangeFoundationAzimuthCommand(final Foundation foundation) {
		this.foundation = foundation;
		oldAzimuth = foundation.getAzimuth();
	}

	public Foundation getFoundation() {
		return foundation;
	}

	public double getOldAzimuth() {
		return oldAzimuth;
	}

	public double getNewAzimuth() {
		return newAzimuth;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newAzimuth = foundation.getAzimuth();
		foundation.setAzimuth(oldAzimuth);
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		foundation.setAzimuth(newAzimuth);
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Azimuth for Selected Foundation";
	}

}
