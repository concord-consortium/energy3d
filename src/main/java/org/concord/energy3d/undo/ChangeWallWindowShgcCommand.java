package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

public class ChangeWallWindowShgcCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double[] orgShgc, newShgc;
	private Wall wall;
	private List<Window> windows;

	public ChangeWallWindowShgcCommand(Wall wall) {
		this.wall = wall;
		windows = Scene.getInstance().getWindowsOnWall(wall);
		int n = windows.size();
		orgShgc = new double[n];
		for (int i = 0; i < n; i++) {
			orgShgc[i] = windows.get(i).getSolarHeatGainCoefficient();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		int n = windows.size();
		newShgc = new double[n];
		for (int i = 0; i < n; i++) {
			newShgc[i] = windows.get(i).getSolarHeatGainCoefficient();
			windows.get(i).setSolarHeatGainCoefficient(orgShgc[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		int n = windows.size();
		for (int i = 0; i < n; i++) {
			windows.get(i).setSolarHeatGainCoefficient(newShgc[i]);
		}
	}

	// for action logging
	public Wall getWall() {
		return wall;
	}

	@Override
	public String getPresentationName() {
		return "SHGC Change for All Windows on Wall";
	}

}
