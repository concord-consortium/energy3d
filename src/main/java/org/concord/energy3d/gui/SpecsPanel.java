package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.concord.energy3d.model.Building;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.simulation.DesignSpecs;

/**
 * @author Charles Xie
 *
 */
public class SpecsPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final DecimalFormat twoDecimals = new DecimalFormat();
	private final DecimalFormat noDecimals = new DecimalFormat();
	private JPanel heightPanel, areaPanel, windowToFloorPanel;
	private ColorBar heightBar, areaBar, windowToFloorBar;

	public SpecsPanel() {

		super(new BorderLayout());

		twoDecimals.setMaximumFractionDigits(2);
		noDecimals.setMaximumFractionDigits(0);

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		add(container, BorderLayout.NORTH);

		// area for the selected building

		areaPanel = new JPanel(new BorderLayout());
		areaPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Area (\u33A1)", TitledBorder.LEADING, TitledBorder.TOP));
		areaPanel.setToolTipText("<html>The area of the selected building<br><b>Must be within the specified range (if any).</b></html>");
		container.add(areaPanel);
		areaBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
		areaBar.setUnit("");
		areaBar.setUnitPrefix(false);
		areaBar.setVerticalLineRepresentation(false);
		areaBar.setDecimalDigits(1);
		areaBar.setToolTipText(areaPanel.getToolTipText());
		areaBar.setPreferredSize(new Dimension(100, 16));
		areaPanel.add(areaBar, BorderLayout.CENTER);

		// height for the selected building

		heightPanel = new JPanel(new BorderLayout());
		heightPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Height (m)", TitledBorder.LEADING, TitledBorder.TOP));
		heightPanel.setToolTipText("<html>The height of the selected building<br><b>Must be within the specified range (if any).</b></html>");
		container.add(heightPanel);
		heightBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
		heightBar.setUnit("");
		heightBar.setUnitPrefix(false);
		heightBar.setVerticalLineRepresentation(false);
		heightBar.setDecimalDigits(1);
		heightBar.setToolTipText(heightPanel.getToolTipText());
		heightBar.setPreferredSize(new Dimension(100, 16));
		heightPanel.add(heightBar, BorderLayout.CENTER);

		// window-to-floor area ratio for the selected building

		windowToFloorPanel = new JPanel(new BorderLayout());
		windowToFloorPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Window/floor area ratio", TitledBorder.LEADING, TitledBorder.TOP));
		windowToFloorPanel.setToolTipText("<html>The window to floor area ratio of the selected building<br><b>Must be within the specified range (if any).</b></html>");
		container.add(windowToFloorPanel);
		windowToFloorBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
		windowToFloorBar.setUnit("");
		windowToFloorBar.setUnitPrefix(false);
		windowToFloorBar.setVerticalLineRepresentation(false);
		windowToFloorBar.setDecimalDigits(3);
		windowToFloorBar.setToolTipText(heightPanel.getToolTipText());
		windowToFloorBar.setPreferredSize(new Dimension(100, 16));
		windowToFloorPanel.add(windowToFloorBar, BorderLayout.CENTER);

	}

	void update(Foundation foundation) {
		Building b = new Building(foundation);
		if (b.isWallComplete()) {
			b.calculate();
			switch (Scene.getInstance().getUnit()) {
			case InternationalSystemOfUnits:
				heightBar.setValue((float) b.getHeight());
				areaBar.setValue((float) b.getArea());
				break;
			case USCustomaryUnits:
				heightBar.setValue((float) (b.getHeight() * 3.28084));
				areaBar.setValue((float) (b.getArea() * 3.28084 * 3.28084));
				break;
			}
			windowToFloorBar.setValue((float) b.getWindowToFloorRatio());
		} else {
			clear();
		}
	}

	void clear() {
		heightBar.setValue(0);
		areaBar.setValue(0);
		windowToFloorBar.setValue(0);
	}

	public void updateArea() {
		final DesignSpecs specs = Scene.getInstance().getDesignSpecs();
		final double r = 3.28084 * 3.28084;
		String t = "Area (";
		switch (Scene.getInstance().getUnit()) {
		case InternationalSystemOfUnits:
			if (specs.isAreaEnabled())
				t += twoDecimals.format(specs.getMinimumArea()) + " - " + twoDecimals.format(specs.getMaximumArea()) + " ";
			t += "m\u00B2)";
			areaBar.setMinimum(specs.getMinimumArea());
			areaBar.setMaximum(specs.getMaximumArea());
			break;
		case USCustomaryUnits:
			if (specs.isAreaEnabled())
				t += noDecimals.format(specs.getMinimumArea() * r) + " - " + noDecimals.format(specs.getMaximumArea() * r) + " ";
			t += "ft\u00B2)";
			areaBar.setMinimum(specs.getMinimumArea() * r);
			areaBar.setMaximum(specs.getMaximumArea() * r);
			break;
		}
		areaPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
		areaBar.setEnabled(specs.isAreaEnabled());
		areaBar.repaint();
	}

	public void updateHeight() {
		final DesignSpecs specs = Scene.getInstance().getDesignSpecs();
		final double r = 3.28084;
		String t = "Height (";
		switch (Scene.getInstance().getUnit()) {
		case InternationalSystemOfUnits:
			if (specs.isHeightEnabled())
				t += twoDecimals.format(specs.getMinimumHeight()) + " - " + twoDecimals.format(specs.getMaximumHeight()) + " ";
			t += "m)";
			heightBar.setMinimum(specs.getMinimumHeight());
			heightBar.setMaximum(specs.getMaximumHeight());
			break;
		case USCustomaryUnits:
			if (specs.isHeightEnabled())
				t += noDecimals.format(specs.getMinimumHeight() * r) + " - " + noDecimals.format(specs.getMaximumHeight() * r) + " ";
			t += "ft)";
			heightBar.setMinimum(specs.getMinimumHeight() * r);
			heightBar.setMaximum(specs.getMaximumHeight() * r);
			break;
		}
		heightPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
		heightBar.setEnabled(specs.isHeightEnabled());
		heightBar.repaint();
	}

	public void updateWindowToFloorRatio() {
		final DesignSpecs specs = Scene.getInstance().getDesignSpecs();
		String t = "Window/floor area ratio";
		if (specs.isWindowToFloorRatioEnabled())
			t += " (" + twoDecimals.format(specs.getMinimumWindowToFloorRatio()) + " - " + twoDecimals.format(specs.getMaximumWindowToFloorRatio()) + ")";
		windowToFloorBar.setMinimum(specs.getMinimumWindowToFloorRatio());
		windowToFloorBar.setMaximum(specs.getMaximumWindowToFloorRatio());
		windowToFloorPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
		windowToFloorBar.setEnabled(specs.isWindowToFloorRatioEnabled());
		windowToFloorBar.repaint();
	}

}
