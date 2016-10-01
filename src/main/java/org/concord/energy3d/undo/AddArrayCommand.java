package org.concord.energy3d.undo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

public class AddArrayCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final List<HousePart> oldArray;
	private final List<HousePart> newArray;
	private final HousePart parent;
	private final Class<?> type;

	public AddArrayCommand(final List<HousePart> parts, final HousePart parent, final Class<?> type) {
		oldArray = new ArrayList<HousePart>(parts);
		newArray = new ArrayList<HousePart>();
		this.parent = parent;
		this.type = type;
	}

	public HousePart getParent() {
		return parent;
	}

	public List<HousePart> getOldArray() {
		return oldArray;
	}

	public Class<?> getType() {
		return type;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for (final HousePart c : parent.getChildren()) {
			if (type.isInstance(c)) {
				newArray.add(c);
			}
		}
		for (final HousePart p : newArray) {
			Scene.getInstance().remove(p, false);
		}
		for (final HousePart p : oldArray) {
			Scene.getInstance().add(p, false);
		}
		parent.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		for (final HousePart p : oldArray) {
			Scene.getInstance().remove(p, false);
		}
		for (final HousePart p : newArray) {
			Scene.getInstance().add(p, false);
		}
		parent.draw();
	}

	@Override
	public String getPresentationName() {
		return "Add " + type.getSimpleName() + " Array";
	}

}