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

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.PieChart;
import org.concord.energy3d.simulation.PvDesignSpecs;
import org.concord.energy3d.simulation.PvProjectCost;
import org.concord.energy3d.util.ClipImage;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
public class PvProjectCostGraph extends JPanel {

	public final static Color[] colors = new Color[] { new Color(250, 128, 114), new Color(135, 206, 250) };
	private static final long serialVersionUID = 1L;

	private PieChart pie;
	private final Box buttonPanel;
	private final JPanel budgetPanel;
	private final ColorBar budgetBar;
	private final JPopupMenu popupMenu;
	private final DecimalFormat noDecimals = new DecimalFormat();
	private Foundation foundation;
	private double landSum;
	private double solarPanelSum;
	private double totalCost;

	public PvProjectCostGraph() {
		super(new BorderLayout());

		noDecimals.setMaximumFractionDigits(0);
		budgetPanel = new JPanel(new BorderLayout());
		budgetBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
		budgetBar.setPreferredSize(new Dimension(100, 16));
		budgetBar.setToolTipText("<html>The total project cost must not exceed the limit (if specified).</html>");
		budgetPanel.add(budgetBar, BorderLayout.CENTER);

		buttonPanel = new Box(BoxLayout.Y_AXIS);
		buttonPanel.setBackground(Color.WHITE);
		buttonPanel.add(Box.createVerticalGlue());
		final JButton button = new JButton("Show");
		button.setAlignmentX(CENTER_ALIGNMENT);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				SceneManager.getInstance().autoSelectBuilding(true);
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
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
			public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
			}

			@Override
			public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(final PopupMenuEvent e) {
			}

		});
		final JMenuItem mi = new JMenuItem("Copy Image");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				new ClipImage().copyImageToClipboard(PvProjectCostGraph.this);
			}
		});
		popupMenu.add(mi);
	}

	private void calculateCost() {
		landSum = 0;
		solarPanelSum = 0;
		landSum = PvProjectCost.getPartCost(foundation);
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.getTopContainer() == foundation) {
				if (p instanceof SolarPanel || p instanceof Rack) {
					solarPanelSum += PvProjectCost.getPartCost(p);
				}
			}
		}
		totalCost = landSum + solarPanelSum;
	}

	public void removeGraph() {
		removeAll();
		repaint();
		add(buttonPanel, BorderLayout.CENTER);
		EnergyPanel.getInstance().validate();
		foundation = null;
	}

	public void updateBudget() {
		if (budgetPanel != null && foundation != null) {
			calculateCost();
			final PvDesignSpecs pvSpecs = Scene.getInstance().getPvDesignSpecs();
			budgetBar.setEnabled(pvSpecs.isBudgetEnabled());
			budgetBar.setMaximum(pvSpecs.getMaximumBudget());
			final String t = "Total (" + (pvSpecs.isBudgetEnabled() ? "\u2264 $" + noDecimals.format(pvSpecs.getMaximumBudget()) : "$") + ")";
			budgetPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
			budgetBar.setValue((float) totalCost);
			budgetBar.repaint();
			budgetPanel.repaint();
		}
	}

	public void addGraph(final Foundation foundation) {

		removeAll();

		this.foundation = foundation;
		calculateCost();
		updateBudget();

		add(budgetPanel, BorderLayout.NORTH);

		final double[] data = new double[] { landSum, solarPanelSum };
		final String[] legends = new String[] { "Land (" + Scene.getInstance().getPvCustomPrice().getLifespan() + " years)", "Solar Panels" };

		pie = new PieChart(data, colors, legends, "$", null, "Move mouse for more info", false);
		pie.setBackground(Color.WHITE);
		pie.setPreferredSize(new Dimension(getWidth() - 5, getHeight() - budgetPanel.getHeight() - 5));
		pie.setBorder(BorderFactory.createEtchedBorder());
		pie.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() >= 2) {
					PvProjectCost.getInstance().showGraph();
				} else {
					if (Util.isRightClick(e)) {
						popupMenu.show(PvProjectCostGraph.this, e.getX(), e.getY());
					}
				}
			}
		});

		add(pie, BorderLayout.CENTER);

		repaint();

		EnergyPanel.getInstance().validate();

	}

}
