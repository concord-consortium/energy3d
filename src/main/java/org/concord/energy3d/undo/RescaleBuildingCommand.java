package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class RescaleBuildingCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldXLength, newXLength;
	private final double oldYLength, newYLength;
	private final double oldZLength, newZLength;
	private final Foundation foundation;

	public RescaleBuildingCommand(final Foundation foundation, final double oldXLength, final double newXLength, final double oldYLength, final double newYLength, final double oldZLength, final double newZLength) {
		this.foundation = foundation;
		this.oldXLength = oldXLength;
		this.newXLength = newXLength;
		this.oldYLength = oldYLength;
		this.newYLength = newYLength;
		this.oldZLength = oldZLength;
		this.newZLength = newZLength;
	}

	public Foundation getFoundation() {
		return foundation;
	}

	public double getOldXLength() {
		return oldXLength;
	}

	public double getNewXLength() {
		return newXLength;
	}

	public double getOldYLength() {
		return oldYLength;
	}

	public double getNewYLength() {
		return newYLength;
	}

	public double getOldZLength() {
		return oldZLength;
	}

	public double getNewZLength() {
		return newZLength;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (foundation.isGroupMaster()) {
			final List<Foundation> g = Scene.getInstance().getFoundationGroup(foundation);
			for (final Foundation f : g) {
				f.rescale(oldXLength / newXLength, oldYLength / newYLength, oldZLength / newZLength);
				f.draw();
				f.drawChildren();
			}
		} else {
			foundation.rescale(oldXLength / newXLength, oldYLength / newYLength, oldZLength / newZLength);
			foundation.draw();
			foundation.drawChildren();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (foundation.isGroupMaster()) {
			final List<Foundation> g = Scene.getInstance().getFoundationGroup(foundation);
			for (final Foundation f : g) {
				f.rescale(newXLength / oldXLength, newYLength / oldYLength, newZLength / oldZLength);
				f.draw();
				f.drawChildren();
			}
		} else {
			foundation.rescale(newXLength / oldXLength, newYLength / oldYLength, newZLength / oldZLength);
			foundation.draw();
			foundation.drawChildren();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Rescale Building";
	}

}
