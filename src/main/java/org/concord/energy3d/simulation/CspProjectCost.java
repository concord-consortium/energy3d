package org.concord.energy3d.simulation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.model.SolarCollector;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

/**
 * Calculate the cost of a CSP project, covering four types of CSP technologies.
 * 
 * @author Charles Xie
 * 
 */
public class CspProjectCost extends ProjectCost {

	private static CspProjectCost instance = new CspProjectCost();

	private CspProjectCost() {
	}

	public static CspProjectCost getInstance() {
		return instance;
	}

	static double getPartCost(final HousePart part) {

		final CspCustomPrice price = Scene.getInstance().getCspCustomPrice();

		if (part instanceof Mirror) {
			return price.getHeliostatUnitPrice() * part.getArea();
		}

		if (part instanceof ParabolicTrough) {
			return price.getParabolicTroughUnitPrice() * part.getArea();
		}

		if (part instanceof ParabolicDish) {
			return price.getParabolicDishUnitPrice() * part.getArea();
		}

		if (part instanceof FresnelReflector) {
			return price.getFresnelReflectorUnitPrice() * part.getArea();
		}

		if (part instanceof Foundation) {
			final Foundation f = (Foundation) part;
			if (f.isSolarPowerTower()) {
				return price.getTowerUnitPrice() * f.getSolarReceiverHeight(0) * Scene.getInstance().getAnnotationScale();
			}
			return f.getArea() * price.getLandUnitPrice() * price.getLifespan();
		}

		return 0;

	}

	@Override
	public double getCostByFoundation(final Foundation foundation) {
		if (foundation == null || foundation.getProjectType() != Foundation.TYPE_CSP_STATION) {
			return 0;
		}
		double sum = getPartCost(foundation);
		if (foundation.isSolarPowerTower()) {
			return sum;
		}
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.getTopContainer() == foundation) {
				if (p instanceof SolarCollector) { // assuming that sensor doesn't cost anything
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
				final Foundation foundation = (Foundation) p;
				if (!foundation.isSolarPowerTower()) {
					count++;
					if (selectedFoundation == null) {
						details += "$" + (int) getCostByFoundation(foundation) + " (" + foundation.getId() + ") | ";
					}
				}
			}
		}
		if (selectedFoundation == null) {
			if (count > 0) {
				details = details.substring(0, details.length() - 2);
			}
		}

		double landSum = 0;
		double solarCollectorSum = 0;
		double towerSum = 0;
		String info;
		if (selectedFoundation != null) {
			info = "Zone #" + selectedFoundation.getId();
			if (selectedFoundation.isSolarPowerTower()) {
				towerSum = getPartCost(selectedFoundation);
			} else {
				landSum = getPartCost(selectedFoundation);
				final List<Mirror> mirrors = selectedFoundation.getMirrors();
				if (!mirrors.isEmpty()) {
					final ArrayList<Foundation> towers = new ArrayList<Foundation>();
					for (final Mirror m : mirrors) {
						if (m.getHeliostatTarget() != null) {
							if (!towers.contains(m.getHeliostatTarget())) {
								towers.add(m.getHeliostatTarget());
							}
						}
					}
					if (!towers.isEmpty()) {
						for (final Foundation tower : towers) {
							towerSum += getPartCost(tower);
						}
					}
				}
			}
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p.getTopContainer() == selectedFoundation) {
					if (p instanceof SolarCollector) { // assuming that sensor doesn't cost anything
						solarCollectorSum += getPartCost(p);
					}
				}
			}
		} else {
			info = count + " zones";
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p instanceof Foundation) {
					final Foundation f = (Foundation) p;
					if (f.isSolarPowerTower()) {
						towerSum += getPartCost(p);
					} else {
						landSum += getPartCost(p);
					}
				} else if (p instanceof SolarCollector) {
					solarCollectorSum += getPartCost(p);
				}
			}
		}

		final double[] data = new double[] { landSum, solarCollectorSum, towerSum };
		final String[] legends = new String[] { "Land (" + Scene.getInstance().getCspCustomPrice().getLifespan() + " years)", "Solar Collectors", "Towers" };
		final Color[] colors = new Color[] { Color.RED, Color.GREEN, Color.BLUE };

		// show them in a popup window
		final PieChart pie = new PieChart(data, colors, legends, "$", info, count > 1 ? details : null, true);
		pie.setBackground(Color.WHITE);
		pie.setBorder(BorderFactory.createEtchedBorder());
		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Costs by Category", true);
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
