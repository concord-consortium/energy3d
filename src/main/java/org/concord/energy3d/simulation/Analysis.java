package org.concord.energy3d.simulation;

import java.awt.Point;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
public abstract class Analysis {

	Graph graph;
	volatile boolean analysisStopped;
	static Point windowLocation = new Point();

	protected void stopAnalysis() {
		analysisStopped = true;
	}
	
	protected void runAnalysis(Runnable task) {
		Util.selectSilently(MainPanel.getInstance().getSolarButton(), true);
		SceneManager.getInstance().setSolarColorMapWithoutUpdate(true);
		graph.clearData();
		SceneManager.getInstance().getSolarLand().setVisible(true);
		EnergyPanel.getInstance().disableActions(true);
		new Thread(task, getClass().getName()).start();
	}

	abstract void updateGraph();

}
