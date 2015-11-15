/**
 * 
 */
package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.simulation.Cost;
import org.concord.energy3d.simulation.PieChart;

/**
 * @author Charles Xie
 *
 */
public class CostGraph extends JPanel {

	private static final long serialVersionUID = 1L;

	private PieChart pie;

	public CostGraph() {
		super(new BorderLayout());
	}

	public void removeGraph() {
		if (pie != null)
			remove(pie);
		repaint();
	}

	public void addGraph(Foundation building) {

		removeGraph();

		int countBuildings = 0;
		for (HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Foundation)
				countBuildings++;
		}

		int wallSum = 0;
		int windowSum = 0;
		int roofSum = 0;
		int doorSum = 0;
		int solarPanelSum = 0;
		int treeSum = 0;
		int foundationSum = Cost.getInstance().getPartCost(building);
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

		final float[] data = new float[] { wallSum, windowSum, roofSum, foundationSum, doorSum, solarPanelSum, treeSum };
		final String[] legends = new String[] { "Walls", "Windows", "Roof", "Ground Floor", "Doors", "Solar Panels", "Trees" };
		final Color[] colors = new Color[] { Color.RED, Color.BLUE, Color.GRAY, Color.MAGENTA, Color.PINK, Color.YELLOW, Color.GREEN };

		pie = new PieChart(data, colors, legends, "$", null, null, false);
		pie.setPreferredSize(new Dimension(getWidth() - 10, getHeight()));
		pie.setBackground(Color.WHITE);
		pie.setBorder(BorderFactory.createEtchedBorder());
		pie.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					Cost.getInstance().showGraph();
				}
			}
		});

		add(pie, BorderLayout.CENTER);

		repaint();

	}

}
