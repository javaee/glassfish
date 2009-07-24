/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.util;

/**
 * A simple class that fills a hole in the JDK.  It parses out the version numbers
 *  of the JDK we are running.
 * Example:<p>
 * 1.6.0_u14 == major = 1 minor = 6, subminor = 0, update = 14
 *
 * @author bnevins
 */
public final class JDK {

    private JDK() {
        // no instances allowed!
    }

    /**
     * See if the current JDK is legal for running GlassFish
     * @return true if the JDK is >= 1.6.0
     */
    public static boolean ok() {
        return major == 1 && minor >= 6;
    }

    public static int getMajor() {
        return major;
    }
    public static int getMinor() {
        return minor;
    }

    public static int getSubMinor() {
        return subminor;
    }

    public static int getUpdate() {
        return update;
    }

    /**
     * No instances are allowed so it is pointless to override toString
     * @return Parsed version numbers
     */
    public static String toStringStatic() {
        return "major: " + JDK.getMajor() + 
        "\nminor: " + JDK.getMinor() + 
        "\nsubminor: " + JDK.getSubMinor() +
        "\nupdate: " + JDK.getUpdate() +
        "\nOK ==>" + JDK.ok();
    }

    static {
        initialize();
    }

    // DO NOT initialize these variables.  You'll be sorry if you do!
    private static int major;
    private static int minor;
    private static int subminor;
    private static int update;

    // silently fall back to ridiculous defaults if something is crazily wrong...
    private static void initialize() {
        major = 1;
        minor = subminor = update = 0;
        try {
            String jv = System.getProperty("java.version");

            if(!StringUtils.ok(jv))
                return; // not likely!!

            String[] ss = jv.split("\\.");

            if(ss == null || ss.length < 3 || !ss[0].equals("1"))
                return;

            major = Integer.parseInt(ss[0]);
            minor = Integer.parseInt(ss[1]);
            ss = ss[2].split("_");

            if(ss == null || ss.length < 1)
                return;

            subminor = Integer.parseInt(ss[0]);

            if(ss.length > 1)
                update = Integer.parseInt(ss[1]);
        }
        catch(Exception e) {
            // ignore -- use defaults
        }
    }
}
