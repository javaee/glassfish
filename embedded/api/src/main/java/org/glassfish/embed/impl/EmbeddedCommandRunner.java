/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed.impl;

import com.sun.enterprise.v3.admin.CommandRunner;
import java.io.*;
import java.util.*;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;

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

        System.out.println("CommandName = " + commandName);
        return super.doCommand(commandName, command, parameters, report, uploadedFiles);
    }

}
