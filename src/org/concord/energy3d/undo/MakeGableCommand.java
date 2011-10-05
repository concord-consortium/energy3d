package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Roof;

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
		super.undo();
		isReallyEdited(); // to force update of new vertices
		roof.setGable(roofPartIndex, false);
	}
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		roof.setGable(roofPartIndex, true);
	}
	
	@Override
	public String getPresentationName() {
		return "Convert to Gable";
	}
}
