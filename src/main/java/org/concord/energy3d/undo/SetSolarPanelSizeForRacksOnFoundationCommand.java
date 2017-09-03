package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.SceneManager;

public class SetSolarPanelSizeForRacksOnFoundationCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double[] oldWidths;
	private double[] newWidths;
	private final double[] oldHeights;
	private double[] newHeights;
	private final int[] oldCellNxs, oldCellNys;
	private int[] newCellNxs, newCellNys;
	private final Foundation foundation;
	private final List<Rack> racks;

	public SetSolarPanelSizeForRacksOnFoundationCommand(final Foundation foundation) {
		this.foundation = foundation;
		racks = foundation.getRacks();
		final int n = racks.size();
		oldWidths = new double[n];
		oldHeights = new double[n];
		oldCellNxs = new int[n];
		oldCellNys = new int[n];
		for (int i = 0; i < n; i++) {
			final SolarPanel s = racks.get(i).getSolarPanel();
			oldWidths[i] = s.getPanelWidth();
			oldHeights[i] = s.getPanelHeight();
			oldCellNxs[i] = s.getNumberOfCellsInX();
			oldCellNys[i] = s.getNumberOfCellsInY();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = racks.size();
		newWidths = new double[n];
		newHeights = new double[n];
		for (int i = 0; i < n; i++) {
			final Rack r = racks.get(i);
			final SolarPanel s = r.getSolarPanel();
			newWidths[i] = s.getPanelWidth();
			newHeights[i] = s.getPanelHeight();
			s.setPanelWidth(oldWidths[i]);
			s.setPanelHeight(oldHeights[i]);
			newCellNxs[i] = s.getNumberOfCellsInX();
			newCellNys[i] = s.getNumberOfCellsInY();
			s.setNumberOfCellsInX(oldCellNxs[i]);
			s.setNumberOfCellsInY(oldCellNys[i]);
			r.ensureFullSolarPanels(false);
			r.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = racks.size();
		for (int i = 0; i < n; i++) {
			final Rack r = racks.get(i);
			final SolarPanel s = r.getSolarPanel();
			s.setPanelWidth(newWidths[i]);
			s.setPanelHeight(newHeights[i]);
			s.setNumberOfCellsInX(newCellNxs[i]);
			s.setNumberOfCellsInY(newCellNys[i]);
			r.ensureFullSolarPanels(false);
			r.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Set Solar Panel Size for All Racks on Selected Foundation";
	}

}
