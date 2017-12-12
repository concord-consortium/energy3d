package org.concord.energy3d.undo;

import java.util.ArrayList;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Sensor;

import com.ardor3d.math.Vector3;

public class EditPartCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	final HousePart part;
	protected final ArrayList<Vector3> orgPoints;
	private final double orgHeight;
	private ArrayList<Vector3> newPoints;
	private double newHeight;
	protected final int editPoint;

	public EditPartCommand(final HousePart housePart) {
		this.part = housePart;
		orgHeight = housePart.getHeight();
		orgPoints = new ArrayList<Vector3>(housePart.getPoints().size());
		for (final Vector3 p : housePart.getPoints()) {
			orgPoints.add(p.clone());
		}
		editPoint = housePart.getEditPoint();
	}

	public HousePart getPart() {
		return part;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		part.setHeight(orgHeight);
		for (int i = 0; i < orgPoints.size(); i++) {
			part.getPoints().set(i, orgPoints.get(i).clone());
		}
		part.draw();
		if (part.getContainer() != null) {
			part.getContainer().draw();
		}
		EnergyPanel.getInstance().updateRadiationHeatMap();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		part.setHeight(newHeight);
		for (int i = 0; i < newPoints.size(); i++) {
			part.getPoints().set(i, newPoints.get(i).clone());
		}
		part.draw();
		if (part.getContainer() != null) {
			part.getContainer().draw();
		}
		EnergyPanel.getInstance().updateRadiationHeatMap();
	}

	public boolean isReallyEdited() {
		saveNewPoints();
		return !orgPoints.equals(newPoints);
	}

	public void saveNewPoints() {
		newHeight = part.getHeight();
		newPoints = new ArrayList<Vector3>(part.getPoints().size());
		for (final Vector3 p : part.getPoints()) {
			newPoints.add(p.clone());
		}
	}

	@Override
	public char getOneLetterCode() {
		if (part instanceof Floor) {
			return 'F';
		}
		if (part instanceof Human) {
			return 'H';
		}
		if (part instanceof Foundation) {
			return 'N';
		}
		if (part instanceof Sensor) {
			return 'S';
		}
		return 'P';
	}

	@Override
	public String getPresentationName() {
		return "Edit " + part.getClass().getSimpleName();
	}

}
