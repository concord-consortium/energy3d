package org.concord.energy3d.simulation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.JDialog;
import javax.swing.JMenuBar;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.model.PartGroup;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.BugReporter;

/**
 * @author Charles Xie
 *
 */
public class GroupAnnualAnalysis extends AnnualAnalysis {

	private final List<HousePart> selectedParts;
	private final PartGroup group;

	public GroupAnnualAnalysis(final PartGroup group) {
		super();
		this.group = group;
		selectedParts = new ArrayList<HousePart>();
		for (final Long i : group.getIds()) {
			selectedParts.add(Scene.getInstance().getPart(i));
		}
		graph = new PartEnergyAnnualGraph();
		graph.setPreferredSize(new Dimension(600, 400));
		graph.setBackground(Color.WHITE);
		double i = 0;
		final double n = selectedParts.size();
		for (final HousePart p : selectedParts) {
			final int a = (int) ((n - i) / n * 128);
			final int b = 255 - a;
			String s = p.getLabelCustomText();
			if (s != null) {
				s = graph.getDataNameDelimiter() + s;
				Graph.setColor("Solar " + p.getId() + s, new Color(255, a, b));
				Graph.setColor("PV " + p.getId() + s, new Color(255, a, b));
				Graph.setColor("CSP " + p.getId() + s, new Color(255, a, b));
				Graph.setColor("PV " + p.getId() + s + " mean", new Color(255, a, b));
				Graph.setColor("CSP " + p.getId() + s + " mean", new Color(255, a, b));
				Graph.setColor("Heat Gain " + p.getId() + s, new Color(a, b, 255));
				Graph.setColor("Building " + p.getId() + s, new Color(a, b, 255));
			} else {
				Graph.setColor("Solar " + p.getId(), new Color(255, a, b));
				Graph.setColor("PV " + p.getId(), new Color(255, a, b));
				Graph.setColor("CSP " + p.getId(), new Color(255, a, b));
				Graph.setColor("PV " + p.getId() + " mean", new Color(255, a, b));
				Graph.setColor("CSP " + p.getId() + " mean", new Color(255, a, b));
				Graph.setColor("Heat Gain " + p.getId(), new Color(a, b, 255));
				Graph.setColor("Building " + p.getId(), new Color(a, b, 255));
			}
			i++;
		}
	}

	@Override
	void runAnalysis(final JDialog parent) {
		graph.info = "Calculating...";
		graph.repaint();
		onStart();
		for (final int m : MONTHS) {
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() {
					if (!analysisStopped) {
						final Calendar c = Heliodon.getInstance().getCalendar();
						c.set(Calendar.MONTH, m);
						Scene.getInstance().updateTrackables();
						final Throwable t = compute();
						if (t != null) {
							stopAnalysis();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									BugReporter.report(t);
								}
							});
						}
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								EnergyPanel.getInstance().getDateSpinner().setValue(c.getTime());
							}
						});
					}
					return null;
				}
			});
		}

		SceneManager.getTaskManager().update(new Callable<Object>() {

			@Override
			public Object call() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						onCompletion();
					}
				});
				return null;
			}

		});
	}

	@Override
	public void updateGraph() {
		for (final HousePart p : selectedParts) {
			final String customText = p.getLabelCustomText();
			if (p instanceof Window) {
				final Window window = (Window) p;
				final double solar = p.getSolarPotentialToday() * window.getSolarHeatGainCoefficient();
				graph.addData("Solar " + p.getId(), solar);
				final double[] loss = p.getHeatLoss();
				double sum = 0;
				for (final double x : loss) {
					sum += x;
				}
				graph.addData("Heat Gain " + p.getId(), -sum);
			} else if (p instanceof Wall || p instanceof Roof) {
				final double[] loss = p.getHeatLoss();
				double sum = 0;
				for (final double x : loss) {
					sum += x;
				}
				graph.addData("Heat Gain " + p.getId(), -sum);
			} else if (p instanceof SolarPanel) {
				if (customText != null) {
					graph.addData("Solar " + p.getId() + graph.getDataNameDelimiter() + customText, ((SolarPanel) p).getYieldToday());
				} else {
					graph.addData("Solar " + p.getId(), ((SolarPanel) p).getYieldToday());
				}
			} else if (p instanceof Rack) {
				if (customText != null) {
					graph.addData("Solar " + p.getId() + graph.getDataNameDelimiter() + customText, ((Rack) p).getYieldToday());
				} else {
					graph.addData("Solar " + p.getId(), ((Rack) p).getYieldToday());
				}
			} else if (p instanceof Mirror) {
				final Mirror mirror = (Mirror) p;
				final double solar = mirror.getSolarPotentialToday() * mirror.getSystemEfficiency();
				if (customText != null) {
					graph.addData("Solar " + p.getId() + graph.getDataNameDelimiter() + customText, solar);
				} else {
					graph.addData("Solar " + p.getId(), solar);
				}
			} else if (p instanceof ParabolicTrough) {
				final ParabolicTrough trough = (ParabolicTrough) p;
				final double solar = trough.getSolarPotentialToday() * trough.getSystemEfficiency();
				if (customText != null) {
					graph.addData("Solar " + p.getId() + graph.getDataNameDelimiter() + customText, solar);
				} else {
					graph.addData("Solar " + p.getId(), solar);
				}
			} else if (p instanceof ParabolicDish) {
				final ParabolicDish dish = (ParabolicDish) p;
				final double solar = dish.getSolarPotentialToday() * dish.getSystemEfficiency();
				if (customText != null) {
					graph.addData("Solar " + p.getId() + graph.getDataNameDelimiter() + customText, solar);
				} else {
					graph.addData("Solar " + p.getId(), solar);
				}
			} else if (p instanceof FresnelReflector) {
				final FresnelReflector reflector = (FresnelReflector) p;
				final double solar = reflector.getSolarPotentialToday() * reflector.getSystemEfficiency();
				if (customText != null) {
					graph.addData("Solar " + p.getId() + graph.getDataNameDelimiter() + customText, solar);
				} else {
					graph.addData("Solar " + p.getId(), solar);
				}
			} else if (p instanceof Foundation) {
				final boolean mean = group.getType().endsWith("(Mean)");
				final Foundation foundation = (Foundation) p;
				switch (foundation.getProjectType()) {
				case Foundation.TYPE_PV_PROJECT:
					double pv = foundation.getPhotovoltaicToday();
					if (mean) {
						pv /= foundation.getNumberOfSolarPanels();
						if (customText != null) {
							graph.addData("PV " + p.getId() + graph.getDataNameDelimiter() + customText + " mean", pv);
						} else {
							graph.addData("PV " + p.getId() + " mean", pv);
						}
					} else {
						if (customText != null) {
							graph.addData("PV " + p.getId() + graph.getDataNameDelimiter() + customText, pv);
						} else {
							graph.addData("PV " + p.getId(), pv);

						}
					}
					break;
				case Foundation.TYPE_CSP_PROJECT:
					double csp = foundation.getCspToday();
					if (mean) {
						csp /= foundation.countParts(new Class[] { Mirror.class, ParabolicTrough.class, ParabolicDish.class });
						if (customText != null) {
							graph.addData("CSP " + p.getId() + graph.getDataNameDelimiter() + customText + " mean", csp);
						} else {
							graph.addData("CSP " + p.getId() + " mean", csp);
						}
					} else {
						if (customText != null) {
							graph.addData("CSP " + p.getId() + graph.getDataNameDelimiter() + customText, csp);
						} else {
							graph.addData("CSP " + p.getId(), csp);
						}
					}
					break;
				case Foundation.TYPE_BUILDING:
					final double totalEnergy = foundation.getTotalEnergyToday();
					graph.addData("Building " + p.getId(), totalEnergy);
					break;
				}
			}
		}
		graph.repaint();
	}

	public void show(final String title) {
		final JDialog dialog = createDialog(title);
		final JMenuBar menuBar = new JMenuBar();
		dialog.setJMenuBar(menuBar);
		menuBar.add(createOptionsMenu(dialog, selectedParts, true, false));
		menuBar.add(createTypesMenu());
		menuBar.add(createRunsMenu());
		dialog.setVisible(true);
	}

	@Override
	public String toJson() {
		String type = "Unknown";
		final ArrayList<String> names = new ArrayList<String>();
		for (final HousePart p : selectedParts) {
			if (p instanceof SolarPanel) {
				names.add("Solar " + p.getId() + (p.getLabelCustom() ? graph.getDataNameDelimiter() + p.getLabelCustomText() : ""));
				type = "Solar Panel";
			} else if (p instanceof Rack) {
				names.add("Solar " + p.getId() + (p.getLabelCustom() ? graph.getDataNameDelimiter() + p.getLabelCustomText() : ""));
				type = "Rack";
			} else if (p instanceof Mirror) {
				names.add("Solar " + p.getId() + (p.getLabelCustom() ? graph.getDataNameDelimiter() + p.getLabelCustomText() : ""));
				type = "Mirror";
			} else if (p instanceof ParabolicTrough) {
				names.add("Solar " + p.getId() + (p.getLabelCustom() ? graph.getDataNameDelimiter() + p.getLabelCustomText() : ""));
				type = "Parabolic Trough";
			} else if (p instanceof ParabolicDish) {
				names.add("Solar " + p.getId() + (p.getLabelCustom() ? graph.getDataNameDelimiter() + p.getLabelCustomText() : ""));
				type = "Parabolic Dish";
			} else if (p instanceof FresnelReflector) {
				names.add("Solar " + p.getId() + (p.getLabelCustom() ? graph.getDataNameDelimiter() + p.getLabelCustomText() : ""));
				type = "Fresnel Reflector";
			} else if (p instanceof Wall) {
				names.add("Heat Gain " + p.getId());
				type = "Wall";
			} else if (p instanceof Roof) {
				names.add("Heat Gain " + p.getId());
				type = "Roof";
			} else if (p instanceof Door) {
				names.add("Heat Gain " + p.getId());
				type = "Door";
			} else if (p instanceof Window) {
				names.add("Solar " + p.getId());
				names.add("Heat Gain " + p.getId());
				type = "Window";
			} else if (p instanceof Foundation) {
				final Foundation foundation = (Foundation) p;
				switch (foundation.getProjectType()) {
				case Foundation.TYPE_PV_PROJECT:
					names.add("PV " + p.getId() + (p.getLabelCustom() ? graph.getDataNameDelimiter() + p.getLabelCustomText() : ""));
					break;
				case Foundation.TYPE_CSP_PROJECT:
					names.add("CSP " + p.getId() + (p.getLabelCustom() ? graph.getDataNameDelimiter() + p.getLabelCustomText() : ""));
					break;
				case Foundation.TYPE_BUILDING:
					names.add("Building " + p.getId());
					break;
				}
				type = "Foundation";
			}
		}
		String s = "{\"Type\": \"" + type + "\", \"Months\": " + getNumberOfDataPoints();
		for (final String name : names) {
			final List<Double> data = graph.getData(name);
			if (data == null) {
				continue;
			}
			s += ", \"" + name + "\": {";
			s += "\"Monthly\": [";
			for (final Double x : data) {
				s += Graph.ENERGY_FORMAT.format(x) + ",";
			}
			s = s.substring(0, s.length() - 1);
			s += "]\n";
			s += ", \"Total\": " + Graph.ENERGY_FORMAT.format(getResult(name));
			s += "}";
		}
		s += "}";
		return s;
	}

}
