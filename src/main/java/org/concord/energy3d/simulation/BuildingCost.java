package org.concord.energy3d.simulation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Building;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

/**
 * Calculate the cost of a building.
 * 
 * @author Charles Xie
 * 
 */
public class BuildingCost extends ProjectCost {

	private static BuildingCost instance = new BuildingCost();

	private BuildingCost() {
	}

	public static BuildingCost getInstance() {
		return instance;
	}

	public double getTotalCost() {
		double sum = 0;
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.isFrozen()) {
				continue;
			}
			sum += getPartCost(p);
		}
		return sum;
	}

	public double getCost(final Foundation foundation) {
		if (foundation == null) {
			return 0;
		}
		double sum = 0;
		switch (foundation.getStructureType()) {
		case Foundation.TYPE_BUILDING:
			int buildingCount = 0;
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p instanceof Foundation) {
					buildingCount++;
				}
			}
			if (buildingCount == 1) {
				for (final HousePart p : Scene.getInstance().getParts()) { // if there is only one building, trees are included in its cost
					if (p.isFrozen() && p instanceof Tree) {
						continue;
					}
					sum += getPartCost(p);
				}
			} else {
				sum = getPartCost(foundation);
				for (final HousePart p : Scene.getInstance().getParts()) {
					if (p.getTopContainer() == foundation) {
						sum += getPartCost(p);
					}
				}
			}
			break;
		case Foundation.TYPE_PV_STATION:
			break;
		case Foundation.TYPE_CSP_STATION:
			break;
		}
		return sum;
	}

	@SuppressWarnings("serial")
	public void showItemizedCost() {

		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Itemized Construction Cost", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		final JPanel contentPane = new JPanel(new BorderLayout());
		dialog.setContentPane(contentPane);

		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		contentPane.add(panel, BorderLayout.CENTER);

		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		Foundation foundation = null;
		if (selectedPart instanceof Foundation) {
			foundation = (Foundation) selectedPart;
		}

		final String[] header = new String[] { "ID", "Type", "Cost" };
		final int m = header.length;
		final List<HousePart> parts = Scene.getInstance().getParts();
		int n = 0;
		int foundationCount = 0;
		for (final HousePart p : parts) {
			if (p instanceof Foundation) {
				foundationCount++;
			}
		}
		for (final HousePart p : parts) {
			if (p == foundation || p.getTopContainer() == foundation || (foundationCount == 1 && p instanceof Tree)) {
				n++;
			}
		}
		final Object[][] column = new Object[n][m];
		int i = 0;
		for (final HousePart p : parts) {
			if (p == foundation || p.getTopContainer() == foundation || (foundationCount == 1 && p instanceof Tree)) {
				column[i][0] = p.getId();
				String partName = p.toString().substring(0, p.toString().indexOf(')') + 1);
				final int beg = partName.indexOf("(");
				if (beg != -1) {
					partName = partName.substring(0, beg);
				}
				column[i][1] = partName;
				column[i][2] = "$" + getPartCost(p);
				i++;
			}
		}

		final JTable table = new JTable(column, header);
		table.setModel(new DefaultTableModel(column, header) {
			@Override
			public boolean isCellEditable(final int row, final int col) {
				return false;
			}
		});
		panel.add(new JScrollPane(table), BorderLayout.CENTER);

		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		final JButton button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				windowLocation.setLocation(dialog.getLocationOnScreen());
				dialog.dispose();
			}
		});
		buttonPanel.add(button);

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				windowLocation.setLocation(dialog.getLocationOnScreen());
				dialog.dispose();
			}
		});

		dialog.pack();
		if (windowLocation.x > 0 && windowLocation.y > 0) {
			dialog.setLocation(windowLocation);
		} else {
			dialog.setLocationRelativeTo(MainFrame.getInstance());
		}
		dialog.setVisible(true);

	}

	public void showGraph() {
		show();
		TimeSeriesLogger.getInstance().logAnalysis(this);
	}

	private void show() {

		String details = "";
		int count = 0;
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Foundation) {
				count++;
				final Foundation foundation = (Foundation) p;
				details += "#" + foundation.getId() + ":$" + getCost(foundation) + "/";
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
		final JButton buttonItemize = new JButton("Itemize");
		buttonItemize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				showItemizedCost();
			}
		});
		buttonPanel.add(buttonItemize);
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

	public String toJson() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		String s;
		if (selectedPart != null) {
			s = "{";
			s += "\"Building\": " + Building.getBuildingId(selectedPart);
			s += ", \"Amount\": " + getCost(selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer());
			s += "}";
		} else {
			s = "[";
			int count = 0;
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p instanceof Foundation) {
					count++;
					s += "{\"Building\": " + Building.getBuildingId(p) + ", \"Amount\": " + getCost((Foundation) p) + "}, ";
				}
			}
			if (count > 0) {
				s = s.substring(0, s.length() - 2);
			}
			s += "]";

		}
		return s;
	}

}
