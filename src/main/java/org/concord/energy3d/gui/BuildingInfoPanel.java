package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.concord.energy3d.model.Building;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.simulation.DesignSpecs;

/**
 * @author Charles Xie
 *
 */
public class BuildingInfoPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final DecimalFormat twoDecimals = new DecimalFormat();
	private final DecimalFormat noDecimals = new DecimalFormat();
	private final JPanel heightPanel, areaPanel, windowToFloorPanel;
	private final ColorBar heightBar, areaBar, windowToFloorBar;
	private final JPanel solarPanelCountPanel, windowCountPanel, wallCountPanel;
	private final ColorBar solarPanelCountBar, windowCountBar, wallCountBar;

	public BuildingInfoPanel() {

		super(new BorderLayout());

		twoDecimals.setMaximumFractionDigits(2);
		noDecimals.setMaximumFractionDigits(0);

		final JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		add(container, BorderLayout.NORTH);

		// area for the selected building

		areaPanel = new JPanel(new BorderLayout());
		areaPanel.setBorder(EnergyPanel.createTitledBorder("Area (\u33A1)", true));
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
		heightPanel.setBorder(EnergyPanel.createTitledBorder("Height (m)", true));
		heightPanel.setToolTipText("<html>The height of the selected building<br><b>Must be within the specified range (if any).</b></html>");
		container.add(heightPanel);
		heightBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
		heightBar.setUnit("");
		heightBar.setUnitPrefix(false);
		heightBar.setVerticalLineRepresentation(false);
		heightBar.setDecimalDigits(2);
		heightBar.setToolTipText(heightPanel.getToolTipText());
		heightBar.setPreferredSize(new Dimension(100, 16));
		heightPanel.add(heightBar, BorderLayout.CENTER);

		// window-to-floor area ratio for the selected building

		windowToFloorPanel = new JPanel(new BorderLayout());
		windowToFloorPanel.setBorder(EnergyPanel.createTitledBorder("Window/floor area ratio", true));
		windowToFloorPanel.setToolTipText("<html>The window to floor area ratio of the selected building<br><b>Must be within the specified range (if any).</b></html>");
		container.add(windowToFloorPanel);
		windowToFloorBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
		windowToFloorBar.setUnit("");
		windowToFloorBar.setUnitPrefix(false);
		windowToFloorBar.setVerticalLineRepresentation(false);
		windowToFloorBar.setDecimalDigits(3);
		windowToFloorBar.setToolTipText(windowToFloorPanel.getToolTipText());
		windowToFloorBar.setPreferredSize(new Dimension(100, 16));
		windowToFloorPanel.add(windowToFloorBar, BorderLayout.CENTER);

		// window count for the selected building

		windowCountPanel = new JPanel(new BorderLayout());
		windowCountPanel.setBorder(EnergyPanel.createTitledBorder("Number of windows", true));
		windowCountPanel.setToolTipText("<html>The number of windows of the selected building<br><b>Must be within the specified range (if any).</b></html>");
		container.add(windowCountPanel);
		windowCountBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
		windowCountBar.setUnit("");
		windowCountBar.setUnitPrefix(false);
		windowCountBar.setVerticalLineRepresentation(false);
		windowCountBar.setDecimalDigits(0);
		windowCountBar.setToolTipText(windowCountPanel.getToolTipText());
		windowCountBar.setPreferredSize(new Dimension(100, 16));
		windowCountPanel.add(windowCountBar, BorderLayout.CENTER);

		// wall count for the selected building

		wallCountPanel = new JPanel(new BorderLayout());
		wallCountPanel.setBorder(EnergyPanel.createTitledBorder("Number of walls", true));
		wallCountPanel.setToolTipText("<html>The number of walls of the selected building<br><b>Must be within the specified range (if any).</b></html>");
		container.add(wallCountPanel);
		wallCountBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
		wallCountBar.setUnit("");
		wallCountBar.setUnitPrefix(false);
		wallCountBar.setVerticalLineRepresentation(false);
		wallCountBar.setDecimalDigits(0);
		wallCountBar.setToolTipText(wallCountPanel.getToolTipText());
		wallCountBar.setPreferredSize(new Dimension(100, 16));
		wallCountPanel.add(wallCountBar, BorderLayout.CENTER);

		// solar panel count for the selected building

		solarPanelCountPanel = new JPanel(new BorderLayout());
		solarPanelCountPanel.setBorder(EnergyPanel.createTitledBorder("Number of solar panels", true));
		solarPanelCountPanel.setToolTipText("<html>The number of solar panels of the selected building<br><b>Must be within the specified range (if any).</b></html>");
		container.add(solarPanelCountPanel);
		solarPanelCountBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
		solarPanelCountBar.setUnit("");
		solarPanelCountBar.setUnitPrefix(false);
		solarPanelCountBar.setVerticalLineRepresentation(false);
		solarPanelCountBar.setDecimalDigits(0);
		solarPanelCountBar.setToolTipText(solarPanelCountPanel.getToolTipText());
		solarPanelCountBar.setPreferredSize(new Dimension(100, 16));
		solarPanelCountPanel.add(solarPanelCountBar, BorderLayout.CENTER);

	}

	void update(final Foundation foundation) {
		final Building b = new Building(foundation);
		if (b.areWallsAcceptable()) {
			b.calculate(false);
			switch (Scene.getInstance().getUnit()) {
			case InternationalSystemOfUnits:
				areaBar.setValue((float) b.getArea());
				break;
			case USCustomaryUnits:
				areaBar.setValue((float) (b.getArea() * 3.28084 * 3.28084));
				break;
			}
			windowToFloorBar.setValue((float) b.getWindowToFloorRatio());
		} else {
			areaBar.setValue(0);
			windowToFloorBar.setValue(0);
		}
		// relax the requirement of a building
		solarPanelCountBar.setValue(foundation.getNumberOfSolarPanels());
		windowCountBar.setValue(foundation.countParts(Window.class));
		wallCountBar.setValue(foundation.countParts(Wall.class));
		final double height = Scene.getInstance().getScale() * foundation.getBoundingHeight();
		switch (Scene.getInstance().getUnit()) {
		case InternationalSystemOfUnits:
			heightBar.setValue((float) height);
			break;
		case USCustomaryUnits:
			heightBar.setValue((float) (height * 3.28084));
			break;
		}
	}

	public void updateAreaBounds() {
		final DesignSpecs specs = Scene.getInstance().getDesignSpecs();
		if (specs == null) {
			return;
		}
		final double r = 3.28084 * 3.28084;
		String t = "Area (";
		switch (Scene.getInstance().getUnit()) {
		case InternationalSystemOfUnits:
			if (specs.isAreaEnabled()) {
				t += twoDecimals.format(specs.getMinimumArea()) + " - " + twoDecimals.format(specs.getMaximumArea()) + " ";
			}
			t += "m\u00B2)";
			areaBar.setMinimum(specs.getMinimumArea());
			areaBar.setMaximum(specs.getMaximumArea());
			break;
		case USCustomaryUnits:
			if (specs.isAreaEnabled()) {
				t += noDecimals.format(specs.getMinimumArea() * r) + " - " + noDecimals.format(specs.getMaximumArea() * r) + " ";
			}
			t += "ft\u00B2)";
			areaBar.setMinimum(specs.getMinimumArea() * r);
			areaBar.setMaximum(specs.getMaximumArea() * r);
			break;
		}
		areaPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
		areaBar.setEnabled(specs.isAreaEnabled());
		areaBar.repaint();
	}

	public void updateHeightBounds() {
		final DesignSpecs specs = Scene.getInstance().getDesignSpecs();
		if (specs == null) {
			return;
		}
		final double r = 3.28084;
		String t = "Height (";
		switch (Scene.getInstance().getUnit()) {
		case InternationalSystemOfUnits:
			if (specs.isHeightEnabled()) {
				t += twoDecimals.format(specs.getMinimumHeight()) + " - " + twoDecimals.format(specs.getMaximumHeight()) + " ";
			}
			t += "m)";
			heightBar.setMinimum(specs.getMinimumHeight());
			heightBar.setMaximum(specs.getMaximumHeight());
			break;
		case USCustomaryUnits:
			if (specs.isHeightEnabled()) {
				t += noDecimals.format(specs.getMinimumHeight() * r) + " - " + noDecimals.format(specs.getMaximumHeight() * r) + " ";
			}
			t += "ft)";
			heightBar.setMinimum(specs.getMinimumHeight() * r);
			heightBar.setMaximum(specs.getMaximumHeight() * r);
			break;
		}
		heightPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
		heightBar.setEnabled(specs.isHeightEnabled());
		heightBar.repaint();
	}

	public void updateWindowToFloorRatioBounds() {
		final DesignSpecs specs = Scene.getInstance().getDesignSpecs();
		if (specs == null) {
			return;
		}
		String t = "Window/floor area ratio";
		if (specs.isWindowToFloorRatioEnabled()) {
			t += " (" + twoDecimals.format(specs.getMinimumWindowToFloorRatio()) + " - " + twoDecimals.format(specs.getMaximumWindowToFloorRatio()) + ")";
		}
		windowToFloorBar.setMinimum(specs.getMinimumWindowToFloorRatio());
		windowToFloorBar.setMaximum(specs.getMaximumWindowToFloorRatio());
		windowToFloorPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
		windowToFloorBar.setEnabled(specs.isWindowToFloorRatioEnabled());
		windowToFloorBar.repaint();
	}

	public void updateSolarPanelNumberBounds() {
		final DesignSpecs specs = Scene.getInstance().getDesignSpecs();
		if (specs == null) {
			return;
		}
		String t = "Number of solar panels";
		if (specs.isNumberOfSolarPanelsEnabled()) {
			t += " (" + specs.getMinimumNumberOfSolarPanels() + " - " + specs.getMaximumNumberOfSolarPanels() + ")";
		}
		solarPanelCountBar.setMinimum(specs.getMinimumNumberOfSolarPanels());
		solarPanelCountBar.setMaximum(specs.getMaximumNumberOfSolarPanels());
		solarPanelCountPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
		solarPanelCountBar.setEnabled(specs.isNumberOfSolarPanelsEnabled());
		solarPanelCountBar.repaint();
	}

	public void updateWindowNumberBounds() {
		final DesignSpecs specs = Scene.getInstance().getDesignSpecs();
		if (specs == null) {
			return;
		}
		String t = "Number of windows";
		if (specs.isNumberOfWindowsEnabled()) {
			if (specs.getMinimumNumberOfWindows() == 0) {
				t += " (<" + specs.getMaximumNumberOfWindows() + ")";
			} else {
				t += " (" + specs.getMinimumNumberOfWindows() + " - " + specs.getMaximumNumberOfWindows() + ")";
			}
		}
		windowCountBar.setMinimum(specs.getMinimumNumberOfWindows());
		windowCountBar.setMaximum(specs.getMaximumNumberOfWindows());
		windowCountPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
		windowCountBar.setEnabled(specs.isNumberOfWindowsEnabled());
		windowCountBar.repaint();
	}

	public void updateWallNumberBounds() {
		final DesignSpecs specs = Scene.getInstance().getDesignSpecs();
		if (specs == null) {
			return;
		}
		String t = "Number of walls";
		if (specs.isNumberOfWallsEnabled()) {
			t += " (" + specs.getMinimumNumberOfWalls() + " - " + specs.getMaximumNumberOfWalls() + ")";
		}
		wallCountBar.setMinimum(specs.getMinimumNumberOfWalls());
		wallCountBar.setMaximum(specs.getMaximumNumberOfWalls());
		wallCountPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
		wallCountBar.setEnabled(specs.isNumberOfWallsEnabled());
		wallCountBar.repaint();
	}

}
