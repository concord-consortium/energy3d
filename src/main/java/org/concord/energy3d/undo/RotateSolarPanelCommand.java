package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class RotateSolarPanelCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final boolean oldValue;
	private boolean newValue;
	private final SolarPanel solarPanel;

	public RotateSolarPanelCommand(final SolarPanel solarPanel) {
		this.solarPanel = solarPanel;
		oldValue = solarPanel.isRotated();
	}

	public SolarPanel getSolarPanel() {
		return solarPanel;
	}

	public boolean getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = solarPanel.isRotated();
		solarPanel.setRotated(oldValue);
		solarPanel.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		solarPanel.setRotated(newValue);
		solarPanel.draw();
	}

	@Override
	public String getPresentationName() {
		return "Rotate Solar Panel";
	}

}
