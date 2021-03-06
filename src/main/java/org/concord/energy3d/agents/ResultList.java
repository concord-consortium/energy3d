package org.concord.energy3d.agents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.concord.energy3d.gui.BuildingDailyEnergyGraph;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.simulation.AnnualSensorData;
import org.concord.energy3d.simulation.BuildingEnergyAnnualGraph;
import org.concord.energy3d.simulation.BuildingEnergyDailyGraph;
import org.concord.energy3d.simulation.DailySensorData;
import org.concord.energy3d.simulation.EnergyAnnualAnalysis;
import org.concord.energy3d.simulation.EnergyDailyAnalysis;
import org.concord.energy3d.simulation.Graph;
import org.concord.energy3d.simulation.PartEnergyAnnualGraph;
import org.concord.energy3d.simulation.PartEnergyDailyGraph;
import org.concord.energy3d.util.ClipImage;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
public class ResultList {

	private Graph graph;
	private final List<AnalysisEvent> events;
	private JPanel graphContainer;
	private JLabel fileLabel;

	public ResultList() {
		events = EventUtil.getAnalysisEvents();
		if (!events.isEmpty()) {
			setGraph(events.get(0));
		}
	}

	private void setGraph(final AnalysisEvent e) {
		final Map<String, List<Double>> results = e.getResults();
		boolean daily = false;
		for (final String key : results.keySet()) {
			daily = results.get(key).size() > 12;
			break;
		}
		if (daily) {
			if (EnergyDailyAnalysis.class.getSimpleName().equals(e.getName())) {
				graph = results.size() > 2 ? new BuildingEnergyDailyGraph() : new PartEnergyDailyGraph();
			} else if (BuildingDailyEnergyGraph.class.getSimpleName().equals(e.getName())) {
				graph = new BuildingEnergyDailyGraph();
			} else {
				graph = new PartEnergyDailyGraph();
				if (DailySensorData.class.getSimpleName().equals(e.getName())) {
					graph.setInstrumentType(Graph.SENSOR);
				}
			}
		} else {
			if (EnergyAnnualAnalysis.class.getSimpleName().equals(e.getName())) {
				graph = results.size() > 2 ? new BuildingEnergyAnnualGraph() : new PartEnergyAnnualGraph();
			} else {
				graph = new PartEnergyAnnualGraph();
				if (AnnualSensorData.class.getSimpleName().equals(e.getName())) {
					graph.setInstrumentType(Graph.SENSOR);
				}
			}
		}
	}

	public void showGui() {

		if (events.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "No previous results were found.", "No Result", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		final JDialog dialog = new JDialog(MainFrame.getInstance(), "List of Results", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		final JPanel contentPane = new JPanel(new BorderLayout());
		dialog.setContentPane(contentPane);

		final JMenuBar menuBar = new JMenuBar();
		dialog.setJMenuBar(menuBar);

		final JMenu menu = new JMenu("Export");
		menuBar.add(menu);

		final JMenuItem mi = new JMenuItem("Copy Image");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (graph != null) {
					new ClipImage().copyImageToClipboard(graph);
				}
			}
		});
		menu.add(mi);

		graphContainer = new JPanel(new BorderLayout());
		graphContainer.setBorder(BorderFactory.createEtchedBorder());
		contentPane.add(graphContainer, BorderLayout.CENTER);

		if (graph != null) {
			graph.setPreferredSize(new Dimension(600, 400));
			graph.setBackground(Color.WHITE);
			graphContainer.add(graph, BorderLayout.CENTER);
		}

		final DefaultListModel<String> listModel = new DefaultListModel<String>();
		for (final AnalysisEvent e : events) {
			listModel.addElement(e.getName() + " (" + EnergyPanel.ONE_DECIMAL.format(0.001 * (System.currentTimeMillis() - e.getTimestamp())) + " s ago)");
		}
		final JList<String> list = new JList<String>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				if (graph != null) {
					graphContainer.remove(graph);
				}
				final int i = list.getSelectedIndex();
				final AnalysisEvent a = events.get(i);
				setGraph(a);
				fileLabel.setText("File: " + Util.getFileName(a.getFile().getPath()));
				graphContainer.add(graph, BorderLayout.CENTER);
				graphContainer.validate();
				final Map<String, List<Double>> r = a.getResults();
				graph.clearData();
				final Map<String, List<Double>> d = graph.getData();
				for (final String x : r.keySet()) {
					d.put(x, new ArrayList<Double>(r.get(x)));
				}
				graph.repaint();
			}
		});
		final JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		graphContainer.add(scrollPane, BorderLayout.WEST);

		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		fileLabel = new JLabel();
		buttonPanel.add(fileLabel);

		final JButton button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.dispose();
			}
		});
		buttonPanel.add(button);

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				dialog.dispose();
			}
		});

		dialog.pack();
		dialog.setLocationRelativeTo(MainFrame.getInstance());
		dialog.setVisible(true);

	}

}
