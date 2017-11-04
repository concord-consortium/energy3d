package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;

public class ChooseSolarPanelSizeForRackCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldWidth;
	private double newWidth;
	private final double oldHeight;
	private double newHeight;
	private final double oldRackWidth;
	private double newRackWidth;
	private final double oldRackHeight;
	private double newRackHeight;
	private final int oldCellNx, oldCellNy;
	private int newCellNx, newCellNy;
	private final Rack rack;

	public ChooseSolarPanelSizeForRackCommand(final Rack rack) {
		this.rack = rack;
		oldRackWidth = rack.getRackWidth();
		oldRackHeight = rack.getRackHeight();
		final SolarPanel s = rack.getSolarPanel();
		oldWidth = s.getPanelWidth();
		oldHeight = s.getPanelHeight();
		oldCellNx = s.getNumberOfCellsInX();
		oldCellNy = s.getNumberOfCellsInY();
	}

	public Rack getRack() {
		return rack;
	}

	public double getOldWidth() {
		return oldWidth;
	}

	public double getOldHeight() {
		return oldHeight;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final SolarPanel s = rack.getSolarPanel();
		newRackWidth = rack.getRackWidth();
		newRackHeight = rack.getRackHeight();
		newWidth = s.getPanelWidth();
		newHeight = s.getPanelHeight();
		newCellNx = s.getNumberOfCellsInX();
		newCellNy = s.getNumberOfCellsInY();
		s.setPanelWidth(oldWidth);
		s.setPanelHeight(oldHeight);
		s.setNumberOfCellsInX(oldCellNx);
		s.setNumberOfCellsInY(oldCellNy);
		rack.setRackWidth(oldRackWidth);
		rack.setRackHeight(oldRackHeight);
		rack.ensureFullSolarPanels(false);
		rack.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final SolarPanel s = rack.getSolarPanel();
		s.setPanelWidth(newWidth);
		s.setPanelHeight(newHeight);
		s.setNumberOfCellsInX(newCellNx);
		s.setNumberOfCellsInY(newCellNy);
		rack.setRackWidth(newRackWidth);
		rack.setRackHeight(newRackHeight);
		rack.ensureFullSolarPanels(false);
		rack.draw();
	}

	@Override
	public String getPresentationName() {
		return "Choose Solar Panel Size for Selected Rack";
	}

}
