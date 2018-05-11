package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Window;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangeMuntinColorCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final ReadOnlyColorRGBA oldColor;
	private ReadOnlyColorRGBA newColor;
	private final Window window;

	public ChangeMuntinColorCommand(final Window window) {
		this.window = window;
		oldColor = window.getMuntinColor();
	}

	public Window getWindow() {
		return window;
	}

	public ReadOnlyColorRGBA getOldColor() {
		return oldColor;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newColor = window.getMuntinColor();
		window.setMuntinColor(oldColor);
		window.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		window.setMuntinColor(newColor);
		window.draw();
	}

	@Override
	public String getPresentationName() {
		return "Muntin Color Change for Selected Window";
	}

}
