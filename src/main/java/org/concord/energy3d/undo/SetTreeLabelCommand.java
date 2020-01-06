package org.concord.energy3d.undo;

import org.concord.energy3d.model.Tree;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class SetTreeLabelCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldLabelId;
	private final boolean oldLabelCustom;
	private boolean newLabelId;
	private boolean newLabelCustom;
	private final Tree tree;

	public SetTreeLabelCommand(final Tree tree) {
		this.tree = tree;
		oldLabelId = tree.getLabelId();
		oldLabelCustom = tree.getLabelCustom();
	}

	public Tree getTree() {
		return tree;
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
		newLabelId = tree.getLabelId();
		newLabelCustom = tree.getLabelCustom();
		tree.setLabelId(oldLabelId);
		tree.setLabelCustom(oldLabelCustom);
		tree.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		tree.setLabelId(newLabelId);
		tree.setLabelCustom(newLabelCustom);
		tree.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Label of Tree";
	}

}
