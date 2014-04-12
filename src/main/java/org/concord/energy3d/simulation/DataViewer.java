package org.concord.energy3d.simulation;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * @author Charles Xie
 * 
 */
class DataViewer {

	private DataViewer() {
	}

	@SuppressWarnings("serial")
	static void showDataWindow(String title, Object[][] column, String[] header, final Window parent) {
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

}
