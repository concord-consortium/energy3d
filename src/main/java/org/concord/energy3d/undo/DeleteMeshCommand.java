package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.NodeState;
import org.concord.energy3d.model.UserData;
import org.concord.energy3d.scene.Scene;

import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;

public class DeleteMeshCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final Mesh mesh;
	private final Node parent;
	private final Foundation foundation;
	private final List<HousePart> deletedParts;

	public DeleteMeshCommand(final Mesh mesh, final Foundation foundation, final List<HousePart> deletedParts) {
		this.mesh = mesh;
		this.foundation = foundation;
		this.deletedParts = deletedParts;
		parent = mesh.getParent();
	}

	public Mesh getMesh() {
		return mesh;
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		parent.attachChild(mesh);
		foundation.draw();
		final int meshIndex = ((UserData) mesh.getUserData()).getMeshIndex();
		final NodeState ns = foundation.getNodeState(parent);
		ns.getDeletedMeshes().remove(Integer.valueOf(meshIndex));
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
		parent.detachChild(mesh);
		foundation.draw();
		foundation.getNodeState(parent).deleteMesh(((UserData) mesh.getUserData()).getMeshIndex());
		if (deletedParts != null) {
			for (final HousePart p : deletedParts) {
				Scene.getInstance().remove(p, true);
			}
		}
		EnergyPanel.getInstance().updateRadiationHeatMap();
	}

	@Override
	public String getPresentationName() {
		return "Delete Mesh";
	}

}
