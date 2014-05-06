package org.concord.energy3d.simulation;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.SceneManager;

/**
 * @author Charles Xie
 * 
 */
class DataViewer {

	private DataViewer() {
	}

	@SuppressWarnings("serial")
	private static void showDataWindow(String title, Object[][] column, String[] header, final java.awt.Window parent) {
		final JDialog dataWindow = new JDialog(JOptionPane.getFrameForComponent(parent), title, true);
		dataWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		final JTable table = new JTable(column, header);
		table.setModel(new DefaultTableModel(column, header) {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		});
		dataWindow.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
		JPanel p = new JPanel();
		dataWindow.getContentPane().add(p, BorderLayout.SOUTH);
		JButton button = new JButton("Copy Data");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				table.selectAll();
				ActionEvent ae = new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "copy");
				if (ae != null) {
					table.getActionMap().get(ae.getActionCommand()).actionPerformed(ae);
					JOptionPane.showMessageDialog(parent, "The data is now ready for pasting.", "Copy Data", JOptionPane.INFORMATION_MESSAGE);
					table.clearSelection();
				}
			}
		});
		button.setToolTipText("Copy data to the system clipboard");
		p.add(button);
		button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dataWindow.dispose();
			}
		});
		p.add(button);
		dataWindow.pack();
		dataWindow.setLocationRelativeTo(parent);
		dataWindow.setVisible(true);
	}

	static void viewRawData(final JDialog parent, Graph graph) {
		String[] header = null;
		if (graph instanceof BuildingEnergyAnnualGraph) {
			header = new String[] { "Month", "Windows", "Solar Panels", "Heater", "AC", "Net" };
		} else if (graph instanceof PartEnergyAnnualGraph) {
			final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			if (selectedPart instanceof SolarPanel) {
				header = new String[] { "Month", "Solar" };
			} else if (selectedPart instanceof Wall || selectedPart instanceof Roof) {
				header = new String[] { "Month", "Heat Gain" };
			} else if (selectedPart instanceof Window) {
				header = new String[] { "Month", "Solar", "Heat Gain" };
			}
		} else if (graph instanceof BuildingEnergyAngularGraph) {
			header = new String[] { "Degree", "Windows", "Solar Panels", "Heater", "AC", "Net" };
		} else if (graph instanceof PartEnergyAngularGraph) {
			final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			if (selectedPart instanceof SolarPanel) {
				header = new String[] { "Degree", "Solar" };
			} else if (selectedPart instanceof Wall || selectedPart instanceof Roof) {
				header = new String[] { "Degree", "Heat Gain" };
			} else if (selectedPart instanceof Window) {
				header = new String[] { "Degree", "Solar", "Heat Gain" };
			}
		}
		if (header == null) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Problem in finding data.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		final int m = header.length;
		final int n = graph.getLength();
		final Object[][] column = new Object[n][m + 1];
		for (int i = 0; i < n; i++)
			column[i][0] = (i + 1);
		for (int j = 1; j < header.length; j++) {
			final List<Double> list = graph.getData(header[j]);
			for (int i = 0; i < n; i++)
				column[i][j] = list.get(i);
		}
		showDataWindow("Data", column, header, parent);
	}

}
