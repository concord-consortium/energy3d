package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;

public class SetSolarPanelArrayOnRackCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final SolarPanel oldValue;
	private SolarPanel newValue;
	private final Rack rack;

	public SetSolarPanelArrayOnRackCommand(final Rack rack) {
		this.rack = rack;
		final SolarPanel s = rack.getSolarPanel();
		oldValue = (SolarPanel) s.copy(false);
	}

	public Rack getRack() {
		return rack;
	}

	public SolarPanel getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final SolarPanel s = rack.getSolarPanel();
		newValue = (SolarPanel) s.copy(false);
		copySolarPanelProperties(s, oldValue);
		rack.ensureFullSolarPanels(false);
		rack.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final SolarPanel s = rack.getSolarPanel();
		copySolarPanelProperties(s, newValue);
		rack.ensureFullSolarPanels(false);
		rack.draw();
	}

	private void copySolarPanelProperties(final SolarPanel des, final SolarPanel src) {
		des.setPanelWidth(src.getPanelWidth());
		des.setPanelHeight(src.getPanelHeight());
		des.setNumberOfCellsInX(src.getNumberOfCellsInX());
		des.setNumberOfCellsInY(src.getNumberOfCellsInY());
		des.setRotated(src.isRotated());
		des.setCellType(src.getCellType());
		des.setNumberOfCellsInX(src.getNumberOfCellsInX());
		des.setNumberOfCellsInY(src.getNumberOfCellsInY());
		des.setColorOption(src.getColorOption());
		des.setCellEfficiency(src.getCellEfficiency());
		des.setInverterEfficiency(src.getInverterEfficiency());
		des.setNominalOperatingCellTemperature(src.getNominalOperatingCellTemperature());
		des.setTemperatureCoefficientPmax(src.getTemperatureCoefficientPmax());
		des.setShadeTolerance(src.getShadeTolerance());
	}

	@Override
	public String getPresentationName() {
		return "Set Solar Panel Array on Rack";
	}

}
