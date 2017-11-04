package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;
import org.concord.energy3d.simulation.PvModuleSpecs;

public class ChooseSolarPanelModelForRackCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final PvModuleSpecs oldModel;
	private PvModuleSpecs newModel;
	private final double oldRackWidth, oldRackHeight;
	private double newRackWidth, newRackHeight;
	private final Rack rack;

	public ChooseSolarPanelModelForRackCommand(final Rack rack) {
		this.rack = rack;
		oldModel = rack.getSolarPanel().getPvModuleSpecs();
		oldRackWidth = rack.getRackWidth();
		oldRackHeight = rack.getRackHeight();
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
		newModel = rack.getSolarPanel().getPvModuleSpecs();
		newRackWidth = rack.getRackWidth();
		newRackHeight = rack.getRackHeight();
		rack.getSolarPanel().setPvModuleSpecs(oldModel);
		rack.setRackWidth(oldRackWidth);
		rack.setRackHeight(oldRackHeight);
		rack.ensureFullSolarPanels(false);
		rack.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		rack.getSolarPanel().setPvModuleSpecs(newModel);
		rack.setRackWidth(newRackWidth);
		rack.setRackHeight(newRackHeight);
		rack.ensureFullSolarPanels(false);
		rack.draw();
	}

	@Override
	public String getPresentationName() {
		return "Choose Solar Panel Model for Selected Rack";
	}

}
