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
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

/**
 * Calculate the cost of a CSP project
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

		if (part instanceof Mirror) {
			return Scene.getInstance().getCspCustomPrice().getHeliostatUnitPrice() * part.getArea();
		}

		if (part instanceof ParabolicTrough) {
			return Scene.getInstance().getCspCustomPrice().getParabolicTroughUnitPrice() * part.getArea();
		}

		if (part instanceof ParabolicDish) {
			return Scene.getInstance().getCspCustomPrice().getParabolicDishUnitPrice() * part.getArea();
		}

		if (part instanceof FresnelReflector) {
			return Scene.getInstance().getCspCustomPrice().getFresnelReflectorUnitPrice() * part.getArea();
		}

		return 0;

	}

	public double getTotalCost() {
		double sum = 0;
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Foundation) {
				sum += getCostByFoundation((Foundation) p);
			}
		}
		return sum;
	}

	@Override
	public double getCostByFoundation(final Foundation foundation) {
		if (foundation == null || foundation.getStructureType() != Foundation.TYPE_CSP_STATION) {
			return 0;
		}
		double sum = 0;
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
	void show() {

		String details = "";
		int count = 0;
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Foundation) {
				count++;
				final Foundation foundation = (Foundation) p;
				details += "#" + foundation.getId() + ":$" + getCostByFoundation(foundation) + "/";
			}
		}
		if (count > 0) {
			details = details.substring(0, details.length() - 1);
		}

		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		final Foundation selectedBuilding;
		if (selectedPart == null || selectedPart instanceof Tree || selectedPart instanceof Human) {
			selectedBuilding = null;
		} else if (selectedPart instanceof Foundation) {
			selectedBuilding = (Foundation) selectedPart;
		} else {
			selectedBuilding = selectedPart.getTopContainer();
			selectedPart.setEditPointsVisible(false);
			SceneManager.getInstance().setSelectedPart(selectedBuilding);
		}
		double wallSum = 0;
		double floorSum = 0;
		double windowSum = 0;
		double roofSum = 0;
		double foundationSum = 0;
		double doorSum = 0;
		double solarPanelSum = 0;
		double treeSum = 0;
		String info;
		if (selectedBuilding != null) {
			info = "Building #" + selectedBuilding.getId();
			foundationSum = getPartCost(selectedBuilding);
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p.getTopContainer() == selectedBuilding) {
					if (p instanceof Wall) {
						wallSum += getPartCost(p);
					} else if (p instanceof Floor) {
						floorSum += getPartCost(p);
					} else if (p instanceof Window) {
						windowSum += getPartCost(p);
					} else if (p instanceof Roof) {
						roofSum += getPartCost(p);
					} else if (p instanceof Door) {
						doorSum += getPartCost(p);
					} else if (p instanceof SolarPanel) {
						solarPanelSum += getPartCost(p);
					} else if (p instanceof Rack) {
						solarPanelSum += getPartCost(p);
					}
				}
				if (count <= 1) {
					if (p instanceof Tree && !p.isFrozen()) {
						treeSum += getPartCost(p);
					}
				}
			}
		} else {
			info = count + " buildings";
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p instanceof Wall) {
					wallSum += getPartCost(p);
				} else if (p instanceof Floor) {
					floorSum += getPartCost(p);
				} else if (p instanceof Window) {
					windowSum += getPartCost(p);
				} else if (p instanceof Roof) {
					roofSum += getPartCost(p);
				} else if (p instanceof Foundation) {
					foundationSum += getPartCost(p);
				} else if (p instanceof Door) {
					doorSum += getPartCost(p);
				} else if (p instanceof SolarPanel) {
					solarPanelSum += getPartCost(p);
				} else if (p instanceof Tree && !p.isFrozen()) {
					treeSum += getPartCost(p);
				}
			}
		}

		final double[] data = new double[] { wallSum, windowSum, roofSum, foundationSum, floorSum, doorSum, solarPanelSum, treeSum };
		final String[] legends = new String[] { "Walls", "Windows", "Roof", "Foundation", "Floors", "Doors", "Solar Panels", "Trees" };
		final Color[] colors = new Color[] { Color.RED, Color.BLUE, Color.GRAY, Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW, Color.GREEN };

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
