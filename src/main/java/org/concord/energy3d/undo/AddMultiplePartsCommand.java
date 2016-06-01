package org.concord.energy3d.undo;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

public class AddMultiplePartsCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final List<HousePart> parts;
	private final URL url; // if null, adding part happens through importing from an existing file

	public AddMultiplePartsCommand(final List<HousePart> parts, URL url) {
		this.parts = new ArrayList<HousePart>(parts);
		this.url = url;
	}

	public URL getURL() {
		return url;
	}

	public List<HousePart> getParts() {
		return parts;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for (HousePart p : parts)
			Scene.getInstance().remove(p, true);
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		for (HousePart p : parts)
			Scene.getInstance().add(p, true);
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public String getPresentationName() {
		return "Add Parts";
	}

}
