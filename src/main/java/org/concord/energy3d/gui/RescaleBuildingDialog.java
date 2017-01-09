package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
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

		final JTextField oldXField = new JTextField();
		final JTextField newXField = new JTextField();
		final JTextField oldYField = new JTextField();
		final JTextField newYField = new JTextField();
		final JTextField oldZField = new JTextField();
		final JTextField newZField = new JTextField();

		getContentPane().setLayout(new BorderLayout());
		final JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
		panel.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(panel, BorderLayout.CENTER);

		final ActionListener okListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				double oldX = 1;
				String t = oldXField.getText();
				if (t != null && !t.trim().equals("")) {
					try {
						oldX = Double.parseDouble(t);
					} catch (final NumberFormatException ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Invalid input for current length in X-direction: " + ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (oldX <= 0) {
						JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Current length in X-direction must be greater than zero!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				double newX = 1;
				t = newXField.getText();
				if (t != null && !t.trim().equals("")) {
					try {
						newX = Double.parseDouble(t);
					} catch (final NumberFormatException ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Invalid input for new length in X-direction: " + ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (newX <= 0) {
						JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "New length in X-direction must be greater than zero!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				double oldY = 1;
				t = oldYField.getText();
				if (t != null && !t.trim().equals("")) {
					try {
						oldY = Double.parseDouble(t);
					} catch (final NumberFormatException ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Invalid input for current length in Y-direction: " + ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (oldY <= 0) {
						JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Current length in Y-direction must be greater than zero!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				double newY = 1;
				t = newYField.getText();
				if (t != null && !t.trim().equals("")) {
					try {
						newY = Double.parseDouble(t);
					} catch (final NumberFormatException ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Invalid input for new length in Y-direction: " + ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (newY <= 0) {
						JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "New length in Y-direction must be greater than zero!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				double oldZ = 1;
				t = oldZField.getText();
				if (t != null && !t.trim().equals("")) {
					try {
						oldZ = Double.parseDouble(t);
					} catch (final NumberFormatException ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Invalid input for current height: " + ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (oldZ <= 0) {
						JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Current height must be greater than zero!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				double newZ = 1;
				t = newZField.getText();
				if (t != null && !t.trim().equals("")) {
					try {
						newZ = Double.parseDouble(t);
					} catch (final NumberFormatException ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "Invalid input for new height: " + ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (newZ <= 0) {
						JOptionPane.showMessageDialog(RescaleBuildingDialog.this, "New height must be greater than zero!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				final double scaleX = newX / oldX;
				final double scaleY = newY / oldY;
				final double scaleZ = newZ / oldZ;
				SceneManager.getTaskManager().update(new Callable<Object>() {
					@Override
					public Object call() {
						if (foundation.isGroupMaster()) {
							final List<Foundation> g = Scene.getInstance().getFoundationGroup(foundation);
							for (final Foundation f : g) {
								f.rescale(scaleX, scaleY, scaleZ);
								f.draw();
								f.drawChildren();
							}
						} else {
							foundation.rescale(scaleX, scaleY, scaleZ);
							foundation.draw();
							foundation.drawChildren();
						}
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

		panel.add(new JLabel("Current Height: "));
		panel.add(oldZField);
		panel.add(new JLabel("New Height: "));
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