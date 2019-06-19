package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;

import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.util.SpringUtilities;

/**
 * @author Charles Xie
 */
class PropertiesDialogForFresnelReflector extends PropertiesDialogFactory {

    static JDialog getDialog(final FresnelReflector reflector) {

        final JDialog dialog = new JDialog(MainFrame.getInstance(), "Fresnel Reflector", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        final String info = reflector.toString().substring(0, reflector.toString().indexOf(')') + 1);
        dialog.setTitle("Properties - " + info);

        dialog.getContentPane().setLayout(new BorderLayout());
        final JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        final JScrollPane scroller = new JScrollPane(panel);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setPreferredSize(new Dimension(300, 160));
        dialog.getContentPane().add(scroller, BorderLayout.CENTER);

        int i = 0;

        panel.add(new JLabel("Total Length: "));
        final JTextField totalLengthField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(reflector.getLength()) + " m (" + reflector.getNumberOfModules() + "\u00D7" +
                PopupMenuFactory.threeDecimalsFormat.format(reflector.getModuleLength()) + " m)");
        totalLengthField.setEditable(false);
        panel.add(totalLengthField);
        i++;

        panel.add(new JLabel("Mirror Reflectance: "));
        final JTextField reflectanceField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(reflector.getReflectance() * 100) + "%");
        reflectanceField.setEditable(false);
        panel.add(reflectanceField);
        i++;

        panel.add(new JLabel("Receiver Absorptance: "));
        final JTextField absorptanceField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(reflector.getAbsorptance() * 100) + "%");
        absorptanceField.setEditable(false);
        panel.add(absorptanceField);
        i++;

        panel.add(new JLabel("Optical Efficiency: "));
        final JTextField opticalEfficiencyField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(reflector.getOpticalEfficiency() * 100) + "%");
        opticalEfficiencyField.setEditable(false);
        panel.add(opticalEfficiencyField);
        i++;

        panel.add(new JLabel("Thermal Efficiency: "));
        final JTextField thermalEfficiencyField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(reflector.getThermalEfficiency() * 100) + "%");
        thermalEfficiencyField.setEditable(false);
        panel.add(thermalEfficiencyField);
        i++;

        SpringUtilities.makeCompactGrid(panel, i, 2, 4, 4, 4, 4);

        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        final JButton button = new JButton("Close");
        button.addActionListener(e -> dialog.dispose());
        buttonPanel.add(button);

        dialog.pack();

        return dialog;

    }

}