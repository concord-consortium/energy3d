package org.concord.energy3d.gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.html.HTMLDocument;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.agents.Agent;
import org.concord.energy3d.agents.EventFrequency;
import org.concord.energy3d.agents.EventTimeSeries;
import org.concord.energy3d.agents.QuestionnaireEvent;
import org.concord.energy3d.agents.QuestionnaireModel;
import org.concord.energy3d.model.PartGroup;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.AnnualEnvironmentalTemperature;
import org.concord.energy3d.simulation.DailyEnvironmentalTemperature;
import org.concord.energy3d.simulation.GroupAnnualAnalysis;
import org.concord.energy3d.simulation.GroupDailyAnalysis;
import org.concord.energy3d.simulation.MonthlySunshineHours;
import org.concord.energy3d.util.Util;

/**
 * This contains a JEditorPane that has extra user interface such as a pop-up menu and extra functionalities such as opening hyperlinks with an external browser.
 * 
 * @author Charles Xie
 *
 */
public class MyEditorPane {

	private final JEditorPane editorPane;
	private final JPopupMenu popupMenu;
	private final int id;

	public MyEditorPane(final int id, final boolean popup) {

		this.id = id;

		editorPane = new JEditorPane();
		editorPane.setEditable(false);
		editorPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		editorPane.setToolTipText("<html>Double-click to enlarge this window<br>Right-click to open a popup menu for editing</html>");
		editorPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(final HyperlinkEvent e) {
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
								if (s.startsWith("href=goto://")) {
									s = s.substring(12);
								}
								if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
									EnergyPanel.getInstance().getCityComboBox().setSelectedItem(s.trim());
								} else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
									editorPane.setToolTipText(s);
								} else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
									editorPane.setToolTipText("<html>Double-click to enlarge this window<br>Right-click to open a popup menu for editing</html>");
								}
							}
						}
					}
				}
			}
		});

		final JMenuItem miEdit = new JMenuItem("Edit");
		popupMenu = new JPopupMenu();
		popupMenu.setInvoker(editorPane);
		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
				// miEdit.setEnabled(!Scene.getInstance().isStudentMode());
			}

			@Override
			public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				miEdit.setEnabled(true);
			}

			@Override
			public void popupMenuCanceled(final PopupMenuEvent e) {
				miEdit.setEnabled(true);
			}

		});

		miEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (!popup) {
					new InstructionSheetDialog(MyEditorPane.this, "Sheet " + (id + 1), id, true).setVisible(true);
				}
			}
		});
		popupMenu.add(miEdit);

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
		});
	}

	public void repaint() {
		editorPane.repaint();
	}

	public int getId() {
		return id;
	}

	public JEditorPane getEditorPane() {
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

				// look for buttons
				DefaultButtonModel buttonModel = null;
				String action = null;
				String question = null;
				String choice = null;
				String key = null;
				while (en.hasMoreElements()) {
					final Object n = en.nextElement();
					final Object v = as.getAttribute(n);
					if (v instanceof DefaultButtonModel) {
						buttonModel = (DefaultButtonModel) v;
					} else if (n.toString().equals("action")) {
						action = v.toString();
					} else if (n.toString().equals("question")) {
						question = v.toString();
					} else if (n.toString().equals("choice")) {
						choice = v.toString();
					} else if (n.toString().equals("key")) {
						key = v.toString();
					}
				}
				if (buttonModel != null && action != null) {
					final String a = action;
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
						buttonModel.addItemListener(new ItemListener() {
							@Override
							public void itemStateChanged(final ItemEvent e) {
								if (e.getStateChange() == ItemEvent.SELECTED) {
									interpret(a, qm, bm);
								}
							}
						});
					} else {
						buttonModel.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(final ActionEvent e) {
								interpret(a, qm, bm);
							}
						});
					}
				}

			}
		}

	}

	private void interpret(final String act, final QuestionnaireModel questionnaireModel, final DefaultButtonModel buttonModel) {

		if (act == null) {
			return;
		}

		// instruction sheet selection commands
		if ("Sheet 1".equals(act)) {
			EnergyPanel.getInstance().selectInstructionSheet(0);
		} else if ("Sheet 2".equals(act)) {
			EnergyPanel.getInstance().selectInstructionSheet(1);
		} else if ("Sheet 3".equals(act)) {
			EnergyPanel.getInstance().selectInstructionSheet(2);
		} else if ("Sheet 4".equals(act)) {
			EnergyPanel.getInstance().selectInstructionSheet(3);
		} else if ("Sheet 5".equals(act)) {
			EnergyPanel.getInstance().selectInstructionSheet(4);
		}

		else if ("Questionnaire".equals(act)) {
			if (questionnaireModel != null) {
				MainApplication.addEvent(new QuestionnaireEvent(Scene.getURL(), System.currentTimeMillis(), questionnaireModel));
			}
		}

		else if ("Agent Test".equals(act)) {
			final Agent a = MainApplication.getAgent(act);
			if (a != null) {
				a.actuate();
			}
		}

		// show actions
		else if ("Event Frequency".equals(act)) {
			new EventFrequency().showGui();
		} else if ("Event Time Series".equals(act)) {
			new EventTimeSeries().showGui();
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

		// group analysis tools

		else if (act.startsWith("Daily Analysis for Group")) {
			if (EnergyPanel.getInstance().checkCity()) {
				PartGroup g = null;
				final GroupSelector selector = new GroupSelector();
				for (final String s : GroupSelector.types) {
					final int index = act.indexOf(s);
					if (index > 0) {
						selector.setCurrentGroupType(s);
						try {
							final String t = act.substring(index + s.length()).trim();
							if (!t.equals("")) {
								g = new PartGroup(s);
								final String[] a = t.split(",");
								for (final String x : a) {
									g.addId(Integer.parseInt(x.trim()));
								}
							}
						} catch (final Exception e) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + act + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
							g = null;
						}
						break;
					}
				}
				if (g == null) {
					g = selector.select();
				}
				if (g != null) {
					final PartGroup g2 = g;
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() { // for some reason, this may be delayed in the AWT Event Queue in order to avoid a HTML form NullPointerException
							final GroupDailyAnalysis a = new GroupDailyAnalysis(g2);
							a.show(g2.getType() + ": " + g2.getIds());
						}
					});
				}
				SceneManager.getInstance().hideAllEditPoints();
			}
		} else if (act.startsWith("Annual Analysis for Group")) {
			if (EnergyPanel.getInstance().checkCity()) {
				PartGroup g = null;
				final GroupSelector selector = new GroupSelector();
				for (final String s : GroupSelector.types) {
					final int index = act.indexOf(s);
					if (index > 0) {
						selector.setCurrentGroupType(s);
						try {
							final String t = act.substring(index + s.length()).trim();
							if (!t.equals("")) {
								g = new PartGroup(s);
								final String[] a = t.split(",");
								for (final String x : a) {
									g.addId(Integer.parseInt(x.trim()));
								}
							}
						} catch (final Exception e) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Error in <i>" + act + "</i>.<br>Please select the IDs manually.</html>", "Input Error", JOptionPane.ERROR_MESSAGE);
							g = null;
						}
						break;
					}
				}
				if (g == null) {
					g = selector.select();
				}
				if (g != null) {
					final PartGroup g2 = g;
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() { // for some reason, this may be delayed in the AWT Event Queue in order to avoid a HTML form NullPointerException
							final GroupAnnualAnalysis a = new GroupAnnualAnalysis(g2);
							a.show(g2.getType() + ": " + g2.getIds());
						}
					});
				}
				SceneManager.getInstance().hideAllEditPoints();
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
		}

		else {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>" + act + "</html>", "Information", JOptionPane.INFORMATION_MESSAGE);
		}

	}

	public String getText() {
		return editorPane.getText();
	}

	public void setEditable(final boolean editable) {
		editorPane.setEditable(editable);
	}

	public void setContentType(final String type) {
		editorPane.setContentType(type);
	}

	public String getContentType() {
		return editorPane.getContentType();
	}

}
