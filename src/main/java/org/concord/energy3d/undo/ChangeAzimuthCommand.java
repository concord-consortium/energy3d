package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;

public class ChangeAzimuthCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private double oldValue, newValue;
	private final HousePart part;

	public ChangeAzimuthCommand(final HousePart part) {
		this.part = part;
		if (part instanceof SolarPanel) {
			oldValue = ((SolarPanel) part).getRelativeAzimuth();
		} else if (part instanceof Rack) {
			oldValue = ((Rack) part).getRelativeAzimuth();
		} else if (part instanceof Mirror) {
			oldValue = ((Mirror) part).getRelativeAzimuth();
		} else if (part instanceof ParabolicTrough) {
			oldValue = ((ParabolicTrough) part).getRelativeAzimuth();
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
			newValue = ((SolarPanel) part).getRelativeAzimuth();
		} else if (part instanceof Rack) {
			newValue = ((Rack) part).getRelativeAzimuth();
		} else if (part instanceof Mirror) {
			newValue = ((Mirror) part).getRelativeAzimuth();
		} else if (part instanceof ParabolicTrough) {
			newValue = ((ParabolicTrough) part).getRelativeAzimuth();
		}
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (part instanceof SolarPanel) {
			newValue = ((SolarPanel) part).getRelativeAzimuth();
			((SolarPanel) part).setRelativeAzimuth(oldValue);
		} else if (part instanceof Rack) {
			newValue = ((Rack) part).getRelativeAzimuth();
			((Rack) part).setRelativeAzimuth(oldValue);
		} else if (part instanceof Mirror) {
			newValue = ((Mirror) part).getRelativeAzimuth();
			((Mirror) part).setRelativeAzimuth(oldValue);
		} else if (part instanceof ParabolicTrough) {
			newValue = ((ParabolicTrough) part).getRelativeAzimuth();
			((ParabolicTrough) part).setRelativeAzimuth(oldValue);
		}
		part.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (part instanceof SolarPanel) {
			((SolarPanel) part).setRelativeAzimuth(newValue);
		} else if (part instanceof Rack) {
			((Rack) part).setRelativeAzimuth(newValue);
		} else if (part instanceof Mirror) {
			((Mirror) part).setRelativeAzimuth(newValue);
		} else if (part instanceof ParabolicTrough) {
			((ParabolicTrough) part).setRelativeAzimuth(newValue);
		}
		part.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Azimuth";
	}

}
