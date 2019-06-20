package org.concord.energy3d.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ExtensionFileFilter extends FileFilter {

    private String description;
    private final String[] extensions;

    ExtensionFileFilter(final String description, final String extension) {
        this(description, new String[]{extension});
    }

    private ExtensionFileFilter(final String description, final String[] extensions) {
        if (description == null) {
            this.description = extensions[0] + "{ " + extensions.length + "} ";
        } else {
            this.description = description;
        }
        this.extensions = extensions.clone();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean accept(final File file) {
        if (file.isDirectory()) {
            return true;
        } else {
            final String path = file.getAbsolutePath().toLowerCase();
            for (final String x : extensions) {
                if ((path.endsWith(x) && (path.charAt(path.length() - x.length() - 1)) == '.')) {
                    return true;
                }
            }
        }
        return false;
    }

}