package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFoundationFresnelReflectorAbsorberCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final Foundation[] oldValues;
	private Foundation[] newValues;
	private final Foundation foundation;
	private final List<FresnelReflector> reflectors;

	public ChangeFoundationFresnelReflectorAbsorberCommand(final Foundation foundation) {
		this.foundation = foundation;
		reflectors = foundation.getFresnelReflectors();
		final int n = reflectors.size();
		oldValues = new Foundation[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = reflectors.get(i).getAbsorber();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = reflectors.size();
		newValues = new Foundation[n];
		for (int i = 0; i < n; i++) {
			final FresnelReflector r = reflectors.get(i);
			newValues[i] = r.getAbsorber();
			r.setAbsorber(oldValues[i]);
			r.draw();
			if (oldValues[i] != null) {
				oldValues[i].drawSolarReceiver();
			}
			if (newValues[i] != null) {
				newValues[i].drawSolarReceiver();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = reflectors.size();
		for (int i = 0; i < n; i++) {
			final FresnelReflector r = reflectors.get(i);
			r.setAbsorber(newValues[i]);
			r.draw();
			if (oldValues[i] != null) {
				oldValues[i].drawSolarReceiver();
			}
			if (newValues[i] != null) {
				newValues[i].drawSolarReceiver();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Absorber for All Fresnel Reflectors on Selected Foundation";
	}

}
