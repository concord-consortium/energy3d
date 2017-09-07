package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class ChangeSolarCellPropertiesCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldEfficiency;
	private final int oldType;
	private final int oldColor;
	private double newEfficiency;
	private int newType;
	private int newColor;
	private final SolarPanel solarPanel;

	public ChangeSolarCellPropertiesCommand(final SolarPanel solarPanel) {
		this.solarPanel = solarPanel;
		oldEfficiency = solarPanel.getCellEfficiency();
		oldType = solarPanel.getCellType();
		oldColor = solarPanel.getColorOption();
	}

	public SolarPanel getSolarPanel() {
		return solarPanel;
	}

	public double getOldEfficiency() {
		return oldEfficiency;
	}

	public int getOldType() {
		return oldType;
	}

	public int getOldColor() {
		return oldColor;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newEfficiency = solarPanel.getCellEfficiency();
		newType = solarPanel.getCellType();
		newColor = solarPanel.getColorOption();
		solarPanel.setCellEfficiency(oldEfficiency);
		solarPanel.setCellType(oldType);
		solarPanel.setColorOption(oldColor);
		solarPanel.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		solarPanel.setCellEfficiency(newEfficiency);
		solarPanel.setCellType(newType);
		solarPanel.setColorOption(newColor);
		solarPanel.draw();
	}

	@Override
	public String getPresentationName() {
		return "Solar Cell Property Change for Selected Solar Panel";
	}

}
