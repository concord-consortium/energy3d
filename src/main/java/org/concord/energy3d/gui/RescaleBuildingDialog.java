package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.RescaleBuildingCommand;

/**
 * @author Charles Xie
 * 
 */
class RescaleBuildingDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public RescaleBuildingDialog(final Foundation foundation) {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Rescale Building #" + foundation.getId());

		final JTextField oldXField = new JTextField("1.0");
		final JTextField newXField = new JTextField("1.0");
		final JTextField oldYField = new JTextField("1.0");
		final JTextField newYField = new JTextField("1.0");
		final JTextField oldZField = new JTextField("1.0");
		final JTextField newZField = new JTextField("1.0");

		getContentPane().setLayout(new BorderLayout());
		final JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
		panel.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(panel, BorderLayout.CENTER);

		ActionListener okListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				double oldX = 1;
				try {
					oldX = Double.parseDouble(oldXField.getText());
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Invalid input for current length in X-direction: " + ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (oldX <= 0) {
					JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Current length in X-direction must be greater than zero!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				double newX = 1;
				try {
					newX = Double.parseDouble(newXField.getText());
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Invalid input for new length in X-direction: " + ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (newX <= 0) {
					JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "New length in X-direction must be greater than zero!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				double oldY = 1;
				try {
					oldY = Double.parseDouble(oldYField.getText());
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Invalid input for current length in Y-direction: " + ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (oldY <= 0) {
					JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Current length in Y-direction must be greater than zero!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				double newY = 1;
				try {
					newY = Double.parseDouble(newYField.getText());
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Invalid input for new length in Y-direction: " + ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (newY <= 0) {
					JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "New length in Y-direction must be greater than zero!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				double oldZ = 1;
				try {
					oldZ = Double.parseDouble(oldZField.getText());
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Invalid input for current length in Z-direction: " + ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (oldZ <= 0) {
					JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Current length in Z-direction must be greater than zero!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				double newZ = 1;
				try {
					newZ = Double.parseDouble(newZField.getText());
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Invalid input for new length in Z-direction: " + ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (newZ <= 0) {
					JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "New length in Z-direction must be greater than zero!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				final double scaleX = newX / oldX;
				final double scaleY = newY / oldY;
				final double scaleZ = newZ / oldZ;
				SceneManager.getTaskManager().update(new Callable<Object>() {
					@Override
					public Object call() {
						foundation.rescale(scaleX, scaleY, scaleZ);
						Scene.getInstance().redrawAll();
						return null;
					}
				});
				SceneManager.getInstance().getUndoManager().addEdit(new RescaleBuildingCommand(foundation, oldX, newX, oldY, newY, oldZ, newZ));
				Scene.getInstance().setEdited(true);
				EnergyPanel.getInstance().update();
				RescaleBuildingDialog.this.dispose();
			}
		};

		panel.add(new JLabel("Current Length in X-Direction: "));
		panel.add(oldXField);
		panel.add(new JLabel("New Length in X-Direction: "));
		panel.add(newXField);

		panel.add(new JLabel("Current Length in Y-Direction: "));
		panel.add(oldYField);
		panel.add(new JLabel("New Length in Y-Direction: "));
		panel.add(newYField);

		panel.add(new JLabel("Current Length in Z-Direction: "));
		panel.add(oldZField);
		panel.add(new JLabel("New Length in Z-Direction: "));
		panel.add(newZField);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(okListener);
		okButton.setActionCommand("OK");
		buttonPanel.add(okButton);
		getRootPane().setDefaultButton(okButton);

		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				RescaleBuildingDialog.this.dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPanel.add(cancelButton);

		pack();
		setLocationRelativeTo(MainFrame.getInstance());

	}

}