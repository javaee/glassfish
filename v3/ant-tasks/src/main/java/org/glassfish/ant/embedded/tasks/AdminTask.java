/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.ant.embedded.tasks;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;

import org.glassfish.api.embedded.Server;
import org.glassfish.api.ActionReport;
import com.sun.enterprise.admin.cli.Parser;
import com.sun.enterprise.admin.cli.ArgumentTokenizer;

import java.util.*;

public class AdminTask extends TaskBase {

    String serverID = Constants.DEFAULT_SERVER_ID;
    String command, commandLine;
    CommandProperty commandProperty;
    List<CommandProperty> commandProperties = new ArrayList<CommandProperty>();

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public void setCommand(String command) {
        this.command = command;
    }


    public void setCommandLine(String cmdLine) {
        this.commandLine = cmdLine;
    }

    public CommandProperty createCommandProperty() {
        commandProperty = new CommandProperty();
        commandProperties.add(commandProperty);
        return commandProperty;
    }
  
    public CommandProperty createCommandProperty(String name, String value) {
        CommandProperty property = new CommandProperty();
        property.setName(name);
        return property;
    }

    private ParameterMap getCommandParameters() {
        ParameterMap params = new ParameterMap();
        for (CommandProperty property : commandProperties) {
            params.set(property.getName(), property.getValue());
        }
        return params;
    }



    public void execute() throws BuildException {
        if (command == null && commandLine == null) {
            error("Either command or commandLine should be specified");
            return;
        }
        Server server = Server.getServer(serverID);
        if (server == null) {
           error("Embedded Server [" + serverID + "] not running");
           return;
        }
        CommandRunner runner = server.getHabitat().getComponent(CommandRunner.class);
        ActionReport report = server.getHabitat().getComponent(ActionReport.class);

        if (commandLine != null) {
            try {
            log("executing admin task[serverID=" + serverID + "] : " + commandLine);
                String args[] = getArgs(commandLine);
                if (args.length == 0) {
                   error("admin command not specified");
                   return;
                }
                if (args[0].equalsIgnoreCase("set") && args.length > 1) {
                    commandLine = args[0] + " --values = " + commandLine.substring(args[0].length() + 1);
                    args = getArgs(commandLine);
                }
                Parser parser = new Parser(args, 1,  null, true);
                ParameterMap pMap = parser.getOptions();
                pMap.set("DEFAULT", parser.getOperands());
                runner.getCommandInvocation(args[0], report).parameters(pMap).execute();
                if (report.hasFailures())
                    throw new Exception(report.getFailureCause());

                if (report.hasWarnings())
                    System.out.println(report.getMessage());

                System.out.println("executed : " + commandLine);
            } catch (Exception ex) {
                error(ex);
            }
        }
        else {
            log("executing admin command: " + command + " serverID = " + serverID);
            runner.getCommandInvocation(command, report).
		parameters(getCommandParameters()).execute();
        }
        log("admin task executed");
    }

    private String[] getArgs(String line)
                                throws ArgumentTokenizer.ArgumentException {
        List<String> args = new ArrayList<String>();
        ArgumentTokenizer t = new ArgumentTokenizer(line);
        while (t.hasMoreTokens()) {
            args.add(t.nextToken());
        }
        return args.toArray(new String[args.size()]);
    }

    public class CommandProperty  {
        String name, value;

        public void setName(String name) {
            this.name = name;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }


    }

}
