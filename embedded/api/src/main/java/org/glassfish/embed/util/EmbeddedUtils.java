/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed.util;

import com.sun.enterprise.universal.io.SmartFile;
import java.io.*;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.embed.EmbeddedException;

/**
 * These methods are NOT part of the Embedded GlassFish API.  They are <i>used</i> by the API.
 * They have to be public because there is no concept of hierarchical package
 * privileges in Java yet.  We don't want to pollute the embedded package with this
 * sort of code.
 *
 * @author Byron Nevins
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

    /**
     * Return a separate copy of the given File with a sanitized path <b>and</b>
     * the file must exist.
     *
     * @param f The File to copy
     * @return
     * @throws EmbeddedException if the File does not exist.
     */
    public static File cloneAndVerifyFile(File f) throws EmbeddedException {
        File cleaned = cloneFile(f);

        if(!cleaned.exists())
            throw new EmbeddedException("no_such_file", f);

        return cleaned;
    }
    /**
     * Return a separate copy of the given File with a sanitized path.
     *
     * @param f The File to copy
     * @return
     * @throws EmbeddedException if the File argument is null
     */
    public static File cloneFile(File f) throws EmbeddedException {
        if(f == null)
            throw new EmbeddedException("null_arg", "EmbeddedUtils.cloneFile(File f)");

        File parent = f.getParentFile();
        String filename = f.getName();
        File cleaned = null;

        if(parent != null)
            cleaned = SmartFile.sanitize(new File(parent, filename));
        else
            cleaned = SmartFile.sanitize(new File(filename));

        return cleaned;
    }

    private EmbeddedUtils() {
    }
}
