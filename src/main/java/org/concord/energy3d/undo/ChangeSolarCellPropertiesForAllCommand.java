package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeSolarCellPropertiesForAllCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldEfficiencies;
	private final int[] oldTypes;
	private final int[] oldColors;
	private double[] newEfficiencies;
	private int[] newTypes;
	private int[] newColors;
	private final List<SolarPanel> panels;

	public ChangeSolarCellPropertiesForAllCommand() {
		panels = Scene.getInstance().getAllSolarPanels();
		final int n = panels.size();
		oldEfficiencies = new double[n];
		oldTypes = new int[n];
		oldColors = new int[n];
		SolarPanel p;
		for (int i = 0; i < n; i++) {
			p = panels.get(i);
			oldEfficiencies[i] = p.getCellEfficiency();
			oldTypes[i] = p.getCellType();
			oldColors[i] = p.getColorOption();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = panels.size();
		newEfficiencies = new double[n];
		newTypes = new int[n];
		newColors = new int[n];
		SolarPanel p;
		for (int i = 0; i < n; i++) {
			p = panels.get(i);
			newEfficiencies[i] = p.getCellEfficiency();
			p.setCellEfficiency(oldEfficiencies[i]);
			newTypes[i] = p.getCellType();
			p.setCellType(oldTypes[i]);
			newColors[i] = p.getColorOption();
			p.setColorOption(oldColors[i]);
			p.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = panels.size();
		SolarPanel p;
		for (int i = 0; i < n; i++) {
			p = panels.get(i);
			p.setCellEfficiency(newEfficiencies[i]);
			p.setCellType(newTypes[i]);
			p.setColorOption(newColors[i]);
			p.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Solar Cell Property Change for All Solar Panels";
	}

}
