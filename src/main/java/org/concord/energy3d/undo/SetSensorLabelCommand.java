package org.concord.energy3d.undo;

import org.concord.energy3d.model.Sensor;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class SetSensorLabelCommand extends MyAbstractUndoableEdit {

    private static final long serialVersionUID = 1L;
    private final boolean oldLabelId;
    private final boolean oldLabelCustom;
    private final boolean oldLabelLigthSensorOutput;
    private final boolean oldLabelHeatFluxSensorOutput;
    private boolean newLabelId;
    private boolean newLabelCustom;
    private boolean newLabelLigthSensorOutput;
    private boolean newLabelHeatFluxSensorOutput;
    private final Sensor sensor;

    public SetSensorLabelCommand(final Sensor sensor) {
        this.sensor = sensor;
        oldLabelId = sensor.getLabelId();
        oldLabelCustom = sensor.getLabelCustom();
        oldLabelLigthSensorOutput = sensor.getLabelLightOutput();
        oldLabelHeatFluxSensorOutput = sensor.getLabelHeatFluxOutput();
    }

    public Sensor getSensor() {
        return sensor;
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        newLabelId = sensor.getLabelId();
        newLabelCustom = sensor.getLabelCustom();
        newLabelLigthSensorOutput = sensor.getLabelLightOutput();
        newLabelHeatFluxSensorOutput = sensor.getLabelHeatFluxOutput();
        sensor.setLabelId(oldLabelId);
        sensor.setLabelCustom(oldLabelCustom);
        sensor.setLabelLightOutput(oldLabelLigthSensorOutput);
        sensor.setLabelHeatFluxOutput(oldLabelHeatFluxSensorOutput);
        sensor.draw();
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        sensor.setLabelId(newLabelId);
        sensor.setLabelCustom(newLabelCustom);
        sensor.setLabelLightOutput(newLabelLigthSensorOutput);
        sensor.setLabelHeatFluxOutput(newLabelHeatFluxSensorOutput);
        sensor.draw();
    }

    @Override
    public String getPresentationName() {
        return "Change Label of Sensor";
    }

}