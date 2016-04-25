package org.concord.energy3d.undo;

import java.awt.Component;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.util.Util;

public class ChangeGraphTabCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private Component orgComponent, newComponent;

	public ChangeGraphTabCommand() {
		orgComponent = (Component) EnergyPanel.getInstance().getGraphTabbedPane().getClientProperty("Selection");
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newComponent = EnergyPanel.getInstance().getGraphTabbedPane().getSelectedComponent();
		Util.setSilently(EnergyPanel.getInstance().getGraphTabbedPane(), orgComponent);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Util.setSilently(EnergyPanel.getInstance().getGraphTabbedPane(), newComponent);
	}

	public String getCurrentTitle() {
		return EnergyPanel.getInstance().getGraphTabbedPane().getTitleAt(EnergyPanel.getInstance().getGraphTabbedPane().getSelectedIndex());
	}

	@Override
	public String getPresentationName() {
		return "Change Graph Tab";
	}

	@Override
	public boolean isSignificant() {
		return false;
	}

}
