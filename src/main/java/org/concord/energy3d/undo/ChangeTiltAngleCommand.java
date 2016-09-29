package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;

public class ChangeTiltAngleCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldValue, newValue;
	private final HousePart part;

	public ChangeTiltAngleCommand(final HousePart part) {
		this.part = part;
		if (part instanceof SolarPanel) {
			oldValue = ((SolarPanel) part).getTiltAngle();
		} else if (part instanceof Rack) {
			oldValue = ((Rack) part).getTiltAngle();
		} else if (part instanceof Mirror) {
			oldValue = ((Mirror) part).getTiltAngle();
		}
	}

	public HousePart getPart() {
		return part;
	}

	public double getOldValue() {
		return oldValue;
	}

	public double getNewValue() {
		if (part instanceof SolarPanel) {
			newValue = ((SolarPanel) part).getTiltAngle();
		} else if (part instanceof Rack) {
			newValue = ((Rack) part).getTiltAngle();
		} else if (part instanceof Mirror) {
			newValue = ((Mirror) part).getTiltAngle();
		}
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (part instanceof SolarPanel) {
			newValue = ((SolarPanel) part).getTiltAngle();
			((SolarPanel) part).setTiltAngle(oldValue);
		} else if (part instanceof Rack) {
			newValue = ((Rack) part).getTiltAngle();
			((Rack) part).setTiltAngle(oldValue);
		} else if (part instanceof Mirror) {
			newValue = ((Mirror) part).getTiltAngle();
			((Mirror) part).setTiltAngle(oldValue);
		}
		part.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (part instanceof SolarPanel) {
			((SolarPanel) part).setTiltAngle(newValue);
		} else if (part instanceof Rack) {
			((Rack) part).setTiltAngle(newValue);
		} else if (part instanceof Mirror) {
			((Mirror) part).setTiltAngle(newValue);
		}
		part.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Tilt Angle";
	}

}
