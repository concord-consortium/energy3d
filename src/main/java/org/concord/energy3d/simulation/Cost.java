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

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

/**
 * Calculate the cost (the prices are fictitious).
 * 
 * @author Charles Xie
 * 
 */
public class Cost {

	private static Cost instance = new Cost();

	private Cost() {
	}

	public static Cost getInstance() {
		return instance;
	}

	public int getTotalCost() {
		int sum = 0;
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.isFrozen())
				continue;
			sum += getPartCost(p);
		}
		return sum;
	}

	public int getBuildingCost(final Foundation foundation) {
		int buildingCount = 0;
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Foundation)
				buildingCount++;
		}
		int sum = 0;
		if (buildingCount == 1) {
			for (final HousePart p : Scene.getInstance().getParts()) { // if there is only one building, trees are included in its cost
				if (p.getTopContainer() == foundation && !p.isFrozen())
					sum += getPartCost(p);
			}
		} else {
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p.getTopContainer() == foundation) {
					sum += getPartCost(p);
				}
			}
		}
		return sum;
	}

	private int getPartCost(final HousePart part) {
		if (part instanceof Wall) {
			double uFactor = part.getUFactor();
			if (uFactor == 0)
				uFactor = HeatLoad.parseValue(EnergyPanel.getInstance().getWallsComboBox());
			double price;
			if (uFactor < 0.05)
				price = 100;
			else if (uFactor < 0.2)
				price = 90;
			else
				price = 80;
			return (int) (part.getArea() * price);
		}
		if (part instanceof Window) {
			double uFactor = part.getUFactor();
			if (uFactor == 0)
				uFactor = HeatLoad.parseValue(EnergyPanel.getInstance().getWindowsComboBox());
			double price;
			if (uFactor <= 0.15) // triple pane
				price = 250;
			else if (uFactor < 0.4) // double pane
				price = 200;
			else
				price = 150;
			return (int) (part.getArea() * price);
		}
		if (part instanceof Roof) {
			double uFactor = part.getUFactor();
			if (uFactor == 0)
				uFactor = HeatLoad.parseValue(EnergyPanel.getInstance().getRoofsComboBox());
			double price;
			if (uFactor < 0.05)
				price = 100;
			else if (uFactor < 0.2)
				price = 90;
			else
				price = 80;
			return (int) (part.getArea() * price);
		}
		if (part instanceof Door) {
			double uFactor = part.getUFactor();
			if (uFactor == 0)
				uFactor = HeatLoad.parseValue(EnergyPanel.getInstance().getDoorsComboBox());
			int price;
			if (uFactor < 0.5)
				price = 100;
			else
				price = 50;
			return (int) (part.getArea() * price);
		}
		if (part instanceof SolarPanel) {
			final double efficiency = ((SolarPanel) part).getEfficiency();
			if (efficiency == 0)
				Double.parseDouble((String) EnergyPanel.getInstance().getSolarPanelEfficiencyComboBox().getSelectedItem());
			int price;
			if (efficiency >= 20)
				price = 1000;
			else if (efficiency >= 17.5)
				price = 800;
			else if (efficiency >= 15)
				price = 600;
			else if (efficiency >= 12.5)
				price = 400;
			else
				price = 300;
			return price;
		}
		if (part instanceof Tree) {
			Tree tree = (Tree) part;
			int price;
			switch (tree.getTreeType()) {
			case Tree.OAK:
				price = 2000;
			case Tree.PINE:
				price = 1500;
			case Tree.MAPLE:
				price = 1000;
			default:
				price = 500;
			}
			return price;
		}
		return 0;
	}

	public void showGraph() {
		EnergyPanel.getInstance().requestDisableActions(this);
		show();
		EnergyPanel.getInstance().requestDisableActions(null);
	}

	private void show() {

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
		int wallSum = 0;
		int windowSum = 0;
		int roofSum = 0;
		int doorSum = 0;
		int solarPanelSum = 0;
		int treeSum = 0;
		String info;
		if (selectedBuilding != null) {
			info = "Building #" + selectedBuilding.getId();
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p.getTopContainer() == selectedBuilding) {
					if (p instanceof Wall)
						wallSum += getPartCost(p);
					else if (p instanceof Window)
						windowSum += getPartCost(p);
					else if (p instanceof Roof)
						roofSum += getPartCost(p);
					else if (p instanceof Door)
						doorSum += getPartCost(p);
					else if (p instanceof SolarPanel)
						solarPanelSum += getPartCost(p);
				}
			}
		} else {
			int buildingCount = 0;
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p instanceof Foundation)
					buildingCount++;
			}
			info = buildingCount + " buildings";
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p instanceof Wall)
					wallSum += getPartCost(p);
				else if (p instanceof Window)
					windowSum += getPartCost(p);
				else if (p instanceof Roof)
					roofSum += getPartCost(p);
				else if (p instanceof Door)
					doorSum += getPartCost(p);
				else if (p instanceof SolarPanel)
					solarPanelSum += getPartCost(p);
				else if (p instanceof Tree && !p.isFrozen())
					treeSum += getPartCost(p);
			}
		}

		final float[] data = new float[] { wallSum, windowSum, roofSum, doorSum, solarPanelSum, treeSum };
		final String[] legends = new String[] { "Walls", "Windows", "Roof", "Doors", "Solar Panels", "Trees" };
		final Color[] colors = new Color[] { Color.RED, Color.BLUE, Color.GRAY, Color.PINK, Color.YELLOW, Color.GREEN };

		String details = "";
		int count = 0;
		for (HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Foundation) {
				count++;
				Foundation foundation = (Foundation) p;
				details += "#" + foundation.getId() + ":$" + getBuildingCost(foundation) + "/";
			}
		}
		if (count > 0)
			details = details.substring(0, details.length() - 1);

		// show them in a popup window
		final PieChart pie = new PieChart(data, colors, legends, "$", info, count > 1 ? details : null);
		pie.setBackground(Color.WHITE);
		pie.setBorder(BorderFactory.createEtchedBorder());
		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Material Costs by Category", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getContentPane().add(pie, BorderLayout.CENTER);
		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		final JButton button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.dispose();
			}
		});
		buttonPanel.add(button);
		dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		dialog.pack();
		dialog.setLocationRelativeTo(MainFrame.getInstance());
		dialog.setVisible(true);

	}

}
