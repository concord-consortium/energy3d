package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeAzimuthForAllFresnelReflectorsCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final List<FresnelReflector> reflectors;

	public ChangeAzimuthForAllFresnelReflectorsCommand() {
		reflectors = Scene.getInstance().getAllFresnelReflectors();
		final int n = reflectors.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = reflectors.get(i).getRelativeAzimuth();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = reflectors.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			final FresnelReflector r = reflectors.get(i);
			newValues[i] = r.getRelativeAzimuth();
			r.setRelativeAzimuth(oldValues[i]);
			r.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = reflectors.size();
		for (int i = 0; i < n; i++) {
			final FresnelReflector r = reflectors.get(i);
			r.setRelativeAzimuth(newValues[i]);
			r.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Azimuth for All Fresnel Reflectors";
	}

}
