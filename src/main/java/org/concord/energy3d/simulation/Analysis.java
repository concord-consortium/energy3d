package org.concord.energy3d.simulation;

import java.awt.Point;

import javax.swing.JButton;

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
	JButton runButton;

	void stopAnalysis() {
		analysisStopped = true;
	}

	void runAnalysis(Runnable task) {
		onStart();
		new Thread(task, getClass().getName()).start();
	}

	void compute() {
		try {
			EnergyPanel.getInstance().computeNow();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		updateGraph();
	}

	abstract void updateGraph();

	void onCompletion() {
		EnergyPanel.getInstance().disableActions(false);
		EnergyPanel.getInstance().progress();
		runButton.setEnabled(true);
	}

	private void onStart() {
		EnergyPanel.getInstance().disableActions(true);
		Util.selectSilently(MainPanel.getInstance().getSolarButton(), true);
		SceneManager.getInstance().setSolarColorMapWithoutUpdate(true);
		graph.clearData();
		SceneManager.getInstance().getSolarLand().setVisible(true);
	}

}
