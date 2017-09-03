package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;

public class AdjustThermostatCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final int[][][] oldTemperatures;
	private int[][][] newTemperatures;
	private final Foundation foundation;

	public AdjustThermostatCommand(final Foundation foundation) {
		this.foundation = foundation;
		oldTemperatures = new int[12][7][25];
		final int[][][] values = foundation.getThermostat().getTemperatures();
		for (int i = 0; i < 12; i++) {
			for (int j = 0; j < 7; j++) {
				for (int k = 0; k < 25; k++) {
					oldTemperatures[i][j][k] = values[i][j][k];
				}
			}
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newTemperatures = new int[12][7][25];
		final int[][][] values = foundation.getThermostat().getTemperatures();
		for (int i = 0; i < 12; i++) {
			for (int j = 0; j < 7; j++) {
				for (int k = 0; k < 25; k++) {
					newTemperatures[i][j][k] = values[i][j][k];
				}
			}
		}
		foundation.getThermostat().setTemperatures(oldTemperatures);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		foundation.getThermostat().setTemperatures(newTemperatures);
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public String getPresentationName() {
		return "Thermostat Adjustment";
	}

}
