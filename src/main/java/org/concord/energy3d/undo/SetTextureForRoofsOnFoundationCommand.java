package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.scene.SceneManager;

public class SetTextureForRoofsOnFoundationCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int[] oldTextureTypes;
	private int[] newTextureTypes;
	private final Foundation foundation;
	private final List<Roof> roofs;

	public SetTextureForRoofsOnFoundationCommand(final Foundation foundation) {
		this.foundation = foundation;
		roofs = foundation.getRoofs();
		final int n = roofs.size();
		oldTextureTypes = new int[n];
		for (int i = 0; i < n; i++) {
			oldTextureTypes[i] = roofs.get(i).getTextureType();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = roofs.size();
		newTextureTypes = new int[n];
		for (int i = 0; i < n; i++) {
			final Roof r = roofs.get(i);
			newTextureTypes[i] = r.getTextureType();
			r.setTextureType(oldTextureTypes[i]);
			r.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = roofs.size();
		for (int i = 0; i < n; i++) {
			final Roof r = roofs.get(i);
			r.setTextureType(newTextureTypes[i]);
			r.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Set Texture for All Roofs on Selected Foundation";
	}

}
