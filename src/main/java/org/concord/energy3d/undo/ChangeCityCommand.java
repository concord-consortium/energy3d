package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.simulation.LocationData;
import org.concord.energy3d.util.Util;

public class ChangeCityCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private String oldValue, newValue;

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
		Float latitude = LocationData.getInstance().getLatitutes().get(EnergyPanel.getInstance().getCityComboBox().getSelectedItem());
		if (latitude != null)
			Util.setSilently(EnergyPanel.getInstance().getLatitudeSpinner(), latitude.intValue());
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setCity(newValue);
		Util.selectSilently(EnergyPanel.getInstance().getCityComboBox(), newValue);
		Float latitude = LocationData.getInstance().getLatitutes().get(EnergyPanel.getInstance().getCityComboBox().getSelectedItem());
		if (latitude != null)
			Util.setSilently(EnergyPanel.getInstance().getLatitudeSpinner(), latitude.intValue());
	}

	@Override
	public String getPresentationName() {
		return "Change City";
	}

}
