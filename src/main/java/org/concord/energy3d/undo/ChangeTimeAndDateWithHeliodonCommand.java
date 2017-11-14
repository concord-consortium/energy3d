package org.concord.energy3d.undo;

import java.util.Date;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.Util;

public class ChangeTimeAndDateWithHeliodonCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final Date oldTime;
	private Date newTime;

	public ChangeTimeAndDateWithHeliodonCommand(final Date oldTime) {
		this.oldTime = oldTime;
	}

	public Date getOldTime() {
		return oldTime;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newTime = Heliodon.getInstance().getCalendar().getTime();
		Scene.getInstance().setDate(oldTime);
		Heliodon.getInstance().setDate(oldTime);
		Heliodon.getInstance().setTime(oldTime);
		Heliodon.getInstance().update();
		Util.setSilently(EnergyPanel.getInstance().getTimeSpinner(), oldTime);
		Util.setSilently(EnergyPanel.getInstance().getDateSpinner(), oldTime);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setDate(newTime);
		Heliodon.getInstance().setDate(newTime);
		Heliodon.getInstance().setTime(newTime);
		Heliodon.getInstance().update();
		Util.setSilently(EnergyPanel.getInstance().getTimeSpinner(), newTime);
		Util.setSilently(EnergyPanel.getInstance().getDateSpinner(), newTime);
	}

	@Override
	public String getPresentationName() {
		return "Change Time and Date";
	}

}
