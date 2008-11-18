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
    void succeed(ActionReport ar) {
        ar.setActionExitCode(ExitCode.SUCCESS);
    }
    void succeed(ActionReport ar, String msg) {
        ar.setActionExitCode(ExitCode.SUCCESS);
        ar.setMessage(msg);
    }

    void fail(ActionReport ar) {
        ar.setActionExitCode(ExitCode.FAILURE);
    }

    void fail(ActionReport ar, Exception e) {
        ar.setActionExitCode(ExitCode.FAILURE);
        ar.setFailureCause(e);
    }
}
