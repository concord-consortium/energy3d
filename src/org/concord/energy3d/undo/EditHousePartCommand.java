package org.concord.energy3d.undo;

import java.util.ArrayList;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

import com.ardor3d.math.Vector3;

@SuppressWarnings("serial")
public class EditHousePartCommand extends AbstractUndoableEdit {
	private final HousePart housePart;
	protected final ArrayList<Vector3> orgPoints;
	private final double orgHeight;
	private ArrayList<Vector3> newPoints;
	private double newHeight;


	public EditHousePartCommand(final HousePart housePart) {
		this.housePart = housePart;
		orgHeight = housePart.getHeight();
		orgPoints = new ArrayList<Vector3>(housePart.getPoints().size());
		for (final Vector3 p : housePart.getPoints())
			orgPoints.add(p.clone());
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		housePart.setHeight(orgHeight);
//		housePart.getPoints().clear();
//		housePart.getPoints().addAll(orgPoints);
		for (int i = 0; i < orgPoints.size(); i++)
			housePart.getPoints().set(i, orgPoints.get(i).clone());
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		housePart.setHeight(newHeight);
//		housePart.getPoints().clear();
//		housePart.getPoints().addAll(newPoints);
		for (int i = 0; i < newPoints.size(); i++)
			housePart.getPoints().set(i, newPoints.get(i).clone());
		Scene.getInstance().redrawAll();
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
		if (newPoints == null) {
			newHeight = housePart.getHeight();
			newPoints = new ArrayList<Vector3>(housePart.getPoints().size());
			for (final Vector3 p : housePart.getPoints())
				newPoints.add(p.clone());
		}
	}
}
