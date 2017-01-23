package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;

import com.ardor3d.scenegraph.Node;

public class DeleteNodeCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final Node node;
	private final Node parent;
	private final Foundation foundation;

	public DeleteNodeCommand(final Node node, final Foundation foundation) {
		this.node = node;
		this.foundation = foundation;
		parent = node.getParent();
	}

	public Node getNode() {
		return node;
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		parent.attachChild(node);
		foundation.draw();
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		parent.detachChild(node);
		foundation.draw();
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public String getPresentationName() {
		return "Delete Node";
	}

}
