package org.concord.energy3d.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.AdjustThermostatCommand;

/**
 * @author Charles Xie
 */
class ThermostatView extends JPanel {

    private static final long serialVersionUID = 1L;

    private Color allColor = new Color(108, 108, 108);
    private Color warmColor = new Color(223, 67, 0);
    private Color coolColor = new Color(0, 67, 223);
    final Map<Float, Integer> hourlyTemperatures;
    private int selectedHour = -1;
    int monthOfYear;
    int dayOfWeek;
    private Foundation foundation;
    private int previousY;
    private boolean increaseTemperature = false;
    private boolean decreaseTemperature = false;
    private boolean mouseHeld;

    ThermostatView(Foundation foundation, int monthOfYear, int dayOfWeek) {
        super();
        this.foundation = foundation;
        this.monthOfYear = monthOfYear;
        this.dayOfWeek = dayOfWeek;
        hourlyTemperatures = Collections.synchronizedMap(new LinkedHashMap<>());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                processMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                processMouseReleased(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                processMouseExited(e);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                processMouseMoved(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                processMouseDragged(e);
            }
        });
    }

    void setButton(float x, int y) {
        hourlyTemperatures.put(x, y);
    }

    public void clearButtons() {
        hourlyTemperatures.clear();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        update(g);
    }

    public void update(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        Dimension dim = getSize();
        int width = dim.width;
        int height = dim.height;
        g2.setColor(getBackground());
        g2.fillRect(0, 0, width, height);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();

        synchronized (hourlyTemperatures) {
            Object[] keys = hourlyTemperatures.keySet().toArray();
            int n1 = keys.length - 1;
            int temperatureForAll = hourlyTemperatures.get(keys[n1]);
            for (int i = 0; i < keys.length; i++) {
                float radius = 0.4f * height;
                int diameter = (int) (2 * radius);
                Color c = null;
                if (i < n1) {
                    int temp = hourlyTemperatures.get(keys[i]);
                    if (temp > temperatureForAll) {
                        c = warmColor;
                    } else if (temp < temperatureForAll) {
                        c = coolColor;
                    }
                } else {
                    c = allColor;
                }
                float cx = (Float) keys[i] * (width - diameter);
                float cy = height / 2;
                if (c != null) {
                    g2.setColor(c);
                    g2.fillOval((int) (cx - radius), (int) (cy - radius), diameter, diameter);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(Color.GRAY);
                }
                if (i == selectedHour) {
                    g2.drawOval((int) (cx - radius), (int) (cy - radius), diameter, diameter);
                }
                String reading = hourlyTemperatures.get(keys[i]) + "";
                g2.drawString(reading, cx - fm.stringWidth(reading) / 2f, cy + (fm.getAscent() - fm.getDescent()) / 2f);
            }
        }

    }

    private void processMouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int width = getWidth();
        int height = getHeight();
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        synchronized (hourlyTemperatures) {
            Object[] keys = hourlyTemperatures.keySet().toArray();
            for (Object key : keys) {
                float hr = 0.4f * height;
                float hx = (Float) key * (width - hr * 2);
                float hy = height / 2;
                if ((x - hx) * (x - hx) + (y - hy) * (y - hy) < hr * hr) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    break;
                }
            }
        }
        repaint();
    }

    private void processMousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int width = getWidth();
        int height = getHeight();
        previousY = y;
        selectedHour = -1;
        mouseHeld = true;
        synchronized (hourlyTemperatures) {
            Object[] keys = hourlyTemperatures.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                float hr = 0.4f * height;
                float hx = (Float) keys[i] * (width - hr * 2);
                float hy = height / 2;
                if ((x - hx) * (x - hx) + (y - hy) * (y - hy) < hr * hr) {
                    selectedHour = i;
                    break;
                }
            }
        }
        repaint();
    }

    private void processMouseDragged(MouseEvent e) {
        int y = e.getY();
        increaseTemperature = y < previousY;
        decreaseTemperature = y > previousY;
        previousY = y;
        repaint();
    }

    private void processMouseReleased(MouseEvent e) {
        mouseHeld = false;
        if (!increaseTemperature && !decreaseTemperature)
            return;
        if (selectedHour >= 0) {
            Object[] keys = hourlyTemperatures.keySet().toArray();
            Object selectedKey = keys[selectedHour];
            int newTemperature = hourlyTemperatures.get(selectedKey);
            if (increaseTemperature)
                newTemperature++;
            if (decreaseTemperature)
                newTemperature--;
            AdjustThermostatCommand c = new AdjustThermostatCommand(foundation);
            if (selectedHour == 24) { // temperature for the whole day
                int originalTemperatureForAll = hourlyTemperatures.get(keys[24]);
                for (int i = 0; i < keys.length; i++) {
                    Float k = (Float) keys[i];
                    if (hourlyTemperatures.get(k) == originalTemperatureForAll) {
                        hourlyTemperatures.put(k, newTemperature);
                        foundation.getThermostat().setTemperature(monthOfYear, dayOfWeek, i, newTemperature);
                    }
                }
            } else {
                hourlyTemperatures.put((Float) selectedKey, newTemperature);
                foundation.getThermostat().setTemperature(monthOfYear, dayOfWeek, selectedHour, newTemperature);
            }
            selectedHour = -1;
            SceneManager.getInstance().getUndoManager().addEdit(c);
        }
        repaint();
        increaseTemperature = false;
        decreaseTemperature = false;
    }

    private void processMouseExited(MouseEvent e) {
        if (!mouseHeld)
            selectedHour = -1;
        repaint();
    }

}