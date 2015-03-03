package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangeColorTextureCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private ReadOnlyColorRGBA orgFoundationColor;
	private ReadOnlyColorRGBA orgWallColor;
	private ReadOnlyColorRGBA orgDoorColor;
	private ReadOnlyColorRGBA orgFloorColor;
	private ReadOnlyColorRGBA orgRoofColor;
	private final TextureMode orgTextureMode;
	private ReadOnlyColorRGBA newFoundationColor;
	private ReadOnlyColorRGBA newWallColor;
	private ReadOnlyColorRGBA newDoorColor;
	private ReadOnlyColorRGBA newFloorColor;
	private ReadOnlyColorRGBA newRoofColor;
	private TextureMode newTextureMode;
	private HousePart selectedPart;

	public ChangeColorTextureCommand() {
		orgFoundationColor = Scene.getInstance().getFoundationColor();
		orgWallColor = Scene.getInstance().getWallColor();
		orgDoorColor = Scene.getInstance().getDoorColor();
		orgFloorColor = Scene.getInstance().getFloorColor();
		orgRoofColor = Scene.getInstance().getRoofColor();
		orgTextureMode = Scene.getInstance().getTextureMode();
		selectedPart = SceneManager.getInstance().getSelectedPart();
		ReadOnlyColorRGBA c = selectedPart == null ? null : selectedPart.getColor();
		if (c != null) {
			if (selectedPart instanceof Foundation)
				orgFoundationColor = c;
			else if (selectedPart instanceof Wall)
				orgWallColor = c;
			else if (selectedPart instanceof Door)
				orgDoorColor = c;
			else if (selectedPart instanceof Floor)
				orgFloorColor = c;
			else if (selectedPart instanceof Roof)
				orgRoofColor = c;
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (newFoundationColor == null) {
			newFoundationColor = Scene.getInstance().getFoundationColor();
			newWallColor = Scene.getInstance().getWallColor();
			newDoorColor = Scene.getInstance().getDoorColor();
			newFloorColor = Scene.getInstance().getFloorColor();
			newRoofColor = Scene.getInstance().getRoofColor();
			newTextureMode = Scene.getInstance().getTextureMode();
			ReadOnlyColorRGBA c = selectedPart == null ? null : selectedPart.getColor();
			if (c != null) {
				if (selectedPart instanceof Foundation)
					newFoundationColor = c;
				else if (selectedPart instanceof Wall)
					newWallColor = c;
				else if (selectedPart instanceof Door)
					newDoorColor = c;
				else if (selectedPart instanceof Floor)
					newFloorColor = c;
				else if (selectedPart instanceof Roof)
					newRoofColor = c;
			}
		}
		Scene.getInstance().setFoundationColor(orgFoundationColor);
		setWallColor(orgWallColor);
		setDoorColor(orgDoorColor);
		setFloorColor(orgFloorColor);
		setRoofColor(orgRoofColor);
		Scene.getInstance().setTextureMode(orgTextureMode);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		setFoundationColor(newFoundationColor);
		setWallColor(newWallColor);
		setDoorColor(newDoorColor);
		setFloorColor(newFloorColor);
		setRoofColor(newRoofColor);
		Scene.getInstance().setTextureMode(newTextureMode);
	}

	private void setFoundationColor(ReadOnlyColorRGBA c) {
		if (selectedPart instanceof Foundation)
			selectedPart.setColor(c);
		else
			Scene.getInstance().setFoundationColor(c);
	}

	private void setWallColor(ReadOnlyColorRGBA c) {
		if (selectedPart instanceof Wall)
			selectedPart.setColor(c);
		else
			Scene.getInstance().setWallColor(c);
	}

	private void setDoorColor(ReadOnlyColorRGBA c) {
		if (selectedPart instanceof Door)
			selectedPart.setColor(c);
		else
			Scene.getInstance().setDoorColor(c);
	}

	private void setFloorColor(ReadOnlyColorRGBA c) {
		if (selectedPart instanceof Floor)
			selectedPart.setColor(c);
		else
			Scene.getInstance().setFloorColor(c);
	}

	private void setRoofColor(ReadOnlyColorRGBA c) {
		if (selectedPart instanceof Roof)
			selectedPart.setColor(c);
		else
			Scene.getInstance().setRoofColor(c);
	}

	// for action logging
	public HousePart getHousePart() {
		return selectedPart;
	}

	@Override
	public String getPresentationName() {
		if (orgTextureMode != newTextureMode)
			return "Texture Change";
		if (orgFoundationColor != newFoundationColor)
			return "Foundation Color Change";
		if (orgFloorColor != newFloorColor)
			return "Floor Color Change";
		if (orgWallColor != newWallColor)
			return "Wall Color Change";
		if (orgRoofColor != newRoofColor)
			return "Roof Color Change";
		if (orgDoorColor != newDoorColor)
			return "Door Color Change";
		return "Change Appearance";
	}

}
