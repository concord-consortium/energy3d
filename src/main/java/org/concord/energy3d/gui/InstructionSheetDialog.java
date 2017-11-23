package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.concord.energy3d.scene.Scene;

/**
 * @author Charles Xie
 * 
 */
class InstructionSheetDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public InstructionSheetDialog(final MyEditorPane sheet, final String title, final int i, final boolean editMode) {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle(title);
		getContentPane().setLayout(new BorderLayout());
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JScrollPane scroller;

		if (editMode) {

			final JTextArea textArea = new JTextArea(sheet.getText());
			textArea.setLineWrap(true);
			textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
			scroller = new JScrollPane(textArea);

			final JCheckBox htmlCheckBox = new JCheckBox("Use HTML");

			final ActionListener okListener = new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String type = htmlCheckBox.isSelected() ? "text/html" : "text/plain";
					final String text = textArea.getText();
					sheet.setContentType(type);
					sheet.setText(text);
					sheet.repaint();
					Scene.getInstance().setInstructionSheetTextType(i, type);
					Scene.getInstance().setInstructionSheetText(i, text);
					Scene.getInstance().setEdited(true);
					dispose();
				}
			};

			htmlCheckBox.setSelected("text/html".equals(Scene.getInstance().getInstructionSheetTextType(i)));
			htmlCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (htmlCheckBox.isSelected()) {
						final JEditorPane tmp = new JEditorPane();
						tmp.setContentType("text/html");
						tmp.setText(textArea.getText());
						textArea.setText(tmp.getText());
					} else {
						final HtmlToText parser = new HtmlToText();
						try {
							parser.parse(new StringReader(textArea.getText()));
						} catch (final IOException ioe) {
							ioe.printStackTrace();
						}
						final String s = parser.getText();
						textArea.setText(s);
					}
				}
			});
			buttonPanel.add(htmlCheckBox);

			final JButton okButton = new JButton("OK");
			okButton.addActionListener(okListener);
			okButton.setActionCommand("OK");
			buttonPanel.add(okButton);
			getRootPane().setDefaultButton(okButton);

		} else {

			final MyEditorPane myEditor = new MyEditorPane(sheet.getId(), true);
			myEditor.setContentType(sheet.getContentType());
			myEditor.setText(sheet.getText());
			myEditor.setEditable(false);
			myEditor.getEditorPane().setBorder(new EmptyBorder(10, 10, 10, 10));
			scroller = new JScrollPane(myEditor.getEditorPane());

			final JButton editButton = new JButton("Edit");
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					dispose();
					new InstructionSheetDialog(sheet, "Sheet " + (i + 1), i, true).setVisible(true);
				}
			});
			editButton.setActionCommand(editButton.getText());
			buttonPanel.add(editButton);

		}

		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setPreferredSize(new Dimension(800, 400));
		getContentPane().add(scroller, BorderLayout.CENTER);

		final JButton cancelOrCloseButton = new JButton(editMode ? "Cancel" : "Close");
		cancelOrCloseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dispose();
			}
		});
		cancelOrCloseButton.setActionCommand(cancelOrCloseButton.getText());
		buttonPanel.add(cancelOrCloseButton);

		pack();
		setLocationRelativeTo(MainFrame.getInstance());

	}

}