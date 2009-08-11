/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.sun.enterprise.admin.cli;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.cli.remote.*;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import static com.sun.enterprise.admin.cli.CLIConstants.EOL;

/**
 * A local ListCommands Command.
 *  
 * @author bnevins
 * @author Bill Shannon
 */
public class ListCommandsCommand extends CLICommand {
    String[] remoteCommands;
    String[] localCommands;
    boolean localOnly;
    boolean remoteOnly;
    private static final String SPACES = "                                                            ";
    private static final LocalStringsImpl strings =
            new LocalStringsImpl(ListCommandsCommand.class);

    public ListCommandsCommand(String name, ProgramOptions programOpts,
            Environment env) throws CommandException {
        super(name, programOpts, env);
    }

    @Override
    protected void prepare()
            throws CommandException, CommandValidationException {
        /*
         * Don't fetch information from server.
         * We need to work even if server is down.
         * XXX - could "merge" options if server is up
         */
        Set<ValidOption> opts = new LinkedHashSet<ValidOption>();
        addOption(opts, "localonly", '\0', "BOOLEAN", false, "false");
        addOption(opts, "remoteonly", '\0', "BOOLEAN", false, "false");
        addOption(opts, "help", '?', "BOOLEAN", false, "false");
        commandOpts = Collections.unmodifiableSet(opts);
        operandType = "STRING";
        operandMin = 0;
        operandMax = 0;

        processProgramOptions();
    }

    @Override
    protected void validate()
            throws CommandException, CommandValidationException {
        super.validate();
        localOnly = getBooleanOption("localonly");
        remoteOnly = getBooleanOption("remoteonly");
        
        if (localOnly && remoteOnly) {
            throw new CommandException(strings.get("listCommands.notBoth"));
        }
    }

    @Override
    public int executeCommand()
            throws CommandException, CommandValidationException {
        if (!remoteOnly) {
            getLocalCommands();
            printLocalCommands();
        }
        if (!localOnly) {
            getRemoteCommands();
            printRemoteCommands();
        }
        logger.printMessage("");
        return 0;
    }

    // XXX - public because AsadminMain uses it
    public String[] getLocalCommands() throws CommandException {
        List<String> names = new ArrayList<String>();

        names.addAll(new CommandTable().keySet());
        localCommands = names.toArray(new String[names.size()]);
        Arrays.sort(localCommands);
        return localCommands;
    }

    /**
     * Get the list of commands from the remote server.
     *
     * @return the commands as a String array, sorted
     */
    // XXX - public because AsadminMain uses it
    public String[] getRemoteCommands()
            throws CommandException, CommandValidationException {
        RemoteCommand cmd =
            new RemoteCommand("list-commands", programOpts, env);
        String cmds = cmd.executeAndReturnOutput("list-commands");
        List<String> rcmds = new ArrayList<String>();
        BufferedReader r = new BufferedReader(new StringReader(cmds));
        String line;

        /*
         * The output of the remote list-commands command is a bunch of
         * lines of the form:
         * Command : cmd-name
         * We extract the command name from each such line.
         * XXX - depending on this output format is gross;
         * should be able to send --terse to remote command
         * to cause it to produce exactly the output we want.
         */
        try {
            while ((line = r.readLine()) != null) {
                if (line.startsWith("Command")) {
                    int i = line.indexOf(':');
                    if (i < 0)
                        continue;
                    String s = line.substring(i + 1).trim();
                    rcmds.add(s);
                }
            }
        } catch (IOException ioex) {
            // ignore it, will never happen
        }
        Collections.sort(rcmds);
        remoteCommands = rcmds.toArray(new String[rcmds.size()]);
        return remoteCommands;
    }

    void printLocalCommands() {
        logger.printMessage("********** Local Commands **********");
        
        for (String s : localCommands) {
            logger.printMessage(s);
        }
    }

    void printRemoteCommands() {
        logger.printMessage("********** Remote Commands **********");
        
        // there are a LOT of remote commands -- make 2 columns
        int num = remoteCommands.length;
        int offset = (num / 2) + (num % 2);
        StringBuilder sb = new StringBuilder();
 
        for (int i = 0; i < offset; i++) {
            sb.append(remoteCommands[i]);
            sb.append(justify(remoteCommands[i], 40));
            if (i + offset < num) {
                sb.append(remoteCommands[i + offset]);
            }
            if (i < offset - 1)
                sb.append(EOL);
        }
        logger.printMessage(sb.toString());
    }
 
    private String justify(String s, int width) {
        int numSpaces = width - s.length();
        return SPACES.substring(0, numSpaces);
    }
}
