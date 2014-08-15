package org.concord.energy3d.simulation;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JButton;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Building;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.Scene;
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

	public double getResult(final String name) {
		return graph.getSum(name);
	}

	public Map<String, Double> getRecordedResults(final String name) {
		final Map<String, Double> recordedResults = new TreeMap<String, Double>();
		for (final Results r : graph.getRecords()) {
			final Map<String, List<Double>> x = r.getData();
			final List<Double> list = x.get(name);
			if (list != null) {
				double sum = 0;
				for (final Double d : list) {
					sum += d;
				}
				recordedResults.put(r.getID() + (r.getFileName() == null ? "" : " (file: " + r.getFileName() + ")"), sum);
			}
		}
		return recordedResults;
	}

	public int getNumberOfDataPoints() {
		return graph.getLength();
	}

	void stopAnalysis() {
		analysisStopped = true;
	}

	void runAnalysis(final Runnable task) {
		onStart();
		new Thread(task, getClass().getName()).start();
	}

	// return the exception if unsuccessful
	Throwable compute() {
		try {
			EnergyPanel.getInstance().computeNow();
		} catch (final Throwable e) {
			e.printStackTrace();
			return e;
		}
		updateGraph();
		Scene.getInstance().redrawAll();
		SceneManager.getInstance().refreshNow();
		return null;
	}

	abstract void updateGraph();

	void onCompletion() {
		EnergyPanel.getInstance().requestDisableActions(null);
		EnergyPanel.getInstance().progress(0);
		runButton.setEnabled(true);
	}

	private void onStart() {
		SceneManager.getInstance().setHeatFlowDaily(true);
		EnergyPanel.getInstance().requestDisableActions(this);
		Util.selectSilently(MainPanel.getInstance().getSolarButton(), true);
		SceneManager.getInstance().setSolarColorMapWithoutUpdate(true);
		SceneManager.getInstance().setHeatFlowArrows(true);
		graph.clearData();
		SceneManager.getInstance().getSolarLand().setVisible(true);
	}

	public static boolean isBuildingComplete(final Foundation foundation) {
		final Building b = new Building((int) foundation.getId());
		final ArrayList<HousePart> children = foundation.getChildren();
		for (final HousePart x : children) {
			if (x instanceof Wall)
				b.addWall((Wall) x);
		}
		if (!b.isWallComplete())
			return false;
		return b.getRoof() != null;
	}

}
