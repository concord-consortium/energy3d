package org.concord.energy3d.undo;

import java.util.Calendar;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.Util;

public class ChangeInsideTemperatureCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private int orgTemperature, newTemperature;
	private int month;

	public ChangeInsideTemperatureCommand() {
		month = Heliodon.getInstance().getCalender().get(Calendar.MONTH);
		orgTemperature = Scene.getInstance().getThermostat().getTemperature(month);
	}

	public int getMonth() {
		return month;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newTemperature = Scene.getInstance().getThermostat().getTemperature(month);
		Scene.getInstance().getThermostat().setTemperature(month, orgTemperature);
		Util.setSilently(EnergyPanel.getInstance().getInsideTemperatureSpinner(), orgTemperature);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().getThermostat().setTemperature(month, newTemperature);
		Util.setSilently(EnergyPanel.getInstance().getInsideTemperatureSpinner(), newTemperature);
	}

	@Override
	public String getPresentationName() {
		return "Change Inside Temperature";
	}

}
