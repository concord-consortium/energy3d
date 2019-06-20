package org.concord.energy3d.util;

public class Config {

    public static final RenderMode RENDER_MODE = RenderMode.JOGL;
    private static boolean isWebstart;
    private static final boolean isMac = System.getProperty("os.name").toLowerCase().startsWith("mac");
    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("win");

    public enum RenderMode {
        NEWT, JOGL, LWJGL
    }

    public static void setWebStart(final boolean webstart) {
        isWebstart = webstart;
    }

    public static boolean isWebStart() {
        return isWebstart;
    }

    public static boolean isEclipse() {
        return "true".equalsIgnoreCase(System.getProperty("runInEclipse"));
    }

    public static boolean isMac() {
        return isMac;
    }

    public static boolean isWindows() {
        return isWindows;
    }

}