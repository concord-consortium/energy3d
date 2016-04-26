package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.simulation.Graph;

/**
 * WARNING: As this is currently treated as insignificant, this is not fully implemented.
 */

public class ShowCurveCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private Graph graph;
	private String curveName;
	private boolean oldValue, newValue;

	public ShowCurveCommand(Graph graph, String curveName) {
		this.graph = graph;
		this.curveName = curveName;
		oldValue = !graph.isDataHidden(curveName);
	}

	public ShowCurveCommand(Graph graph, boolean showAll) {
		this.graph = graph;
		this.curveName = "All";
		oldValue = showAll;
	}

	public Graph getGraph() {
		return graph;
	}

	public String getCurveName() {
		return curveName;
	}

	public boolean isShown() {
		return !oldValue; // return the opposite of the old value because this isn't fully implemented
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = !graph.isDataHidden(curveName);
		graph.hideData(curveName, !oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		graph.hideData(curveName, !newValue);
	}

	@Override
	public String getPresentationName() {
		return "Show Curve";
	}

	@Override
	public boolean isSignificant() {
		return false;
	}

}
