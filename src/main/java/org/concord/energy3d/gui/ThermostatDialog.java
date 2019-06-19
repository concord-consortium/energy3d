package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.text.DateFormatSymbols;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.Thermostat;

import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;

/**
 * This implements a GUI for changing hourly temperature according to the week of the month and the month of the year.
 *
 * @author Charles Xie
 */
class ThermostatDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    ThermostatDialog(final Foundation foundation) {

        super(MainFrame.getInstance(), true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        Calendar calendar = Heliodon.getInstance().getCalendar();
        int month = calendar.get(Calendar.MONTH);
        setTitle("Thermostat Schedule: " + new DateFormatSymbols().getMonths()[month]);
        getContentPane().setLayout(new BorderLayout());

        ThermostatView[] temperatureButtons = new ThermostatView[7];
        Thermostat t = foundation.getThermostat();
        Color bgColor = new Color(225, 225, 225);
        for (int i = 0; i < temperatureButtons.length; i++) {
            temperatureButtons[i] = new ThermostatView(foundation, month, i);
            temperatureButtons[i].setBackground(bgColor);
            temperatureButtons[i].setPreferredSize(new Dimension(720, 30));
            temperatureButtons[i].setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
            int numberOfSteps = 25;
            for (int j = 0; j < numberOfSteps; j++)
                temperatureButtons[i].setButton((float) (j + 1) / numberOfSteps, t.getTemperature(month, i, j));
        }

        final JLabel hourLabel = new JLabel(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                int w = getIconWidth();
                int h = getIconHeight();
                g.setColor(Color.WHITE);
                g.fillOval(x, y, w, h);
                g.setColor(Color.GRAY);
                g.drawOval(x, y, w, h);
                g.drawLine(x + w / 2, y + h / 2, x + w / 2, y + 2);
                g.drawLine(x + w / 2, y + h / 2, x + w - 2, y + h / 2);
            }

            @Override
            public int getIconWidth() {
                return 16;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }
        });
        hourLabel.setHorizontalAlignment(JLabel.LEFT);
        hourLabel.setVerticalAlignment(JLabel.CENTER);
        hourLabel.setMinimumSize(new Dimension(40, 20));
        HourPanel hourPanel = new HourPanel(foundation, temperatureButtons);
        hourPanel.setPreferredSize(new Dimension(temperatureButtons[0].getPreferredSize().width, 20));
        hourPanel.setBackground(bgColor);
        hourPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        JLabel[] labels = new JLabel[7];
        labels[0] = new JLabel("Sun");
        labels[1] = new JLabel("Mon");
        labels[2] = new JLabel("Tue");
        labels[3] = new JLabel("Wed");
        labels[4] = new JLabel("Thu");
        labels[5] = new JLabel("Fri");
        labels[6] = new JLabel("Sat");
        for (JLabel jLabel : labels) {
            jLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
            jLabel.setMinimumSize(new Dimension(40, 30));
            jLabel.setAlignmentX(CENTER_ALIGNMENT);
        }

        final JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.CENTER);

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        // layout.setAutoCreateGaps(true); // Turn on automatically adding gaps between components

        // Turn on automatically creating gaps between components that touch the edge of the container and the container.
        layout.setAutoCreateContainerGaps(true);

        // Create a sequential group for the horizontal axis.
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

        // The sequential group in turn contains two parallel groups. One parallel group contains the labels, the other the sliders.
        // Putting the labels in a parallel group along the horizontal axis positions them at the same x location. Variable indentation is used to reinforce the level of grouping.
        ParallelGroup pg = layout.createParallelGroup();
        for (JLabel label : labels) {
            pg = pg.addComponent(label);
        }
        pg = pg.addComponent(hourLabel);
        hGroup.addGroup(pg);
        pg = layout.createParallelGroup();
        for (ThermostatView temperatureButton : temperatureButtons) {
            pg = pg.addComponent(temperatureButton);
        }
        pg = pg.addComponent(hourPanel);
        hGroup.addGroup(pg);
        layout.setHorizontalGroup(hGroup);

        // Create a sequential group for the vertical axis.
        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

        // The sequential group contains two parallel groups that align the contents along the baseline. The first parallel group contains the first label and text field, and the
        // second parallel group contains the second label and text field. By using a sequential group the labels and text fields are positioned vertically after one another.
        for (int i = 0; i < labels.length; i++) {
            vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(labels[i]).addComponent(temperatureButtons[i]));
        }
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(hourLabel).addComponent(hourPanel));
        layout.setVerticalGroup(vGroup);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        final JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(actionPanel, BorderLayout.WEST);

        actionPanel.add(new JLabel("  Drag a number up or down to raise or lower temperature"));

        final JPanel okPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okPanel, BorderLayout.EAST);

        final JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            EnergyPanel.getInstance().updateThermostat();
            Scene.getInstance().setEdited(true);
            ThermostatDialog.this.dispose();
        });
        okButton.setActionCommand("OK");
        okPanel.add(okButton);
        getRootPane().setDefaultButton(okButton);

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            EnergyPanel.getInstance().updateThermostat();
            ThermostatDialog.this.dispose();
        });
        cancelButton.setActionCommand("Cancel");
        okPanel.add(cancelButton);

        pack();
        setLocationRelativeTo(MainFrame.getInstance());

    }

}