package org.concord.energy3d.simulation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
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

	private int budget = 100000;
	private static Cost instance = new Cost();

	private Cost() {
	}

	public static Cost getInstance() {
		return instance;
	}

	public void setBudget(int budget) {
		this.budget = budget;
		EnergyPanel.getInstance().setBudget(budget);
	}

	public int getBudget() {
		return budget;
	}

	public int getTotalCost() {
		int sum = 0;
		for (HousePart p : Scene.getInstance().getParts()) {
			if (p.isFrozen())
				continue;
			sum += getPartCost(p);
		}
		return sum;
	}

	public int getBuildingCost(Foundation foundation) {
		int sum = 0;
		for (HousePart p : Scene.getInstance().getParts()) {
			if (p.getTopContainer() == foundation) {
				sum += getPartCost(p);
			}
		}
		return sum;
	}

	private int getPartCost(HousePart part) {
		if (part instanceof Wall) {
			double uValue = HeatLoad.parseUFactor(EnergyPanel.getInstance().getWallsComboBox());
			double price;
			if (uValue < 0.05)
				price = 100;
			else if (uValue < 0.2)
				price = 90;
			else
				price = 80;
			return (int) (part.computeArea() * price);
		}
		if (part instanceof Window) {
			double uValue = HeatLoad.parseUFactor(EnergyPanel.getInstance().getWindowsComboBox());
			double price;
			if (uValue <= 0.15) // triple pane
				price = 250;
			else if (uValue < 0.4) // double pane
				price = 200;
			else
				price = 150;
			return (int) (part.computeArea() * price);
		}
		if (part instanceof Roof) {
			double uValue = HeatLoad.parseUFactor(EnergyPanel.getInstance().getRoofsComboBox());
			double price;
			if (uValue < 0.05)
				price = 100;
			else if (uValue < 0.2)
				price = 90;
			else
				price = 80;
			return (int) (part.computeArea() * price);
		}
		if (part instanceof Door) {
			double uValue = HeatLoad.parseUFactor(EnergyPanel.getInstance().getDoorsComboBox());
			int price;
			if (uValue < 0.5)
				price = 500;
			else
				price = 300;
			return price;
		}
		if (part instanceof SolarPanel) {
			double efficiency = Double.parseDouble((String) EnergyPanel.getInstance().getSolarPanelEfficiencyComboBox().getSelectedItem());
			int price;
			if (efficiency > 30)
				price = 300;
			else if (efficiency > 20)
				price = 250;
			else
				price = 200;
			return price;
		}
		return 0;
	}

	public void show() {

		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		final Foundation selectedBuilding;
		if (selectedPart == null)
			selectedBuilding = null;
		else if (selectedPart instanceof Foundation)
			selectedBuilding = (Foundation) selectedPart;
		else
			selectedBuilding = (Foundation) selectedPart.getTopContainer();
		if (selectedBuilding == null) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "No building is selected.", "No Building", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		int wallSum = 0;
		int windowSum = 0;
		int roofSum = 0;
		int doorSum = 0;
		int solarPanelSum = 0;
		for (HousePart p : Scene.getInstance().getParts()) {
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

		float[] data = new float[] { wallSum, windowSum, roofSum, doorSum, solarPanelSum };
		String[] legends = new String[] { "Walls", "Windows", "Roof", "Doors", "Solar Panels" };
		Color[] colors = new Color[] { Color.RED, Color.BLUE, Color.GRAY, Color.PINK, Color.GREEN };

		// show them in a popup window
		PieChart pie = new PieChart(data, colors, legends, "$");
		pie.setBackground(Color.WHITE);
		pie.setBorder(BorderFactory.createEtchedBorder());
		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Construction Costs by Category", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getContentPane().add(pie, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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