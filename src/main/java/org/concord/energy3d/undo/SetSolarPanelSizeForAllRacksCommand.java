package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class SetSolarPanelSizeForAllRacksCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldWidths;
	private double[] newWidths;
	private final double[] oldHeights;
	private double[] newHeights;
	private final List<Rack> racks;

	public SetSolarPanelSizeForAllRacksCommand() {
		racks = Scene.getInstance().getAllRacks();
		final int n = racks.size();
		oldWidths = new double[n];
		oldHeights = new double[n];
		for (int i = 0; i < n; i++) {
			final SolarPanel s = racks.get(i).getSolarPanel();
			oldWidths[i] = s.getPanelWidth();
			oldHeights[i] = s.getPanelHeight();
		}
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
			s.setPanelWidth(oldWidths[i]);
			newHeights[i] = s.getPanelHeight();
			s.setPanelHeight(oldHeights[i]);
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
			r.ensureFullSolarPanels(false);
			r.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Set Solar Panel Size for All Racks";
	}

}
