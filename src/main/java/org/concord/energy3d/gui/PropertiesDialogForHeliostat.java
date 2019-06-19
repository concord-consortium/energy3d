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

import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.util.SpringUtilities;

/**
 * @author Charles Xie
 */
class PropertiesDialogForHeliostat extends PropertiesDialogFactory {

    static JDialog getDialog(final Mirror mirror) {

        final JDialog dialog = new JDialog(MainFrame.getInstance(), "Heliostat", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        final String info = mirror.toString().substring(0, mirror.toString().indexOf(')') + 1);
        dialog.setTitle("Properties - " + info);

        dialog.getContentPane().setLayout(new BorderLayout());
        final JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        final JScrollPane scroller = new JScrollPane(panel);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setPreferredSize(new Dimension(300, 80));
        dialog.getContentPane().add(scroller, BorderLayout.CENTER);

        int i = 0;

        panel.add(new JLabel("Size: "));
        final JTextField sizeField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(mirror.getApertureWidth()) + "\u00D7" +
                PopupMenuFactory.threeDecimalsFormat.format(mirror.getApertureHeight()) + " m");
        sizeField.setEditable(false);
        panel.add(sizeField);
        i++;

        panel.add(new JLabel("Reflectance: "));
        final JTextField reflectanceField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(mirror.getReflectance() * 100) + "%");
        reflectanceField.setEditable(false);
        panel.add(reflectanceField);
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