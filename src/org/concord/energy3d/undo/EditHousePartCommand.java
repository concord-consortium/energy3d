package org.concord.energy3d.undo;

import java.util.ArrayList;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.Operation;

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
		super.undo();
//		Foundation foundation = null;
//		if (housePart instanceof Foundation) {			
//			foundation = (Foundation)housePart;
//			foundation.setResizeHouseMode(isResizeMode);
//			if (!isResizeMode) 
//				foundation.prepareForNotResizing();
//		}
		housePart.setHeight(orgHeight);
		housePart.getPoints().clear();
		housePart.getPoints().addAll(orgPoints);
//		if (foundation != null) {
//			foundation.complete();
//			foundation.setResizeHouseMode(SceneManager.getInstance().getOperation() == Operation.RESIZE);
//		}
		Scene.getInstance().redrawAll();
	}
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
//		housePart.setHeight(newHeight);
//		if (!isResizeMode && housePart instanceof Foundation)
//			((Foundation)housePart).prepareForNotResizing();		
//		housePart.getPoints().clear();
//		housePart.getPoints().addAll(newPoints);
//		if (housePart instanceof Foundation)
////			((Foundation)housePart).applyNewHeight(isResizeMode);
//			housePart.complete();
//		Scene.getInstance().redrawAll();
//		Foundation foundation = null;
//		if (housePart instanceof Foundation) {			
//			foundation = (Foundation)housePart;
//			foundation.setResizeHouseMode(isResizeMode);
//			if (!isResizeMode) 
//				foundation.prepareForNotResizing();
//		}
		housePart.setHeight(newHeight);
		housePart.getPoints().clear();
		housePart.getPoints().addAll(newPoints);
//		if (foundation != null) {
//			foundation.complete();
//			foundation.setResizeHouseMode(SceneManager.getInstance().getOperation() == Operation.RESIZE);
//		}
		Scene.getInstance().redrawAll();		
	}
	
	@Override
	public String getPresentationName() {
		return "Edit " + housePart.getClass().getSimpleName();
	}
	
	public boolean isReallyEdited() {
		if (newPoints == null) {
			this.newHeight = housePart.getHeight();
			this.newPoints = new ArrayList<Vector3>(housePart.getPoints().size());
			for (final Vector3 p : housePart.getPoints())
				this.newPoints.add(p.clone());
		}
		return !orgPoints.equals(newPoints);
	}
}
