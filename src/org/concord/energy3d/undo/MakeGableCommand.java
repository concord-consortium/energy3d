package org.concord.energy3d.undo;

import java.util.concurrent.Callable;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Roof;
import org.concord.energy3d.scene.SceneManager;

@SuppressWarnings("serial")
public class MakeGableCommand extends EditHousePartCommand {
	private Roof roof;
	private int roofPartIndex;

	public MakeGableCommand(final Roof roof, final int roofPartIndex) {
		super(roof);
		this.roof = roof;
		this.roofPartIndex = roofPartIndex;
	}
	
	@Override
	public void undo() throws CannotUndoException {
		saveNewPoints();
		super.undo();
		SceneManager.getTaskManager().update(new Callable<Object>() {
			public Object call() {		
				roof.setGable(roofPartIndex, false);
				return null;
			}
		});
	}
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		SceneManager.getTaskManager().update(new Callable<Object>() {
			public Object call() {		
				roof.setGable(roofPartIndex, true);
				return null;
			}
		});
	}
	
	@Override
	public String getPresentationName() {
		return "Convert to Gable";
	}
}
