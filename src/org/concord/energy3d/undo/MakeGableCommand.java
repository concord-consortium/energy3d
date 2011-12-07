package org.concord.energy3d.undo;

import java.util.concurrent.Callable;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Roof;
import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.math.Vector3;

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
		System.out.println("---------Before UNDO--------------");
		for (final Vector3 p : orgPoints)
			System.out.println(p);

		saveNewPoints();
		super.undo();
//		roof.draw();
		SceneManager.getTaskManager().update(new Callable<Object>() {
//		SceneManager.getTaskManager().render(new Callable<Object>() {
			public Object call() {		
				roof.setGable(roofPartIndex, false);
				System.out.println("-After UNDO");
				for (final Vector3 p : orgPoints)
					System.out.println(p);
				return null;
			}
		});
		
		
	}
	
	@Override
	public void redo() throws CannotRedoException {
		System.out.println("---------Before REDO--------------");
		for (final Vector3 p : orgPoints)
			System.out.println(p);		
		super.redo();
		SceneManager.getTaskManager().update(new Callable<Object>() {
			public Object call() {		
				roof.setGable(roofPartIndex, true);
				System.out.println("-After REDO");
				for (final Vector3 p : orgPoints)
					System.out.println(p);		
				return null;
			}
		});
	}
	
	@Override
	public String getPresentationName() {
		return "Convert to Gable";
	}
}
