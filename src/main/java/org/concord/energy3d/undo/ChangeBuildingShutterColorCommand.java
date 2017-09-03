package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangeBuildingShutterColorCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final ReadOnlyColorRGBA[] oldColors;
	private ReadOnlyColorRGBA[] newColors;
	private final Window window;
	private final List<HousePart> parts;

	public ChangeBuildingShutterColorCommand(final Window window) {
		this.window = window;
		parts = Scene.getInstance().getPartsOfSameTypeInBuilding(window);
		final int n = parts.size();
		oldColors = new ReadOnlyColorRGBA[n];
		for (int i = 0; i < n; i++) {
			oldColors[i] = ((Window) parts.get(i)).getShutterColor();
		}
	}

	public Window getWindow() {
		return window;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = parts.size();
		newColors = new ReadOnlyColorRGBA[n];
		for (int i = 0; i < n; i++) {
			final Window w = (Window) parts.get(i);
			newColors[i] = w.getShutterColor();
			w.setShutterColor(oldColors[i]);
			w.draw();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = parts.size();
		for (int i = 0; i < n; i++) {
			final Window w = (Window) parts.get(i);
			w.setShutterColor(newColors[i]);
			w.draw();
		}
	}

	@Override
	public String getPresentationName() {
		return "Shutter Color Change for Whole Building";
	}

}
