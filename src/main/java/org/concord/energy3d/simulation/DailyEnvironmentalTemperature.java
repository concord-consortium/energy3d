package org.concord.energy3d.simulation;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.agents.OperationEvent;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.ClipImage;
import org.concord.energy3d.util.Util;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Path2D;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * This graph shows the daily variation of air and ground temperatures for the current day and location.
 *
 * @author Charles Xie
 */

public class DailyEnvironmentalTemperature extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int CIRCLE = 0;
    private static final int DIAMOND = 1;
    private static final int SQUARE = 2;
    private static final int TRIANGLEUP = 3;
    private static final int TRIANGLEDOWN = 4;

    private final int top = 50, right = 50, bottom = 80, left = 90;
    private final double xmin = 0;
    private final double xmax = 23;
    private double ymin = 1000;
    private double ymax = -1000;
    private double dx;
    private double dy;
    private final double xNow;
    private final int symbolSize = 8;
    private final int numberOfTicks = 24;
    private final String xAxisLabel = "Hour";
    private final String yAxisLabel = "Temperature (\u00b0C)";
    private final BasicStroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{2f}, 0.0f);
    private final BasicStroke thin = new BasicStroke(1);
    private final BasicStroke thick = new BasicStroke(2);
    private final String city;
    private final Calendar today;

    private final double lowestAirTemperature;
    private final double highestAirTemperature;

    private final double[] depth;
    private final double[][] groundTemperature;

    private final int[] symbol;
    private final BasicStroke[] stroke;
    private final Map<double[], Boolean> hideData;

    public DailyEnvironmentalTemperature() {

        super();
        setPreferredSize(new Dimension(600, 400));
        setBackground(Color.WHITE);

        hideData = new HashMap<>();

        depth = new double[]{0, 0.5, 1, 2, 6};
        symbol = new int[]{-1, CIRCLE, DIAMOND, SQUARE, -1};
        stroke = new BasicStroke[]{thick, thick, thick, thick, dashed};
        final int m = depth.length;
        groundTemperature = new double[m][24];

        for (int i = 0; i < m; i++) {
            hideData.put(groundTemperature[i], false);
        }

        city = (String) EnergyPanel.getInstance().getRegionComboBox().getSelectedItem();
        today = Heliodon.getInstance().getCalendar();
        xNow = today.get(Calendar.HOUR_OF_DAY) + today.get(Calendar.MINUTE) / 60.0;
        final int day = today.get(Calendar.DAY_OF_YEAR);
        final double[] r = Weather.computeOutsideTemperature(today, city);
        lowestAirTemperature = r[0];
        highestAirTemperature = r[1];
        final double amp = 0.5 * (highestAirTemperature - lowestAirTemperature);
        for (int h = 0; h < 24; h++) {
            for (int i = 0; i < m; i++) {
                groundTemperature[i][h] = i == 0 ? Weather.getInstance().getOutsideTemperatureAtMinute(highestAirTemperature, lowestAirTemperature, h * 60) : Scene.getInstance().getGround().getTemperatureMinuteOfDay(day, h * 60, amp, depth[i]);
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
        g2.setColor(Color.GRAY);
        g2.drawRect(left / 2, top / 2, width - (left + right) / 2, height - (top + bottom) / 2);

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, 8));
        final float tickWidth = (float) (width - left - right) / (float) (numberOfTicks - 1);
        float xTick;
        for (int i = 0; i < numberOfTicks; i++) {
            final String s = i + "";
            final int sWidth = g2.getFontMetrics().stringWidth(s);
            xTick = left + tickWidth * i;
            g2.drawString(s, xTick - sWidth / 2f, height - bottom / 2 + 16);
            g2.drawLine((int) xTick, height - bottom / 2, (int) xTick, height - bottom / 2 - 4);
        }
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        final int xAxisLabelWidth = g2.getFontMetrics().stringWidth(xAxisLabel);
        final int yAxisLabelWidth = g2.getFontMetrics().stringWidth(yAxisLabel);
        g2.drawString(xAxisLabel, (width - xAxisLabelWidth) / 2, height - 8);
        g2.rotate(-Math.PI / 2, 16, (height + yAxisLabelWidth) / 2);
        g2.drawString(yAxisLabel, 16, (height + yAxisLabelWidth) / 2);
        g2.rotate(Math.PI / 2, 16, (height + yAxisLabelWidth) / 2);

        calculateBounds();
        int digits = String.valueOf(Math.round(ymax - ymin)).length() - 1;
        digits = (int) Math.pow(10, digits);
        final int i1 = (int) Math.round(ymin / digits) - 2;
        final int i2 = (int) Math.round(ymax / digits) + 2;
        float hVal;
        int hPos;
        for (int i = i1; i <= i2; i++) {
            hVal = i * digits;
            hPos = (int) Math.round(getHeight() - top - (hVal - ymin) * dy);
            if (hPos > top / 2 && hPos < getHeight() - bottom / 2) {
                drawHorizontalLine(g2, hPos, Graph.ONE_DECIMAL.format(hVal));
            }
            hVal = (i + 0.5f) * digits;
            hPos = (int) Math.round(getHeight() - top - (hVal - ymin) * dy);
            if (hPos >= top / 2 && hPos <= getHeight() - bottom / 2) {
                drawHorizontalLine(g2, hPos, Graph.ONE_DECIMAL.format(hVal));
            }
        }

        for (int i = 0; i < depth.length; i++) {
            if (!hideData.get(groundTemperature[i])) {
                drawCurve(g2, groundTemperature[i], Color.GRAY, symbol[i], stroke[i]);
            }
        }

        drawLegends(g2);

        g2.setFont(new Font("Arial", Font.BOLD, 14));
        final FontMetrics fm = g2.getFontMetrics();
        final String cityAndDate = city + " - " + (today.get(Calendar.MONTH) + 1) + "/" + today.get(Calendar.DAY_OF_MONTH);
        g2.drawString(cityAndDate, (width - fm.stringWidth(cityAndDate)) / 2, 20);

        g2.setColor(Color.LIGHT_GRAY);
        g2.setStroke(thin);
        final int xTodayLine = (int) Math.round(left + dx * xNow);
        g2.drawLine(xTodayLine, top / 2, xTodayLine, height - bottom / 2);

    }

    private void drawLegends(final Graphics2D g2) {

        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.setStroke(thin);
        final int x0 = left / 2 + 20;
        int y0 = top - 10;

        for (int i = 0; i < depth.length - 1; i++) {
            if (!hideData.get(groundTemperature[i])) {
                g2.setColor(Color.GRAY);
                g2.setStroke(stroke[i]);
                g2.drawLine(x0 - 7, y0 + 4, x0 + 13, y0 + 4);
                g2.setStroke(thin);
                drawSymbol(g2, symbol[i], x0, y0);
                g2.drawString(i == 0 ? "Air" : "Ground (" + depth[i] + "m deep)", x0 + 20, y0 + 8);
                y0 += 14;
            }
        }

        if (!hideData.get(groundTemperature[depth.length - 1])) {
            g2.setColor(Color.GRAY);
            g2.setStroke(dashed);
            g2.drawLine(x0 - 7, y0 + 4, x0 + 13, y0 + 4);
            g2.setColor(Color.BLACK);
            g2.drawString("Ground (" + depth[depth.length - 1] + "m deep)", x0 + 20, y0 + 8);
        }

    }

    private void drawSymbol(final Graphics g2, final int sym, final int x0, final int y0) {
        switch (sym) {
            case CIRCLE:
                Graph.drawCircle(g2, x0, y0, 6, Color.WHITE);
                break;
            case DIAMOND:
                Graph.drawDiamond(g2, x0 + 3, y0 + 4, 4, Color.WHITE);
                break;
            case SQUARE:
                Graph.drawSquare(g2, x0, y0, 6, Color.WHITE);
                break;
            case TRIANGLEUP:
                Graph.drawTriangleUp(g2, x0, y0, 6, Color.WHITE);
                break;
            case TRIANGLEDOWN:
                Graph.drawTriangleDown(g2, x0, y0, 6, Color.WHITE);
                break;
        }
    }

    private void drawCurve(final Graphics2D g2, final double[] data, final Color color, final int sym, final BasicStroke stk) {

        double dataX, dataY;
        final Path2D.Float path = new Path2D.Float();
        for (int i = 0; i < data.length; i++) {
            dataX = left + dx * i;
            dataY = getHeight() - top - (data[i] - ymin) * dy;
            if (i == 0) {
                path.moveTo(dataX, dataY);
            } else {
                path.lineTo(dataX, dataY);
            }
        }
        g2.setColor(color);
        g2.setStroke(stk);
        g2.draw(path);

        if (sym != -1) {
            g2.setStroke(thin);
            for (int i = 0; i < data.length; i++) {
                dataX = left + dx * i;
                dataY = getHeight() - top - (data[i] - ymin) * dy;
                switch (sym) {
                    case CIRCLE:
                        Graph.drawCircle(g2, (int) Math.round(dataX - symbolSize / 2 + 1), (int) Math.round(dataY - symbolSize / 2 + 1), symbolSize - 2, Color.WHITE);
                        break;
                    case SQUARE:
                        Graph.drawSquare(g2, (int) Math.round(dataX - symbolSize / 2 + 1), (int) Math.round(dataY - symbolSize / 2 + 1), symbolSize - 2, Color.WHITE);
                        break;
                    case DIAMOND:
                        Graph.drawDiamond(g2, (int) dataX, (int) dataY, symbolSize / 2, Color.WHITE);
                        break;
                    case TRIANGLEUP:
                        Graph.drawTriangleUp(g2, (int) Math.round(dataX - symbolSize / 2 + 1), (int) Math.round(dataY - symbolSize / 2 + 1), symbolSize - 2, Color.WHITE);
                        break;
                    case TRIANGLEDOWN:
                        Graph.drawTriangleDown(g2, (int) Math.round(dataX - symbolSize / 2 + 1), (int) Math.round(dataY - symbolSize / 2 + 1), symbolSize - 2, Color.WHITE);
                        break;
                }
            }
        }

    }

    private void drawHorizontalLine(final Graphics2D g2, final int yValue, final String yLabel) {
        g2.setStroke(thin);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(left / 2, yValue, getWidth() - right / 2, yValue);
        g2.setColor(Color.BLACK);
        final int yLabelWidth = g2.getFontMetrics().stringWidth(yLabel);
        g2.drawString(yLabel, left / 2 - 5 - yLabelWidth, yValue + 4);
    }

    private void calculateBounds() {
        if (ymin > lowestAirTemperature) {
            ymin = lowestAirTemperature;
        }
        if (ymax < highestAirTemperature) {
            ymax = highestAirTemperature;
        }
        double t;
        for (int i = 0; i < groundTemperature.length; i++) {
            for (int j = 0; j < groundTemperature[i].length; j++) {
                t = groundTemperature[i][j];
                if (t < ymin) {
                    ymin = t;
                }
                if (t > ymax) {
                    ymax = t;
                }
            }
        }
        dx = (getWidth() - left - right) / (xmax - xmin);
        dy = (getHeight() - top - bottom) / (ymax - ymin);
    }

    public void showDialog() {

        final JDialog dialog = new JDialog(MainFrame.getInstance(), "Daily Environmental Temperature", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        final JPanel contentPane = new JPanel(new BorderLayout());
        dialog.setContentPane(contentPane);

        final JMenuBar menuBar = new JMenuBar();
        dialog.setJMenuBar(menuBar);

        final JCheckBoxMenuItem[] cbmiGroundTemperature = new JCheckBoxMenuItem[depth.length];
        for (int i = 0; i < depth.length; i++) {
            cbmiGroundTemperature[i] = new JCheckBoxMenuItem(i == 0 ? "Air Temperature" : "Ground Temperature (" + depth[i] + "m)");
            Util.selectSilently(cbmiGroundTemperature[i], !hideData.get(groundTemperature[i]));
        }

        final JMenu menuView = new JMenu("View");
        menuBar.add(menuView);
        menuView.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(final MenuEvent e) {
                for (int i = 0; i < depth.length; i++) {
                    Util.selectSilently(cbmiGroundTemperature[i], !hideData.get(groundTemperature[i]));
                }
            }

            @Override
            public void menuDeselected(final MenuEvent e) {
            }

            @Override
            public void menuCanceled(final MenuEvent e) {
            }
        });

        for (int i = 0; i < depth.length; i++) {
            final int i2 = i;
            cbmiGroundTemperature[i].addItemListener(e -> {
                final JCheckBoxMenuItem source = (JCheckBoxMenuItem) e.getSource();
                hideData.put(groundTemperature[i2], !source.isSelected());
                DailyEnvironmentalTemperature.this.repaint();
            });
            menuView.add(cbmiGroundTemperature[i]);
        }

        final JMenu menuExport = new JMenu("Export");
        menuBar.add(menuExport);

        final JMenuItem mi = new JMenuItem("Copy Image");
        mi.addActionListener(e -> new ClipImage().copyImageToClipboard(DailyEnvironmentalTemperature.this));
        menuExport.add(mi);

        final JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        contentPane.add(panel, BorderLayout.CENTER);

        panel.add(this, BorderLayout.CENTER);

        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        final JButton button = new JButton("Close");
        button.addActionListener(e -> dialog.dispose());
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

        TimeSeriesLogger.getInstance().logAnalysis(this);
        final HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("Location", Scene.getInstance().getCity());
        attributes.put("Date", Scene.getInstance().getDate().toString());
        MainApplication.addEvent(new OperationEvent(Scene.getURL(), System.currentTimeMillis(), getClass().getSimpleName(), attributes));

    }

    // TODO
    public String toJson() {
        final String s = "{}";
        return s;
    }

}