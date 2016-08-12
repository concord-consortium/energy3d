package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.Graph;
import org.concord.energy3d.simulation.MirrorDailyAnalysis;
import org.concord.energy3d.simulation.PartEnergyDailyGraph;
import org.concord.energy3d.simulation.SolarRadiation;

/**
 * @author Charles Xie
 *
 */
public class CspStationDailyEnergyGraph extends JPanel {

	private static final long serialVersionUID = 1L;

	private PartEnergyDailyGraph graph;
	private Foundation base;
	private Box buttonPanel;

	public CspStationDailyEnergyGraph() {
		super(new BorderLayout());

		buttonPanel = new Box(BoxLayout.Y_AXIS);
		buttonPanel.setBackground(Color.WHITE);
		buttonPanel.add(Box.createVerticalGlue());
		JButton button = new JButton("Show");
		button.setAlignmentX(CENTER_ALIGNMENT);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SceneManager.getInstance().autoSelectBuilding(true);
				HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart instanceof Foundation) {
					addGraph((Foundation) selectedPart);
					EnergyPanel.getInstance().validate();
				}
			}
		});
		buttonPanel.add(button);
		buttonPanel.add(Box.createVerticalGlue());

		graph = new PartEnergyDailyGraph();
		graph.setPopup(false);
		graph.setBackground(Color.WHITE);
		graph.setBorder(BorderFactory.createEtchedBorder());
		graph.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (SceneManager.getInstance().autoSelectBuilding(true) instanceof Foundation) {
						MirrorDailyAnalysis analysis = new MirrorDailyAnalysis();
						analysis.updateGraph();
						analysis.show();
					}
				}
			}
		});
	}

	public void setCalendar(Calendar today) {
		graph.setCalendar(today);
	}

	public Foundation getBuilding() {
		return base;
	}

	public double getResult(final String name) {
		return graph.getSum(name);
	}

	public void clearData() {
		graph.clearData();
		graph.repaint();
	}

	public void removeGraph() {
		removeAll();
		repaint();
		add(buttonPanel, BorderLayout.CENTER);
		repaint();
		EnergyPanel.getInstance().validate();
	}

	public boolean hasGraph() {
		return getComponentCount() > 0 && getComponent(0) == graph;
	}

	public void updateGraph() {
		if (base == null)
			return;
		graph.clearData();
		List<Mirror> panels = base.getMirrors();
		if (!panels.isEmpty()) {
			for (int i = 0; i < 24; i++) {
				SolarRadiation.getInstance().computeEnergyAtHour(i);
				double output = 0;
				for (Mirror sp : panels) {
					output += sp.getSolarPotentialNow() * sp.getReflectivity();
				}
				graph.addData("Solar", output);
			}
		}
		repaint();
	}

	public void addGraph(Foundation base) {

		removeAll();

		this.base = base;
		graph.setPreferredSize(new Dimension(getWidth() - 5, getHeight() - 5));
		if (SceneManager.getInstance().getSolarHeatMap()) {
			updateGraph();
		}
		add(graph, BorderLayout.NORTH);
		repaint();
		EnergyPanel.getInstance().validate();
	}

	public String toJson() {
		String s = "{";
		if (base != null) {
			s += "\"Foundation\": " + base.getId();
			List<Double> data = graph.getData("Solar");
			if (data != null) {
				s += ", \"Solar\": {";
				s += "\"Hourly\": [";
				for (Double x : data) {
					s += Graph.FIVE_DECIMALS.format(x) + ",";
				}
				s = s.substring(0, s.length() - 1);
				s += "]\n";
				s += ", \"Total\": " + Graph.ENERGY_FORMAT.format(getResult("Solar"));
				s += "}";
			}
		} else {
			// TODO
		}
		s += "}";
		return s;
	}

}
