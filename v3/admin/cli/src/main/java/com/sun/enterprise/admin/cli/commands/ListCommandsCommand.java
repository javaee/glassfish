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

package com.sun.enterprise.admin.cli.commands;

import java.util.*;
import java.util.logging.*;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.cli.remote.*;
import com.sun.enterprise.cli.framework.ValidOption;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import static com.sun.enterprise.admin.cli.CLIConstants.EOL;

/**
 * A local ListCommands Command.
 *  
 * @author bnevins
 * @author Bill Shannon
 */
public class ListCommandsCommand extends RemoteCommand {
    String[] remoteCommands;
    String[] localCommands;
    boolean localOnly;
    boolean remoteOnly;
    private static final String SPACES = "                                                            ";
    private static final LocalStringsImpl strings =
            new LocalStringsImpl(ListCommandsCommand.class);

    public ListCommandsCommand(String name, ProgramOptions po,
            Environment env) throws CommandException {
        super(name, po, env);
    }

    @Override
    protected void fetchCommandMetadata() {
        /*
         * Don't fetch information from server.
         * We need to work even if server is down.
         * XXX - could "merge" options if server is up
         */
        Set<ValidOption> opts = new HashSet<ValidOption>();
        addOption(opts, "localonly", '\0', "BOOLEAN", false, "false");
        addOption(opts, "remoteonly", '\0', "BOOLEAN", false, "false");
        commandOpts = Collections.unmodifiableSet(opts);
        operandType = "STRING";
        operandMin = 0;
        operandMax = 0;
    }

    @Override
    protected void validate() throws CommandException {
        super.validate();
        localOnly = getBooleanOption("localonly");
        remoteOnly = getBooleanOption("remoteonly");
        
        if (localOnly && remoteOnly) {
            throw new CommandException(strings.get("listCommands.notBoth"));
        }
    }

    private boolean getBooleanOption(String name) {
        String val = options.get(name);
        return val != null && Boolean.parseBoolean(val);
    }

    @Override
    public int executeCommand() throws CommandException {
        // throw away all arguments and options for remote
        argv = new String[] { argv[0] };
        options = new HashMap<String, String>();
        operands = Collections.emptyList();
 
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

    String[] getLocalCommands() throws CommandException {
        List<String> names = new ArrayList<String>();

        names.addAll(new CommandTable().keySet());
        localCommands = names.toArray(new String[names.size()]);
        Arrays.sort(localCommands);
        return localCommands;
    }

    String[] getRemoteCommands() throws CommandException {
        /*
         * XXX - This is very disgusting.
         * We increase the logging level such that normal command
         * output is suppressed.  Then, after running the remote
         * command, we retrieve the results of the command from
         * the "back channel" that is "mainAtts", which comes from
         * the manifest returned from the server.
         * XXX - Like I said, this is very disgusting.
         */
        int ret = -1;
        try {
            logger.pushAndLockLevel(Level.WARNING);
            ret = super.executeCommand();
        } catch (Exception e) {
            throw new CommandException(
                strings.get("LocalRemoteCommand.errorRemote", e.getMessage()));
        } finally {
            logger.popAndUnlockLevel();
        }

        if (ret == 0) {
            // throw away everything but the main atts
            Map<String,String> mainAtts = this.getMainAtts();
            String cmds = mainAtts.get("children");
            remoteCommands = cmds.split(";");
        }
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
