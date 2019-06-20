package org.concord.energy3d.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.net.InetAddress;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.logger.SnapshotLogger;
import org.concord.energy3d.scene.Scene;

/**
 * @author Charles Xie
 */
public class DataCollector {

    public static void collect(final String info) {
        File file;
        try {
            file = SnapshotLogger.getInstance().saveSnapshot("model");
        } catch (final Exception ex) {
            ex.printStackTrace();
            file = null;
        }
        final File currentFile = file;
        EventQueue.invokeLater(() -> {
            final JPanel panel = new JPanel(new BorderLayout(10, 10));
            final JScrollPane scrollPane = new JScrollPane(new JTextArea(info));
            scrollPane.setPreferredSize(new Dimension(400, 400));
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(new JLabel("<html><b>Submit your data to the researchers now?</b></html>"), BorderLayout.SOUTH);
            if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), panel, "Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION) {
                new Uploader(info, currentFile).execute();
            }
        });
    }

    private static class Uploader extends SwingWorker<String, Void> {

        private final String text;
        private final File currentFile;

        Uploader(final String text, final File currentFile) {
            super();
            this.text = text;
            this.currentFile = currentFile;
        }

        @Override
        protected String doInBackground() throws Exception {
            return collect(text, currentFile);
        }

        @Override
        protected void done() {
            try {
                JOptionPane.showMessageDialog(MainFrame.getInstance(), get(), "Notice", JOptionPane.INFORMATION_MESSAGE);
            } catch (final Exception e) { // backup solution
                e.printStackTrace();
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
                JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html><h1>Data copied</h1>Please paste it in your email and send it to qxie@concord.org.</html>", "Noficiation", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private static String collect(final String data, final File currentFile) throws Exception {
        final MultipartUtility multipart = new MultipartUtility("http://energy3d.concord.org/data/collect.php", "UTF-8");
        multipart.addFormField("ip_address", InetAddress.getLocalHost().getHostAddress());
        multipart.addFormField("os_name", System.getProperty("os.name"));
        multipart.addFormField("user_name", System.getProperty("user.name"));
        multipart.addFormField("os_version", System.getProperty("os.version"));
        multipart.addFormField("energy3d_version", MainApplication.VERSION);
        multipart.addFormField("data", data);
        if (!Scene.isInternalFile() && currentFile != null) {
            multipart.addFilePart("current_file", currentFile);
        }
        return multipart.finish();
    }

}