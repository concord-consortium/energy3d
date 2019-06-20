package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
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
 */
class ResultGraphPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final Optimizer op;

    ResultGraphPanel(final Optimizer op) {

        super();
        setBackground(Color.DARK_GRAY);
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        this.op = op;

        final Individual[] individuals = op.getPopulation().getIndividuals();
        final int n = individuals[0].getChromosomeLength();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int mx = (int) Math.round((double) screenSize.width / (double) n);
        if (mx > 400) {
            mx = 400;
        }

        for (int i = 0; i < n; i++) {
            int nbin = 100;
            if (op.isGeneInteger(i)) {
                nbin = (int) Math.round(op.getGeneMaximum(i) - op.getGeneMinimum(i));
            }
            final GeneSpatialGraph g = new GeneSpatialGraph(individuals, i, op.getGeneName(i), nbin, op.getGeneMinimum(i), op.getGeneMaximum(i));
            g.setPreferredSize(new Dimension(mx, 400));
            add(g);
        }

    }

    void display(final String title) {

        final JDialog dialog = new JDialog(MainFrame.getInstance(), title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        final JPanel contentPane = new JPanel(new BorderLayout());
        dialog.setContentPane(contentPane);

        final JMenuBar menuBar = new JMenuBar();
        dialog.setJMenuBar(menuBar);

        final JMenu menu = new JMenu("Export");
        menuBar.add(menu);

        final JMenuItem mi = new JMenuItem("Copy Image");
        mi.addActionListener(e -> new ClipImage().copyImageToClipboard(ResultGraphPanel.this));
        menu.add(mi);

        final JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        contentPane.add(panel, BorderLayout.CENTER);

        panel.add(this, BorderLayout.CENTER);

        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        JButton button = new JButton("Objective");
        button.addActionListener(e -> {
            if (op != null) {
                if (op.getFittestOfGenerations() != null) {
                    new ObjectiveTemporalGraph(op.getFittestOfGenerations()).display("Objective Trend");
                }
            } else {
                JOptionPane.showMessageDialog(MainFrame.getInstance(), "No result is available.", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        buttonPanel.add(button);

        button = new JButton("Close");
        button.addActionListener(e -> dialog.dispose());
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