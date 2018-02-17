package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.SceneManager;

public class RemoveMultipleShuttersCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final List<Window> windows;
	private final boolean[] leftShutters, rightShutters;

	public RemoveMultipleShuttersCommand(final List<Window> windows) {
		this.windows = windows;
		final int n = windows.size();
		leftShutters = new boolean[n];
		rightShutters = new boolean[n];
		for (int i = 0; i < n; i++) {
			final Window w = windows.get(i);
			leftShutters[i] = w.getLeftShutter();
			rightShutters[i] = w.getRightShutter();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for (int i = 0; i < windows.size(); i++) {
			final Window w = windows.get(i);
			if (w.isDrawable()) { // as an extra defense of potential invisible ghost part
				w.setLeftShutter(leftShutters[i]);
				w.setRightShutter(rightShutters[i]);
				w.draw();
			}
		}
		if (SceneManager.getInstance().getSolarHeatMap()) {
			EnergyPanel.getInstance().updateRadiationHeatMap();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		for (final Window w : windows) {
			if (w.isDrawable()) { // as an extra defense of potential invisible ghost part
				w.setLeftShutter(false);
				w.setRightShutter(false);
				w.draw();
			}
		}
		if (SceneManager.getInstance().getSolarHeatMap()) {
			EnergyPanel.getInstance().updateRadiationHeatMap();
		}
	}

	@Override
	public String getPresentationName() {
		if (windows.isEmpty()) {
			return "Remove Nothing";
		}
		return "Remove All Window Shutters";
	}

}
