package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.SolarCollector;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFoundationSolarCollectorPoleHeightCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final Foundation foundation;
	private final List<SolarCollector> collectors;

	public ChangeFoundationSolarCollectorPoleHeightCommand(final Foundation foundation, final Class<?> c) {
		this.foundation = foundation;
		collectors = foundation.getSolarCollectors(c);
		final int n = collectors.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = collectors.get(i).getPoleHeight();
		}
	}

	public Foundation getFoundation() {
		return foundation;
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
			final SolarCollector c = collectors.get(i);
			newValues[i] = c.getPoleHeight();
			c.setPoleHeight(oldValues[i]);
			if (c instanceof HousePart) {
				((HousePart) c).draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = collectors.size();
		for (int i = 0; i < n; i++) {
			final SolarCollector c = collectors.get(i);
			c.setPoleHeight(newValues[i]);
			if (c instanceof HousePart) {
				((HousePart) c).draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Base Height for All " + getFirstSolarCollector().getClass().getSimpleName() + "s on Selected Foundation";
	}

}