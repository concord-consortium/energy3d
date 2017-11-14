package org.concord.energy3d.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.html.HTMLDocument;

import org.concord.energy3d.util.Util;

/**
 * This is a JEditorPane that has extra user interface such as a pop-up menu and extra functionalities such as opening hyperlinks with an external browser.
 * 
 * @author Charles Xie
 *
 */
class MyEditorPane {

	private final JEditorPane editorPane;
	private final JPopupMenu popupMenu;

	MyEditorPane(final int id) {

		editorPane = new JEditorPane();
		editorPane.setEditable(false);
		editorPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		editorPane.setToolTipText("<html>Double-click to enlarge this window<br>Right-click to open a popup menu for editing</html>");
		editorPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(final HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					Util.openBrowser(e.getURL());
				} else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
					editorPane.setToolTipText(e.getURL().toString());
				} else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
					editorPane.setToolTipText("<html>Double-click to enlarge this window<br>Right-click to open a popup menu for editing</html>");
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
				new InstructionSheetDialog(MyEditorPane.this, "Sheet " + (id + 1), id, true).setVisible(true);
			}
		});
		popupMenu.add(miEdit);

		editorPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() >= 2) {
					new InstructionSheetDialog(MyEditorPane.this, "Sheet " + (id + 1), id, false).setVisible(true);
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				if (Util.isRightClick(e)) {
					popupMenu.show(editorPane, e.getX(), e.getY());
				}
			}
		});
	}

	public void repaint() {
		editorPane.repaint();
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
				final Enumeration<?> enumm = as.getAttributeNames();

				// look for buttons
				DefaultButtonModel buttonModel = null;
				String action = null;
				while (enumm.hasMoreElements()) {
					final Object n = enumm.nextElement();
					final Object v = as.getAttribute(n);
					if (v instanceof DefaultButtonModel) {
						buttonModel = (DefaultButtonModel) v;
					} else if (n.toString().equals("action")) {
						action = v.toString();
					}
				}
				if (buttonModel != null && action != null) {
					final String action2 = action;
					buttonModel.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>" + action2 + "</html>", "Information", JOptionPane.INFORMATION_MESSAGE);
						}
					});
				}
			}
		}
	}

	public String getText() {
		return editorPane.getText();
	}

	public void setContentType(final String type) {
		editorPane.setContentType(type);
	}

	public String getContentType() {
		return editorPane.getContentType();
	}

}
