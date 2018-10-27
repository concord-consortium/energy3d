package org.concord.energy3d.gui;

import java.awt.EventQueue;

import javax.swing.JOptionPane;

import org.concord.energy3d.geneticalgorithms.applications.SolarPanelArrayOptimizer;
import org.concord.energy3d.geneticalgorithms.applications.SolarPanelTiltAngleOptimizer;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.PartGroup;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.GroupAnnualAnalysis;
import org.concord.energy3d.simulation.GroupDailyAnalysis;
import org.concord.energy3d.simulation.PvAnnualAnalysis;
import org.concord.energy3d.simulation.PvDailyAnalysis;

/**
 * @author Charles Xie
 *
 */
public final class TaskFactory {

	public static void run(final String taskName) {
		if (taskName.startsWith("Daily Yield Analysis of Solar Panels")) {
			dailyYieldAnalysisOfSolarPanels();
		} else if (taskName.startsWith("Annual Yield Analysis of Solar Panels")) {
			annualYieldAnalysisOfSolarPanels();
		} else if (taskName.startsWith("Daily Analysis for Group")) {
			TaskFactory.dailyAnalysisForGroup(taskName);
		} else if (taskName.startsWith("Annual Analysis for Group")) {
			TaskFactory.annualAnalysisForGroup(taskName);
		} else if (taskName.startsWith("Solar Panel Tilt Angle Optimizer")) {
			TaskFactory.solarPanelTiltAngleOptimizer(taskName);
		} else if (taskName.startsWith("Solar Panel Array Optimizer")) {
			TaskFactory.solarPanelArrayOptimizer(taskName);
		} else if (taskName.startsWith("Solar Panel Array Layout Manager")) {
			TaskFactory.solarPanelArrayLayoutManager(taskName);
		}
	}

	public static void dailyYieldAnalysisOfSolarPanels() {

		if (EnergyPanel.getInstance().checkCity()) {
			int n = Scene.getInstance().countParts(new Class[] { SolarPanel.class, Rack.class });
			if (n <= 0) {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no solar panel to analyze.", "No Solar Panel", JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (EnergyPanel.getInstance().adjustCellSize()) {
				return;
			}
			final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			if (selectedPart != null) {
				Foundation foundation;
				if (selectedPart instanceof Foundation) {
					foundation = (Foundation) selectedPart;
				} else {
					foundation = selectedPart.getTopContainer();
				}
				if (foundation != null) {
					n = foundation.countParts(new Class[] { SolarPanel.class, Rack.class });
					if (n <= 0) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no solar panel on this foundation to analyze.", "No Solar Panel", JOptionPane.WARNING_MESSAGE);
						return;
					}
				}
			}
			final PvDailyAnalysis a = new PvDailyAnalysis();
			if (SceneManager.getInstance().getSolarHeatMap()) {
				a.updateGraph();
			}
			a.show();
		}

	}

	public static void annualYieldAnalysisOfSolarPanels() {
		if (EnergyPanel.getInstance().checkCity()) {
			int n = Scene.getInstance().countParts(new Class[] { SolarPanel.class, Rack.class });
			if (n <= 0) {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no solar panel to analyze.", "No Solar Panel", JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (EnergyPanel.getInstance().adjustCellSize()) {
				return;
			}
			final PvAnnualAnalysis a = new PvAnnualAnalysis();
			final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			if (selectedPart != null) {
				Foundation foundation;
				if (selectedPart instanceof Foundation) {
					foundation = (Foundation) selectedPart;
				} else {
					foundation = selectedPart.getTopContainer();
				}
				if (foundation != null) {
					n = foundation.countParts(new Class[] { SolarPanel.class, Rack.class });
					if (n <= 0) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no solar panel on this foundation to analyze.", "No Solar Panel", JOptionPane.WARNING_MESSAGE);
						return;
					}
					a.setUtilityBill(foundation.getUtilityBill());
				}
			} else {
				a.setUtilityBill(Scene.getInstance().getUtilityBill());
			}
			a.show();
		}
	}

	public static void dailyAnalysisForGroup(final String taskName) {
		if (EnergyPanel.getInstance().checkCity()) {
			PartGroup g = null;
			final GroupSelector selector = new GroupSelector();
			for (final String s : GroupSelector.types) {
				final int index = taskName.indexOf(s);
				if (index > 0) {
					selector.setCurrentGroupType(s);
					try {
						final String t = taskName.substring(index + s.length()).trim();
						if (!t.equals("")) {
							g = new PartGroup(s);
							final String[] a = t.split(",");
							for (final String x : a) {
								g.addId(Integer.parseInt(x.trim()));
							}
						}
					} catch (final Exception e) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + taskName + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
						g = null;
					}
					break;
				}
			}
			if (g == null) {
				g = selector.select();
			}
			if (g != null) {
				final PartGroup g2 = g;
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() { // for some reason, this may be delayed in the AWT Event Queue in order to avoid a HTML form NullPointerException
						final GroupDailyAnalysis a = new GroupDailyAnalysis(g2);
						a.show(g2.getType() + ": " + g2.getIds());
					}
				});
			}
			SceneManager.getInstance().hideAllEditPoints();
		}
	}

	public static void annualAnalysisForGroup(final String taskName) {
		if (EnergyPanel.getInstance().checkCity()) {
			PartGroup g = null;
			final GroupSelector selector = new GroupSelector();
			for (final String s : GroupSelector.types) {
				final int index = taskName.indexOf(s);
				if (index > 0) {
					selector.setCurrentGroupType(s);
					try {
						final String t = taskName.substring(index + s.length()).trim();
						if (!t.equals("")) {
							g = new PartGroup(s);
							final String[] a = t.split(",");
							for (final String x : a) {
								g.addId(Integer.parseInt(x.trim()));
							}
						}
					} catch (final Exception e) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + taskName + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
						g = null;
					}
					break;
				}
			}
			if (g == null) {
				g = selector.select();
			}
			if (g != null) {
				final PartGroup g2 = g;
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() { // for some reason, this may be delayed in the AWT Event Queue in order to avoid a HTML form NullPointerException
						final GroupAnnualAnalysis a = new GroupAnnualAnalysis(g2);
						a.show(g2.getType() + ": " + g2.getIds());
					}
				});
			}
			SceneManager.getInstance().hideAllEditPoints();
		}
	}

	public static void solarPanelTiltAngleOptimizer(final String taskName) {
		String s = taskName.substring("Solar Panel Tilt Angle Optimizer".length()).trim();
		if ("Stop".equalsIgnoreCase(s)) {
			SolarPanelTiltAngleOptimizer.stopIt();
		} else {
			boolean silent = false;
			if (s.startsWith("silent")) {
				silent = true;
				s = s.substring("silent".length()).trim();
			}
			boolean local = false;
			if (s.startsWith("local")) {
				local = true;
				s = s.substring("local".length()).trim();
			}
			boolean daily = false;
			if (s.startsWith("daily")) {
				daily = true;
				s = s.substring("daily".length()).trim();
			}
			boolean profit = false;
			if (s.startsWith("profit")) {
				profit = true;
				s = s.substring("profit".length()).trim();
			}
			try {
				final String[] t = s.split("\\s+");
				final int foundationID = Integer.parseInt(t[0]);
				int population = -1;
				int generations = -1;
				float mutation = -1;
				if (t.length > 1) {
					population = Integer.parseInt(t[1]);
				}
				if (t.length > 2) {
					generations = Integer.parseInt(t[2]);
				}
				if (t.length > 3) {
					mutation = Float.parseFloat(t[3]);
				}
				final HousePart p = Scene.getInstance().getPart(foundationID);
				if (p instanceof Foundation) {
					if (silent) {
						SolarPanelTiltAngleOptimizer.runIt((Foundation) p, local, daily, profit, population, generations, mutation);
					} else {
						SolarPanelTiltAngleOptimizer.make((Foundation) p);
					}
				} else {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + taskName + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
				}
			} catch (final Exception e) {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + taskName + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public static void solarPanelArrayOptimizer(final String taskName) {
		String s = taskName.substring("Solar Panel Array Optimizer".length()).trim();
		if ("Stop".equalsIgnoreCase(s)) {
			SolarPanelArrayOptimizer.stopIt();
		} else {
			boolean silent = false;
			if (s.startsWith("silent")) {
				silent = true;
				s = s.substring("silent".length()).trim();
			}
			boolean local = false;
			if (s.startsWith("local")) {
				local = true;
				s = s.substring("local".length()).trim();
			}
			boolean daily = false;
			if (s.startsWith("daily")) {
				daily = true;
				s = s.substring("daily".length()).trim();
			}
			boolean profit = false;
			if (s.startsWith("profit")) {
				profit = true;
				s = s.substring("profit".length()).trim();
			}
			try {
				final String[] t = s.split("\\s+");
				final int foundationID = Integer.parseInt(t[0]);
				int population = -1;
				int generations = -1;
				float mutation = -1;
				if (t.length > 1) {
					population = Integer.parseInt(t[1]);
				}
				if (t.length > 2) {
					generations = Integer.parseInt(t[2]);
				}
				if (t.length > 3) {
					mutation = Float.parseFloat(t[3]);
				}
				final HousePart p = Scene.getInstance().getPart(foundationID);
				if (p instanceof Foundation) {
					if (silent) {
						SolarPanelArrayOptimizer.runIt((Foundation) p, local, daily, profit, population, generations, mutation);
					} else {
						SolarPanelArrayOptimizer.make((Foundation) p);
					}
				} else {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + taskName + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
				}
			} catch (final Exception e) {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + taskName + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public static void solarPanelArrayLayoutManager(final String options) {
		final String s = options.substring("Solar Panel Array Layout Manager".length()).trim();
		try {
			final int i = Integer.parseInt(s);
			final HousePart p = Scene.getInstance().getPart(i);
			if (p instanceof Foundation) {
				SceneManager.getInstance().setSelectedPart(p);
				new PopupMenuForFoundation.SolarPanelArrayLayoutManager().open();
			} else {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + options + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + options + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
		}
	}

}
