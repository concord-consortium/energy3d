package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

public class RemoveMultiplePartsCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final List<HousePart> parts;
	private boolean sameType = true;
	private boolean sameParent = true;

	public RemoveMultiplePartsCommand(final List<HousePart> parts) {
		this.parts = parts;
		if (parts.size() > 1) {
			Class<?> c = parts.get(0).getClass();
			for (int i = 1; i < parts.size(); i++) {
				if (!c.isInstance(parts.get(i))) {
					sameType = false;
					break;
				}
			}
			HousePart parent = parts.get(0).getContainer();
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
		int n = parts.size();
		if (n == 0)
			return null;
		Foundation foundation = parts.get(0).getTopContainer();
		for (int i = 1; i < n; i++) {
			if (parts.get(i).getTopContainer() != foundation) // parts are on multiple foundations, so this removes everything
				return null;
		}
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for (HousePart p : parts) {
			if (p.isDrawable()) // as an extra defense of potential invisible ghost part
				Scene.getInstance().add(p, true);
		}
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		for (HousePart p : parts) {
			Scene.getInstance().remove(p, true);
		}
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public String getPresentationName() {
		if (parts.isEmpty())
			return "Remove Nothing";
		if (sameParent && !sameType)
			return "Remove All Elements of a Container";
		if (sameType)
			return "Remove All " + parts.get(0).getClass().getSimpleName() + "s";
		return "Remove Multiple Parts";
	}

}
