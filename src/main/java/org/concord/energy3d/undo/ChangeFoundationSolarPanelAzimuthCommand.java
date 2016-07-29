package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFoundationSolarPanelAzimuthCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double[] oldValues, newValues;
	private Foundation foundation;
	private List<SolarPanel> panels;

	public ChangeFoundationSolarPanelAzimuthCommand(Foundation foundation) {
		this.foundation = foundation;
		panels = Scene.getInstance().getSolarPanelsOnFoundation(foundation);
		int n = panels.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = panels.get(i).getRelativeAzimuth();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		int n = panels.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			SolarPanel p = panels.get(i);
			newValues[i] = p.getRelativeAzimuth();
			p.setRelativeAzimuth(oldValues[i]);
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
			p.setRelativeAzimuth(newValues[i]);
			p.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Azimuth for All Solar Panels on Selected Foundation";
	}

}
