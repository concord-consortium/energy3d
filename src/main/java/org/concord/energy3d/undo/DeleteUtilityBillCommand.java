package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.simulation.UtilityBill;

public class DeleteUtilityBillCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private Foundation foundation;
	private UtilityBill bill;

	public DeleteUtilityBillCommand(Foundation foundation) {
		this.foundation = foundation;
		bill = foundation.getUtilityBill();
	}

	public Foundation getFoundation() {
		return foundation;
	}

	public UtilityBill getUtilityBill() {
		return bill;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		foundation.setUtilityBill(bill);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		foundation.setUtilityBill(null);
	}

	@Override
	public String getPresentationName() {
		return "Delete Utility Bill";
	}

}
