package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.simulation.AnnualGraph;

/**
 * @author Charles Xie
 * 
 */
class UtilityBillDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final static DecimalFormat FORMAT1 = new DecimalFormat("#0.##");

	public UtilityBillDialog(final Foundation foundation) {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Input Electricty Usage from Utility Bill (kWh)");

		final JLabel[] labels = new JLabel[12];
		final JTextField[] fields = new JTextField[12];
		for (int i = 0; i < 12; i++) {
			labels[i] = new JLabel(AnnualGraph.THREE_LETTER_MONTH[i]);
			labels[i].setFont(new Font("Courier New", Font.PLAIN, 12));
			fields[i] = new JTextField(FORMAT1.format(foundation.getUtilityBill().getMonthlyEnergy(i)), 10);
		}

		getContentPane().setLayout(new BorderLayout());
		JPanel container = new JPanel(new GridLayout(1, 2, 10, 10));
		container.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		getContentPane().add(container, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		container.add(panel);

		for (int i = 0; i < 6; i++) {
			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
			p.add(labels[i]);
			p.add(fields[i]);
			panel.add(p);
		}

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		container.add(panel);

		for (int i = 6; i < 12; i++) {
			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
			p.add(labels[i]);
			p.add(fields[i]);
			panel.add(p);
		}

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {

				double[] x = new double[12];
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
					foundation.getUtilityBill().setMonthlyEnergy(i, x[i]);
				}

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