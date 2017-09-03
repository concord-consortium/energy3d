package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.SceneManager;

public class ChangeTiltAngleForSolarPanelRowCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final List<SolarPanel> panels;

	public ChangeTiltAngleForSolarPanelRowCommand(final List<SolarPanel> panels) {
		this.panels = panels;
		final int n = panels.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = panels.get(i).getTiltAngle();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = panels.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			final SolarPanel p = panels.get(i);
			newValues[i] = p.getTiltAngle();
			p.setTiltAngle(oldValues[i]);
			p.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = panels.size();
		for (int i = 0; i < n; i++) {
			final SolarPanel p = panels.get(i);
			p.setTiltAngle(newValues[i]);
			p.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Tilt Angle for Solar Panel Row";
	}

}
