package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.SolarCollector;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeBaseHeightForAllSolarCollectorsCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final List<SolarCollector> collectors;

	public ChangeBaseHeightForAllSolarCollectorsCommand(final Class<?> c) {
		collectors = Scene.getInstance().getAllSolarCollectors(c);
		final int n = collectors.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = collectors.get(i).getBaseHeight();
		}
	}

	public SolarCollector getFirstSolarCollector() {
		return collectors.get(0);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = collectors.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			final SolarCollector t = collectors.get(i);
			newValues[i] = t.getBaseHeight();
			t.setBaseHeight(oldValues[i]);
			if (t instanceof HousePart) {
				((HousePart) t).draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = collectors.size();
		for (int i = 0; i < n; i++) {
			final SolarCollector t = collectors.get(i);
			t.setBaseHeight(newValues[i]);
			if (t instanceof HousePart) {
				((HousePart) t).draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Base Height for All " + collectors.get(0).getClass().getSimpleName() + "s";
	}

}
