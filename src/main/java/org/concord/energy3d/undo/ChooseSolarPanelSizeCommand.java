package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class ChooseSolarPanelSizeCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldWidth, newWidth;
	private double oldHeight, newHeight;
	private SolarPanel solarPanel;

	public ChooseSolarPanelSizeCommand(SolarPanel solarPanel) {
		this.solarPanel = solarPanel;
		oldWidth = solarPanel.getPanelWidth();
		oldHeight = solarPanel.getPanelHeight();
	}

	public SolarPanel getSolarPanel() {
		return solarPanel;
	}

	public double getOldWidth() {
		return oldWidth;
	}

	public double getOldHeight() {
		return oldHeight;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newWidth = solarPanel.getPanelWidth();
		newHeight = solarPanel.getPanelHeight();
		solarPanel.setPanelWidth(oldWidth);
		solarPanel.setPanelHeight(oldHeight);
		solarPanel.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		solarPanel.setPanelWidth(newWidth);
		solarPanel.setPanelHeight(newHeight);
		solarPanel.draw();
	}

	@Override
	public String getPresentationName() {
		return "Choose Size for Selected Solar Panel";
	}

}
