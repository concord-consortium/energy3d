package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.simulation.LocationData;
import org.concord.energy3d.util.Util;

public class ChangeCityCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final String oldValue;
	private String newValue;

	public ChangeCityCommand() {
		oldValue = Scene.getInstance().getCity();
	}

	public String getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = Scene.getInstance().getCity();
		Scene.getInstance().setCity(oldValue);
		Util.selectSilently(EnergyPanel.getInstance().getCityComboBox(), oldValue);
		final Float latitude = LocationData.getInstance().getLatitudes().get(EnergyPanel.getInstance().getCityComboBox().getSelectedItem());
		if (latitude != null) {
			Util.setSilently(EnergyPanel.getInstance().getLatitudeSpinner(), latitude.intValue());
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setCity(newValue);
		Util.selectSilently(EnergyPanel.getInstance().getCityComboBox(), newValue);
		final Float latitude = LocationData.getInstance().getLatitudes().get(EnergyPanel.getInstance().getCityComboBox().getSelectedItem());
		if (latitude != null) {
			Util.setSilently(EnergyPanel.getInstance().getLatitudeSpinner(), latitude.intValue());
		}
	}

	@Override
	public char getOneLetterCode() {
		return 'C';
	}

	@Override
	public String getPresentationName() {
		return "Change City";
	}

}
