package org.concord.energy3d.undo;

import java.util.ArrayList;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.math.type.ReadOnlyVector3;

public class MakeGableCommand extends EditPartCommand {

	private static final long serialVersionUID = 1L;
	private final Roof roof;
	private final Wall wall;
	private final ArrayList<ReadOnlyVector3> roofPartMeshUpperPoints;

	public MakeGableCommand(final Roof roof, final Wall wall, final ArrayList<ReadOnlyVector3> roofPartMeshUpperPoints) {
		super(roof);
		this.roof = roof;
		this.wall = wall;
		this.roofPartMeshUpperPoints = roofPartMeshUpperPoints;
	}

	@Override
	public void undo() throws CannotUndoException {
		saveNewPoints();
		roof.setGable(wall, false, false, roofPartMeshUpperPoints);
		super.undo();
		if (SceneManager.getInstance().getSolarHeatMap()) {
			EnergyPanel.getInstance().updateRadiationHeatMap();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		roof.setGable(wall, true, false, roofPartMeshUpperPoints);
		super.redo();
		if (SceneManager.getInstance().getSolarHeatMap()) {
			EnergyPanel.getInstance().updateRadiationHeatMap();
		}
	}

	@Override
	public String getPresentationName() {
		return "Convert to Gable";
	}

}
