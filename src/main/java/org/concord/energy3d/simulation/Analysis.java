package org.concord.energy3d.simulation;

import java.awt.Cursor;
import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JButton;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Building;
import org.concord.energy3d.model.Foundation;
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
		graph.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			EnergyPanel.getInstance().computeNow();
		} catch (final Throwable e) {
			e.printStackTrace();
			return e;
		} finally {
			graph.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		updateGraph();
		SceneManager.getInstance().refreshNow();
		return null;
	}

	public abstract void updateGraph();

	void onCompletion() {
		TimeSeriesLogger.getInstance().logAnalysis(this);
		EnergyPanel.getInstance().progress(0);
		runButton.setEnabled(true);
		EnergyPanel.getInstance().disableDateSpinner(false);
	}

	void onStart() {
		EnergyPanel.getInstance().disableDateSpinner(true);
		SceneManager.getInstance().setHeatFluxDaily(true);
		Util.selectSilently(MainPanel.getInstance().getEnergyViewButton(), true);
		SceneManager.getInstance().setSolarHeatMapWithoutUpdate(true);
		SceneManager.getInstance().setHeatFluxVectorsVisible(true);
		SceneManager.getInstance().getSolarLand().setVisible(true);
		graph.clearData();
	}

	static boolean isBuildingComplete(final Foundation foundation) {
		return new Building(foundation).isWallComplete();
	}

	public abstract String toJson();

}
