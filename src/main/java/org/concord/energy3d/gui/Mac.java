package org.concord.energy3d.gui;

import java.awt.Toolkit;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.OpenFilesEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

class Mac {

	public static void init() {

		final Application application = Application.getApplication();
		application.setDockIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("icons/icon.png")));

		application.setOpenFileHandler(new OpenFilesHandler() {
			@Override
			public void openFiles(final OpenFilesEvent e) {
				MainFrame.getInstance().open(e.getFiles().get(0).toString());
			}
		});

		application.setAboutHandler(new AboutHandler() {
			@Override
			public void handleAbout(final AboutEvent e) {
				MainFrame.getInstance().showAbout();
				;
			}
		});

		application.setPreferencesHandler(new PreferencesHandler() {
			@Override
			public void handlePreferences(final PreferencesEvent e) {
				MainFrame.getInstance().showPreferences();
			}
		});

		application.setQuitHandler(new QuitHandler() {
			@Override
			public void handleQuitRequestWith(final QuitEvent e, final QuitResponse r) {
				MainFrame.getInstance().exit();
				r.cancelQuit();
			}
		});

	}

}
