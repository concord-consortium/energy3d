package org.concord.energy3d.agents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.undo.ChangeDateCommand;
import org.concord.energy3d.undo.ChangePartUValueCommand;
import org.concord.energy3d.util.ClipImage;

/**
 * @author Charles Xie
 *
 */
public class EventString extends JPanel {

	private static final long serialVersionUID = 1L;
	private final JEditorPane html;
	private final String eventString;

	public EventString() {

		super();
		setPreferredSize(new Dimension(600, 400));
		setLayout(new BorderLayout());

		html = new JEditorPane();
		html.setBackground(Color.WHITE);
		html.setContentType("text/html");
		add(html, BorderLayout.CENTER);

		eventString = EventUtil.eventsToString(new Class[] { AnalysisEvent.class, ChangePartUValueCommand.class, ChangeDateCommand.class }, 10000, null);
		String text = "<html><table border=0 cellpadding=0 cellspacing=0><tr>";
		for (int i = 0; i < eventString.length(); i++) {
			final char c = eventString.charAt(i);
			switch (c) {
			case 'A':
				text += "<td bgcolor=#993322><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font>";
				break;
			case 'C':
				text += "<td bgcolor=#339922><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font>";
				break;
			case 'U':
				text += "<td bgcolor=#226699><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font>";
				break;
			case '_':
				text += "<td bgcolor=#dddddd><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font>";
				break;
			default:
				text += "<td bgcolor=#333333><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font>";
			}
			text += "</td>";
		}
		text += "</tr></table></html>";
		html.setText(text);

	}

	public void showGui() {

		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Event String", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		final JPanel contentPane = new JPanel(new BorderLayout());
		dialog.setContentPane(contentPane);

		final JMenuBar menuBar = new JMenuBar();
		dialog.setJMenuBar(menuBar);

		final JMenu menu = new JMenu("View");
		menuBar.add(menu);

		final JMenuItem mi = new JMenuItem("Copy Image");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				new ClipImage().copyImageToClipboard(EventString.this);
			}
		});
		menu.add(mi);

		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		contentPane.add(panel, BorderLayout.CENTER);

		panel.add(this, BorderLayout.CENTER);

		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("Copy String");
		button.setEnabled(!eventString.equals(""));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				html.selectAll();
				final ActionEvent ae = new ActionEvent(html, ActionEvent.ACTION_PERFORMED, "copy");
				if (ae != null) {
					html.getActionMap().get(ae.getActionCommand()).actionPerformed(ae);
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "The string is now ready for pasting.", "Copy String", JOptionPane.INFORMATION_MESSAGE);
					html.select(0, 0);
				}
			}
		});
		button.setToolTipText("Copy data to the system clipboard");
		buttonPanel.add(button);

		button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.dispose();
			}
		});
		buttonPanel.add(button);

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				dialog.dispose();
			}
		});

		dialog.pack();
		dialog.setLocationRelativeTo(MainFrame.getInstance());
		dialog.setVisible(true);

	}

}
