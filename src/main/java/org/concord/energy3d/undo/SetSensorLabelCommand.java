package org.concord.energy3d.undo;

import org.concord.energy3d.model.Sensor;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class SetSensorLabelCommand extends MyAbstractUndoableEdit {

    private static final long serialVersionUID = 1L;
    private final boolean oldLabelId;
    private boolean newLabelId;
    private final Sensor sensor;

    public SetSensorLabelCommand(final Sensor sensor) {
        this.sensor = sensor;
        oldLabelId = this.sensor.getLabelId();
    }

    public Sensor getSensor() {
        return sensor;
    }

    public boolean getOldLabelId() {
        return oldLabelId;
    }

    public boolean getNewLabelId() {
        return newLabelId;
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        newLabelId = sensor.getLabelId();
        sensor.setLabelId(oldLabelId);
        sensor.draw();
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        sensor.setLabelId(newLabelId);
        sensor.draw();
    }

    @Override
    public String getPresentationName() {
        return "Change Label of Sensor";
    }

}