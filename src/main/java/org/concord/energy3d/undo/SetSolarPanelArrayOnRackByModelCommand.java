package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.simulation.PvModuleSpecs;

public class SetSolarPanelArrayOnRackByModelCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final PvModuleSpecs oldModel;
	private final double oldRackWidth, oldRackHeight;
	private PvModuleSpecs newModel;
	private double newRackWidth, newRackHeight;
	private final boolean oldRotated;
	private boolean newRotated;
	private final double oldInverterEfficiency;
	private double newInverterEfficiency;
	private final Rack rack;

	public SetSolarPanelArrayOnRackByModelCommand(final Rack rack) {
		this.rack = rack;
		final SolarPanel s = rack.getSolarPanel();
		oldModel = s.getPvModuleSpecs();
		oldRackWidth = rack.getRackWidth();
		oldRackHeight = rack.getRackHeight();
		oldRotated = s.isRotated();
		oldInverterEfficiency = s.getInverterEfficiency();
	}

	public Rack getRack() {
		return rack;
	}

	public PvModuleSpecs getOldModel() {
		return oldModel;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final SolarPanel s = rack.getSolarPanel();
		newModel = s.getPvModuleSpecs();
		newRackWidth = rack.getRackWidth();
		newRackHeight = rack.getRackHeight();
		newRotated = s.isRotated();
		newInverterEfficiency = s.getInverterEfficiency();
		s.setPvModuleSpecs(oldModel);
		s.setRotated(oldRotated);
		s.setInverterEfficiency(oldInverterEfficiency);
		rack.setRackWidth(oldRackWidth);
		rack.setRackHeight(oldRackHeight);
		rack.ensureFullSolarPanels(false);
		rack.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final SolarPanel s = rack.getSolarPanel();
		s.setPvModuleSpecs(newModel);
		s.setRotated(newRotated);
		s.setInverterEfficiency(newInverterEfficiency);
		rack.setRackWidth(newRackWidth);
		rack.setRackHeight(newRackHeight);
		rack.ensureFullSolarPanels(false);
		rack.draw();
	}

	@Override
	public String getPresentationName() {
		return "Set Solar Panel Array on Rack by Model";
	}

}
