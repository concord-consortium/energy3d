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

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.SpringUtilities;

import com.ardor3d.math.Vector3;

/**
 * @author Charles Xie
 */
class PropertiesDialogForFoundation extends PropertiesDialogFactory {

    static JDialog getDialog(final Foundation foundation) {

        final JDialog dialog = new JDialog(MainFrame.getInstance(), "Foundation", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        final String info = foundation.toString().substring(0, foundation.toString().indexOf(')') + 1);
        dialog.setTitle("Properties - " + info);

        dialog.getContentPane().setLayout(new BorderLayout());
        final JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        final JScrollPane scroller = new JScrollPane(panel);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setPreferredSize(new Dimension(300, 120));
        dialog.getContentPane().add(scroller, BorderLayout.CENTER);

        final double s = Scene.getInstance().getScale();
        final Vector3 v0 = foundation.getAbsPoint(0);
        final Vector3 v1 = foundation.getAbsPoint(1);
        final Vector3 v2 = foundation.getAbsPoint(2);
        final Vector3 v3 = foundation.getAbsPoint(3);
        final double cx = 0.25 * (v0.getX() + v1.getX() + v2.getX() + v3.getX()) * s;
        final double cy = 0.25 * (v0.getY() + v1.getY() + v2.getY() + v3.getY()) * s;
        final double lx = v0.distance(v2) * s;
        final double ly = v0.distance(v1) * s;
        final double lz = foundation.getHeight() * s;

        int i = 0;

        panel.add(new JLabel("Size: "));
        final JTextField sizeField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(lx) + "\u00D7" +
                PopupMenuFactory.threeDecimalsFormat.format(ly) + "\u00D7" + PopupMenuFactory.threeDecimalsFormat.format(lz) + " m");
        sizeField.setEditable(false);
        panel.add(sizeField);
        i++;

        panel.add(new JLabel("Center: "));
        final JTextField reflectanceField = new JTextField("(" + PopupMenuFactory.threeDecimalsFormat.format(cx) + ", " + PopupMenuFactory.threeDecimalsFormat.format(cy) + ") m");
        reflectanceField.setEditable(false);
        panel.add(reflectanceField);
        i++;

        panel.add(new JLabel("Project Type: "));
        String project = "Auto";
        switch (foundation.getProjectType()) {
            case Foundation.TYPE_BUILDING:
                project = "Building";
                break;
            case Foundation.TYPE_PV_PROJECT:
                project = "PV";
                break;
            case Foundation.TYPE_CSP_PROJECT:
                project = "CSP";
                break;
        }
        final JTextField projectField = new JTextField(project);
        projectField.setEditable(false);
        panel.add(projectField);
        i++;

        panel.add(new JLabel("Group Master: "));
        final JTextField groupMasterField = new JTextField(foundation.isGroupMaster() ? "Yes" : "No");
        groupMasterField.setEditable(false);
        panel.add(groupMasterField);
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