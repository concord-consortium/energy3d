package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;

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
	private JPopupMenu popupMenu;
	private static Point windowLocation = new Point();

	public ConstructionCostGraph() {
		super(new BorderLayout());
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
				showItemizedCost();
			}
		});
		popupMenu.add(mi);
	}

	@SuppressWarnings("serial")
	private void showItemizedCost() {

		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Itemized Construction Cost", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		final JPanel contentPane = new JPanel(new BorderLayout());
		dialog.setContentPane(contentPane);

		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		contentPane.add(panel, BorderLayout.CENTER);

		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		Foundation foundation = null;
		if (selectedPart instanceof Foundation)
			foundation = (Foundation) selectedPart;

		String[] header = new String[] { "ID", "Type", "Cost" };
		final int m = header.length;
		final List<HousePart> parts = Scene.getInstance().getParts();
		int n = 0;
		int foundationCount = 0;
		for (HousePart p : parts) {
			if (p instanceof Foundation)
				foundationCount++;
		}
		for (HousePart p : parts) {
			if (p == foundation || p.getTopContainer() == foundation || (foundationCount == 1 && p instanceof Tree)) {
				n++;
			}
		}
		final Object[][] column = new Object[n][m];
		int i = 0;
		for (HousePart p : parts) {
			if (p == foundation || p.getTopContainer() == foundation || (foundationCount == 1 && p instanceof Tree)) {
				column[i][0] = p.getId();
				String partName = p.toString().substring(0, p.toString().indexOf(')') + 1);
				int beg = partName.indexOf("(");
				if (beg != -1)
					partName = partName.substring(0, beg);
				column[i][1] = partName;
				column[i][2] = "$" + Cost.getInstance().getPartCost(p);
				i++;
			}
		}

		JTable table = new JTable(column, header);
		table.setModel(new DefaultTableModel(column, header) {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		});
		panel.add(new JScrollPane(table), BorderLayout.CENTER);

		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("Close");
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
		if (windowLocation.x > 0 && windowLocation.y > 0)
			dialog.setLocation(windowLocation);
		else
			dialog.setLocationRelativeTo(MainFrame.getInstance());
		dialog.setVisible(true);

	}

	public void removeGraph() {
		if (pie != null)
			remove(pie);
		repaint();
		add(buttonPanel, BorderLayout.CENTER);
		EnergyPanel.getInstance().validate();
	}

	public void addGraph(Foundation building) {

		if (pie != null)
			remove(pie);
		remove(buttonPanel);

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

		pie = new PieChart(data, colors, legends, "$", null, "Move mouse for more info", false);
		pie.setPreferredSize(new Dimension(getWidth() - 5, getHeight() - 5));
		pie.setBackground(Color.WHITE);
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
