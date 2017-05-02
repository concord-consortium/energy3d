package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class SetCellTypeForAllSolarPanelsCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int[] oldValues;
	private int[] newValues;
	private final List<SolarPanel> panels;

	public SetCellTypeForAllSolarPanelsCommand() {
		panels = Scene.getInstance().getAllSolarPanels();
		final int n = panels.size();
		oldValues = new int[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = panels.get(i).getCellType();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = panels.size();
		newValues = new int[n];
		for (int i = 0; i < n; i++) {
			final SolarPanel p = panels.get(i);
			newValues[i] = p.getCellType();
			p.setCellType(oldValues[i]);
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
			p.setCellType(newValues[i]);
			p.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Cell Type for All Solar Panels";
	}

}
