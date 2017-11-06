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

	/* The material and installation costs are partly based on http://www.homewyse.com, but should be considered as largely fictitious. */
	public static double getPartCost(final HousePart part) {

		// According to http://www.homewyse.com/services/cost_to_insulate_your_home.html
		// As of 2015, a 1000 square feet wall insulation will cost as high as $1500 to insulate in Boston.
		// This translates into $16/m^2. We don't know what R-value this insulation will be. But let's assume it is R13 material that has a U-value of 0.44 W/m^2/C.
		// Let's also assume that the insulation cost is inversely proportional to the U-value.
		// The baseline cost for a wall is set to be $300/m^2, close to homewyse's estimates of masonry walls, interior framing, etc.
		if (part instanceof Wall) {
			final double uFactor = ((Wall) part).getUValue();
			final double unitPrice = 300.0 + 8.0 / uFactor;
			return part.getArea() * unitPrice;
		}

		// According to http://www.homewyse.com/costs/cost_of_double_pane_windows.html
		// A storm window of about 1 m^2 costs about $500. A double-pane window of about 1 m^2 costs about $700.
		if (part instanceof Window) {
			final double uFactor = ((Window) part).getUValue();
			final double unitPrice = 500.0 + 800.0 / uFactor;
			return part.getArea() * unitPrice;
		}

		// According to http://www.homewyse.com/services/cost_to_insulate_attic.html
		// As of 2015, a 1000 square feet of attic area costs as high as $3200 to insulate in Boston.
		// This translates into $34/m^2. We don't know the R-value of this insulation. But let's assume it is R22 material that has a U-value of 0.26 W/m^2/C.
		// Let's also assume that the insulation cost is inversely proportional to the U-value.
		// The baseline (that is, the structure without insulation) cost for a roof is set to be $100/m^2.
		if (part instanceof Roof) {
			final double uFactor = ((Roof) part).getUValue();
			final double unitPrice = 100.0 + 10.0 / uFactor;
			return part.getArea() * unitPrice;
		}

		// http://www.homewyse.com/costs/cost_of_floor_insulation.html
		// As of 2015, a 1000 square feet of floor area costs as high as $3000 to insulate in Boston. This translates into $32/m^2.
		// Now, we don't know what R-value this insulation is. But let's assume it is R25 material (minimum insulation recommended
		// for zone 5 by energystar.gov) that has a U-value of 0.23 W/m^2/C.
		// Let's also assume that the insulation cost is inversely proportional to the U-value.
		// The baseline cost (that is, the structure without insulation) for floor is set to be $100/m^2.
		// The foundation cost is set to be $200/m^2.
		if (part instanceof Foundation) {
			final Foundation foundation = (Foundation) part;
			final Building b = new Building(foundation);
			if (b.isWallComplete()) {
				b.calculate();
				final double uFactor = foundation.getUValue();
				final double unitPrice = 300.0 + 8.0 / uFactor;
				return b.getArea() * unitPrice;
			}
			return -1; // the building is incomplete yet, so we can assume the floor insulation isn't there yet
		}

		if (part instanceof Floor) {
			final double area = part.getArea();
			if (area > 0) {
				return part.getArea() * 100.0;
			}
			return -1;
		}

		// According to http://www.homewyse.com/costs/cost_of_exterior_doors.html
		if (part instanceof Door) {
			final double uFactor = ((Door) part).getUValue();
			final double unitPrice = 500.0 + 100.0 / uFactor;
			return part.getArea() * unitPrice;
		}

		if (part instanceof SolarPanel) {
			return Scene.getInstance().getPvCustomPrice().getTotalCost((SolarPanel) part);
		}

		if (part instanceof Rack) {
			return Scene.getInstance().getPvCustomPrice().getTotalCost((Rack) part);
		}

		if (part instanceof Tree) {
			switch (((Tree) part).getTreeType()) {
			case Tree.LINDEN:
				return 3000;
			case Tree.COTTONWOOD:
				return 2500;
			case Tree.ELM:
				return 2000;
			case Tree.OAK:
				return 2000;
			case Tree.PINE:
				return 1500;
			case Tree.MAPLE:
				return 1000;
			default:
				return 500;
			}
		}

		return 0;

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

	@Override
	public double getCostByFoundation(final Foundation foundation) {
		if (foundation == null || foundation.getStructureType() != Foundation.TYPE_BUILDING) {
			return 0;
		}
		double sum = 0;
		int buildingCount = 0;
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Foundation) {
				buildingCount++;
			}
		}
		if (buildingCount == 1) {
			for (final HousePart p : Scene.getInstance().getParts()) { // if there is only one building, trees are included in its cost
				if (!p.isFrozen() && !(p instanceof Human)) {
					sum += getPartCost(p);
				}
			}
		} else {
			sum = getPartCost(foundation);
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p.getTopContainer() == foundation) {
					sum += getPartCost(p);
				}
			}
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
			if ((p instanceof Human) || (foundationCount > 1 && p instanceof Tree)) {
				continue;
			}
			if (p == foundation || p.getTopContainer() == foundation) {
				n++;
			}
		}
		final Object[][] column = new Object[n][m];
		int i = 0;
		for (final HousePart p : parts) {
			if ((p instanceof Human) || (foundationCount > 1 && p instanceof Tree)) {
				continue;
			}
			if (p == foundation || p.getTopContainer() == foundation) {
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

}
