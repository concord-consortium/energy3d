package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeContainerWindowSizeCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldWidths, oldHeights;
	private double[] newWidths, newHeights;
	private final HousePart container;
	private final List<Window> windows;

	public ChangeContainerWindowSizeCommand(final HousePart container) {
		this.container = container;
		windows = Scene.getInstance().getWindowsOnContainer(container);
		final int n = windows.size();
		oldWidths = new double[n];
		oldHeights = new double[n];
		for (int i = 0; i < n; i++) {
			oldWidths[i] = windows.get(i).getWindowWidth();
			oldHeights[i] = windows.get(i).getWindowHeight();
		}
	}

	public HousePart getContainer() {
		return container;
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
		}
		container.draw();
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
		}
		container.draw();
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Size Change for All Windows on " + (container instanceof Wall ? "Wall" : "Roof");
	}

}
