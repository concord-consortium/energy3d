package org.concord.energy3d.simulation;

import java.awt.Point;

import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public abstract class ProjectCost {

	static Point windowLocation = new Point();

	abstract void showPieChart();

	public abstract double getCostByFoundation(final Foundation foundation);

	public void showGraph() {
		showPieChart();
		TimeSeriesLogger.getInstance().logAnalysis(this);
	}

	public static double getCost(final HousePart p) {
		final Foundation f = p instanceof Foundation ? (Foundation) p : p.getTopContainer();
		if (f == null) {
			return BuildingCost.getPartCost(p);
		}
		switch (f.getProjectType()) {
		case Foundation.TYPE_PV_PROJECT:
			return PvProjectCost.getPartCost(p);
		case Foundation.TYPE_CSP_PROJECT:
			return CspProjectCost.getPartCost(p);
		default:
			return BuildingCost.getPartCost(p);
		}
	}

	static long getFoundationId(final HousePart p) {
		if (p == null) {
			return -1;
		}
		if (p instanceof Foundation) {
			return p.getId();
		}
		final HousePart x = p.getTopContainer();
		return x == null ? -1 : x.getId();
	}

	public String toJson() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		String s;
		if (selectedPart != null) {
			s = "{";
			s += "\"Foundation\": " + getFoundationId(selectedPart);
			s += ", \"Amount\": " + getCostByFoundation(selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer());
			s += "}";
		} else {
			s = "[";
			int count = 0;
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p instanceof Foundation) {
					count++;
					s += "{\"Foundation\": " + getFoundationId(p) + ", \"Amount\": " + getCostByFoundation((Foundation) p) + "}, ";
				}
			}
			if (count > 0) {
				s = s.substring(0, s.length() - 2);
			}
			s += "]";

		}
		return s;
	}

}