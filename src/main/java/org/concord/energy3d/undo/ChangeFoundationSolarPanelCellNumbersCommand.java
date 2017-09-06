package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFoundationSolarPanelCellNumbersCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int[] oldNxs;
	private int[] newNxs;
	private final int[] oldNys;
	private int[] newNys;
	private final Foundation foundation;
	private final List<SolarPanel> panels;
	private final List<Rack> racks;

	public ChangeFoundationSolarPanelCellNumbersCommand(final Foundation foundation) {
		this.foundation = foundation;
		panels = foundation.getSolarPanels();
		racks = foundation.getRacks();
		final int n = panels.size() + racks.size();
		oldNxs = new int[n];
		oldNys = new int[n];
		for (int i = 0; i < panels.size(); i++) {
			oldNxs[i] = panels.get(i).getNumberOfCellsInX();
			oldNys[i] = panels.get(i).getNumberOfCellsInY();
		}
		for (int i = 0; i < racks.size(); i++) {
			final int j = i + panels.size();
			final SolarPanel p = racks.get(i).getSolarPanel();
			oldNxs[j] = p.getNumberOfCellsInX();
			oldNys[j] = p.getNumberOfCellsInY();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = panels.size() + racks.size();
		newNxs = new int[n];
		newNys = new int[n];
		for (int i = 0; i < panels.size(); i++) {
			final SolarPanel p = panels.get(i);
			newNxs[i] = p.getNumberOfCellsInX();
			newNys[i] = p.getNumberOfCellsInY();
			p.setNumberOfCellsInX(oldNxs[i]);
			p.setNumberOfCellsInY(oldNys[i]);
			p.draw();
		}
		for (int i = 0; i < racks.size(); i++) {
			final int j = i + panels.size();
			final SolarPanel p = racks.get(i).getSolarPanel();
			newNxs[j] = p.getNumberOfCellsInX();
			newNys[j] = p.getNumberOfCellsInY();
			p.setNumberOfCellsInX(oldNxs[j]);
			p.setNumberOfCellsInY(oldNys[j]);
			racks.get(i).draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		for (int i = 0; i < panels.size(); i++) {
			final SolarPanel p = panels.get(i);
			p.setNumberOfCellsInX(newNxs[i]);
			p.setNumberOfCellsInY(newNys[i]);
			p.draw();
		}
		for (int i = 0; i < racks.size(); i++) {
			final int j = i + panels.size();
			final SolarPanel p = racks.get(i).getSolarPanel();
			p.setNumberOfCellsInX(newNxs[j]);
			p.setNumberOfCellsInY(newNys[j]);
			racks.get(i).draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Cell Numbers for All Solar Panels on Selected Foundation";
	}

}
