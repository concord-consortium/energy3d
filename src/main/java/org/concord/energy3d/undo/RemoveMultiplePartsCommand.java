package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

public class RemoveMultiplePartsCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final List<HousePart> parts;
	private boolean sameType = true;
	private boolean sameParent = true;

	public RemoveMultiplePartsCommand(final List<HousePart> parts) {
		this.parts = parts;
		if (parts.size() > 1) {
			final Class<?> c = parts.get(0).getClass();
			for (int i = 1; i < parts.size(); i++) {
				if (!c.isInstance(parts.get(i))) {
					sameType = false;
					break;
				}
			}
			final HousePart parent = parts.get(0).getContainer();
			for (int i = 1; i < parts.size(); i++) {
				if (parts.get(i).getContainer() != parent) {
					sameParent = false;
					break;
				}
			}
		}
	}

	// for action logging: Return the foundation if all the parts are on the same one; return null otherwise to indicate that all the parts are removed
	public Foundation getFoundation() {
		final int n = parts.size();
		if (n == 0) {
			return null;
		}
		final Foundation foundation = parts.get(0).getTopContainer();
		for (int i = 1; i < n; i++) {
			if (parts.get(i).getTopContainer() != foundation) {
				return null;
			}
		}
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for (final HousePart p : parts) {
			if (p.isDrawable()) {
				Scene.getInstance().add(p, true);
			}
		}
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		for (final HousePart p : parts) {
			Scene.getInstance().remove(p, true);
		}
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public String getPresentationName() {
		if (parts.isEmpty()) {
			return "Remove Nothing";
		}
		if (sameParent && !sameType) {
			return "Remove All Elements of a Container";
		}
		if (sameType) {
			return "Remove " + parts.get(0).getClass().getSimpleName() + "s";
		}
		return "Remove Multiple Parts";
	}

}
