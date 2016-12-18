package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFoundationSolarPanelCellNumbersCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int[] oldNxs;
	private int[] newNxs;
	private final int[] oldNys;
	private int[] newNys;
	private final Foundation foundation;
	private final List<SolarPanel> panels;

	public ChangeFoundationSolarPanelCellNumbersCommand(final Foundation foundation) {
		this.foundation = foundation;
		panels = foundation.getSolarPanels();
		final int n = panels.size();
		oldNxs = new int[n];
		oldNys = new int[n];
		for (int i = 0; i < n; i++) {
			oldNxs[i] = panels.get(i).getNumberOfCellsInX();
			oldNys[i] = panels.get(i).getNumberOfCellsInY();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = panels.size();
		newNxs = new int[n];
		newNys = new int[n];
		for (int i = 0; i < n; i++) {
			final SolarPanel p = panels.get(i);
			newNxs[i] = p.getNumberOfCellsInX();
			newNys[i] = p.getNumberOfCellsInY();
			p.setNumberOfCellsInX(oldNxs[i]);
			p.setNumberOfCellsInY(oldNys[i]);
			p.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = panels.size();
		for (int i = 0; i < n; i++) {
			final SolarPanel p = panels.get(i);
			p.setNumberOfCellsInX(newNxs[i]);
			p.setNumberOfCellsInY(newNys[i]);
			p.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Cell Numbers for All Solar Panels on Selected Foundation";
	}

}
