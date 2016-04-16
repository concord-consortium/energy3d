package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.Cost;
import org.concord.energy3d.simulation.DesignSpecs;
import org.concord.energy3d.simulation.PieChart;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
public class ConstructionCostGraph extends JPanel {

	private static final long serialVersionUID = 1L;

	private PieChart pie;
	private Box buttonPanel;
	private JPanel budgetPanel;
	private ColorBar budgetBar;
	private JPopupMenu popupMenu;
	private final DecimalFormat noDecimals = new DecimalFormat();
	private Foundation building;
	private int wallSum;
	private int windowSum;
	private int roofSum;
	private int doorSum;
	private int solarPanelSum;
	private int treeSum;
	private int foundationSum;
	private int totalCost;

	public ConstructionCostGraph() {
		super(new BorderLayout());

		noDecimals.setMaximumFractionDigits(0);
		budgetPanel = new JPanel(new BorderLayout());
		budgetBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
		budgetBar.setPreferredSize(new Dimension(100, 16));
		budgetBar.setToolTipText("<html>The total construction cost for the selected building<br><b>Must not exceed the limit (if specified).</b></html>");
		budgetPanel.add(budgetBar, BorderLayout.CENTER);

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
		popupMenu = new JPopupMenu();
		popupMenu.setInvoker(this);
		popupMenu.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

		});
		JMenuItem mi = new JMenuItem("View Itemized Cost...");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Cost.getInstance().showItemizedCost();
			}
		});
		popupMenu.add(mi);
	}

	private void calculateCost() {
		int countBuildings = 0;
		for (HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Foundation)
				countBuildings++;
		}
		wallSum = 0;
		windowSum = 0;
		roofSum = 0;
		doorSum = 0;
		solarPanelSum = 0;
		treeSum = 0;
		foundationSum = Cost.getInstance().getPartCost(building);
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.getTopContainer() == building) {
				if (p instanceof Wall)
					wallSum += Cost.getInstance().getPartCost(p);
				else if (p instanceof Window)
					windowSum += Cost.getInstance().getPartCost(p);
				else if (p instanceof Roof)
					roofSum += Cost.getInstance().getPartCost(p);
				else if (p instanceof Door)
					doorSum += Cost.getInstance().getPartCost(p);
				else if (p instanceof SolarPanel)
					solarPanelSum += Cost.getInstance().getPartCost(p);
			}
			if (countBuildings <= 1) {
				if (p instanceof Tree && !p.isFrozen())
					treeSum += Cost.getInstance().getPartCost(p);
			}
		}
		totalCost = wallSum + windowSum + roofSum + doorSum + solarPanelSum + treeSum + foundationSum;
	}

	public void removeGraph() {
		removeAll();
		repaint();
		add(buttonPanel, BorderLayout.CENTER);
		EnergyPanel.getInstance().validate();
		building = null;
	}

	public void updateBudget() {
		if (budgetPanel != null) {
			calculateCost();
			final DesignSpecs specs = Scene.getInstance().getDesignSpecs();
			budgetBar.setEnabled(specs.isBudgetEnabled());
			budgetBar.setMaximum(specs.getMaximumBudget());
			budgetBar.setValue(totalCost);
			budgetBar.repaint();
			String t = "Total (" + (specs.isBudgetEnabled() ? "\u2264 $" + noDecimals.format(specs.getMaximumBudget()) : "$") + ")";
			budgetPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
			budgetPanel.repaint();
		}
	}

	public Foundation getBuilding() {
		return building;
	}

	public void addGraph(Foundation building) {

		removeAll();

		this.building = building;
		calculateCost();
		updateBudget();

		add(budgetPanel, BorderLayout.NORTH);

		final float[] data = new float[] { wallSum, windowSum, roofSum, foundationSum, doorSum, solarPanelSum, treeSum };
		final String[] legends = new String[] { "Walls", "Windows", "Roof", "Ground Floor", "Doors", "Solar Panels", "Trees" };
		final Color[] colors = new Color[] { Color.RED, Color.BLUE, Color.GRAY, Color.MAGENTA, Color.PINK, Color.YELLOW, Color.GREEN };

		pie = new PieChart(data, colors, legends, "$", null, "Move mouse for more info", false);
		pie.setBackground(Color.WHITE);
		pie.setPreferredSize(new Dimension(getWidth() - 5, getHeight() - budgetPanel.getHeight() - 5));
		pie.setBorder(BorderFactory.createEtchedBorder());
		pie.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					Cost.getInstance().showGraph();
				} else {
					if (Util.isRightClick(e)) {
						popupMenu.show(ConstructionCostGraph.this, e.getX(), e.getY());
					}
				}
			}
		});

		add(pie, BorderLayout.CENTER);

		repaint();

		EnergyPanel.getInstance().validate();

	}

}
