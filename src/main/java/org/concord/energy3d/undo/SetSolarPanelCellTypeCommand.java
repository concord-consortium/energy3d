package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class SetSolarPanelCellTypeCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;
	private final SolarPanel solarPanel;

	public SetSolarPanelCellTypeCommand(final SolarPanel solarPanel) {
		this.solarPanel = solarPanel;
		oldValue = solarPanel.getCellType();
	}

	public SolarPanel getSolarPanel() {
		return solarPanel;
	}

	public int getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = solarPanel.getCellType();
		solarPanel.setCellType(oldValue);
		solarPanel.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		solarPanel.setCellType(newValue);
		solarPanel.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Solar Panel Cell Type";
	}

}
