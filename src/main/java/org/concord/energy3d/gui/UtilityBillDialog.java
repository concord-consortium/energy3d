package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.simulation.AnnualGraph;
import org.concord.energy3d.simulation.UtilityBill;
import org.concord.energy3d.util.SpringUtilities;

/**
 * @author Charles Xie
 * 
 */
class UtilityBillDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final static DecimalFormat FORMAT1 = new DecimalFormat("#0.##");

	public UtilityBillDialog(final UtilityBill utilityBill) {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Input Electricty Usage from Utility Bill (kWh)");

		final JLabel[] labels = new JLabel[12];
		final JTextField[] fields = new JTextField[12];
		for (int i = 0; i < 12; i++) {
			labels[i] = new JLabel(AnnualGraph.THREE_LETTER_MONTH[i]);
			fields[i] = new JTextField(FORMAT1.format(utilityBill.getMonthlyEnergy(i)), 10);
		}

		getContentPane().setLayout(new BorderLayout());
		final JPanel container = new JPanel(new GridLayout(1, 2, 10, 10));
		container.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		getContentPane().add(container, BorderLayout.CENTER);

		final JPanel panel = new JPanel();
		container.add(panel);
		panel.setLayout(new SpringLayout());
		for (int i = 0; i < 12; i++) {
			panel.add(labels[i]);
			panel.add(fields[i]);
		}
		SpringUtilities.makeCompactGrid(panel, 6, 4, 6, 6, 6, 6);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {

				final double[] x = new double[12];
				try {
					for (int i = 0; i < 12; i++) {
						x[i] = Double.parseDouble(fields[i].getText());
					}
				} catch (final NumberFormatException err) {
					err.printStackTrace();
					JOptionPane.showMessageDialog(UtilityBillDialog.this, "Invalid input: " + err.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				for (int i = 0; i < 12; i++) {
					if (x[i] < 0) {
						JOptionPane.showMessageDialog(UtilityBillDialog.this, "Energy usage cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					utilityBill.setMonthlyEnergy(i, x[i]);
				}
				Scene.getInstance().setEdited(true);
				UtilityBillDialog.this.dispose();

			}
		});
		okButton.setActionCommand("OK");
		buttonPanel.add(okButton);
		getRootPane().setDefaultButton(okButton);

		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				UtilityBillDialog.this.dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPanel.add(cancelButton);

		pack();
		setLocationRelativeTo(MainFrame.getInstance());

	}

}