package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.simulation.CityData;
import org.concord.energy3d.util.Util;

public class ChangeCityCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private String orgCity, newCity;

	public ChangeCityCommand() {
		orgCity = Scene.getInstance().getCity();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newCity = Scene.getInstance().getCity();
		Scene.getInstance().setCity(orgCity);
		Util.selectSilently(EnergyPanel.getInstance().getCityComboBox(), orgCity);
		Integer latitude = CityData.getInstance().getLatitutes().get(EnergyPanel.getInstance().getCityComboBox().getSelectedItem()).intValue();
		Util.setSilently(EnergyPanel.getInstance().getLatitudeSpinner(), latitude);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setCity(newCity);
		Util.selectSilently(EnergyPanel.getInstance().getCityComboBox(), newCity);
		Integer latitude = CityData.getInstance().getLatitutes().get(EnergyPanel.getInstance().getCityComboBox().getSelectedItem()).intValue();
		Util.setSilently(EnergyPanel.getInstance().getLatitudeSpinner(), latitude);
	}

	@Override
	public String getPresentationName() {
		return "Change City";
	}

}
