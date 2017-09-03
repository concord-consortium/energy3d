package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFoundationSizeCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double oldLength, oldWidth, oldHeight;
	private final double newLength;
	private final double newWidth;
	private final double newHeight;
	private final Foundation foundation;

	public ChangeFoundationSizeCommand(final Foundation foundation, final double oldLength, final double newLength, final double oldWidth, final double newWidth, final double oldHeight, final double newHeight) {
		this.foundation = foundation;
		this.oldLength = oldLength;
		this.newLength = newLength;
		this.oldWidth = oldWidth;
		this.newWidth = newWidth;
		this.oldHeight = oldHeight;
		this.newHeight = newHeight;
	}

	public Foundation getFoundation() {
		return foundation;
	}

	public double getOldLength() {
		return oldLength;
	}

	public double getNewLength() {
		return newLength;
	}

	public double getOldWidth() {
		return oldWidth;
	}

	public double getNewWidth() {
		return newWidth;
	}

	public double getOldHeight() {
		return oldHeight;
	}

	public double getNewHeight() {
		return newHeight;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		foundation.rescale(oldLength / newLength, oldWidth / newWidth, 1);
		foundation.setHeight(oldHeight / Scene.getInstance().getAnnotationScale());
		foundation.draw();
		foundation.drawChildren();
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		foundation.rescale(newLength / oldLength, newWidth / oldWidth, 1);
		foundation.setHeight(newHeight / Scene.getInstance().getAnnotationScale());
		foundation.draw();
		foundation.drawChildren();
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Size for Selected Foundation";
	}

}
