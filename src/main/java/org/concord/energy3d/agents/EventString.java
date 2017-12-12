package org.concord.energy3d.agents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
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
		setPreferredSize(new Dimension(400, 300));
		setLayout(new BorderLayout());

		html = new JEditorPane();
		html.setBackground(Color.WHITE);
		html.setContentType("text/html");
		add(html, BorderLayout.CENTER);

		eventString = EventUtil.eventsToString(EventMinerSheet2.observers, 10000, null);
		String text = "<html>";
		final int columns = 20;
		for (int i = 0; i < eventString.length(); i++) {
			if (i != 0 && i % columns == 0) {
				text += "<br>";
			}
			final char c = eventString.charAt(i);
			switch (c) {
			case 'A':
				text += "<span style=\"background-color: #99aa33\"><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font></span>";
				break;
			case 'Y':
				text += "<span style=\"background-color: #9933aa\"><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font></span>";
				break;
			case 'C':
				text += "<span style=\"background-color: #339922\"><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font></span>";
				break;
			case 'D':
				text += "<span style=\"background-color: #aa9922\"><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font></span>";
				break;
			case 'L':
				text += "<span style=\"background-color: #22aa77\"><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font></span>";
				break;
			case 'P':
				text += "<span style=\"background-color: #77cc77\"><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font></span>";
				break;
			case 'T':
				text += "<span style=\"background-color: #2277aa\"><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font></span>";
				break;
			case 'W':
				text += "<span style=\"background-color: #226699\"><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font></span>";
				break;
			case 'Z':
				text += "<span style=\"background-color: #aa16a9\"><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font></span>";
				break;
			case '#':
				text += "<span style=\"background-color: #ff3322\"><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font></span>";
				break;
			case '_':
				text += "<span style=\"background-color: #dddddd\"><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font></span>";
				break;
			default:
				text += "<span style=\"background-color: #333333\"><font size=3 face=\"Courier New\" color=#ffffff>" + c + "</font></span>";
			}
		}
		text += "</html>";
		html.setText(text);

	}

	public void showGui() {

		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Event String", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		final JPanel contentPane = new JPanel(new BorderLayout());
		dialog.setContentPane(contentPane);

		final JMenuBar menuBar = new JMenuBar();
		dialog.setJMenuBar(menuBar);

		final JMenu menu = new JMenu("Export");
		menuBar.add(menu);

		JMenuItem mi = new JMenuItem("Copy Colored String");
		mi.addActionListener(new ActionListener() {
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
		menu.add(mi);

		mi = new JMenuItem("Copy Plain String");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(eventString), null);
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "The string is now ready for pasting.", "Copy String", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		menu.add(mi);

		mi = new JMenuItem("Copy Image");
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

		final JButton button = new JButton("Close");
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
