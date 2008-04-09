
package com.sun.enterprise.universal.io;

import java.io.*;

/**
 *
 * @author bnevins
 */
public class FileUtils {
    private FileUtils() {
    }

    public static void liquidate(File parent) {
        if (!parent.isDirectory()) {
            return;
        }

        File[] kids = parent.listFiles();

        for (File kid : kids) {
            if (kid.isDirectory())
                liquidate(kid);
            else {
                if(!kid.delete()) {
                    kid.deleteOnExit();
                }
            }
        }
        if(!parent.delete())
            parent.deleteOnExit();
    }
}
