package org.concord.energy3d.speech;

import java.awt.EventQueue;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.concord.energy3d.gui.MainFrame;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class Talker {

	public static void say(final String text) {
		new SwingWorker<Object, Void>() {
			@Override
			protected Object doInBackground() {
				try {
					final VoiceManager vm = VoiceManager.getInstance();
					final Voice voice = vm.getVoice("kevin16");
					voice.allocate();
					voice.speak(text);
					voice.deallocate();
				} catch (final Throwable t) {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "Error: " + t.getLocalizedMessage());
						}
					});
				}
				return null;
			}

			@Override
			protected void done() {
			}
		}.execute();
	}

}