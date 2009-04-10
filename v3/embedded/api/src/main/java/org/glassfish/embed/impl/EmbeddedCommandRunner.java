/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed.impl;

import com.sun.enterprise.v3.admin.CommandRunnerImpl;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.embed.EmbeddedException;
import org.glassfish.embed.EmbeddedInfo;
import org.glassfish.embed.Server;
import org.glassfish.embed.util.StringHelper;
import org.glassfish.embed.util.LoggerHelper;
import org.glassfish.embed.util.EmbeddedUtils;
import org.glassfish.api.admin.Payload;

/**
 *
 * @author Byron Nevins
 */
public class EmbeddedCommandRunner extends CommandRunnerImpl{

    public ActionReport doCommand(
            final String commandName,
            final AdminCommand command,
            final Properties parameters,
            final ActionReport report,
            final Payload.Inbound inboundPayload,
            final Payload.Outbound outboundPayload) {

        if(commandName == null) // Impossible???
            return super.doCommand(commandName, command, parameters, report, inboundPayload, outboundPayload);

        if(commandName.equals("stop-domain")) {
            System.out.println("Stopping Embedded Server");
            //LoggerHelper.info("cli stop-domain command does  System.exit().  Goodbye!");
            //System.exit(0);
        }

        return super.doCommand(commandName, command, parameters, report, inboundPayload, outboundPayload);
    }

    private ActionReport doDeploy(Properties parameters, ActionReport report,
                List<File> uploadedFiles) {

        if(uploadedFiles == null || uploadedFiles.isEmpty()) {
            report.setMessage(StringHelper.get("deploy_failed", "[No File Given]"));
            return EmbeddedUtils.fail(report);
        }

        File f = uploadedFiles.get(0);
        System.out.println(parameters);

		//
		// todo 
		// The "server" name is probably in parameters.  So we could deploy to
		// a server based on its name.
		// for now we support "server"

        try {
            String serverName = parameters.getProperty("serverName");
            if (serverName == null) {
                serverName = "server";
            }
            Server server = Server.getServer(serverName);

			if(server == null) {
				EmbeddedInfo info = new EmbeddedInfo();
				info.setServerName(serverName);
				server = new Server(info);
			}
            server.getDeployer().deploy(f);
            report.setMessage(StringHelper.get("deploy_successful", f.getName()));
            return EmbeddedUtils.succeed(report);
        }
        catch (EmbeddedException e) {
            report.setMessage(StringHelper.get("deploy_failed", f.getName()));
            return EmbeddedUtils.fail(report, e);
        }
    }
}
