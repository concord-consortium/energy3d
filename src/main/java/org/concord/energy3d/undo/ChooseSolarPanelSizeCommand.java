package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class ChooseSolarPanelSizeCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double oldWidth;
	private double newWidth;
	private final double oldHeight;
	private double newHeight;
	private final SolarPanel solarPanel;
	private final int oldCellNx, oldCellNy;
	private int newCellNx, newCellNy;

	public ChooseSolarPanelSizeCommand(final SolarPanel solarPanel) {
		this.solarPanel = solarPanel;
		oldWidth = solarPanel.getPanelWidth();
		oldHeight = solarPanel.getPanelHeight();
		oldCellNx = solarPanel.getNumberOfCellsInX();
		oldCellNy = solarPanel.getNumberOfCellsInY();
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
		newCellNx = solarPanel.getNumberOfCellsInX();
		newCellNy = solarPanel.getNumberOfCellsInY();
		solarPanel.setPanelWidth(oldWidth);
		solarPanel.setPanelHeight(oldHeight);
		solarPanel.setNumberOfCellsInX(oldCellNx);
		solarPanel.setNumberOfCellsInY(oldCellNy);
		solarPanel.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		solarPanel.setPanelWidth(newWidth);
		solarPanel.setPanelHeight(newHeight);
		solarPanel.setNumberOfCellsInX(newCellNx);
		solarPanel.setNumberOfCellsInY(newCellNy);
		solarPanel.draw();
	}

	@Override
	public String getPresentationName() {
		return "Choose Size for Selected Solar Panel";
	}

}
