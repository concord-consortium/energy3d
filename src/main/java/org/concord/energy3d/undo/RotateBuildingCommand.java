package org.concord.energy3d.undo;

import java.util.concurrent.Callable;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.SceneManager;

public class RotateBuildingCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final Foundation foundation;
	private final double rotationAngle;

	public RotateBuildingCommand(final Foundation foundation, final double rotationAngle) {
		this.foundation = foundation;
		this.rotationAngle = rotationAngle;
	}

	public Foundation getFoundation() {
		return foundation;
	}

	public double getRotationAngle() {
		return rotationAngle;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		rotate(-rotationAngle);
		if (SceneManager.getInstance().getSolarHeatMap()) {
			EnergyPanel.getInstance().updateRadiationHeatMap();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		rotate(rotationAngle);
		if (SceneManager.getInstance().getSolarHeatMap()) {
			EnergyPanel.getInstance().updateRadiationHeatMap();
		}
	}

	private void rotate(final double a) {
		SceneManager.getInstance().setSelectedPart(foundation);
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				if (foundation != null) {
					SceneManager.getInstance().rotateBuilding(a, true);
				} else {
					SceneManager.getInstance().rotateAllBuildings(a);
				}
				return null;
			}
		});
	}

	@Override
	public char getOneLetterCode() {
		return 'Z';
	}

	@Override
	public String getPresentationName() {
		return "Rotate Building";
	}

}
