package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.SolarCollector;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.CspProjectCost;
import org.concord.energy3d.simulation.PieChart;
import org.concord.energy3d.simulation.PvDesignSpecs;
import org.concord.energy3d.simulation.PvProjectCost;
import org.concord.energy3d.util.ClipImage;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
public class CspProjectCostGraph extends JPanel {

	private static final long serialVersionUID = 1L;

	private PieChart pie;
	private final Box buttonPanel;
	private final JPanel budgetPanel;
	private final ColorBar budgetBar;
	private final JPopupMenu popupMenu;
	private final DecimalFormat noDecimals = new DecimalFormat();
	private Foundation foundation;
	private double landSum;
	private double collectorSum;
	private double receiverSum;
	private double totalCost;

	public CspProjectCostGraph() {
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
				new ClipImage().copyImageToClipboard(CspProjectCostGraph.this);
			}
		});
		popupMenu.add(mi);
	}

	private void calculateCost() {
		landSum = 0;
		collectorSum = 0;
		receiverSum = 0;
		if (foundation.hasSolarReceiver()) {
			receiverSum = CspProjectCost.getPartCost(foundation);
		} else {
			landSum = CspProjectCost.getPartCost(foundation);
			final List<Mirror> mirrors = foundation.getMirrors();
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
						receiverSum += CspProjectCost.getPartCost(tower);
					}
				}
			} else {
				final List<FresnelReflector> reflectors = foundation.getFresnelReflectors();
				if (!reflectors.isEmpty()) {
					final ArrayList<Foundation> absorbers = new ArrayList<Foundation>();
					for (final FresnelReflector r : reflectors) {
						if (r.getAbsorber() != null) {
							if (!absorbers.contains(r.getAbsorber())) {
								absorbers.add(r.getAbsorber());
							}
						}
					}
					if (!absorbers.isEmpty()) {
						for (final Foundation absorber : absorbers) {
							receiverSum += CspProjectCost.getPartCost(absorber);
						}
					}
				}
			}
		}
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.getTopContainer() == foundation) {
				if (p instanceof SolarCollector) { // assuming that sensor doesn't cost anything
					collectorSum += CspProjectCost.getPartCost(p);
				}
			}
		}
		totalCost = landSum + collectorSum + receiverSum;
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

		double[] data;
		String[] legends;
		Color[] colors;
		if (Util.isZero(receiverSum)) {
			data = new double[] { landSum, collectorSum };
			legends = new String[] { "Land (" + Scene.getInstance().getCspCustomPrice().getLifespan() + " years)", "Collectors" };
			colors = new Color[] { Color.RED, Color.GREEN };
		} else {
			data = new double[] { landSum, collectorSum, receiverSum };
			legends = new String[] { "Land (" + Scene.getInstance().getCspCustomPrice().getLifespan() + " years)", "Collectors", "Receivers" };
			colors = new Color[] { Color.RED, Color.GREEN, Color.BLUE };
		}

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
						popupMenu.show(CspProjectCostGraph.this, e.getX(), e.getY());
					}
				}
			}
		});

		add(pie, BorderLayout.CENTER);

		repaint();

		EnergyPanel.getInstance().validate();

	}

}
