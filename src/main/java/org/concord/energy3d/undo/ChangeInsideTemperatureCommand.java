package org.concord.energy3d.undo;

import java.util.Calendar;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.shapes.Heliodon;

public class ChangeInsideTemperatureCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int orgTemperature;
	private int newTemperature;
	private final int monthOfYear;
	private final int dayOfWeek;
	private final int hourOfDay;
	private final Foundation foundation;

	public ChangeInsideTemperatureCommand(final Foundation foundation) {
		final Calendar c = Heliodon.getInstance().getCalendar();
		monthOfYear = c.get(Calendar.MONTH);
		dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
		hourOfDay = c.get(Calendar.HOUR_OF_DAY);
		orgTemperature = foundation.getThermostat().getTemperature(monthOfYear, dayOfWeek, hourOfDay);
		this.foundation = foundation;
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

	public Foundation getBuilding() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newTemperature = foundation.getThermostat().getTemperature(monthOfYear, dayOfWeek, hourOfDay);
		foundation.getThermostat().setTemperature(monthOfYear, dayOfWeek, hourOfDay, orgTemperature);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		foundation.getThermostat().setTemperature(monthOfYear, dayOfWeek, hourOfDay, newTemperature);
	}

	@Override
	public String getPresentationName() {
		return "Change Room Temperature";
	}

}
