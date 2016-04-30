package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

public class ChangeContainerWindowShgcCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double[] oldValues, newValues;
	private HousePart container;
	private List<Window> windows;

	public ChangeContainerWindowShgcCommand(HousePart container) {
		this.container = container;
		windows = Scene.getInstance().getWindowsOnContainer(container);
		int n = windows.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = windows.get(i).getSolarHeatGainCoefficient();
		}
	}

	public HousePart getContainer() {
		return container;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		int n = windows.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			newValues[i] = windows.get(i).getSolarHeatGainCoefficient();
			windows.get(i).setSolarHeatGainCoefficient(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		int n = windows.size();
		for (int i = 0; i < n; i++) {
			windows.get(i).setSolarHeatGainCoefficient(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "SHGC Change for All Windows on " + (container instanceof Wall ? "Wall" : "Roof");
	}

}
