package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.FresnelReflector;

public class SetFresnelReflectorLabelCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldLabelId;
	private final boolean oldLabelCustom;
	private final boolean oldLabelEnergyOutput;
	private boolean newLabelId;
	private boolean newLabelCustom;
	private boolean newLabelEnergyOutput;
	private final FresnelReflector reflector;

	public SetFresnelReflectorLabelCommand(final FresnelReflector reflector) {
		this.reflector = reflector;
		oldLabelId = reflector.getLabelId();
		oldLabelCustom = reflector.getLabelCustom();
		oldLabelEnergyOutput = reflector.getLabelEnergyOutput();
	}

	public FresnelReflector getFresnelReflector() {
		return reflector;
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
		newLabelId = reflector.getLabelId();
		newLabelCustom = reflector.getLabelCustom();
		newLabelEnergyOutput = reflector.getLabelEnergyOutput();
		reflector.setLabelId(oldLabelId);
		reflector.setLabelCustom(oldLabelCustom);
		reflector.setLabelEnergyOutput(oldLabelEnergyOutput);
		reflector.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		reflector.setLabelId(newLabelId);
		reflector.setLabelCustom(newLabelCustom);
		reflector.setLabelEnergyOutput(newLabelEnergyOutput);
		reflector.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Label of Fresnel Reflector";
	}

}
