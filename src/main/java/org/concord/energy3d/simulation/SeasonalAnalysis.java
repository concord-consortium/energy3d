package org.concord.energy3d.simulation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

/**
 * This calculates and visualizes the seasonal trend and the yearly sum of all energy items for any selected part or building.
 * 
 * For fast feedback, by default, the sum is based on adding the energy items computed for the first day of each month and then that number is multiplied by 365/12.
 * 
 * @author Charles Xie
 * 
 */

public class SeasonalAnalysis {

	public SeasonalAnalysis() {

	}

	public void compute() {

		clearSum();

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				createDialog();
			}
		});

	}

	private void clearSum() {
		for (HousePart x : Scene.getInstance().getParts())
			x.setSolarPotentialSum(0);
	}

	private void createDialog() {

		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Seasonal Analysis", true);
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setPreferredSize(new Dimension(500, 500));
		dialog.setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setBorder(BorderFactory.createEtchedBorder());
		contentPane.add(panel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		JButton button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		buttonPanel.add(button);

		dialog.pack();
		dialog.setLocationRelativeTo(MainFrame.getInstance());
		dialog.setVisible(true);

	}

}
