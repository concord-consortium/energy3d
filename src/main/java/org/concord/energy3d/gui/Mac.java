package org.concord.energy3d.gui;

import com.apple.eawt.Application;
import org.concord.energy3d.MainApplication;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.BugReporter;

import java.awt.*;
import java.io.File;

public class Mac {

    public static void init() {

        final Application application = Application.getApplication();
        application.setDockIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("icons/icon.png")));

        application.setOpenFileHandler(e -> {
            MainApplication.isMacOpeningFile = true;
            SceneManager.getTaskManager().update(() -> {
                // somehow newFile() must be called to set up the scene before we can correctly load the content when an NG3 file is double-clicked without an open instance
                if (Scene.getURL() == null) {
                    Scene.newFile(false);
                }
                try {
                    Scene.open(new File(e.getFiles().get(0).toString()).toURI().toURL());
                } catch (final Throwable err) {
                    BugReporter.report(err, e.getFiles().get(0).toString());
                }
                return null;
            });
        });

        application.setAboutHandler(e -> MainFrame.getInstance().showAbout());

        application.setPreferencesHandler(e -> MainFrame.getInstance().showPreferences());

        application.setQuitHandler((e, r) -> {
            MainFrame.getInstance().exit();
            r.cancelQuit();
        });

    }

    public static void bringToFront() {
        Application.getApplication().requestForeground(true);
    }

}