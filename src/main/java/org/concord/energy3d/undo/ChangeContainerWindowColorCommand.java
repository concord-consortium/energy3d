package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangeContainerWindowColorCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private ReadOnlyColorRGBA[] orgColor, newColor;
	private HousePart container;
	private List<Window> windows;

	public ChangeContainerWindowColorCommand(HousePart container) {
		this.container = container;
		windows = Scene.getInstance().getWindowsOnContainer(container);
		int n = windows.size();
		orgColor = new ColorRGBA[n];
		for (int i = 0; i < n; i++) {
			orgColor[i] = windows.get(i).getColor();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		int n = windows.size();
		newColor = new ColorRGBA[n];
		for (int i = 0; i < n; i++) {
			newColor[i] = windows.get(i).getColor();
			windows.get(i).setColor(orgColor[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		int n = windows.size();
		for (int i = 0; i < n; i++) {
			windows.get(i).setColor(newColor[i]);
		}
	}

	// for action logging
	public HousePart getContainer() {
		return container;
	}
	
	@Override
	public String getPresentationName() {
		return "Color Change for All Windows on " + (container instanceof Wall ? "Wall" : "Roof");
	}

}
