package org.concord.energy3d.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.PlainDocument;
import javax.swing.text.html.HTMLDocument;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.agents.Agent;
import org.concord.energy3d.agents.DataCollectionEvent;
import org.concord.energy3d.agents.EventFrequency;
import org.concord.energy3d.agents.EventString;
import org.concord.energy3d.agents.EventTimeSeries;
import org.concord.energy3d.agents.OperationEvent;
import org.concord.energy3d.agents.QuestionnaireEvent;
import org.concord.energy3d.agents.QuestionnaireModel;
import org.concord.energy3d.geneticalgorithms.applications.BuildingLocationOptimizer;
import org.concord.energy3d.geneticalgorithms.applications.BuildingOrientationOptimizer;
import org.concord.energy3d.geneticalgorithms.applications.HeliostatConcentricFieldOptimizer;
import org.concord.energy3d.geneticalgorithms.applications.HeliostatPositionOptimizer;
import org.concord.energy3d.geneticalgorithms.applications.HeliostatSpiralFieldOptimizer;
import org.concord.energy3d.geneticalgorithms.applications.WindowOptimizer;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.AnnualEnvironmentalTemperature;
import org.concord.energy3d.simulation.DailyEnvironmentalTemperature;
import org.concord.energy3d.simulation.MonthlySunshineHours;
import org.concord.energy3d.util.Html2Text;
import org.concord.energy3d.util.Util;

/**
 * This contains a JEditorPane that has extra user interface such as a pop-up menu and extra functionalities such as opening hyperlinks with an external browser.
 *
 * @author Charles Xie
 */
class MyEditorPane {

    private final JEditorPane editorPane;
    private final JPopupMenu popupMenu;
    private final int id;

    MyEditorPane(final int id, final boolean popup) {

        this.id = id;

        editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        editorPane.addHyperlinkListener(e -> {
            if (e.getURL() != null) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    Util.openBrowser(e.getURL());
                } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                    editorPane.setToolTipText(e.getURL().toString());
                } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                    editorPane.setToolTipText("<html>Double-click to enlarge this window<br>Right-click to open a popup menu for editing</html>");
                }
            } else {
                final Element element = e.getSourceElement();
                final AttributeSet as = element.getAttributes();
                final Enumeration<?> en = as.getAttributeNames();
                while (en.hasMoreElements()) {
                    final Object n = en.nextElement();
                    final Object v = as.getAttribute(n);
                    if (n.toString().equals("a")) {
                        if (v != null) {
                            String s = v.toString();
                            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) { // ad hoc protocols
                                if (s.startsWith("href=goto://")) {
                                    s = s.substring(12).trim();
                                    EnergyPanel.getInstance().getCityComboBox().setSelectedItem(s);
                                } else if (s.startsWith("href=date://")) {
                                    s = s.substring(12).trim();
                                    try {
                                        EnergyPanel.getInstance().getDateSpinner().setValue(new SimpleDateFormat("MMMM dd").parse(s));
                                        Heliodon.getInstance().setDate((Date) EnergyPanel.getInstance().getDateSpinner().getValue());
                                    } catch (final ParseException e1) {
                                        e1.printStackTrace();
                                    }
                                } else if (s.startsWith("href=menu://")) {
                                    s = s.substring(12).trim();
                                    MainFrame.getInstance().openModel(MainApplication.class.getResource(s));
                                } else if (s.startsWith("href=run://")) {
                                    s = s.substring(11).trim();
                                    TaskFactory.run(s);
                                }
                            } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                                editorPane.setToolTipText(s);
                            } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                                editorPane.setToolTipText("<html>Double-click to enlarge this window<br>Right-click to open a popup menu for editing</html>");
                            }
                        }
                    }
                }
            }
        });

        final JMenuItem miEdit = new JMenuItem("Edit");
        final JMenuItem miCopy = new JMenuItem("Copy");
        popupMenu = new JPopupMenu();
        popupMenu.setInvoker(editorPane);
        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
                // miEdit.setEnabled(!Scene.getInstance().isStudentMode());
                miCopy.setEnabled(editorPane.getSelectedText() != null && !editorPane.getSelectedText().equals(""));
            }

            @Override
            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
                miEdit.setEnabled(true);
                miCopy.setEnabled(true);
            }

            @Override
            public void popupMenuCanceled(final PopupMenuEvent e) {
                miEdit.setEnabled(true);
                miCopy.setEnabled(true);
            }

        });

        miEdit.addActionListener(e -> {
            if (!popup) {
                new InstructionSheetDialog(MyEditorPane.this, "Sheet " + (id + 1), id, true).setVisible(true);
            }
        });
        popupMenu.add(miEdit);

        miCopy.addActionListener(e -> {
            final ActionEvent ae = new ActionEvent(editorPane, ActionEvent.ACTION_PERFORMED, "copy");
            editorPane.getActionMap().get(ae.getActionCommand()).actionPerformed(ae);
        });
        popupMenu.add(miCopy);

        editorPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (!popup) {
                    if (e.getClickCount() >= 2) {
                        new InstructionSheetDialog(MyEditorPane.this, "Sheet " + (id + 1), id, false).setVisible(true);
                    }
                }
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                if (!popup) {
                    if (Util.isRightClick(e)) {
                        popupMenu.show(editorPane, e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mouseEntered(final MouseEvent e) {
                editorPane.setToolTipText("<html>Double-click to enlarge this window" + (Scene.isInternalFile() ? "" : "<br>Right-click to open a popup menu for editing") + "</html>");
            }
        });
    }

    public void repaint() {
        editorPane.repaint();
    }

    public int getId() {
        return id;
    }

    JEditorPane getEditorPane() {
        return editorPane;
    }

    public void setText(final String text) {
        editorPane.setText(text);

        if (editorPane.getDocument() instanceof HTMLDocument) {

            final HTMLDocument doc = (HTMLDocument) editorPane.getDocument();
            final ElementIterator it = new ElementIterator(doc);
            Element element;

            while ((element = it.next()) != null) {
                final AttributeSet as = element.getAttributes();
                final Enumeration<?> en = as.getAttributeNames();

                DefaultButtonModel buttonModel = null;
                PlainDocument document = null;
                String action = null;
                String question = null;
                String choice = null;
                String key = null;
                String dataName = null;
                while (en.hasMoreElements()) {
                    final Object n = en.nextElement();
                    final Object v = as.getAttribute(n);
                    if (v instanceof DefaultButtonModel) {
                        buttonModel = (DefaultButtonModel) v;
                    } else if (v instanceof PlainDocument) {
                        document = (PlainDocument) v;
                    } else if (n.toString().equals("action")) {
                        action = v.toString();
                    } else if (n.toString().equals("question")) {
                        question = v.toString();
                    } else if (n.toString().equals("choice")) {
                        choice = v.toString();
                    } else if (n.toString().equals("key")) {
                        key = v.toString();
                    } else if (n.toString().equals("data")) {
                        dataName = v.toString();
                    }
                }
                if (action != null) {
                    final String a = action;
                    if (document != null) {
                        final String n = dataName;
                        final PlainDocument d = document;
                        document.addDocumentListener(new DocumentListener() {
                            @Override
                            public void removeUpdate(final DocumentEvent e) {
                                textFieldUpdated(a, n, d);
                            }

                            @Override
                            public void insertUpdate(final DocumentEvent e) {
                                textFieldUpdated(a, n, d);
                            }

                            @Override
                            public void changedUpdate(final DocumentEvent e) {
                                textFieldUpdated(a, n, d);
                            }
                        });
                    }
                    if (buttonModel != null) {
                        final QuestionnaireModel qm;
                        if (question != null && choice != null) {
                            boolean isKey = false;
                            if ("yes".equalsIgnoreCase(key) || "true".equalsIgnoreCase(key)) {
                                isKey = true;
                            }
                            qm = new QuestionnaireModel(question, choice, isKey);
                        } else {
                            qm = null;
                        }
                        final DefaultButtonModel bm = buttonModel;
                        if (buttonModel instanceof JToggleButton.ToggleButtonModel) {
                            buttonModel.addItemListener(e -> {
                                if (qm != null) { // fire only one for questionnaires
                                    if (e.getStateChange() == ItemEvent.SELECTED) {
                                        buttonActionPerformed(a, qm, bm);
                                    }
                                } else {
                                    buttonActionPerformed(a, null, bm);
                                }
                            });
                        } else {
                            buttonModel.addActionListener(e -> buttonActionPerformed(a, qm, bm));
                        }
                    }
                }

            }
        }

    }

    private void textFieldUpdated(final String act, final String name, final PlainDocument document) {
        if ("Data Collector".equals(act)) {
            String t = null;
            try {
                t = document.getText(0, document.getLength());
            } catch (final BadLocationException e) {
                e.printStackTrace();
            }
            if (t != null) {
                MainApplication.addEvent(new DataCollectionEvent(Scene.getURL(), System.currentTimeMillis(), name, t));
            }
        }
    }

    private void buttonActionPerformed(final String act, final QuestionnaireModel questionnaireModel, final DefaultButtonModel buttonModel) {

        if (act == null) {
            return;
        }

        // instruction sheet selection commands
        if (act.startsWith("Sheet")) {
            final int i = Integer.parseInt(act.substring(5).trim());
            EnergyPanel.getInstance().selectInstructionSheet(i - 1);
        } else if ("Questionnaire".equals(act)) {
            if (questionnaireModel != null) {
                MainApplication.addEvent(new QuestionnaireEvent(Scene.getURL(), System.currentTimeMillis(), questionnaireModel));
            }
        } else if ("Reopen".equals(act)) {
            MainFrame.getInstance().reopen();
        } else if ("Save".equals(act)) {
            MainFrame.getInstance().saveFile(false);
        } else if (act.startsWith("Event Miner") || act.startsWith("Conformance Checker")) {
            final Agent a = MainApplication.getAgent(act);
            if (a != null) {
                final HashMap<String, Object> attributes = new HashMap<>();
                attributes.put("Agent", act);
                MainApplication.addEvent(new OperationEvent(Scene.getURL(), System.currentTimeMillis(), '?', a.getName(), attributes));
                a.actuate();
            }
        }

        // show actions
        else if ("Event Frequency".equals(act)) {
            new EventFrequency().showGui();
        } else if ("Event Time Series".equals(act)) {
            new EventTimeSeries().showGui();
        } else if ("Event String".equals(act)) {
            new EventString().showGui();
        }

        // heliodon commands
        else if ("Heliodon".equals(act)) {
            MainPanel.getInstance().getHeliodonButton().setSelected(buttonModel.isSelected());
        } else if ("Heliodon On".equals(act)) {
            MainPanel.getInstance().getHeliodonButton().setSelected(true);
        } else if ("Heliodon Off".equals(act)) {
            MainPanel.getInstance().getHeliodonButton().setSelected(false);
        }

        // sun motion commands
        else if ("Sun Motion".equals(act)) {
            MainPanel.getInstance().getSunAnimationButton().setSelected(buttonModel.isSelected());
        } else if ("Sun Motion On".equals(act)) {
            MainPanel.getInstance().getSunAnimationButton().setSelected(true);
        } else if ("Sun Motion Off".equals(act)) {
            MainPanel.getInstance().getSunAnimationButton().setSelected(false);
        }

        // shadow commands
        else if ("Shadow".equals(act)) {
            MainPanel.getInstance().getShadowButton().setSelected(buttonModel.isSelected());
        } else if ("Shadow On".equals(act)) {
            MainPanel.getInstance().getShadowButton().setSelected(true);
        } else if ("Shadow Off".equals(act)) {
            MainPanel.getInstance().getShadowButton().setSelected(false);
        }

        // solar analysis tools
        else if (act.startsWith("Daily Yield Analysis of Solar Panels")) {
            TaskFactory.dailyYieldAnalysisOfSolarPanels();
        } else if (act.startsWith("Annual Yield Analysis of Solar Panels")) {
            TaskFactory.annualYieldAnalysisOfSolarPanels();
        }

        // group analysis tools

        else if (act.startsWith("Daily Analysis for Group")) {
            TaskFactory.dailyAnalysisForGroup(act);
        } else if (act.startsWith("Annual Analysis for Group")) {
            TaskFactory.annualAnalysisForGroup(act);
        }

        // generative design functions
        else if (act.startsWith("Building Location Optimizer")) {
            final String s = act.substring("Building Location Optimizer".length()).trim();
            if ("Stop".equalsIgnoreCase(s)) {
                BuildingLocationOptimizer.stopIt();
            } else {
                try {
                    final int i = Integer.parseInt(s);
                    final HousePart p = Scene.getInstance().getPart(i);
                    if (p instanceof Foundation) {
                        BuildingLocationOptimizer.make((Foundation) p);
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + act + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (final Exception e) {
                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + act + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (act.startsWith("Building Orientation Optimizer")) {
            final String s = act.substring("Building Orientation Optimizer".length()).trim();
            if ("Stop".equalsIgnoreCase(s)) {
                BuildingOrientationOptimizer.stopIt();
            } else {
                try {
                    final int i = Integer.parseInt(s);
                    final HousePart p = Scene.getInstance().getPart(i);
                    if (p instanceof Foundation) {
                        BuildingOrientationOptimizer.make((Foundation) p);
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + act + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (final Exception e) {
                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + act + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (act.startsWith("Window Optimizer")) {
            final String s = act.substring("Window Optimizer".length()).trim();
            if ("Stop".equalsIgnoreCase(s)) {
                WindowOptimizer.stopIt();
            } else {
                try {
                    final int i = Integer.parseInt(s);
                    final HousePart p = Scene.getInstance().getPart(i);
                    if (p instanceof Foundation) {
                        WindowOptimizer.make((Foundation) p);
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + act + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (final Exception e) {
                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + act + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (act.startsWith("Solar Panel Tilt Angle Optimizer")) {
            TaskFactory.solarPanelTiltAngleOptimizer(act);
        } else if (act.startsWith("Solar Panel Array Optimizer")) {
            TaskFactory.solarPanelArrayOptimizer(act);
        } else if (act.startsWith("Solar Panel Array Layout Manager")) {
            TaskFactory.solarPanelArrayLayoutManager(act);
        } else if (act.startsWith("Heliostat Position Optimizer")) {
            final String s = act.substring("Heliostat Position Optimizer".length()).trim();
            if ("Stop".equalsIgnoreCase(s)) {
                HeliostatPositionOptimizer.stopIt();
            } else {
                try {
                    final int i = Integer.parseInt(s);
                    final HousePart p = Scene.getInstance().getPart(i);
                    if (p instanceof Foundation) {
                        HeliostatPositionOptimizer.make((Foundation) p);
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + act + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (final Exception e) {
                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + act + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (act.startsWith("Heliostat Concentric Field Optimizer")) {
            final String s = act.substring("Heliostat Concentric Field Optimizer".length()).trim();
            if ("Stop".equalsIgnoreCase(s)) {
                HeliostatConcentricFieldOptimizer.stopIt();
            } else {
                try {
                    final int i = Integer.parseInt(s);
                    final HousePart p = Scene.getInstance().getPart(i);
                    if (p instanceof Foundation) {
                        HeliostatConcentricFieldOptimizer.make((Foundation) p);
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + act + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (final Exception e) {
                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + act + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (act.startsWith("Heliostat Spiral Field Optimizer")) {
            final String s = act.substring("Heliostat Spiral Field Optimizer".length()).trim();
            if ("Stop".equalsIgnoreCase(s)) {
                HeliostatSpiralFieldOptimizer.stopIt();
            } else {
                try {
                    final int i = Integer.parseInt(s);
                    final HousePart p = Scene.getInstance().getPart(i);
                    if (p instanceof Foundation) {
                        HeliostatSpiralFieldOptimizer.make((Foundation) p);
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + act + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (final Exception e) {
                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + act + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        // environmental temperature graph
        else if ("Daily Environmental Temperature".equals(act)) {
            if (EnergyPanel.getInstance().checkCity()) {
                new DailyEnvironmentalTemperature().showDialog();
            }
        } else if ("Annual Environmental Temperature".equals(act)) {
            if (EnergyPanel.getInstance().checkCity()) {
                new AnnualEnvironmentalTemperature().showDialog();
            }
        } else if ("Monthly Sunshine Hours".equals(act)) {
            if (EnergyPanel.getInstance().checkCity()) {
                new MonthlySunshineHours().showDialog();
            }
        } else {
            JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>" + act + "</html>", "Information", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    public String getText() {
        return editorPane.getText();
    }

    String getRawText() throws IOException {
        // return editorPane.getDocument().getText(0, editorPane.getDocument().getLength());
        // use the following method to get a longer pause between paragraphs, instead of the above
        final Html2Text parser = new Html2Text();
        parser.parse(new StringReader(editorPane.getText()));
        return parser.getText();
    }

    public void setEditable(final boolean editable) {
        editorPane.setEditable(editable);
    }

    void setContentType(final String type) {
        editorPane.setContentType(type);
    }

    String getContentType() {
        return editorPane.getContentType();
    }

}