package org.glassfish.enterprise.admin.ncli;

/**
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
final class Constants {

    final static private String[] PREVIOUS_COMMAND_NAMES = {"deploy", "version", }; //so on and so forth

    private Constants() {}//disallow

    static boolean representsOldCmd(String name) {
        for (String s : PREVIOUS_COMMAND_NAMES) {
            if (s.equals(name))
                return true;
        }
        return false;
    }
}
