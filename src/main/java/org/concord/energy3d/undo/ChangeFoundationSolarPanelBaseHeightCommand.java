package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFoundationSolarPanelBaseHeightCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final Foundation foundation;
	private final List<SolarPanel> panels;

	public ChangeFoundationSolarPanelBaseHeightCommand(final Foundation foundation) {
		this.foundation = foundation;
		panels = foundation.getSolarPanels();
		final int n = panels.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = panels.get(i).getBaseHeight();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = panels.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			final SolarPanel p = panels.get(i);
			newValues[i] = p.getBaseHeight();
			p.setBaseHeight(oldValues[i]);
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
			p.setBaseHeight(newValues[i]);
			p.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Base Height for All Solar Panels on Selected Foundation";
	}

}
