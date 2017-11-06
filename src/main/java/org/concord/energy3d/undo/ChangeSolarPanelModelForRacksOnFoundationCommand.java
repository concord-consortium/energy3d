package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.PvModuleSpecs;

public class ChangeSolarPanelModelForRacksOnFoundationCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final PvModuleSpecs[] oldModels;
	private PvModuleSpecs[] newModels;
	private final double[] oldWidths;
	private double[] newWidths;
	private final double[] oldHeights;
	private double[] newHeights;
	private final Foundation foundation;
	private final List<Rack> racks;

	public ChangeSolarPanelModelForRacksOnFoundationCommand(final Foundation foundation) {
		this.foundation = foundation;
		racks = foundation.getRacks();
		final int n = racks.size();
		oldWidths = new double[n];
		oldHeights = new double[n];
		oldModels = new PvModuleSpecs[n];
		for (int i = 0; i < n; i++) {
			final Rack r = racks.get(i);
			oldWidths[i] = r.getRackWidth();
			oldHeights[i] = r.getRackHeight();
			final SolarPanel s = r.getSolarPanel();
			oldModels[i] = s.getPvModuleSpecs();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = racks.size();
		newWidths = new double[n];
		newHeights = new double[n];
		newModels = new PvModuleSpecs[n];
		for (int i = 0; i < n; i++) {
			final Rack r = racks.get(i);
			newWidths[i] = r.getRackWidth();
			newHeights[i] = r.getRackHeight();
			final SolarPanel s = r.getSolarPanel();
			newModels[i] = s.getPvModuleSpecs();
			s.setPvModuleSpecs(oldModels[i]);
			r.setRackWidth(oldWidths[i]);
			r.setRackHeight(oldHeights[i]);
			r.ensureFullSolarPanels(false);
			r.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = racks.size();
		for (int i = 0; i < n; i++) {
			final Rack r = racks.get(i);
			r.setRackWidth(newWidths[i]);
			r.setRackHeight(newHeights[i]);
			final SolarPanel s = r.getSolarPanel();
			s.setPvModuleSpecs(newModels[i]);
			r.ensureFullSolarPanels(false);
			r.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Set Solar Panel Model for All Racks on Selected Foundation";
	}

}
