package org.concord.energy3d.gui;

import java.awt.Toolkit;
import java.io.File;
import java.util.concurrent.Callable;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.BugReporter;

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

public class Mac {

	public static void init() {

		final Application application = Application.getApplication();
		application.setDockIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("icons/icon.png")));

		application.setOpenFileHandler(new OpenFilesHandler() {
			@Override
			public void openFiles(final OpenFilesEvent e) {
				MainApplication.isMacOpeningFile = true;
				SceneManager.getTaskManager().update(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						try {
							Scene.open(new File(e.getFiles().get(0).toString()).toURI().toURL());
						} catch (final Throwable err) {
							BugReporter.report(err, e.getFiles().get(0).toString());
						}
						return null;
					}
				});
			}
		});

		application.setAboutHandler(new AboutHandler() {
			@Override
			public void handleAbout(final AboutEvent e) {
				MainFrame.getInstance().showAbout();
			}
		});

		application.setPreferencesHandler(new PreferencesHandler() {
			@Override
			public void handlePreferences(final PreferencesEvent e) {
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

	public static void bringToFront() {
		Application.getApplication().requestForeground(true);
	}

}
