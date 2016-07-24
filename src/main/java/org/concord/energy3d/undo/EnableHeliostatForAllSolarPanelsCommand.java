package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class EnableHeliostatForAllSolarPanelsCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean[] oldValues, newValues;
	private List<SolarPanel> panels;

	public EnableHeliostatForAllSolarPanelsCommand() {
		panels = Scene.getInstance().getAllSolarPanels();
		int n = panels.size();
		oldValues = new boolean[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = panels.get(i).getHeliostat();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		int n = panels.size();
		newValues = new boolean[n];
		for (int i = 0; i < n; i++) {
			SolarPanel p = panels.get(i);
			newValues[i] = p.getHeliostat();
			p.setHeliostat(oldValues[i]);
			p.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		int n = panels.size();
		for (int i = 0; i < n; i++) {
			SolarPanel p = panels.get(i);
			p.setHeliostat(newValues[i]);
			p.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return (panels.get(0).getHeliostat() ? "Enable" : "Disable") + " Heliostat for All Solar Panels";
	}

}
