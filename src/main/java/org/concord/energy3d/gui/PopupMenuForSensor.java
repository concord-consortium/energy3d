package org.concord.energy3d.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

class PopupMenuForSensor extends PopupMenuFactory {

	private static JPopupMenu popupMenuForSensor;

	static JPopupMenu getPopupMenu() {

		if (popupMenuForSensor == null) {

			final JCheckBoxMenuItem miLight = new JCheckBoxMenuItem("Light", true);
			miLight.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Sensor)) {
						return;
					}
					final Sensor s = (Sensor) selectedPart;
					s.setLightOff(!miLight.isSelected());
					Scene.getInstance().setEdited(true);
				}
			});

			final JCheckBoxMenuItem miHeatFlux = new JCheckBoxMenuItem("Heat Flux", true);
			miHeatFlux.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Sensor)) {
						return;
					}
					final Sensor s = (Sensor) selectedPart;
					s.setHeatFluxOff(!miHeatFlux.isSelected());
					Scene.getInstance().setEdited(true);
				}
			});

			popupMenuForSensor = createPopupMenu(false, false, new Runnable() {
				@Override
				public void run() {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Sensor)) {
						return;
					}
					final Sensor s = (Sensor) selectedPart;
					Util.selectSilently(miLight, !s.isLightOff());
					Util.selectSilently(miHeatFlux, !s.isHeatFluxOff());
				}
			});

			popupMenuForSensor.addSeparator();
			popupMenuForSensor.add(miLight);
			popupMenuForSensor.add(miHeatFlux);

		}

		return popupMenuForSensor;

	}

}
