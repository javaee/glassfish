
package com.sun.enterprise.universal.io;

import java.io.*;

/**
 * static utility methods
 * @author bnevins
 */
public class FileUtils {
    private FileUtils() {
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[4096];
        int len;
        while ((len = in.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }
        out.close();
    }
}
