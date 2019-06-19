package org.concord.energy3d.undo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.*;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeHeightForAllWallsCommand extends MyAbstractUndoableEdit {

    private static final long serialVersionUID = 1L;
    private final double[] oldValues;
    private double[] newValues;
    private final List<HousePart> walls;
    private final List<Foundation> foundations;

    public ChangeHeightForAllWallsCommand(final Wall w) {
        walls = Scene.getInstance().getAllPartsOfSameType(w);
        final int n = walls.size();
        oldValues = new double[n];
        foundations = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            oldValues[i] = walls.get(i).getHeight();
            Foundation f = walls.get(i).getTopContainer();
            if (!foundations.contains(f)) {
                foundations.add(f);
            }
        }
    }

    public List<HousePart> getWalls() {
        return walls;
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        final int n = walls.size();
        newValues = new double[n];
        for (int i = 0; i < n; i++) {
            final Wall w = (Wall) walls.get(i);
            newValues[i] = w.getHeight();
            w.setHeight(oldValues[i], true);
        }
        updateLinkedObjects();
        Scene.getInstance().redrawAllWallsNow();
        if (SceneManager.getInstance().getSolarHeatMap()) {
            EnergyPanel.getInstance().updateRadiationHeatMap();
        }
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        final int n = walls.size();
        for (int i = 0; i < n; i++) {
            final Wall w = (Wall) walls.get(i);
            w.setHeight(newValues[i], true);
        }
        updateLinkedObjects();
        Scene.getInstance().redrawAllWallsNow();
        if (SceneManager.getInstance().getSolarHeatMap()) {
            EnergyPanel.getInstance().updateRadiationHeatMap();
        }
    }

    private void updateLinkedObjects() {
        if (walls.isEmpty() || foundations.isEmpty()) {
            return;
        }
        for (Foundation foundation : foundations) {
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
    }

    @Override
    public String getPresentationName() {
        return "Change Height for All Walls";
    }

}