package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.NodeState;

import com.ardor3d.scenegraph.Node;

public class DeleteNodeCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final Node node;
	private final NodeState nodeState;
	private final Foundation foundation;

	public DeleteNodeCommand(final Node node, final Foundation foundation) {
		this.node = node;
		this.foundation = foundation;
		nodeState = foundation.getNodeState(node);
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
		foundation.addNode(node, nodeState);
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		foundation.deleteNode(node);
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public String getPresentationName() {
		return "Delete Node";
	}

}
