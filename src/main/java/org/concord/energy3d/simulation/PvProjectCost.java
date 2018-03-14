package org.concord.energy3d.simulation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.PvProjectCostGraph;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

/**
 * Calculate the cost of a PV project, be it a ground-mounted solar farm or a rooftop system.
 * 
 * @author Charles Xie
 * 
 */
public class PvProjectCost extends ProjectCost {

	private static PvProjectCost instance = new PvProjectCost();

	private PvProjectCost() {
	}

	public static PvProjectCost getInstance() {
		return instance;
	}

	public static double getPartCost(final HousePart part) {

		final PvCustomPrice price = Scene.getInstance().getPvCustomPrice();

		if (part instanceof SolarPanel) {
			return price.getTotalCost((SolarPanel) part);
		}

		if (part instanceof Rack) {
			return price.getTotalCost((Rack) part);
		}

		if (part instanceof Foundation) {
			final Foundation f = (Foundation) part;
			return f.getArea() * price.getLandUnitPrice() * price.getLifespan();
		}

		return 0;

	}

	@Override
	public double getCostByFoundation(final Foundation foundation) {
		if (foundation == null || foundation.getProjectType() != Foundation.TYPE_PV_PROJECT) {
			return 0;
		}
		double sum = getPartCost(foundation);
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.getTopContainer() == foundation) {
				if (p instanceof SolarPanel || p instanceof Rack) {
					sum += getPartCost(p);
				}
			}
		}
		return sum;
	}

	@Override
	void showPieChart() {

		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		final Foundation selectedFoundation;
		if (selectedPart == null || selectedPart instanceof Tree || selectedPart instanceof Human) {
			selectedFoundation = null;
		} else if (selectedPart instanceof Foundation) {
			selectedFoundation = (Foundation) selectedPart;
		} else {
			selectedFoundation = selectedPart.getTopContainer();
			selectedPart.setEditPointsVisible(false);
			SceneManager.getInstance().setSelectedPart(selectedFoundation);
		}

		String details = "";
		int count = 0;
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Foundation) {
				count++;
				if (selectedFoundation == null) {
					final Foundation foundation = (Foundation) p;
					details += "$" + (int) getCostByFoundation(foundation) + " (" + foundation.getId() + ") | ";
				}
			}
		}
		if (selectedFoundation == null) {
			if (count > 0) {
				details = details.substring(0, details.length() - 2);
			}
		}

		double landSum = 0;
		double solarPanelSum = 0;
		String info;
		if (selectedFoundation != null) {
			info = "Zone #" + selectedFoundation.getId();
			landSum = getPartCost(selectedFoundation);
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p.getTopContainer() == selectedFoundation) {
					if (p instanceof SolarPanel || p instanceof Rack) {
						solarPanelSum += getPartCost(p);
					}
				}
			}
		} else {
			info = count + " zones";
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p instanceof Foundation) {
					landSum += getPartCost(p);
				} else if (p instanceof SolarPanel || p instanceof Rack) {
					solarPanelSum += getPartCost(p);
				}
			}
		}

		final double[] data = new double[] { landSum, solarPanelSum };
		final String[] legends = new String[] { "Land (" + Scene.getInstance().getPvCustomPrice().getLifespan() + " years)", "Solar Panels" };

		// show them in a popup window
		final PieChart pie = new PieChart(data, PvProjectCostGraph.colors, legends, "$", info, count > 1 ? details : null, true);
		pie.setBackground(Color.WHITE);
		pie.setBorder(BorderFactory.createEtchedBorder());
		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Project Costs by Category", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getContentPane().add(pie, BorderLayout.CENTER);
		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		final JButton buttonClose = new JButton("Close");
		buttonClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.dispose();
			}
		});
		buttonPanel.add(buttonClose);
		dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		dialog.pack();
		dialog.setLocationRelativeTo(MainFrame.getInstance());
		dialog.setVisible(true);

	}

}
