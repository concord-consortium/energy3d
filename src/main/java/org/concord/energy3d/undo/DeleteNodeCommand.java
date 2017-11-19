package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.NodeState;
import org.concord.energy3d.scene.Scene;

import com.ardor3d.scenegraph.Node;

public class DeleteNodeCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final Node node;
	private final NodeState nodeState;
	private final Foundation foundation;
	private final List<HousePart> deletedParts;

	public DeleteNodeCommand(final Node node, final Foundation foundation, final List<HousePart> deletedParts) {
		this.node = node;
		this.foundation = foundation;
		this.deletedParts = deletedParts;
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
		if (deletedParts != null) {
			for (final HousePart p : deletedParts) {
				Scene.getInstance().add(p, true);
			}
		}
		EnergyPanel.getInstance().updateRadiationHeatMap();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		foundation.deleteNode(node);
		if (deletedParts != null) {
			for (final HousePart p : deletedParts) {
				Scene.getInstance().remove(p, true);
			}
		}
		EnergyPanel.getInstance().updateRadiationHeatMap();
	}

	@Override
	public String getPresentationName() {
		return "Delete Node";
	}

}
