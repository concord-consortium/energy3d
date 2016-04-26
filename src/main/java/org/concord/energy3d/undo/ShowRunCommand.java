package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.simulation.Graph;

/**
 * WARNING: As this is currently treated as insignificant, this is not fully implemented.
 */

public class ShowRunCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private Graph graph;
	private int runID;
	private boolean oldValue, newValue;

	public ShowRunCommand(Graph graph, int runID) {
		this.graph = graph;
		this.runID = runID;
		oldValue = !graph.isRunHidden(runID);
	}

	public ShowRunCommand(Graph graph, boolean showAll) {
		this.graph = graph;
		this.runID = -1;
		oldValue = !showAll;
	}

	public Graph getGraph() {
		return graph;
	}

	public int getRunID() {
		return runID;
	}

	public boolean isShown() {
		return !oldValue; // return the opposite of the old value because this isn't fully implemented
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = !graph.isRunHidden(runID);
		graph.hideRun(runID, !oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		graph.hideRun(runID, !newValue);
	}

	@Override
	public String getPresentationName() {
		return "Show Run";
	}

	@Override
	public boolean isSignificant() {
		return false;
	}

}
