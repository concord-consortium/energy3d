package org.concord.energy3d.undo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

/**
 * @author Charles Xie
 */

public class AddArrayCommand extends MyAbstractUndoableEdit {

    private static final long serialVersionUID = 1L;
    private final List<HousePart> oldArray;
    private final List<HousePart> newArray;
    private final HousePart parent;
    private final Class<?> type;
    private final Map<String, Double> parameters;

    public AddArrayCommand(final List<HousePart> parts, final HousePart parent, final Class<?> type) {
        oldArray = new ArrayList<>(parts);
        newArray = new ArrayList<>();
        this.parent = parent;
        this.type = type;
        parameters = new HashMap<>();
    }

    public void put(final String parameter, final double value) {
        parameters.put(parameter, value);
    }

    public Map<String, Double> getParameters() {
        return parameters;
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