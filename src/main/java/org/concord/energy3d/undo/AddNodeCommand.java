package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.NodeState;
import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.scenegraph.Node;

public class AddNodeCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final Node node;
	private final NodeState nodeState;
	private final Foundation foundation;

	public AddNodeCommand(final Foundation foundation) {
		this.foundation = foundation;
		final List<Node> nodes = foundation.getImportedNodes();
		if (nodes.isEmpty()) {
			node = null;
			nodeState = null;
		} else {
			node = nodes.get(nodes.size() - 1);
			nodeState = foundation.getNodeState(node);
		}
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
		if (node != null) {
			foundation.deleteNode(node);
		}
		if (SceneManager.getInstance().getSolarHeatMap()) {
			EnergyPanel.getInstance().updateRadiationHeatMap();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (node != null) {
			foundation.addNode(node, nodeState);
		}
		if (SceneManager.getInstance().getSolarHeatMap()) {
			EnergyPanel.getInstance().updateRadiationHeatMap();
		}
	}

	@Override
	public String getPresentationName() {
		return "Add Node";
	}

}
