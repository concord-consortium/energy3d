package org.concord.energy3d.logger;

import java.io.File;
import java.util.Comparator;

/**
 * This is a comparator for file names that do not include their extensions, conforming to the behavior on the OS. For example, "x.ng3" should be before "x-1.ng3" (the default is after).
 *
 * @author Charles Xie
 */

public class FileComparator implements Comparator<File> {

    @Override
    public int compare(File o1, File o2) {
        String o1Name = o1.getName();
        int lastDot = o1Name.lastIndexOf('.');
        o1Name = o1Name.substring(0, lastDot);
        String o2Name = o2.getName();
        lastDot = o2Name.lastIndexOf('.');
        o2Name = o2Name.substring(0, lastDot);
        return o1Name.compareTo(o2Name);
    }

}