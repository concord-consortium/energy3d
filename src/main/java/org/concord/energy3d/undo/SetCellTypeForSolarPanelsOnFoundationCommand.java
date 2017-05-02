package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.SceneManager;

public class SetCellTypeForSolarPanelsOnFoundationCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int[] oldValues;
	private int[] newValues;
	private final Foundation foundation;
	private final List<SolarPanel> solarPanels;

	public SetCellTypeForSolarPanelsOnFoundationCommand(final Foundation foundation) {
		this.foundation = foundation;
		solarPanels = foundation.getSolarPanels();
		final int n = solarPanels.size();
		oldValues = new int[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = solarPanels.get(i).getCellType();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = solarPanels.size();
		newValues = new int[n];
		for (int i = 0; i < n; i++) {
			final SolarPanel s = solarPanels.get(i);
			newValues[i] = s.getCellType();
			s.setCellType(oldValues[i]);
			s.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = solarPanels.size();
		for (int i = 0; i < n; i++) {
			final SolarPanel s = solarPanels.get(i);
			s.setCellType(newValues[i]);
			s.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Cell Type for All Solar Panels on Selected Foundation";
	}

}
