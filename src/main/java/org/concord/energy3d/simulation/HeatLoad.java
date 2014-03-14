package org.concord.energy3d.simulation;

import java.util.Calendar;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

public class HeatLoad {
	private final static HeatLoad instance = new HeatLoad();

	public static HeatLoad getInstance() {
		return instance;
	}

	public void computeEnergyToday(final Calendar today, final double insideTemperature) {
//		final EnergyAmount energyToday = new EnergyAmount();
//
//		today.set(Calendar.SECOND, 0);
//		today.set(Calendar.MINUTE, 0);
//		today.set(Calendar.HOUR_OF_DAY, 0);
//
//		final double[] outsideTemperature;
//
//		if (getCity().isEmpty()) {
//			/* if there are no temperatures available for the selected city compute zero for cooling and heating */
//			outsideTemperature = new double[] { insideTemperature, insideTemperature };
//			energyToday.heating = Double.NaN;
//			energyToday.cooling = Double.NaN;
//		} else
//			outsideTemperature = computeOutsideTemperature(today);
//
//		for (int hour = 0; hour < 24; hour++) {
//			final EnergyAmount energyThisHour = computeEnergyRate(Heliodon.getInstance().computeSunLocation(today), insideTemperature, outsideTemperature[0] + (outsideTemperature[1] - outsideTemperature[0]) / 24 * hour);
//			energyToday.solar += energyThisHour.solar / 1000.0;
//			energyToday.solarPanel += energyThisHour.solarPanel / 1000.0;
//			energyToday.heating += energyThisHour.heating / 1000.0;
//			energyToday.cooling += energyThisHour.cooling / 1000.0;
//			today.add(Calendar.HOUR_OF_DAY, 1);
//		}
//		final double coolingWithSolarPanel = Math.max(0.0, energyToday.cooling - energyToday.solarPanel);
//		final double heatingWithSolarPanel = energyToday.heating - energyToday.solarPanel - (energyToday.cooling - coolingWithSolarPanel);
//		energyToday.cooling = coolingWithSolarPanel;
//		energyToday.heating = heatingWithSolarPanel;
//		return energyToday;
		
		final double wallUFactor, doorUFactor, windowUFactor, roofUFactor;
		try {
			wallUFactor = parseUFactor(EnergyPanel.getInstance().getWallsComboBox());
			doorUFactor = parseUFactor(EnergyPanel.getInstance().getDoorsComboBox());
			windowUFactor = parseUFactor(EnergyPanel.getInstance().getWindowsComboBox());
			roofUFactor = parseUFactor(EnergyPanel.getInstance().getRoofsComboBox());
		} catch (final Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(MainPanel.getInstance(), "Invalid U-Factor value: " + e.getMessage(), "Invalid U-Factor", JOptionPane.WARNING_MESSAGE);
			return;
		}		
		
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.HOUR_OF_DAY, 0);
		
		final double[] outsideTemperatureRange;
				
		for (final HousePart part : Scene.getInstance().getParts())
			part.setHeatLoss(new double[1440 / SolarIrradiation.MINUTE_STEP]);

		if (EnergyPanel.getInstance().getCityComboBox().getSelectedItem().equals("")) {
			return;
//			/* if there are no temperatures available for the selected city compute zero for cooling and heating */
//			outsideTemperatureRange = new double[] { insideTemperature, insideTemperature };
//			energyToday.heating = Double.NaN;
//			energyToday.cooling = Double.NaN;
		} else
			outsideTemperatureRange = CityData.getInstance().computeOutsideTemperature(today, (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem());		
		
		for (int minute = 0; minute < 1440; minute += SolarIrradiation.MINUTE_STEP) {
			for (final HousePart part : Scene.getInstance().getParts()) {
				final double outsideTemperature = outsideTemperatureRange[0] + (outsideTemperatureRange[1] - outsideTemperatureRange[0]) / 24 * minute / 60;
				final double deltaT = insideTemperature - outsideTemperature;
				if (part.isDrawCompleted()) {
					final double uFactor;
					if (part instanceof Wall)
						uFactor = wallUFactor;
					else if (part instanceof Door)
						uFactor = doorUFactor;
					else if (part instanceof Window)
						uFactor = windowUFactor;
					else if (part instanceof Roof)
						uFactor = roofUFactor;
					else
						continue;
					part.getHeatLoss()[minute / SolarIrradiation.MINUTE_STEP] += part.computeArea() * uFactor * deltaT / 1000.0 / 60 * SolarIrradiation.MINUTE_STEP;
				}
			}
		}
		
		
//		if (Heliodon.getInstance().isVisible()) {
//			final double heatingWithSolar = Math.max(0.0, energyRate.heating - energyRate.solar);
//			final double coolingWithSolar = energyRate.cooling + energyRate.solar - (energyRate.heating - heatingWithSolar);
//			energyRate.heating = heatingWithSolar;
//			energyRate.cooling = coolingWithSolar;
//			if (outsideTemperature < insideTemperature)
//				energyRate.cooling = 0;
//		}
	}
	
	private double parseUFactor(final JComboBox<String> comboBox) {
		final String valueStr = comboBox.getSelectedItem().toString();
		final int indexOfSpace = valueStr.indexOf(' ');
		return Double.parseDouble(valueStr.substring(0, indexOfSpace != -1 ? indexOfSpace : valueStr.length()));
	}	
}
