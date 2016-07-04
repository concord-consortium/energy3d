package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Mirror;

public class SetMirrorSizeCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldWidth, newWidth;
	private double oldHeight, newHeight;
	private Mirror mirror;

	public SetMirrorSizeCommand(Mirror solarPanel) {
		this.mirror = solarPanel;
		oldWidth = solarPanel.getMirrorWidth();
		oldHeight = solarPanel.getMirrorHeight();
	}

	public Mirror getMirror() {
		return mirror;
	}

	public double getOldWidth() {
		return oldWidth;
	}

	public double getOldHeight() {
		return oldHeight;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newWidth = mirror.getMirrorWidth();
		newHeight = mirror.getMirrorHeight();
		mirror.setMirrorWidth(oldWidth);
		mirror.setMirrorHeight(oldHeight);
		mirror.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		mirror.setMirrorWidth(newWidth);
		mirror.setMirrorHeight(newHeight);
		mirror.draw();
	}

	@Override
	public String getPresentationName() {
		return "Set Size for Selected Mirror";
	}

}
