/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed.impl;

import com.sun.enterprise.v3.admin.CommandRunner;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.embed.EmbeddedException;
import org.glassfish.embed.Server;
import org.glassfish.embed.StringHelper;
import org.glassfish.embed.util.EmbeddedUtils;

/**
 *
 * @author bnevins
 */
public class EmbeddedCommandRunner extends CommandRunner{

    public ActionReport doCommand(
            final String commandName,
            final AdminCommand command,
            final Properties parameters,
            final ActionReport report,
            final List<File> uploadedFiles) {

        if(commandName == null) // Impossible???
            return super.doCommand(commandName, command, parameters, report, uploadedFiles);

        if(commandName.equals("deploy"))
            return doDeploy(parameters, report, uploadedFiles);

        return super.doCommand(commandName, command, parameters, report, uploadedFiles);
    }

    private ActionReport doDeploy(Properties parameters, ActionReport report,
                List<File> uploadedFiles) {

        if(uploadedFiles == null || uploadedFiles.isEmpty()) {
            report.setMessage(StringHelper.get("deploy_failed", "[No File Given]"));
            return EmbeddedUtils.fail(report);
        }

        File f = uploadedFiles.get(0);

        try {
            Server.get().deploy(f);
            report.setMessage(StringHelper.get("deploy_successful", f.getName()));
            return EmbeddedUtils.succeed(report);
        }
        catch (EmbeddedException e) {
            report.setMessage(StringHelper.get("deploy_failed", f.getName()));
            return EmbeddedUtils.fail(report, e);
        }
    }
}
