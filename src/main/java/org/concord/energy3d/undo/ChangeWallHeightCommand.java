package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeWallHeightCommand extends MyAbstractUndoableEdit {

    private static final long serialVersionUID = 1L;
    private final double oldValue;
    private double newValue;
    private final Wall wall;

    public ChangeWallHeightCommand(final Wall wall) {
        this.wall = wall;
        oldValue = wall.getHeight();
    }

    public Wall getWall() {
        return wall;
    }

    public double getOldValue() {
        return oldValue;
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        newValue = wall.getHeight();
        wall.setHeight(oldValue, true);
        Scene.getInstance().redrawAllWallsNow();
        updateLinkedObjects();
        if (SceneManager.getInstance().getSolarHeatMap()) {
            EnergyPanel.getInstance().updateRadiationHeatMap();
        }
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        wall.setHeight(newValue, true);
        Scene.getInstance().redrawAllWallsNow();
        updateLinkedObjects();
        if (SceneManager.getInstance().getSolarHeatMap()) {
            EnergyPanel.getInstance().updateRadiationHeatMap();
        }
    }

    private void updateLinkedObjects() {
        final Foundation foundation = wall.getTopContainer();
        if (foundation.hasSolarReceiver()) {
            foundation.drawSolarReceiver();
            for (final HousePart x : Scene.getInstance().getParts()) {
                if (x instanceof FresnelReflector) {
                    final FresnelReflector reflector = (FresnelReflector) x;
                    if (foundation == reflector.getReceiver() && reflector.isSunBeamVisible()) {
                        reflector.drawSunBeam();
                    }
                } else if (x instanceof Mirror) {
                    final Mirror heliostat = (Mirror) x;
                    if (foundation == heliostat.getReceiver() && heliostat.isSunBeamVisible()) {
                        heliostat.setNormal();
                        heliostat.drawSunBeam();
                    }
                }
            }
        }
    }

    @Override
    public String getPresentationName() {
        return "Change Height for Selected Wall";
    }

}