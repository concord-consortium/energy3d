package org.concord.energy3d.undo;

import java.util.Date;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.Util;

public class ChangeTimeCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final Date oldTime;
	private Date newTime;

	public ChangeTimeCommand() {
		oldTime = Scene.getInstance().getDate();
	}

	public Date getOldTime() {
		return oldTime;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newTime = Scene.getInstance().getDate();
		Scene.getInstance().setDate(oldTime);
		Util.setSilently(EnergyPanel.getInstance().getTimeSpinner(), oldTime);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setDate(newTime);
		Util.setSilently(EnergyPanel.getInstance().getTimeSpinner(), newTime);
	}

	@Override
	public String getPresentationName() {
		return "Change Time";
	}

}
