package org.concord.energy3d.speech;

import javax.swing.SwingWorker;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class Talker {

	private static Talker instance = new Talker();
	private volatile boolean talking;
	private volatile SwingWorker<Object, Void> worker;
	private Voice voice = null;
	private Runnable completionCallback;

	public static Talker getInstance() {
		return instance;
	}

	private Talker() {
	}

	public void setCompletionCallback(final Runnable completionCallback) {
		this.completionCallback = completionCallback;
	}

	public boolean isTalking() {
		return talking;
	}

	public void interrupt() {
		if (talking && worker != null) {
			if (worker.cancel(true)) {
				if (voice != null) {
					voice.deallocate();
				}
				talking = false;
			}
		}
	}

	public void say(final String text) {
		interrupt();
		worker = new SwingWorker<Object, Void>() {
			@Override
			protected Object doInBackground() {
				talking = true;
				voice = VoiceManager.getInstance().getVoice("kevin16");
				voice.allocate();
				voice.speak(text);
				voice.deallocate();
				talking = false;
				return null;
			}

			@Override
			protected void done() {
				if (completionCallback != null) {
					completionCallback.run();
				}
			}
		};
		worker.execute();
	}

}