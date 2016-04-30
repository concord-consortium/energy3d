package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;

public class SaveCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private static boolean significant = false;

	public static void setGloabalSignificant(final boolean significant) {
		SaveCommand.significant = significant;
	}

	@Override
	public boolean isSignificant() {
		return significant;
	}

}
