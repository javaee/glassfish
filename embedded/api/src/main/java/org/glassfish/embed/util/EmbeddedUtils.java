/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed.util;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;

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

    private EmbeddedUtils() {
    }
}
