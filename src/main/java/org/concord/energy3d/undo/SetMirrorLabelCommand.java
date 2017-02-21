package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Mirror;

public class SetMirrorLabelCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldLabelId;
	private final boolean oldLabelCustom;
	private final boolean oldLabelEnergyOutput;
	private boolean newLabelId;
	private boolean newLabelCustom;
	private boolean newLabelEnergyOutput;
	private final Mirror mirror;

	public SetMirrorLabelCommand(final Mirror mirror) {
		this.mirror = mirror;
		oldLabelId = mirror.getLabelId();
		oldLabelCustom = mirror.getLabelCustom();
		oldLabelEnergyOutput = mirror.getLabelEnergyOutput();
	}

	public Mirror getMirror() {
		return mirror;
	}

	public boolean getOldLabelId() {
		return oldLabelId;
	}

	public boolean getNewLabelId() {
		return newLabelId;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newLabelId = mirror.getLabelId();
		newLabelCustom = mirror.getLabelCustom();
		newLabelEnergyOutput = mirror.getLabelEnergyOutput();
		mirror.setLabelId(oldLabelId);
		mirror.setLabelCustom(oldLabelCustom);
		mirror.setLabelEnergyOutput(oldLabelEnergyOutput);
		mirror.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		mirror.setLabelId(newLabelId);
		mirror.setLabelCustom(newLabelCustom);
		mirror.setLabelEnergyOutput(newLabelEnergyOutput);
		mirror.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Label of Mirror";
	}

}
