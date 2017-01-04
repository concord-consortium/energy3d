package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;

public class RotateSolarPanelsOnRackCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldValue;
	private boolean newValue;
	private final Rack rack;

	public RotateSolarPanelsOnRackCommand(final Rack rack) {
		this.rack = rack;
		oldValue = rack.getSolarPanel().isRotated();
	}

	public Rack getRack() {
		return rack;
	}

	public boolean getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = rack.getSolarPanel().isRotated();
		rack.getSolarPanel().setRotated(oldValue);
		rack.ensureFullSolarPanels(false);
		rack.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		rack.getSolarPanel().setRotated(newValue);
		rack.ensureFullSolarPanels(false);
		rack.draw();
	}

	@Override
	public String getPresentationName() {
		return "Rotate Solar Panels on Rack";
	}

}
