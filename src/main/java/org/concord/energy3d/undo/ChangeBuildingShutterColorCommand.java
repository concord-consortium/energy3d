package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangeBuildingShutterColorCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private ReadOnlyColorRGBA[] oldColors, newColors;
	private Window window;
	private List<HousePart> parts;

	public ChangeBuildingShutterColorCommand(Window window) {
		this.window = window;
		parts = Scene.getInstance().getHousePartsOfSameTypeInBuilding(window);
		int n = parts.size();
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
		int n = parts.size();
		newColors = new ReadOnlyColorRGBA[n];
		for (int i = 0; i < n; i++) {
			Window w = (Window) parts.get(i);
			newColors[i] = w.getShutterColor();
			w.setShutterColor(oldColors[i]);
		}
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		int n = parts.size();
		for (int i = 0; i < n; i++) {
			((Window) parts.get(i)).setShutterColor(newColors[i]);
		}
		Scene.getInstance().redrawAll();
	}

	@Override
	public String getPresentationName() {
		return "Shutter Color Change for Whole Building";
	}

}
