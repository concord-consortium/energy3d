package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.PvModuleSpecs;

public class ChangeFoundationSolarPanelModelCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final PvModuleSpecs[] oldModels;
	private PvModuleSpecs[] newModels;
	private final Foundation foundation;
	private final List<SolarPanel> panels;

	public ChangeFoundationSolarPanelModelCommand(final Foundation foundation) {
		this.foundation = foundation;
		panels = foundation.getSolarPanels();
		final int n = panels.size();
		oldModels = new PvModuleSpecs[n];
		for (int i = 0; i < n; i++) {
			oldModels[i] = panels.get(i).getPvModuleSpecs();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = panels.size();
		newModels = new PvModuleSpecs[n];
		for (int i = 0; i < n; i++) {
			final SolarPanel p = panels.get(i);
			newModels[i] = p.getPvModuleSpecs();
			p.setPvModuleSpecs(oldModels[i]);
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
			p.setPvModuleSpecs(newModels[i]);
			p.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Model for All Solar Panels on Selected Foundation";
	}

}
