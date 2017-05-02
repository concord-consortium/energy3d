package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;

public class SetCellTypeForSolarPanelsOnRackCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;
	private final Rack rack;

	public SetCellTypeForSolarPanelsOnRackCommand(final Rack rack) {
		this.rack = rack;
		oldValue = rack.getSolarPanel().getCellType();
	}

	public Rack getRack() {
		return rack;
	}

	public int getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = rack.getSolarPanel().getCellType();
		rack.getSolarPanel().setCellType(oldValue);
		rack.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		rack.getSolarPanel().setCellType(newValue);
		rack.draw();
	}

	@Override
	public String getPresentationName() {
		return "Set Cell Type of Solar Panels on Selected Rack";
	}

}
