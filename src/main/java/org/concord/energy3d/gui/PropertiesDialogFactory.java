package org.concord.energy3d.gui;

import javax.swing.JDialog;

import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.SceneManager;

/**
 * @author Charles Xie
 *
 */
public abstract class PropertiesDialogFactory {

	public static JDialog getDialog() {

		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart instanceof Window) {
			return null;
		}
		if (selectedPart instanceof Wall) {
			return null;
		}
		if (selectedPart instanceof Roof) {
			return null;
		}
		if (selectedPart instanceof Door) {
			return null;
		}
		if (selectedPart instanceof Floor) {
			return null;
		}
		if (selectedPart instanceof Foundation) {
			return null;
		}
		if (selectedPart instanceof SolarPanel) {
			return PropertiesDialogForSolarPanel.getDialog((SolarPanel) selectedPart);
		}
		if (selectedPart instanceof Rack) {
			return PropertiesDialogForRack.getDialog((Rack) selectedPart);
		}
		if (selectedPart instanceof Mirror) {
			return null;
		}
		if (selectedPart instanceof ParabolicTrough) {
			return null;
		}
		if (selectedPart instanceof ParabolicDish) {
			return null;
		}
		if (selectedPart instanceof FresnelReflector) {
			return null;
		}
		if (selectedPart instanceof Sensor) {
			return null;
		}
		if (selectedPart instanceof Tree) {
			return null;
		}
		if (selectedPart instanceof Human) {
			return null;
		}

		return null;

	}

}
