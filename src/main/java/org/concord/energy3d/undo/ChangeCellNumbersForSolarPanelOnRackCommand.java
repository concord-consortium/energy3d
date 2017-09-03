package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.SceneManager;

public class ChangeCellNumbersForSolarPanelOnRackCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final int[] oldNxs;
	private int[] newNxs;
	private final int[] oldNys;
	private int[] newNys;
	private final Rack rack;

	public ChangeCellNumbersForSolarPanelOnRackCommand(final Rack rack) {
		this.rack = rack;
		final int n = rack.getChildren().size();
		oldNxs = new int[n];
		oldNys = new int[n];
		for (int i = 0; i < n; i++) {
			final SolarPanel s = (SolarPanel) rack.getChildren().get(i);
			oldNxs[i] = s.getNumberOfCellsInX();
			oldNys[i] = s.getNumberOfCellsInY();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = rack.getChildren().size();
		newNxs = new int[n];
		newNys = new int[n];
		for (int i = 0; i < n; i++) {
			final SolarPanel s = (SolarPanel) rack.getChildren().get(i);
			newNxs[i] = s.getNumberOfCellsInX();
			newNys[i] = s.getNumberOfCellsInY();
			s.setNumberOfCellsInX(oldNxs[i]);
			s.setNumberOfCellsInY(oldNys[i]);
			s.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = rack.getChildren().size();
		for (int i = 0; i < n; i++) {
			final SolarPanel s = (SolarPanel) rack.getChildren().get(i);
			s.setNumberOfCellsInX(newNxs[i]);
			s.setNumberOfCellsInY(newNys[i]);
			s.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Cell Numbers for All Solar Panels on Selected Rack";
	}

}
