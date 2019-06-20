package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;

public class AdjustThermostatCommand extends MyAbstractUndoableEdit {

    private static final long serialVersionUID = 1L;
    private final int[][][] oldTemperatures;
    private int[][][] newTemperatures;
    private final Foundation foundation;

    public AdjustThermostatCommand(final Foundation foundation) {
        this.foundation = foundation;
        oldTemperatures = new int[12][7][25];
        final int[][][] values = foundation.getThermostat().getTemperatures();
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 7; j++) {
                System.arraycopy(values[i][j], 0, oldTemperatures[i][j], 0, 25);
            }
        }
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        newTemperatures = new int[12][7][25];
        final int[][][] values = foundation.getThermostat().getTemperatures();
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 7; j++) {
                System.arraycopy(values[i][j], 0, newTemperatures[i][j], 0, 25);
            }
        }
        foundation.getThermostat().setTemperatures(oldTemperatures);
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        foundation.getThermostat().setTemperatures(newTemperatures);
    }

    public Foundation getFoundation() {
        return foundation;
    }

    @Override
    public char getOneLetterCode() {
        return 'T';
    }

    @Override
    public String getPresentationName() {
        return "Thermostat Adjustment";
    }

}