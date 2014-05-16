package org.concord.energy3d.util;

import org.concord.energy3d.gui.MainFrame;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

public class Mac {
	public static void init() {
		@SuppressWarnings("deprecation")
		final Application application = new Application();
		application.setEnabledPreferencesMenu(true);
		application.addApplicationListener(new ApplicationAdapter() {
			@Override
			public void handleQuit(final ApplicationEvent e) {
				e.setHandled(true);
				MainFrame.getInstance().exit();
			}

			@Override
			public void handlePreferences(final ApplicationEvent e) {
				e.setHandled(true);
			}

			@Override
			public void handleAbout(final ApplicationEvent e) {
				MainFrame.getInstance().showAbout();
				e.setHandled(true);
			}

			@Override
			public void handleOpenFile(final ApplicationEvent e) {
				MainFrame.getInstance().open(e.getFilename());
				e.setHandled(true);
			}
		});
	}
}
