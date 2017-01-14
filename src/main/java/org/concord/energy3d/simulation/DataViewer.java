package org.concord.energy3d.simulation;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

/**
 * @author Charles Xie
 * 
 */
class DataViewer {

	private DataViewer() {
	}

	@SuppressWarnings("serial")
	private static void showDataWindow(final String title, final Object[][] column, final String[] header, final java.awt.Window parent) {
		final JDialog dataWindow = new JDialog(JOptionPane.getFrameForComponent(parent), title, true);
		dataWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		final JTable table = new JTable(column, header);
		table.setModel(new DefaultTableModel(column, header) {
			@Override
			public boolean isCellEditable(final int row, final int col) {
				return false;
			}
		});
		dataWindow.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
		final JPanel p = new JPanel();
		dataWindow.getContentPane().add(p, BorderLayout.SOUTH);
		JButton button = new JButton("Copy Data");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				table.selectAll();
				final ActionEvent ae = new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "copy");
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
			@Override
			public void actionPerformed(final ActionEvent e) {
				dataWindow.dispose();
			}
		});
		p.add(button);
		dataWindow.pack();
		dataWindow.setLocationRelativeTo(parent);
		dataWindow.setVisible(true);
	}

	static void viewRawData(final java.awt.Window parent, final Graph graph, final boolean selectAll) {
		String[] header = null;
		if (graph instanceof BuildingEnergyDailyGraph) {
			header = new String[] { "Hour", "Windows", "Solar Panels", "Heater", "AC", "Net" };
		} else if (graph instanceof BuildingEnergyAnnualGraph) {
			header = new String[] { "Month", "Windows", "Solar Panels", "Heater", "AC", "Net" };
		} else if (graph instanceof PartEnergyDailyGraph) {
			final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			if (selectAll || selectedPart instanceof SolarPanel || selectedPart instanceof Rack || selectedPart instanceof Mirror || selectedPart instanceof Foundation) {
				header = new String[] { "Hour", "Solar" };
			} else if (selectedPart instanceof Wall || selectedPart instanceof Roof || selectedPart instanceof Door) {
				header = new String[] { "Hour", "Heat Gain" };
			} else if (selectedPart instanceof Window) {
				header = new String[] { "Hour", "Solar", "Heat Gain" };
			}
			if (graph.type == Graph.SENSOR) {
				final List<HousePart> parts = Scene.getInstance().getParts();
				final List<String> sensorList = new ArrayList<String>();
				for (final HousePart p : parts) {
					if (p instanceof Sensor) {
						final Sensor sensor = (Sensor) p;
						sensorList.add("Light: #" + sensor.getId());
						sensorList.add("Heat Flux: #" + sensor.getId());
					}
				}
				if (!sensorList.isEmpty()) {
					header = new String[1 + sensorList.size()];
					header[0] = "Hour";
					for (int i = 1; i < header.length; i++) {
						header[i] = sensorList.get(i - 1);
					}
				}
			}
		} else if (graph instanceof PartEnergyAnnualGraph) {
			final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			if (selectAll || selectedPart instanceof SolarPanel || selectedPart instanceof Rack || selectedPart instanceof Mirror || selectedPart instanceof Foundation) {
				header = new String[] { "Month", "Solar" };
			} else if (selectedPart instanceof Wall || selectedPart instanceof Roof || selectedPart instanceof Door) {
				header = new String[] { "Month", "Heat Gain" };
			} else if (selectedPart instanceof Window) {
				header = new String[] { "Month", "Solar", "Heat Gain" };
			}
			if (graph.type == Graph.SENSOR) {
				final List<HousePart> parts = Scene.getInstance().getParts();
				final List<String> sensorList = new ArrayList<String>();
				for (final HousePart p : parts) {
					if (p instanceof Sensor) {
						final Sensor sensor = (Sensor) p;
						sensorList.add("Light: #" + sensor.getId());
						sensorList.add("Heat Flux: #" + sensor.getId());
					}
				}
				if (!sensorList.isEmpty()) {
					header = new String[1 + sensorList.size()];
					header[0] = "Month";
					for (int i = 1; i < header.length; i++) {
						header[i] = sensorList.get(i - 1);
					}
				}
			}
		} else if (graph instanceof BuildingEnergyAngularGraph) {
			header = new String[] { "Degree", "Windows", "Solar Panels", "Heater", "AC", "Net" };
		} else if (graph instanceof PartEnergyAngularGraph) {
			final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			if (selectedPart instanceof SolarPanel || selectedPart instanceof Rack || selectedPart instanceof Mirror || selectedPart instanceof Foundation) {
				header = new String[] { "Degree", "Solar" };
			} else if (selectedPart instanceof Wall || selectedPart instanceof Roof || selectedPart instanceof Door) {
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
		for (int i = 0; i < n; i++) {
			column[i][0] = header[0].equals("Hour") ? i : (i + 1);
		}
		for (int j = 1; j < m; j++) {
			final List<Double> list = graph.getData(header[j]);
			for (int i = 0; i < n; i++) {
				column[i][j] = list.get(i);
			}
		}
		showDataWindow("Data", column, header, parent);
	}

	static void viewRawData(final java.awt.Window parent, final Graph graph, final List<HousePart> selectedParts) {
		if (selectedParts == null || selectedParts.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "No part is selected.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		final ArrayList<String> headers = new ArrayList<String>();
		if (graph instanceof PartEnergyDailyGraph) {
			headers.add("Hour");
		} else if (graph instanceof PartEnergyAnnualGraph) {
			headers.add("Month");
		}
		for (final HousePart p : selectedParts) {
			if (p instanceof SolarPanel || p instanceof Rack || p instanceof Mirror) {
				headers.add("Solar " + p.getId());
			} else if (p instanceof Wall || p instanceof Roof || p instanceof Door) {
				headers.add("Heat Gain " + p.getId());
			} else if (p instanceof Window) {
				headers.add("Solar " + p.getId());
				headers.add("Heat Gain " + p.getId());
			}
		}
		final String[] headersArray = new String[headers.size()];
		for (int i = 0; i < headersArray.length; i++) {
			headersArray[i] = headers.get(i);
		}
		final int m = headersArray.length;
		final int n = graph.getLength();
		final Object[][] column = new Object[n][m + 1];
		for (int i = 0; i < n; i++) {
			column[i][0] = (i + 1);
		}
		for (int j = 1; j < m; j++) {
			final List<Double> list = graph.getData(headersArray[j]);
			for (int i = 0; i < n; i++) {
				column[i][j] = list.get(i);
			}
		}
		showDataWindow("Data", column, headersArray, parent);
	}

}
