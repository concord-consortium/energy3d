package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.SceneManager;

public class SetSizeForWindowsOnFoundationCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldWidths;
	private double[] newWidths;
	private final double[] oldHeights;
	private double[] newHeights;
	private final Foundation foundation;
	private final List<Window> windows;

	public SetSizeForWindowsOnFoundationCommand(final Foundation foundation) {
		this.foundation = foundation;
		windows = foundation.getWindows();
		final int n = windows.size();
		oldWidths = new double[n];
		oldHeights = new double[n];
		for (int i = 0; i < n; i++) {
			final Window w = windows.get(i);
			oldWidths[i] = w.getWindowWidth();
			oldHeights[i] = w.getWindowHeight();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = windows.size();
		newWidths = new double[n];
		newHeights = new double[n];
		for (int i = 0; i < n; i++) {
			final Window w = windows.get(i);
			newWidths[i] = w.getWindowWidth();
			newHeights[i] = w.getWindowHeight();
			w.setWindowWidth(oldWidths[i]);
			w.setWindowHeight(oldHeights[i]);
			w.draw();
			w.getContainer().draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = windows.size();
		for (int i = 0; i < n; i++) {
			final Window w = windows.get(i);
			w.setWindowWidth(newWidths[i]);
			w.setWindowHeight(newHeights[i]);
			w.draw();
			w.getContainer().draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Set Size for All Windows of Selected Building";
	}

}
