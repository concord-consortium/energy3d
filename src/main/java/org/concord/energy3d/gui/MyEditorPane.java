package org.concord.energy3d.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.concord.energy3d.util.Util;

/**
 * This is a JEditorPane that has extra user interface such as a pop-up menu.
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
