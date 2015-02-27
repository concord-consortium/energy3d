package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.PropertiesPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

@SuppressWarnings("serial")
public class RemoveHousePartCommand extends AbstractUndoableEdit {
	private final HousePart housePart;
	private final boolean isSignificant;

	public RemoveHousePartCommand(final HousePart housePart) {
		this(housePart, true);
	}

	public RemoveHousePartCommand(final HousePart housePart, final boolean isSignificant) {
		this.housePart = housePart;
		this.isSignificant = isSignificant;
	}

	// for action logging
	public HousePart getHousePart() {
		return housePart;
	}

	@Override
	public boolean isSignificant() {
		return isSignificant;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		Scene.getInstance().add(housePart, true);
		SceneManager.getInstance().setSelectedPart(housePart);
		PropertiesPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().remove(housePart, true);
		PropertiesPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public String getPresentationName() {
		if (housePart instanceof Foundation && !housePart.getChildren().isEmpty())
			return "Remove Building";
		if (housePart instanceof Tree)
			return "Remove " + ((Tree) housePart).getTreeName();
		if (housePart instanceof Human)
			return "Remove " + ((Human) housePart).getHumanName();
		return "Remove " + housePart.getClass().getSimpleName();
	}

}
