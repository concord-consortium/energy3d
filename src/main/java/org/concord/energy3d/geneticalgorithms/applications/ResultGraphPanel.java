package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.util.ClipImage;

/**
 * @author Charles Xie
 *
 */
public class ResultGraphPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final Optimizer op;

	public ResultGraphPanel(final Optimizer op) {

		super();
		setBackground(Color.DARK_GRAY);
		setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

		this.op = op;

		final Individual[] individuals = op.getPopulation().getIndividuals();
		final int n = individuals[0].getChromosomeLength();

		for (int i = 0; i < n; i++) {
			int nbin = 100;
			if (op.isGeneInteger(i)) {
				nbin = (int) Math.round(op.getGeneMaximum(i) - op.getGeneMinimum(i));
			}
			final GeneGraph g = new GeneGraph(individuals, i, op.getGeneName(i), nbin);
			g.setPreferredSize(new Dimension(400, 400));
			add(g);
		}

	}

	public void display() {

		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Previous Results", true);
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
				new ClipImage().copyImageToClipboard(ResultGraphPanel.this);
			}
		});
		menu.add(mi);

		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		contentPane.add(panel, BorderLayout.CENTER);

		panel.add(this, BorderLayout.CENTER);

		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("Fitness");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (op != null) {
					if (op.getFittestOfGenerations() != null) {
						new FitnessGraph(op.getFittestOfGenerations()).display("Fitness Trend of Previous Run");
					}
				} else {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "No previous result is available.", "Information", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		buttonPanel.add(button);

		button = new JButton("Close");
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

		dialog.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				final int n = getComponentCount();
				for (int i = 0; i < n; i++) {
					final Component c = getComponent(i);
					c.setPreferredSize(new Dimension(getWidth() / n, getHeight()));
				}
				repaint();
			}
		});

		dialog.pack();
		dialog.setLocationRelativeTo(MainFrame.getInstance());
		dialog.setVisible(true);

	}

}
