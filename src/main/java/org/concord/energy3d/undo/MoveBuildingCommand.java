package org.concord.energy3d.undo;

import java.util.concurrent.Callable;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.math.Vector3;

public class MoveBuildingCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final Foundation foundation;
	private final Vector3 displacement;

	public MoveBuildingCommand(final Foundation foundation, final Vector3 displacement) {
		this.foundation = foundation;
		this.displacement = displacement;
	}

	public Foundation getFoundation() {
		return foundation;
	}

	public Vector3 getDisplacement() {
		return displacement;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		move(displacement.negate(null));
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		move(displacement);
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	private void move(final Vector3 v) {
		SceneManager.getInstance().setSelectedPart(foundation);
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				if (foundation != null) {
					foundation.move(v, foundation.getGridSize());
				} else {
					for (HousePart p : Scene.getInstance().getParts()) {
						if (p instanceof Foundation) {
							((Foundation) p).move(v, p.getGridSize());
						}
					}
				}
				return null;
			}
		});
	}

	@Override
	public String getPresentationName() {
		return "Move Building";
	}

}
