package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class AddPartCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final HousePart part;

	public AddPartCommand(final HousePart part) {
		this.part = part;
	}

	public HousePart getPart() {
		return part;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		Scene.getInstance().remove(part, true);
		if (SceneManager.getInstance().getSolarHeatMap()) {
			EnergyPanel.getInstance().updateRadiationHeatMap();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().add(part, true);
		if (SceneManager.getInstance().getSolarHeatMap()) {
			EnergyPanel.getInstance().updateRadiationHeatMap();
		}
	}

	@Override
	public char getOneLetterCode() {
		if (part instanceof Floor) {
			return 'F';
		}
		if (part instanceof Human) {
			return 'H';
		}
		if (part instanceof Foundation) {
			return 'N';
		}
		if (part instanceof Sensor) {
			return 'S';
		}
		return 'P';
	}

	@Override
	public String getPresentationName() {
		return "Add " + part.getClass().getSimpleName();
	}

}
