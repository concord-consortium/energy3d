package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

public class ChangeBuildingWindowShgcCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final Foundation foundation;
	private final List<Window> windows;

	public ChangeBuildingWindowShgcCommand(final Foundation foundation) {
		this.foundation = foundation;
		windows = Scene.getInstance().getWindowsOfBuilding(foundation);
		final int n = windows.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = windows.get(i).getSolarHeatGainCoefficient();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = windows.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			newValues[i] = windows.get(i).getSolarHeatGainCoefficient();
			windows.get(i).setSolarHeatGainCoefficient(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = windows.size();
		for (int i = 0; i < n; i++) {
			windows.get(i).setSolarHeatGainCoefficient(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "SHGC Change for All Windows of Selected Building";
	}

}
