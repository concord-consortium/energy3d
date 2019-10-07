package org.concord.energy3d.simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.ClipImage;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 */
public abstract class Graph extends JPanel {

    private static final long serialVersionUID = 1L;

    public final static byte DEFAULT = 0;
    public final static byte SENSOR = 1;

    public final static int LINE_CHART = 0;
    public final static int AREA_CHART = 1;
    public final static int BAR_CHART = 2;

    final static DecimalFormat ONE_DECIMAL = new DecimalFormat();
    final static DecimalFormat TWO_DECIMALS = new DecimalFormat();
    public final static DecimalFormat FIVE_DECIMALS = new DecimalFormat();
    public final static DecimalFormat ENERGY_FORMAT = new DecimalFormat("######.##");

    int graphType = LINE_CHART;
    int top = 50, right = 50, bottom = 80, left = 90;
    BasicStroke dashed = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{2f}, 0.0f);
    BasicStroke thin = new BasicStroke(1);
    BasicStroke thick = new BasicStroke(2);
    final Map<String, List<Double>> data;
    Map<String, Boolean> hideData;
    Map<Integer, Boolean> hideRuns;
    double xmin = 0;
    double xmax = 11;
    double ymin = 0;
    double ymax = 0.001;
    double dx;
    double dy;
    String info = "No new data";
    int symbolSize = 8;
    int numberOfTicks = 12;
    String xAxisLabel = "Month";
    String yAxisLabel = "Energy (kWh)";
    byte instrumentType = DEFAULT;
    static Map<String, Color> colors;
    JDialog parent;
    Calendar today;

    private String dataNameDelimiter = "_";
    private boolean popup = true;
    private final JPopupMenu popupMenu;
    private int legendX, legendY, legendWidth, legendHeight;
    private String legendText = "";
    private final Color windowColor = new Color(245, 245, 245);

    static {
        colors = new HashMap<String, Color>();
        colors.put("Solar", Color.ORANGE);
        colors.put("Heat Gain", Color.GRAY);
        colors.put("Windows", colors.get("Solar"));
        colors.put("Solar Panels", Color.GREEN.darker());
        colors.put("Heater", Color.RED);
        colors.put("AC", Color.BLUE);
        colors.put("Net", Color.MAGENTA);
        colors.put("Utility", Color.LIGHT_GRAY);
    }

    Graph() {
        super();
        data = Collections.synchronizedMap(new HashMap<>());
        hideData = new HashMap<>();
        hideRuns = new HashMap<>();
        hideData("Windows", true);
        ONE_DECIMAL.setMaximumFractionDigits(1);
        TWO_DECIMALS.setMaximumFractionDigits(2);
        FIVE_DECIMALS.setMaximumFractionDigits(5);

        popupMenu = new JPopupMenu();
        popupMenu.setInvoker(this);
        popupMenu.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
                popupMenu.removeAll();

                final JMenu chartMenu = new JMenu("Chart");
                final ButtonGroup chartGroup = new ButtonGroup();
                popupMenu.add(chartMenu);

                final JRadioButtonMenuItem miBar = new JRadioButtonMenuItem("Bar");
                miBar.addItemListener(e1 -> {
                    if (e1.getStateChange() == ItemEvent.SELECTED) {
                        setGraphType(Graph.BAR_CHART);
                        Graph.this.repaint();
                    }
                });
                chartMenu.add(miBar);
                chartGroup.add(miBar);
                miBar.setSelected(graphType == BAR_CHART);

                final JRadioButtonMenuItem miLine = new JRadioButtonMenuItem("Line");
                miLine.addItemListener(e1 -> {
                    if (e1.getStateChange() == ItemEvent.SELECTED) {
                        setGraphType(Graph.LINE_CHART);
                        Graph.this.repaint();
                    }
                });
                chartMenu.add(miLine);
                chartGroup.add(miLine);
                miLine.setSelected(graphType == LINE_CHART);

                final JRadioButtonMenuItem miArea = new JRadioButtonMenuItem("Area");
                miArea.addItemListener(e1 -> {
                    if (e1.getStateChange() == ItemEvent.SELECTED) {
                        setGraphType(Graph.AREA_CHART);
                        Graph.this.repaint();
                    }
                });
                chartMenu.add(miArea);
                chartGroup.add(miArea);
                miArea.setSelected(graphType == AREA_CHART);

                if (Graph.this instanceof DailyGraph) {
                    final DailyGraph g = (DailyGraph) Graph.this;
                    final JCheckBoxMenuItem miMilitaryTime = new JCheckBoxMenuItem("Military Time");
                    miMilitaryTime.setSelected(g.getMilitaryTime());
                    miMilitaryTime.addItemListener(e1 -> {
                        g.setMilitaryTime(miMilitaryTime.isSelected());
                        g.repaint();
                    });
                    popupMenu.add(miMilitaryTime);
                }
                JMenuItem mi = new JMenuItem("View Raw Data...");
                mi.addActionListener(e1 -> DataViewer.viewRawData(popup ? parent : MainFrame.getInstance(), Graph.this, false));
                popupMenu.add(mi);
                mi = new JMenuItem("Copy Image");
                mi.addActionListener(e1 -> new ClipImage().copyImageToClipboard(Graph.this));
                popupMenu.add(mi);
                popupMenu.addSeparator();
                if (!popup) {
                    mi = new JMenuItem("Keep Current Results in Graph");
                    mi.addActionListener(e1 -> keepResults());
                    popupMenu.add(mi);
                    mi = new JMenuItem("Clear Previous Results in Graph");
                    mi.addActionListener(e1 -> {
                        final int i = JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(Graph.this),
                                "Are you sure that you want to clear all the previous results\nrelated to the selected object?", "Confirmation", JOptionPane.YES_NO_OPTION);
                        if (i != JOptionPane.YES_OPTION) {
                            return;
                        }
                        clearRecords();
                        repaint();
                        TimeSeriesLogger.getInstance().logClearGraphData(Graph.this.getClass().getSimpleName());
                    });
                    popupMenu.add(mi);
                    popupMenu.addSeparator();
                }
                for (final String name : data.keySet()) {
                    final JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(name, !isDataHidden(name));
                    cbmi.addItemListener(e1 -> {
                        hideData(name, !cbmi.isSelected());
                        Graph.this.repaint();
                        TimeSeriesLogger.getInstance().logShowCurve(Graph.this.getClass().getSimpleName(), name, cbmi.isSelected());
                    });
                    popupMenu.add(cbmi);
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(final PopupMenuEvent e) {
            }

        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                processMousePressed(e);
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                if (Util.isRightClick(e)) {
                    processMouseReleased(e);
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(final MouseEvent e) {
                processMouseMoved(e);
            }
        });

    }

    private void processMouseMoved(final MouseEvent e) {
        final int x = e.getX();
        final int y = e.getY();
        if (x > legendX && x < legendX + legendWidth && y > legendY && y < legendY + legendHeight) {
            setToolTipText(legendText);
        } else {
            boolean inSymbol = false;
            final double r = 0.5 * symbolSize;
            synchronized (data) {
                for (final String key : data.keySet()) {
                    if (isDataHidden(key)) {
                        continue;
                    }
                    final List<Double> list = data.get(key);
                    if (!list.isEmpty()) {
                        double dataX, dataY;
                        for (int i = 0; i < list.size(); i++) {
                            dataX = left + dx * i;
                            dataY = getHeight() - top - (list.get(i) - ymin) * dy;
                            if (x > dataX - r && x < dataX + r && y > dataY - r && y < dataY + r) {
                                inSymbol = true;
                                setToolTipText(key + ": (" + getXAxisLabel(i) + ", " + FIVE_DECIMALS.format(list.get(i)) + ")");
                                break;
                            }
                        }
                    }
                }
            }
            if (!inSymbol) {
                setToolTipText("<html>Move mouse for more information.<br>Right-click for more options." + (popup ? "" : "<br>Double-click to enlarge this graph.") + "</html>");
            }
        }
    }

    private void processMousePressed(final MouseEvent e) {
    }

    private void processMouseReleased(final MouseEvent e) {
        popupMenu.show(this, e.getX(), e.getY());
    }

    public void setGraphType(final int type) {
        graphType = type;
    }

    public int getGraphType() {
        return graphType;
    }

    public void setInstrumentType(final byte type) {
        instrumentType = type;
    }

    public int getInstrumentType() {
        return instrumentType;
    }

    /**
     * Is this graph going to be in a popup window that allows for more drawing space?
     */
    public void setPopup(final boolean popup) {
        this.popup = popup;
        if (popup) {
            symbolSize = 8;
            top = 50;
            right = 50;
            bottom = 80;
            left = 90;
        } else {
            symbolSize = 4;
            top = 25;
            right = 25;
            bottom = 40;
            left = 65;
        }
        if (!popup) {
            setToolTipText("Double-click to enlarge this graph");
        }
    }

    public static void setColor(final String key, final Color color) {
        colors.put(key, color);
    }

    public void setCalendar(final Calendar today) {
        this.today = today;
    }

    /* keep the records by their class types */
    List<Results> getRecords() {
        if (this instanceof DailyGraph) {
            return DailyGraph.records;
        }
        return AnnualGraph.records;
    }

    void keepResults() {
        if (data.isEmpty()) {
            return;
        }
        getRecords().add(new Results(data));
    }

    boolean hasRecords() {
        return !getRecords().isEmpty();
    }

    private boolean areRecordsShown() {
        for (final Results r : getRecords()) {
            final Boolean x = hideRuns.get(r.getID());
            if (x == null || x == Boolean.FALSE) {
                return true;
            }
        }
        return false;
    }

    void clearRecords() {
        getRecords().clear();
    }

    void setMinimum(final float xmin) {
        this.xmin = xmin;
    }

    void setMaximum(final float xmax) {
        this.xmax = xmax;
    }

    Set<String> getDataNames() {
        return data.keySet();
    }

    public void addData(final String name, final double d) {
        List<Double> list = data.get(name);
        if (list == null) {
            list = new ArrayList<>();
            data.put(name, list);
        }
        list.add(d);
    }

    void hideData(final String name, final boolean hidden) {
        hideData.put(name, hidden);
    }

    boolean isDataHidden(final String name) {
        final Boolean b = hideData.get(name);
        return b != null ? b : false;
    }

    void hideRun(final int id, final boolean hidden) {
        hideRuns.put(id, hidden);
    }

    boolean isRunHidden(final int id) {
        final Boolean b = hideRuns.get(id);
        return b != null ? b : false;
    }

    public Map<String, List<Double>> getData() {
        return data;
    }

    public List<Double> getData(final String name) {
        return data.get(name);
    }

    public double getSum(final String name) {
        synchronized (data) {
            final List<Double> x = getData(name);
            if (x == null || x.isEmpty()) {
                return 0;
            }
            double sum = 0;
            for (final double a : x) {
                sum += a;
            }
            return this instanceof DailyGraph ? sum : sum * 365.0 / 12.0;
        }
    }

    int getLength() {
        if (data.isEmpty()) {
            return 0;
        }
        List<Double> list = null;
        synchronized (data) {
            for (final String name : data.keySet()) {
                list = data.get(name);
                break;
            }
        }
        return list.size();
    }

    boolean hasData() {
        if (data.isEmpty()) {
            return false;
        }
        return data.size() != 1 || data.get("Utility") == null;
    }

    public void clearData() {
        data.clear();
    }

    abstract double getXAxisLabelScalingFactor();

    abstract String getXAxisUnit();

    String getXAxisLabel(final int i) {
        return Math.round((i + 1) * getXAxisLabelScalingFactor()) + getXAxisUnit();
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
        g2.setStroke(thick);
        g2.drawRect(left / 2, top / 2, width - (left + right) / 2, height - (top + bottom) / 2);
        g2.setColor(windowColor);
        g2.fillRect(left / 2, top / 2, width - (left + right) / 2, height - (top + bottom) / 2);

        g2.setColor(Color.BLACK);
        g2.setStroke(thin);
        g2.setFont(new Font("Arial", Font.PLAIN, popup ? 8 : 6));
        final float tickWidth = (float) (width - left - right) / (float) (numberOfTicks - 1);
        float xTick;
        for (int i = 0; i < numberOfTicks; i++) {
            final String s = getXAxisLabel(i);
            final int sWidth = g2.getFontMetrics().stringWidth(s);
            xTick = left + tickWidth * i;
            g2.drawString(s, xTick - sWidth / 2f, height - bottom / 2 + (popup ? 16 : 8));
            if (popup) {
                g2.drawLine((int) xTick, height - bottom / 2, (int) xTick, height - bottom / 2 - 4);
            }
        }
        g2.setFont(new Font("Arial", Font.PLAIN, popup ? 10 : 8));
        final int xAxisLabelWidth = g2.getFontMetrics().stringWidth(xAxisLabel);
        final int yAxisLabelWidth = g2.getFontMetrics().stringWidth(yAxisLabel);
        g2.drawString(xAxisLabel, (width - xAxisLabelWidth) / 2, height - (popup ? 8 : 4));
        g2.rotate(-Math.PI / 2, 16, (height + yAxisLabelWidth) / 2);
        g2.drawString(yAxisLabel, 16, (height + yAxisLabelWidth) / 2);
        g2.rotate(Math.PI / 2, 16, (height + yAxisLabelWidth) / 2);

        final boolean showRecords = hasRecords() && areRecordsShown();
        if (showRecords) {
            recalculateBounds();
            drawRecords(g2);
        } else {
            ymin = 0;
            ymax = 0.001;
        }

        if (data.isEmpty()) {
            g2.setColor(Color.LIGHT_GRAY);
            g2.setFont(new Font("Arial", Font.PLAIN, 20));
            final int infoWidth = g2.getFontMetrics().stringWidth(info);
            g2.drawString(info, (width - infoWidth) / 2, height / 2);
        } else {
            if (!showRecords) {
                recalculateBounds();
            }
            int digits = String.valueOf(Math.round(ymax - ymin)).length() - 1;
            digits = (int) Math.pow(10, digits);
            final int i1 = (int) Math.round(ymin / digits) - 2;
            final int i2 = (int) Math.round(ymax / digits) + 2;
            final int di = i2 - i1;
            float hVal;
            int hPos;
            for (int i = i1; i <= i2; i++) {
                hVal = i * digits;
                hPos = (int) Math.round(getHeight() - top - (hVal - ymin) * dy);
                if (hPos >= top / 2 && hPos <= getHeight() - bottom / 2) {
                    drawHorizontalLine(g2, hPos, ONE_DECIMAL.format(hVal));
                }
                if (di <= 5) {
                    for (int j = 0; j < 10; j++) {
                        hVal = (i + j * 0.1f) * digits;
                        hPos = (int) Math.round(getHeight() - top - (hVal - ymin) * dy);
                        if (hPos >= top / 2 && hPos <= getHeight() - bottom / 2) {
                            drawHorizontalLine(g2, hPos, TWO_DECIMALS.format(hVal));
                        }
                    }
                } else if (di < 10) {
                    hVal = (i + 0.5f) * digits;
                    hPos = (int) Math.round(getHeight() - top - (hVal - ymin) * dy);
                    if (hPos >= top / 2 && hPos <= getHeight() - bottom / 2) {
                        drawHorizontalLine(g2, hPos, ONE_DECIMAL.format(hVal));
                    }
                }
            }
            drawCurves(g2);
        }

        drawLegends(g2);

        final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
        if (!"".equals(city)) {
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, popup ? 14 : 8));
            final FontMetrics fm = g2.getFontMetrics();
            if (today == null) {
                today = Heliodon.getInstance().getCalendar();
            }
            final String cityAndDate = "Weather data from " + city + (this instanceof DailyGraph ? " - " + new SimpleDateFormat("MMM").format(today.getTime()) + " " + today.get(Calendar.DAY_OF_MONTH) : "");
            g2.drawString(cityAndDate, (width - fm.stringWidth(cityAndDate)) / 2, popup ? 20 : 10);
        }

    }

    private void recalculateBounds() {
        if (!getRecords().isEmpty()) {
            for (final Results r : getRecords()) {
                if (isRunHidden(r.getID())) {
                    continue;
                }
                final double[] bound = r.getBound();
                if (bound[0] < ymin) {
                    ymin = bound[0];
                }
                if (bound[1] > ymax) {
                    ymax = bound[1];
                }
            }
        }
        synchronized (data) {
            for (final String key : data.keySet()) {
                final List<Double> list = data.get(key);
                if (!list.isEmpty()) {
                    final double max = Collections.max(list);
                    final double min = Collections.min(list);
                    if (max > ymax) {
                        ymax = max;
                    }
                    if (min < ymin) {
                        ymin = min;
                    }
                }
            }
        }
        dx = (getWidth() - left - right) / (xmax - xmin);
        dy = (getHeight() - top - bottom) / (ymax - ymin);
    }

    private void drawRecords(final Graphics2D g2) {

        double dataX, dataY;
        final Path2D.Float path = new Path2D.Float();
        for (final Results r : getRecords()) {

            if (isRunHidden(r.getID())) {
                continue;
            }

            final Map<String, List<Double>> x = r.getData();
            for (final String key : x.keySet()) {
                if (isDataHidden(key)) {
                    continue;
                }
                final List<Double> list = x.get(key);
                path.reset();
                for (int i = 0; i < list.size(); i++) {
                    dataX = left + dx * i;
                    dataY = getHeight() - top - (list.get(i) - ymin) * dy;
                    if (i == 0) {
                        path.moveTo(dataX, dataY);
                        g2.setColor(Color.GRAY);
                        g2.drawString(r.getID() + "", (int) dataX - 8, (int) dataY + 5);
                    } else {
                        path.lineTo(dataX, dataY);
                    }
                }
                g2.setColor(colors.get(key));
                g2.setStroke(thin);
                g2.draw(path);
            }

        }

    }

    abstract void drawLegends(Graphics2D g2);

    abstract void drawCurves(Graphics2D g2);

    void drawBuildingCurves(final Graphics2D g2) {

        synchronized (data) {

            for (final String key : data.keySet()) {

                if (isDataHidden(key)) {
                    continue;
                }

                final List<Double> list = data.get(key);

                if (!list.isEmpty()) {

                    if (Collections.max(list).equals(Collections.min(list))) {
                        continue;
                    }

                    switch (graphType) {

                        case LINE_CHART:
                            g2.setColor(Color.BLACK);
                            double dataX, dataY;
                            Path2D.Float path = new Path2D.Float();
                            for (int i = 0; i < list.size(); i++) {
                                dataX = left + dx * i;
                                dataY = getHeight() - top - (list.get(i) - ymin) * dy;
                                if (i == 0) {
                                    path.moveTo(dataX, dataY);
                                } else {
                                    path.lineTo(dataX, dataY);
                                }
                            }
                            g2.setStroke("Net".equals(key) ? thick : dashed);
                            g2.draw(path);
                            g2.setStroke(thin);
                            Color c = colors.get(key);
                            for (int i = 0; i < list.size(); i++) {
                                dataX = left + dx * i;
                                dataY = getHeight() - top - (list.get(i) - ymin) * dy;
                                if ("Windows".equals(key)) {
                                    drawDiamond(g2, (int) Math.round(dataX), (int) Math.round(dataY), 2 * symbolSize / 3, c);
                                } else if ("Solar Panels".equals(key)) {
                                    drawSquare(g2, (int) Math.round(dataX - symbolSize / 2.0), (int) Math.round(dataY - symbolSize / 2.0), symbolSize, c);
                                } else if ("Heater".equals(key)) {
                                    drawTriangleUp(g2, (int) Math.round(dataX - symbolSize / 2.0), (int) Math.round(dataY - symbolSize / 2.0), symbolSize, c);
                                } else if ("AC".equals(key)) {
                                    drawTriangleDown(g2, (int) Math.round(dataX - symbolSize / 2.0), (int) Math.round(dataY - symbolSize / 2.0), symbolSize, c);
                                } else if ("Net".equals(key)) {
                                    drawCircle(g2, (int) Math.round(dataX - symbolSize / 2 + 1), (int) Math.round(dataY - symbolSize / 2 + 1), symbolSize - 2, c);
                                }
                            }
                            break;

                        case AREA_CHART:
                            path = new Path2D.Float();
                            dataX = 0;
                            final double dataY0 = getHeight() - top + ymin * dy;
                            for (int i = 0; i < list.size(); i++) {
                                dataX = left + dx * i;
                                dataY = getHeight() - top - (list.get(i) - ymin) * dy;
                                if (i == 0) {
                                    path.moveTo(dataX, dataY0);
                                    path.lineTo(dataX, dataY);
                                } else {
                                    path.lineTo(dataX, dataY);
                                }
                            }
                            path.lineTo(dataX, dataY0);
                            c = colors.get(key);
                            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 128));
                            g2.fill(path);
                            g2.setStroke("Net".equals(key) ? thick : dashed);
                            g2.setColor(Color.BLACK);
                            g2.draw(path);
                            break;

                        case BAR_CHART:
                            break;

                    }

                }

            }

        }

    }

    void drawBuildingLegends(final Graphics2D g2) {

        g2.setFont(new Font("Arial", Font.PLAIN, popup ? 10 : 8));
        g2.setStroke(thin);
        final int x0 = getWidth() - (popup ? 100 : 80) - right;
        int y0 = top - 10;
        legendX = x0;
        legendY = y0;
        legendText = "<html><h4>Energy (kWh):</h4><hr><ul>";

        String s = "Windows";
        if (!isDataHidden(s)) {
            drawDiamond(g2, x0 + 4, y0 + 3, popup ? 5 : 2, colors.get(s));
            final String t = s + " (" + ONE_DECIMAL.format(getSum(s)) + ")";
            g2.drawString("* " + t, x0 + 14, y0 + 8);
            legendText += "<li>" + t;
        }

        s = "Solar Panels";
        y0 += 12;
        if (!isDataHidden(s)) {
            drawSquare(g2, x0, y0, popup ? 8 : 4, colors.get(s));
            final String t = s + " (" + ONE_DECIMAL.format(getSum(s)) + ")";
            g2.drawString("\u2212 " + t, x0 + 14, y0 + 8);
            legendText += "<li>" + t;
        }

        s = "Heater";
        y0 += 12;
        if (!isDataHidden(s)) {
            drawTriangleUp(g2, x0, y0, popup ? 8 : 4, colors.get(s));
            final String t = s + " (" + ONE_DECIMAL.format(getSum(s)) + ")";
            g2.drawString("\u002b " + t, x0 + 14, y0 + 8);
            legendText += "<li>" + t;
        }

        s = "AC";
        y0 += 12;
        if (!isDataHidden(s)) {
            drawTriangleDown(g2, x0, y0, popup ? 8 : 4, colors.get(s));
            final String t = s + " (" + ONE_DECIMAL.format(getSum(s)) + ")";
            g2.drawString("\u002b " + t, x0 + 14, y0 + 8);
            legendText += "<li>" + t;
        }

        s = "Net";
        y0 += 13;
        if (!isDataHidden(s)) {
            drawCircle(g2, x0, y0, popup ? 8 : 4, colors.get(s));
            g2.setFont(new Font("Arial", Font.BOLD, popup ? 11 : 8));
            final String t = s + " (" + ONE_DECIMAL.format(getSum(s)) + ")";
            g2.drawString("\u003d " + t, x0 + 14, y0 + 8);
            legendText += "<li>" + t;
        }

        legendWidth = getWidth() - x0;
        legendHeight = y0 - legendY;

        legendText += "</html>";

    }

    void drawPartCurves(final Graphics2D g2) {

        final Path2D.Float path = new Path2D.Float();

        synchronized (data) {

            for (final String key : data.keySet()) {

                if (isDataHidden(key)) {
                    continue;
                }

                final List<Double> list = data.get(key);

                if (!list.isEmpty()) {

                    if (Collections.max(list).equals(Collections.min(list))) {
                        continue;
                    }

                    switch (graphType) {

                        case BAR_CHART:
                            break;

                        case LINE_CHART:
                            g2.setColor(Color.BLACK);
                            path.reset();
                            double dataX, dataY;
                            double xLabel = 0;
                            double yLabel = Double.MAX_VALUE;
                            if ("Utility".equals(key)) {
                                g2.setStroke(thin);
                                g2.setColor(colors.get(key));
                                double hi;
                                for (int i = 0; i < list.size(); i++) {
                                    dataX = left + dx * i;
                                    hi = (list.get(i) - ymin) * dy;
                                    dataY = getHeight() - top - hi;
                                    g2.fillRect((int) Math.round(dataX - dx / 2 + 5), (int) Math.round(dataY), (int) Math.round(dx - 10), (int) Math.round(hi));
                                }
                            } else {
                                for (int i = 0; i < list.size(); i++) {
                                    dataX = left + dx * i;
                                    dataY = getHeight() - top - (list.get(i) - ymin) * dy;
                                    if (i == 0) {
                                        path.moveTo(dataX, dataY);
                                    } else {
                                        path.lineTo(dataX, dataY);
                                    }
                                    if (dataY < yLabel) {
                                        yLabel = dataY;
                                        xLabel = dataX;
                                    }
                                }
                                g2.setStroke(thin);
                                g2.draw(path);
                            }
                            if (!(this instanceof DailyGraph)) {
                                xLabel = left - 24;
                                yLabel = getHeight() - top - (list.get(0) - ymin) * dy + 5;
                            } else {
                                yLabel -= 8;
                            }
                            g2.setStroke(thin);
                            switch (instrumentType) {
                                case SENSOR:
                                    for (int i = 0; i < list.size(); i++) {
                                        dataX = left + dx * i;
                                        dataY = getHeight() - top - (list.get(i) - ymin) * dy;
                                        if (key.startsWith("Light")) {
                                            drawDiamond(g2, (int) Math.round(dataX), (int) Math.round(dataY), 2 * symbolSize / 3, colors.get("Solar"));
                                        } else if (key.startsWith("Heat Flux")) {
                                            drawSquare(g2, (int) Math.round(dataX - symbolSize / 2.0), (int) Math.round(dataY - symbolSize / 2.0), symbolSize, colors.get("Heat Gain"));
                                        }
                                    }
                                    final FontMetrics fm = g2.getFontMetrics();
                                    final int pound = key.indexOf("#");
                                    String s = key.substring(pound + 1);
                                    g2.drawString(s, (int) (xLabel - 0.5 * fm.stringWidth(s)), (int) yLabel);
                                    if (key.startsWith("Light")) {
                                        double sum = getSum(key);
                                        if (sum < 1) {
                                            s = "(" + TWO_DECIMALS.format(sum) + ")";
                                        } else if (sum < 10) {
                                            s = "(" + ONE_DECIMAL.format(sum) + ")";
                                        } else {
                                            s = "(" + ONE_DECIMAL.format(Math.round(sum)) + ")";
                                        }
                                        g2.drawString(s, (int) (xLabel - 0.5 * fm.stringWidth(s)), (int) (yLabel + fm.getAscent() + fm.getDescent()));
                                    }
                                    break;
                                default:
                                    for (int i = 0; i < list.size(); i++) {
                                        dataX = left + dx * i;
                                        dataY = getHeight() - top - (list.get(i) - ymin) * dy;
                                        if (key.startsWith("Solar") || key.startsWith("PV") || key.startsWith("CSP")) {
                                            drawDiamond(g2, (int) Math.round(dataX), (int) Math.round(dataY), 2 * symbolSize / 3, colors.get(key));
                                        } else if (key.startsWith("Heat Gain") || key.startsWith("Building")) {
                                            drawSquare(g2, (int) Math.round(dataX - symbolSize / 2.0), (int) Math.round(dataY - symbolSize / 2.0), symbolSize, colors.get(key));
                                        }
                                    }
                            }

                            break;

                        case AREA_CHART:
                            path.reset();
                            if ("Utility".equals(key)) {
                                g2.setStroke(thin);
                                g2.setColor(colors.get(key));
                                double hi;
                                for (int i = 0; i < list.size(); i++) {
                                    dataX = left + dx * i;
                                    hi = (list.get(i) - ymin) * dy;
                                    dataY = getHeight() - top - hi;
                                    g2.fillRect((int) Math.round(dataX - dx / 2 + 5), (int) Math.round(dataY), (int) Math.round(dx - 10), (int) Math.round(hi));
                                }
                            } else {
                                dataX = 0;
                                final double dataY0 = getHeight() - top + ymin * dy;
                                for (int i = 0; i < list.size(); i++) {
                                    dataX = left + dx * i;
                                    dataY = getHeight() - top - (list.get(i) - ymin) * dy;
                                    if (i == 0) {
                                        path.moveTo(dataX, dataY0);
                                        path.lineTo(dataX, dataY);
                                    } else {
                                        path.lineTo(dataX, dataY);
                                    }
                                }
                                path.lineTo(dataX, dataY0);
                                final Color c0 = colors.get(key);
                                if (c0 != null) {
                                    g2.setColor(new Color(c0.getRed(), c0.getGreen(), c0.getBlue(), 128));
                                }
                                g2.fill(path);
                                g2.setColor(Color.BLACK);
                                g2.setStroke(thin);
                                g2.draw(path);
                            }
                            break;

                    }

                }

            }

        }

    }

    private boolean containsSensorType(final String s) {
        synchronized (data) {
            for (final String key : data.keySet()) {
                if (key.startsWith(s)) {
                    final List<Double> val = data.get(key);
                    for (final double x : val) {
                        if (x != 0) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    void drawPartLegends(final Graphics2D g2) {

        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.setStroke(thin);
        final int x0 = getWidth() - (popup ? 100 : 80) - right;
        int y0 = top - 10;
        legendX = x0;
        legendY = y0;
        legendText = "<html><h4>Energy (kWh):</h4><hr><ul>";

        switch (instrumentType) {
            case SENSOR:
                String s = "Light";
                if (containsSensorType(s)) {
                    drawDiamond(g2, x0 + 4, y0 + 4, 5, colors.get("Solar"));
                    g2.drawString(s, x0 + 14, y0 + 8);
                    legendText += "<li>" + s;
                }
                s = "Heat Flux";
                y0 += 12;
                if (containsSensorType(s)) {
                    y0 += 14;
                    drawSquare(g2, x0, y0, 8, colors.get("Heat Gain"));
                    g2.drawString(s, x0 + 14, y0 + 8);
                    legendText += "<li>" + s;
                }
                break;
            default:
                boolean found = false;
                s = "Solar";
                if (data.containsKey(s) && !isDataHidden(s)) {
                    drawDiamond(g2, x0 + 4, y0 + 4, 5, colors.get(s));
                    final double sum = getSum(s);
                    s += " (" + TWO_DECIMALS.format(sum) + ")";
                    g2.drawString(s, x0 + 14, y0 + 8);
                    found = true;
                    s = "Solar (" + FIVE_DECIMALS.format(sum) + ")";
                    legendText += "<li>" + s;
                }
                s = "Heat Gain";
                y0 += 12;
                if (data.containsKey(s) && !isDataHidden(s)) {
                    y0 += 14;
                    drawSquare(g2, x0, y0, 8, colors.get(s));
                    final double sum = getSum(s);
                    s += " (" + TWO_DECIMALS.format(sum) + ")";
                    g2.drawString(s, x0 + 14, y0 + 8);
                    found = true;
                    s = "Heat Gain (" + FIVE_DECIMALS.format(sum) + ")";
                    legendText += "<li>" + s;
                }
                s = "Utility";
                y0 += 12;
                if (data.containsKey(s) && !isDataHidden(s)) {
                    y0 += 14;
                    drawCircle(g2, x0, y0, 8, colors.get(s));
                    s += " (" + TWO_DECIMALS.format(getSum(s)) + ")";
                    g2.drawString(s, x0 + 14, y0 + 8);
                    found = true;
                    legendText += "<li>" + s;
                }
                if (!found) {
                    final ArrayList<String> set = new ArrayList<String>(data.keySet());
                    Collections.sort(set);
                    for (final String k : set) {
                        if (isDataHidden(k)) {
                            continue;
                        }
                        y0 += 14;
                        if (k.startsWith("Solar") || k.startsWith("PV") || k.startsWith("CSP")) {
                            drawDiamond(g2, x0 + 4, y0 + 4, 5, colors.get(k));
                        }
                        if (k.startsWith("Heat Gain") || k.startsWith("Building")) {
                            drawSquare(g2, x0, y0, 8, colors.get(k));
                        }
                        String k2 = k;
                        final int i2 = k2.indexOf(dataNameDelimiter);
                        if (i2 != -1) {
                            k2 = k2.substring(i2 + 1);
                        }
                        s = k2 + " (" + TWO_DECIMALS.format(getSum(k)) + ")";
                        g2.drawString(s, x0 + 14, y0 + 8);
                        y0 += 12;
                        legendText += "<li>" + s;
                    }
                }

                legendWidth = getWidth() - x0;
                legendHeight = y0 - legendY;

                legendText += "</html>";

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

    static void drawCircle(final Graphics g, final int upperLeftX, final int upperLeftY, final int d, final Color c) {
        g.setColor(c);
        g.fillOval(upperLeftX, upperLeftY, d, d);
        g.setColor(Color.BLACK);
        g.drawOval(upperLeftX, upperLeftY, d, d);

    }

    static void drawSquare(final Graphics g, final int upperLeftX, final int upperLeftY, final int a, final Color c) {
        g.setColor(c);
        g.fillRect(upperLeftX, upperLeftY, a, a);
        g.setColor(Color.BLACK);
        g.drawRect(upperLeftX, upperLeftY, a, a);
    }

    static void drawTriangleUp(final Graphics g, final int upperLeftX, final int upperLeftY, final int a, final Color c) {
        final int[] xPoints = new int[]{upperLeftX + a / 2, upperLeftX, upperLeftX + a};
        final int[] yPoints = new int[]{upperLeftY, upperLeftY + a, upperLeftY + a};
        g.setColor(c);
        g.fillPolygon(xPoints, yPoints, 3);
        g.setColor(Color.BLACK);
        g.drawPolygon(xPoints, yPoints, 3);
    }

    static void drawTriangleDown(final Graphics g, final int upperLeftX, final int upperLeftY, final int a, final Color c) {
        final int[] xPoints = new int[]{upperLeftX, upperLeftX + a, upperLeftX + a / 2};
        final int[] yPoints = new int[]{upperLeftY, upperLeftY, upperLeftY + a};
        g.setColor(c);
        g.fillPolygon(xPoints, yPoints, 3);
        g.setColor(Color.BLACK);
        g.drawPolygon(xPoints, yPoints, 3);
    }

    static void drawDiamond(final Graphics g, final int x, final int y, final int a, final Color c) {
        g.setColor(c);
        final Polygon p = new Polygon();
        p.addPoint(x, y - a);
        p.addPoint(x + a, y);
        p.addPoint(x, y + a);
        p.addPoint(x - a, y);
        g.fillPolygon(p);
        g.setColor(Color.BLACK);
        g.drawPolygon(p);
    }

    public void setDataNameDelimiter(final String dataNameDelimiter) {
        this.dataNameDelimiter = dataNameDelimiter;
    }

    String getDataNameDelimiter() {
        return dataNameDelimiter;
    }

}