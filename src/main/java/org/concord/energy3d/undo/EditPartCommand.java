package org.concord.energy3d.undo;

import java.util.ArrayList;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.HousePart;

import com.ardor3d.math.Vector3;

public class EditPartCommand extends AbstractUndoableEdit {
	private static final long serialVersionUID = 1L;
	final HousePart housePart;
	protected final ArrayList<Vector3> orgPoints;
	private final double orgHeight;
	private ArrayList<Vector3> newPoints;
	private double newHeight;
	protected final int editPoint;

	public EditPartCommand(final HousePart housePart) {
		this.housePart = housePart;
		orgHeight = housePart.getHeight();
		orgPoints = new ArrayList<Vector3>(housePart.getPoints().size());
		for (final Vector3 p : housePart.getPoints()) {
			orgPoints.add(p.clone());
		}
		editPoint = housePart.getEditPoint();
	}

	public HousePart getPart() {
		return housePart;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		housePart.setHeight(orgHeight);
		for (int i = 0; i < orgPoints.size(); i++) {
			housePart.getPoints().set(i, orgPoints.get(i).clone());
		}
		housePart.draw();
		if (housePart.getContainer() != null) {
			housePart.getContainer().draw();
		}
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		housePart.setHeight(newHeight);
		for (int i = 0; i < newPoints.size(); i++) {
			housePart.getPoints().set(i, newPoints.get(i).clone());
		}
		housePart.draw();
		if (housePart.getContainer() != null) {
			housePart.getContainer().draw();
		}
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public String getPresentationName() {
		return "Edit " + housePart.getClass().getSimpleName();
	}

	public boolean isReallyEdited() {
		saveNewPoints();
		return !orgPoints.equals(newPoints);
	}

	public void saveNewPoints() {
		newHeight = housePart.getHeight();
		newPoints = new ArrayList<Vector3>(housePart.getPoints().size());
		for (final Vector3 p : housePart.getPoints()) {
			newPoints.add(p.clone());
		}
	}

}
