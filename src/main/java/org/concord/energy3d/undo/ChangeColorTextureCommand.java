package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangeColorTextureCommand extends AbstractUndoableEdit {
	private static final long serialVersionUID = 1L;
	private final ReadOnlyColorRGBA orgFoundationColor;
	private ReadOnlyColorRGBA orgWallColor = null;
	private final ReadOnlyColorRGBA orgDoorColor;
	private final ReadOnlyColorRGBA orgFloorColor;
	private final ReadOnlyColorRGBA orgRoofColor;
	private final TextureMode orgTextureMode;
	private ReadOnlyColorRGBA newFoundationColor;
	private ReadOnlyColorRGBA newWallColor;
	private ReadOnlyColorRGBA newDoorColor;
	private ReadOnlyColorRGBA newFloorColor;
	private ReadOnlyColorRGBA newRoofColor;
	private TextureMode newTextureMode;
	private HousePart selectedPart;

	public ChangeColorTextureCommand() {
		selectedPart = SceneManager.getInstance().getSelectedPart();
		orgFoundationColor = Scene.getInstance().getFoundationColor();

		orgWallColor = Scene.getInstance().getWallColor();
		if (selectedPart instanceof Wall) {
			ReadOnlyColorRGBA c = ((Wall) selectedPart).getColor();
			if (c != null)
				orgWallColor = c;
		}

		orgDoorColor = Scene.getInstance().getDoorColor();
		orgFloorColor = Scene.getInstance().getFloorColor();
		orgRoofColor = Scene.getInstance().getRoofColor();
		orgTextureMode = Scene.getInstance().getTextureMode();

	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (newFoundationColor == null) {
			newFoundationColor = Scene.getInstance().getFoundationColor();

			newWallColor = Scene.getInstance().getWallColor();
			if (selectedPart instanceof Wall) {
				ReadOnlyColorRGBA c = ((Wall) selectedPart).getColor();
				if (c != null)
					newWallColor = c;
			}

			newDoorColor = Scene.getInstance().getDoorColor();
			newFloorColor = Scene.getInstance().getFloorColor();
			newRoofColor = Scene.getInstance().getRoofColor();
			newTextureMode = Scene.getInstance().getTextureMode();
		}
		Scene.getInstance().setFoundationColor(orgFoundationColor);
		setWallColor(orgWallColor);
		Scene.getInstance().setDoorColor(orgDoorColor);
		Scene.getInstance().setFloorColor(orgFloorColor);
		Scene.getInstance().setRoofColor(orgRoofColor);
		Scene.getInstance().setTextureMode(orgTextureMode);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setFoundationColor(newFoundationColor);
		setWallColor(newWallColor);
		Scene.getInstance().setDoorColor(newDoorColor);
		Scene.getInstance().setFloorColor(newFloorColor);
		Scene.getInstance().setRoofColor(newRoofColor);
		Scene.getInstance().setTextureMode(newTextureMode);
	}

	private void setWallColor(ReadOnlyColorRGBA c) {
		if (selectedPart instanceof Wall)
			((Wall) selectedPart).setColor(c);
		else
			Scene.getInstance().setWallColor(c);
	}

	@Override
	public String getPresentationName() {
		return "Change Appearance";
	}

}
