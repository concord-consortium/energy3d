package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;

public class ChooseSolarPanelSizeForRackCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldWidth;
	private double newWidth;
	private final double oldHeight;
	private double newHeight;
	private final Rack rack;

	public ChooseSolarPanelSizeForRackCommand(final Rack rack) {
		this.rack = rack;
		oldWidth = rack.getSolarPanel().getPanelWidth();
		oldHeight = rack.getSolarPanel().getPanelHeight();
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
		newWidth = rack.getSolarPanel().getPanelWidth();
		newHeight = rack.getSolarPanel().getPanelHeight();
		rack.getSolarPanel().setPanelWidth(oldWidth);
		rack.getSolarPanel().setPanelHeight(oldHeight);
		rack.ensureFullSolarPanels(false);
		rack.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		rack.getSolarPanel().setPanelWidth(newWidth);
		rack.getSolarPanel().setPanelHeight(newHeight);
		rack.ensureFullSolarPanels(false);
		rack.draw();
	}

	@Override
	public String getPresentationName() {
		return "Choose Solar Panel Size for Selected Rack";
	}

}
