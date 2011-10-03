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
	private final ArrayList<Vector3> orgPoints;
	private final double orgHeight;
	private ArrayList<Vector3> newPoints;
	private double newHeight;
	
	public EditHousePartCommand(final HousePart housePart) {
		this.housePart = housePart;
		this.orgHeight = housePart.getHeight();
		this.orgPoints = new ArrayList<Vector3>(housePart.getPoints().size());		
		for (final Vector3 p : housePart.getPoints())
			this.orgPoints.add(p.clone());
	}
	
	@Override
	public void undo() throws CannotUndoException {
		this.newHeight = housePart.getHeight();
		this.newPoints = new ArrayList<Vector3>(housePart.getPoints().size());
		for (final Vector3 p : housePart.getPoints())
			this.newPoints.add(p.clone());		
		super.undo();
		housePart.setHeight(orgHeight);
		housePart.getPoints().clear();
		housePart.getPoints().addAll(orgPoints);
		Scene.getInstance().redrawAll();
	}
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		housePart.setHeight(newHeight);
		housePart.getPoints().clear();
		housePart.getPoints().addAll(newPoints);
		Scene.getInstance().redrawAll();
	}
	
	@Override
	public String getPresentationName() {
		return "edit " + housePart.getClass().getSimpleName();
	}
}
