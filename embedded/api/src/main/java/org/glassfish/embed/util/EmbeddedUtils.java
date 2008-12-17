/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed.util;

import java.io.*;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.embed.EmbeddedException;

/**
 *
 * @author bnevins
 */
public class EmbeddedUtils {
    // this is too ghastly to type in all the time...
    public static ActionReport succeed(ActionReport ar) {
        ar.setActionExitCode(ExitCode.SUCCESS);
        return ar;
    }

    public static ActionReport fail(ActionReport ar) {
        ar.setActionExitCode(ExitCode.FAILURE);
        return ar;
    }

    public static ActionReport fail(ActionReport ar, Exception e) {
        ar.setActionExitCode(ExitCode.FAILURE);
        ar.setFailureCause(e);
        return ar;
    }

    public static ActionReport message(ActionReport ar, String msg) {
        ar.setMessage(msg);
        return ar;
    }

    /*
     * java.io.File.mkdirs returns true if it created the dir but returns false
     * if it can't create it OR if it already exists
     * We need slightly different behavior.
     * return true if the dir now exists.  return false if it can not be created.
     */

    public static boolean mkdirsIfNotExist(File f) {
        if(f == null)
            return false;

        f.mkdirs();

        return f.isDirectory();
    }

    private EmbeddedUtils() {
    }
}
