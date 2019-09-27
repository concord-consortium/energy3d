package org.concord.energy3d.gui;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.SetSensorLabelCommand;
import org.concord.energy3d.util.Util;

import javax.swing.*;

class PopupMenuForSensor extends PopupMenuFactory {

    private static JPopupMenu popupMenuForSensor;

    static JPopupMenu getPopupMenu() {

        if (popupMenuForSensor == null) {

            final JCheckBoxMenuItem miLight = new JCheckBoxMenuItem("Light", true);
            final JCheckBoxMenuItem miHeatFlux = new JCheckBoxMenuItem("Heat Flux", true);
            final JCheckBoxMenuItem miLabelNone = new JCheckBoxMenuItem("None", true);
            final JCheckBoxMenuItem miLabelCustom = new JCheckBoxMenuItem("Custom");
            final JCheckBoxMenuItem miLabelId = new JCheckBoxMenuItem("ID");

            popupMenuForSensor = createPopupMenu(false, false, () -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (!(selectedPart instanceof Sensor)) {
                    return;
                }
                final Sensor s = (Sensor) selectedPart;
                Util.selectSilently(miLight, !s.isLightOff());
                Util.selectSilently(miHeatFlux, !s.isHeatFluxOff());
                Util.selectSilently(miLabelNone, !s.isLabelVisible());
                Util.selectSilently(miLabelCustom, s.getLabelCustom());
                Util.selectSilently(miLabelId, s.getLabelId());
            });

             miLight.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (!(selectedPart instanceof Sensor)) {
                    return;
                }
                final Sensor s = (Sensor) selectedPart;
                s.setLightOff(!miLight.isSelected());
                Scene.getInstance().setEdited(true);
            });

            miHeatFlux.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (!(selectedPart instanceof Sensor)) {
                    return;
                }
                final Sensor s = (Sensor) selectedPart;
                s.setHeatFluxOff(!miHeatFlux.isSelected());
                Scene.getInstance().setEdited(true);
            });

            popupMenuForSensor.addSeparator();
            popupMenuForSensor.add(miLight);
            popupMenuForSensor.add(miHeatFlux);

            final JMenu labelMenu = new JMenu("Label");
            popupMenuForSensor.addSeparator();
            popupMenuForSensor.add(labelMenu);

            miLabelNone.addActionListener(e -> {
                if (miLabelNone.isSelected()) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Sensor) {
                        final Sensor sensor = (Sensor) selectedPart;
                        final SetSensorLabelCommand c = new SetSensorLabelCommand(sensor);
                        SceneManager.getTaskManager().update(() -> {
                            sensor.clearLabels();
                            sensor.draw();
                            SceneManager.getInstance().refresh();
                            return null;
                        });
                        SceneManager.getInstance().getUndoManager().addEdit(c);
                        Scene.getInstance().setEdited(true);
                    }
                }
            });
            labelMenu.add(miLabelNone);

            miLabelCustom.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Sensor) {
                    final Sensor sensor = (Sensor) selectedPart;
                    final SetSensorLabelCommand c = new SetSensorLabelCommand(sensor);
                    sensor.setLabelCustom(miLabelCustom.isSelected());
                    if (sensor.getLabelCustom()) {
                        sensor.setLabelCustomText(JOptionPane.showInputDialog(MainFrame.getInstance(), "Custom Text", sensor.getLabelCustomText()));
                    }
                    SceneManager.getTaskManager().update(() -> {
                        sensor.draw();
                        SceneManager.getInstance().refresh();
                        return null;
                    });
                    SceneManager.getInstance().getUndoManager().addEdit(c);
                    Scene.getInstance().setEdited(true);
                }
            });
            labelMenu.add(miLabelCustom);

            miLabelId.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Sensor) {
                    final Sensor sensor = (Sensor) selectedPart;
                    final SetSensorLabelCommand c = new SetSensorLabelCommand(sensor);
                    sensor.setLabelId(miLabelId.isSelected());
                    SceneManager.getTaskManager().update(() -> {
                        sensor.draw();
                        SceneManager.getInstance().refresh();
                        return null;
                    });
                    SceneManager.getInstance().getUndoManager().addEdit(c);
                    Scene.getInstance().setEdited(true);
                }
            });
            labelMenu.add(miLabelId);

        }

        return popupMenuForSensor;

    }

}