package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Rack;

public class SetTemperatureCoefficientPmaxForRacksOnFoundationCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final Foundation foundation;
	private final List<Rack> racks;

	public SetTemperatureCoefficientPmaxForRacksOnFoundationCommand(final Foundation foundation) {
		this.foundation = foundation;
		racks = foundation.getRacks();
		final int n = racks.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = racks.get(i).getSolarPanel().getTemperatureCoefficientPmax();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = racks.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			final Rack r = racks.get(i);
			newValues[i] = r.getSolarPanel().getTemperatureCoefficientPmax();
			r.getSolarPanel().setTemperatureCoefficientPmax(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = racks.size();
		for (int i = 0; i < n; i++) {
			racks.get(i).getSolarPanel().setTemperatureCoefficientPmax(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "Set Temperature Coefficient of Pmax for All Racks on Selected Foundation";
	}

}