package org.concord.energy3d.geneticalgorithms.applications;

import java.util.Calendar;

import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.Analysis;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 */
public class SolarOutputObjectiveFunction extends ObjectiveFunction {

    SolarOutputObjectiveFunction(final int type) {
        this.type = type;
    }

    @Override
    public double compute() {
        double result = 0;
        switch (type) {
            case ANNUAL:
                for (final int m : Analysis.MONTHS) {
                    final Calendar c = Heliodon.getInstance().getCalendar();
                    c.set(Calendar.MONTH, m);
                    Scene.getInstance().updateTrackables();
                    EnergyPanel.getInstance().computeNow();
                    result += Util.sum(Scene.getInstance().getSolarResults()[m]);
                }
                break;
            default:
                Scene.getInstance().updateTrackables();
                EnergyPanel.getInstance().computeNow();
                final int month = Heliodon.getInstance().getCalendar().get(Calendar.MONTH);
                result = Util.sum(Scene.getInstance().getSolarResults()[month]);
                break;
        }
        return result;
    }

}