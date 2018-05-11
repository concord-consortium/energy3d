package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangeContainerMuntinColorCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final ReadOnlyColorRGBA[] oldColors;
	private ReadOnlyColorRGBA[] newColors;
	private final HousePart container;
	private final List<Window> windows;

	public ChangeContainerMuntinColorCommand(final HousePart container) {
		this.container = container;
		windows = Scene.getInstance().getWindowsOnContainer(container);
		final int n = windows.size();
		oldColors = new ColorRGBA[n];
		for (int i = 0; i < n; i++) {
			oldColors[i] = windows.get(i).getMuntinColor();
		}
	}

	public HousePart getContainer() {
		return container;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = windows.size();
		newColors = new ColorRGBA[n];
		for (int i = 0; i < n; i++) {
			final Window w = windows.get(i);
			newColors[i] = windows.get(i).getMuntinColor();
			w.setMuntinColor(oldColors[i]);
			w.draw();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = windows.size();
		for (int i = 0; i < n; i++) {
			final Window w = windows.get(i);
			w.setMuntinColor(newColors[i]);
			w.draw();
		}
	}

	@Override
	public String getPresentationName() {
		return "Muntin Color Change for All Windows on Container";
	}

}
