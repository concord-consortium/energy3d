package org.concord.energy3d.undo;

import java.util.Date;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.Util;

public class ChangeDateCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final Date oldDate;
	private Date newDate;

	public ChangeDateCommand() {
		oldDate = Scene.getInstance().getDate();
	}

	public Date getOldDate() {
		return oldDate;
	}

	public Date getNewDate() {
		return newDate;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newDate = Scene.getInstance().getDate();
		Scene.getInstance().setDate(oldDate);
		Util.setSilently(EnergyPanel.getInstance().getDateSpinner(), oldDate);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setDate(newDate);
		Util.setSilently(EnergyPanel.getInstance().getDateSpinner(), newDate);
	}

	@Override
	public char getOneLetterCode() {
		return 'D';
	}

	@Override
	public String getPresentationName() {
		return "Change Date";
	}

}
