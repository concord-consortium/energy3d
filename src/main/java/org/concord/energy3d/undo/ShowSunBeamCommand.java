package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.SolarCollector;
import org.concord.energy3d.scene.SceneManager;

public class ShowSunBeamCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldValue, newValue;
	private final SolarCollector collector;

	public ShowSunBeamCommand(final SolarCollector collector) {
		this.collector = collector;
		oldValue = collector.isSunBeamVisible();
		newValue = !oldValue;
	}

	public boolean getNewValue() {
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		collector.setSunBeamVisible(oldValue);
		collector.drawSunBeam();
		if (collector instanceof HousePart) {
			((HousePart) collector).draw();
			SceneManager.getInstance().refresh();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		collector.setSunBeamVisible(newValue);
		collector.drawSunBeam();
		if (collector instanceof HousePart) {
			((HousePart) collector).draw();
			SceneManager.getInstance().refresh();
		}
	}

	@Override
	public String getPresentationName() {
		return "Sun Beam";
	}

}
