package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.model.PartGroup;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

/**
 * @author Charles Xie
 */
class GroupSelector {

    static final String[] types = {"Solar Panel", "Solar Panel Rack", "Mirror", "Parabolic Trough", "Parabolic Dish", "Fresnel Reflector", "Window", "Wall", "Roof", "Foundation", "Foundation (Mean)"};
    private String currentGroupType = "Solar Panel";

    private ArrayList<Long> getIdArray(final Class<?> c) {
        final ArrayList<Long> idArray = new ArrayList<>();
        for (final HousePart p : Scene.getInstance().getParts()) {
            if (c.isInstance(p)) {
                idArray.add(p.getId());
            }
        }
        Collections.sort(idArray);
        return idArray;
    }

    private Class<?> getCurrentGroupClass() {
        Class<?> c = null;
        if ("Wall".equals(currentGroupType)) {
            c = Wall.class;
        } else if ("Window".equals(currentGroupType)) {
            c = Window.class;
        } else if ("Roof".equals(currentGroupType)) {
            c = Roof.class;
        } else if ("Solar Panel".equals(currentGroupType)) {
            c = SolarPanel.class;
        } else if ("Solar Panel Rack".equals(currentGroupType)) {
            c = Rack.class;
        } else if ("Mirror".equals(currentGroupType)) {
            c = Mirror.class;
        } else if ("Parabolic Trough".equals(currentGroupType)) {
            c = ParabolicTrough.class;
        } else if ("Parabolic Dish".equals(currentGroupType)) {
            c = ParabolicDish.class;
        } else if ("Fresnel Reflector".equals(currentGroupType)) {
            c = FresnelReflector.class;
        } else if ("Foundation".equals(currentGroupType) || currentGroupType.startsWith("Foundation")) {
            c = Foundation.class;
        }
        return c;
    }

    void setCurrentGroupType(final String currentGroupType) {
        this.currentGroupType = currentGroupType;
    }

    PartGroup select() {
        final JPanel gui = new JPanel(new BorderLayout(5, 5));
        gui.setBorder(BorderFactory.createTitledBorder("Types and IDs"));
        final DefaultListModel<Long> idListModel = new DefaultListModel<>();
        final JComboBox<String> typeComboBox = new JComboBox<>(types);
        if (currentGroupType != null) {
            typeComboBox.setSelectedItem(currentGroupType);
        }
        typeComboBox.addItemListener(e -> {
            idListModel.clear();
            currentGroupType = (String) typeComboBox.getSelectedItem();
            final Class<?> c = getCurrentGroupClass();
            if (c != null) {
                final ArrayList<Long> idArray = getIdArray(c);
                for (final Long id : idArray) {
                    idListModel.addElement(id);
                }
            }
        });
        final Class<?> c = getCurrentGroupClass();
        if (c != null) {
            final ArrayList<Long> idArray = getIdArray(c);
            for (final Long id : idArray) {
                idListModel.addElement(id);
            }
        }
        final JList<Long> idList = new JList<>(idListModel);
        idList.addListSelectionListener(e -> {
            SceneManager.getInstance().hideAllEditPoints();
            final List<Long> selectedValues = idList.getSelectedValuesList();
            for (final Long i : selectedValues) {
                final HousePart p = Scene.getInstance().getPart(i);
                p.setEditPointsVisible(true);
                p.draw();
            }
        });
        gui.add(typeComboBox, BorderLayout.NORTH);
        gui.add(new JScrollPane(idList), BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), gui, "Select a Group", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION) {
            return null;
        }
        final List<Long> selectedIds = idList.getSelectedValuesList();
        if (selectedIds.isEmpty()) {
            JOptionPane.showMessageDialog(MainFrame.getInstance(), "You must select a group of parts first.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return new PartGroup((String) typeComboBox.getSelectedItem(), selectedIds);
    }

}