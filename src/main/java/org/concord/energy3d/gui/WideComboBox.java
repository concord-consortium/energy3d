package org.concord.energy3d.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

public class WideComboBox extends JComboBox<String> {
	private static final long serialVersionUID = 1L;
	private Dimension preferredSize;
	private boolean layingOut = false;

	public WideComboBox() {
		setModel(new DefaultComboBoxModel<String>(new String[] {"0.00 "}));
		setMinimumSize(getMinimumSize());
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						((JTextComponent)((JComboBox<?>)e.getSource()).getEditor().getEditorComponent()).setCaretPosition(0);
					}
				});
			}
		});
	}

	@Override
	public void setModel(final ComboBoxModel<String> aModel) {
		super.setModel(aModel);
		if (aModel.getSize() > 1) {
			preferredSize = (Dimension) getPreferredSize().clone();
			setPreferredSize(getMinimumSize());
		}
	}

	@Override
	public void doLayout() {
		try {
			layingOut = true;
			super.doLayout();
		} finally {
			layingOut = false;
		}
	}

	@Override
	public Dimension getSize() {
		final Dimension dim = super.getSize();
		if (!layingOut)
			dim.width = Math.max(dim.width, preferredSize.width);
		return dim;
	}
}
