package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.SolarPanel;

public class ChangeBaseHeightCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldValue, newValue;
	private HousePart part;

	public ChangeBaseHeightCommand(HousePart part) {
		this.part = part;
		if (part instanceof SolarPanel)
			oldValue = ((SolarPanel) part).getBaseHeight();
		else if (part instanceof Mirror)
			oldValue = ((Mirror) part).getBaseHeight();
	}

	public HousePart getPart() {
		return part;
	}

	public double getOldValue() {
		return oldValue;
	}

	public double getNewValue() {
		if (part instanceof SolarPanel) {
			newValue = ((SolarPanel) part).getBaseHeight();
		} else if (part instanceof Mirror) {
			newValue = ((Mirror) part).getBaseHeight();
		}
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (part instanceof SolarPanel) {
			newValue = ((SolarPanel) part).getBaseHeight();
			((SolarPanel) part).setBaseHeight(oldValue);
		} else if (part instanceof Mirror) {
			newValue = ((Mirror) part).getBaseHeight();
			((Mirror) part).setBaseHeight(oldValue);
		}
		part.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (part instanceof SolarPanel)
			((SolarPanel) part).setBaseHeight(newValue);
		else if (part instanceof Mirror)
			((Mirror) part).setBaseHeight(newValue);
		part.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Base Height";
	}

}
