package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;

public class SetSolarPanelColorForRackCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;
	private final Rack rack;

	public SetSolarPanelColorForRackCommand(final Rack rack) {
		this.rack = rack;
		oldValue = rack.getSolarPanel().getColorOption();
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
		newValue = rack.getSolarPanel().getColorOption();
		rack.getSolarPanel().setColorOption(oldValue);
		rack.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		rack.getSolarPanel().setColorOption(newValue);
		rack.draw();
	}

	@Override
	public String getPresentationName() {
		return "Set Color of Solar Panels on Selected Rack";
	}

}
