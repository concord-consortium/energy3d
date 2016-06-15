package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

public class RemoveMultipleShuttersCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final List<Window> windows;
	private final boolean[] leftShutters, rightShutters;

	public RemoveMultipleShuttersCommand(final List<Window> windows) {
		this.windows = windows;
		int n = windows.size();
		leftShutters = new boolean[n];
		rightShutters = new boolean[n];
		for (int i = 0; i < n; i++) {
			Window w = windows.get(i);
			leftShutters[i] = w.getLeftShutter();
			rightShutters[i] = w.getRightShutter();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for (int i = 0; i < windows.size(); i++) {
			Window w = windows.get(i);
			if (w.isDrawable()) { // as an extra defense of potential invisible ghost part
				w.setLeftShutter(leftShutters[i]);
				w.setRightShutter(rightShutters[i]);
			}
		}
		EnergyPanel.getInstance().clearRadiationHeatMap();
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		for (Window w : windows) {
			if (w.isDrawable()) { // as an extra defense of potential invisible ghost part
				w.setLeftShutter(false);
				w.setRightShutter(false);
			}
		}
		EnergyPanel.getInstance().clearRadiationHeatMap();
		Scene.getInstance().redrawAll();
	}

	@Override
	public String getPresentationName() {
		if (windows.isEmpty())
			return "Remove Nothing";
		return "Remove All Window Shutters";
	}

}
