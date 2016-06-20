package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class RotateSolarPanelCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;
	private String type;
	private SolarPanel solarPanel;

	public RotateSolarPanelCommand(SolarPanel solarPanel, String type) {
		this.solarPanel = solarPanel;
		this.type = type;
		oldValue = solarPanel.isRotated();
	}

	public SolarPanel getSolarPanel() {
		return solarPanel;
	}

	public String getType() {
		return type;
	}

	public boolean getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if ("Normal".equals(type)) {
			newValue = solarPanel.isRotated();
			solarPanel.setRotated(oldValue);
		} else if ("Z-Axis".equals(type)) {
			newValue = solarPanel.isRotatedAroundZ();
			solarPanel.setRotatedAroundZ(oldValue);
		}
		solarPanel.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if ("Normal".equals(type))
			solarPanel.setRotated(newValue);
		else if ("Z-Axis".equals(type))
			solarPanel.setRotatedAroundZ(newValue);
		solarPanel.draw();
	}

	@Override
	public String getPresentationName() {
		return "Rotate Solar Panel";
	}

}
