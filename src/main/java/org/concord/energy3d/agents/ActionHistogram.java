package org.concord.energy3d.agents;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.MyAbstractUndoableEdit;
import org.concord.energy3d.util.ClipImage;

/**
 * @author Charles Xie
 *
 */
public class ActionHistogram extends JComponent {

	private static final long serialVersionUID = 1L;
	private final static int LEFT_MARGIN = 60;
	private final static int RIGHT_MARGIN = 20;
	private final static int TOP_MARGIN = 20;
	private final static int BOTTOM_MARGIN = 40;

	// private final BasicStroke dashed = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 8f }, 0.0f);
	private final BasicStroke thin = new BasicStroke(1);
	private final BasicStroke thick = new BasicStroke(2);
	// DecimalFormat format3 = new DecimalFormat("#.###");
	// DecimalFormat format1 = new DecimalFormat("#.#");
	private final int binSize = 20; // in seconds
	private double xmax; // in seconds
	private int ymax = -1;
	private final Vector<UndoableEdit> edits;
	private int[] totalCount;

	public ActionHistogram() {
		super();
		setPreferredSize(new Dimension(800, 500));
		setBackground(Color.WHITE);

		edits = SceneManager.getInstance().getUndoManager().getEdits();
		long t0 = -1;
		long t1 = -1;
		if (edits.size() > 1) {
			final UndoableEdit e0 = edits.get(0);
			final UndoableEdit e1 = edits.get(edits.size() - 1);
			if (e0 instanceof MyAbstractUndoableEdit) {
				t0 = ((MyAbstractUndoableEdit) e0).getTimestamp();
			}
			if (e1 instanceof MyAbstractUndoableEdit) {
				t1 = ((MyAbstractUndoableEdit) e1).getTimestamp();
			}
			if (t0 != -1 && t1 != -1) {
				xmax = (t1 - t0) * 0.001;
				totalCount = new int[(int) Math.round(xmax / binSize) + 1];
				for (final UndoableEdit e : edits) {
					if (e instanceof MyAbstractUndoableEdit) {
						final MyAbstractUndoableEdit x = (MyAbstractUndoableEdit) e;
						final double t = (x.getTimestamp() - t0) * 0.001;
						totalCount[(int) Math.round(t / binSize)]++;
					}
				}
				for (int i = 0; i < totalCount.length; i++) {
					if (totalCount[i] > ymax) {
						ymax = totalCount[i];
					}
				}
			}
		}

	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		update(g);
	}

	@Override
	public void update(final Graphics g) {

		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		final Dimension dim = getSize();
		final int width = dim.width;
		final int height = dim.height;
		g2.setColor(getBackground());
		g2.fillRect(0, 0, width, height);

		g2.setStroke(thick);
		g2.setColor(Color.BLACK);
		final int xAxisY = height - BOTTOM_MARGIN;
		final int graphWindowHeight = height - TOP_MARGIN - BOTTOM_MARGIN;
		final int graphWindowWidth = width - LEFT_MARGIN - RIGHT_MARGIN;
		g2.drawRect(LEFT_MARGIN, TOP_MARGIN, graphWindowWidth, graphWindowHeight);
		g2.setStroke(thin);

		// draw x axis

		g2.setColor(Color.BLACK);
		g2.setFont(new Font("Arial", Font.BOLD, 16));
		g2.drawString("Time", width - RIGHT_MARGIN - 36, xAxisY + 24);

		// draw y axis

		final int x0 = LEFT_MARGIN + 1;
		String yLabel;
		final int scaleY = 60;
		final int ny = Math.round(graphWindowHeight / (float) scaleY);
		g2.setFont(new Font("Arial", Font.PLAIN, 14));
		for (int i = 0; i < ny; i++) {
			g2.drawLine(x0, height - BOTTOM_MARGIN - i * scaleY, x0 + 5, height - BOTTOM_MARGIN - i * scaleY);
			g2.drawString(i + "", x0 - 12, height - BOTTOM_MARGIN - i * scaleY + 4);
		}
		yLabel = "Action Count";
		g2.setFont(new Font("Arial", Font.BOLD, 16));
		final int yLabelX = x0 - 30;
		final int yLabelY = height / 2 + g2.getFontMetrics().stringWidth(yLabel) / 2 - 8;
		g2.rotate(-Math.PI * 0.5, yLabelX, yLabelY);
		g2.drawString(yLabel, yLabelX, yLabelY);
		g2.rotate(Math.PI * 0.5, yLabelX, yLabelY);

		if (totalCount != null) {
			final double dx = graphWindowWidth / totalCount.length;
			final double dy = graphWindowHeight / ymax;
			for (int i = 0; i < totalCount.length; i++) {
				g2.drawRect((int) (x0 + i * dx), graphWindowHeight - (int) (totalCount[i] * dy), binSize, (int) (totalCount[i] * dy));
			}
		}

	}

	public void showDialog() {

		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Action Histogram", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		final JPanel contentPane = new JPanel(new BorderLayout());
		dialog.setContentPane(contentPane);

		final JMenuBar menuBar = new JMenuBar();
		dialog.setJMenuBar(menuBar);

		final JMenu menu = new JMenu("View");
		menuBar.add(menu);

		final JMenuItem mi = new JMenuItem("Copy Image");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				new ClipImage().copyImageToClipboard(ActionHistogram.this);
			}
		});
		menu.add(mi);

		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		contentPane.add(panel, BorderLayout.CENTER);

		panel.add(this, BorderLayout.CENTER);

		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

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
