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
	private int monthOfYear;
	private int dayOfWeek;
	private int hourOfDay;

	public ChangeInsideTemperatureCommand() {
		Calendar c = Heliodon.getInstance().getCalender();
		monthOfYear = c.get(Calendar.MONTH);
		dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
		hourOfDay = c.get(Calendar.HOUR_OF_DAY);
		orgTemperature = Scene.getInstance().getThermostat().getTemperature(monthOfYear, dayOfWeek, hourOfDay);
	}

	public int getMonthOfYear() {
		return monthOfYear;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public int getHourOfDay() {
		return hourOfDay;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newTemperature = Scene.getInstance().getThermostat().getTemperature(monthOfYear, dayOfWeek, hourOfDay);
		Scene.getInstance().getThermostat().setTemperature(monthOfYear, dayOfWeek, hourOfDay, orgTemperature);
		Util.setSilently(EnergyPanel.getInstance().getInsideTemperatureSpinner(), orgTemperature);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().getThermostat().setTemperature(monthOfYear, dayOfWeek, hourOfDay, newTemperature);
		Util.setSilently(EnergyPanel.getInstance().getInsideTemperatureSpinner(), newTemperature);
	}

	@Override
	public String getPresentationName() {
		return "Change Room Temperature";
	}

}
