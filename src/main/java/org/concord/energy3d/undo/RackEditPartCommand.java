package org.concord.energy3d.undo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Rack;

public class RackEditPartCommand extends EditPartCommand {
	private static final long serialVersionUID = 1L;
	private final List<EditPartCommand> solarPanelEditCommands;

	public RackEditPartCommand(final Rack rack) {
		super(rack);
		solarPanelEditCommands = new ArrayList<EditPartCommand>(rack.getChildren().size());
		for (final HousePart part : rack.getChildren()) {
			solarPanelEditCommands.add(new EditPartCommand(part));
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for (final EditPartCommand command : solarPanelEditCommands) {
			command.undo();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		for (final EditPartCommand command : solarPanelEditCommands) {
			command.redo();
		}
	}

	@Override
	public void saveNewPoints() {
		super.saveNewPoints();
		for (final EditPartCommand command : solarPanelEditCommands) {
			command.saveNewPoints();
		}
	}

}
