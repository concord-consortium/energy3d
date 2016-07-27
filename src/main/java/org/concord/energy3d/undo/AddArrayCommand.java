package org.concord.energy3d.undo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

public class AddArrayCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private List<HousePart> oldArray;
	private List<HousePart> newArray;
	private Foundation foundation;
	private Class<?> type;

	public AddArrayCommand(final List<HousePart> parts, final Foundation foundation, Class<?> type) {
		oldArray = new ArrayList<HousePart>(parts);
		newArray = new ArrayList<HousePart>();
		this.foundation = foundation;
		this.type = type;
	}

	public Foundation getFoundation() {
		return foundation;
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
		for (final HousePart c : foundation.getChildren()) {
			if (type.isInstance(c))
				newArray.add(c);
		}
		for (final HousePart p : newArray) {
			Scene.getInstance().remove(p, false);
		}
		for (HousePart p : oldArray) {
			Scene.getInstance().add(p, false);
		}
		foundation.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		for (final HousePart p : oldArray) {
			Scene.getInstance().remove(p, false);
		}
		for (HousePart p : newArray) {
			Scene.getInstance().add(p, false);
		}
		foundation.draw();
	}

	@Override
	public String getPresentationName() {
		return "Add " + type.getSimpleName() + " Array";
	}

}