/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *   Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 *   The contents of this file are subject to the terms of either the GNU
 *   General Public License Version 2 only ("GPL") or the Common Development
 *   and Distribution License("CDDL") (collectively, the "License").  You
 *   may not use this file except in compliance with the License. You can obtain
 *   a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 *   or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 *   language governing permissions and limitations under the License.
 *
 *   When distributing the software, include this License Header Notice in each
 *   file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *   Sun designates this particular file as subject to the "Classpath" exception
 *   as provided by Sun in the GPL Version 2 section of the License file that
 *   accompanied this code.  If applicable, add the following below the License
 *   Header, with the fields enclosed by brackets [] replaced by your own
 *   identifying information: "Portions Copyrighted [year]
 *   [name of copyright owner]"
 *
 *   Contributor(s):
 *
 *   If you wish your version of this file to be governed by only the CDDL or
 *   only the GPL Version 2, indicate your decision by adding "[Contributor]
 *   elects to include this software in this distribution under the [CDDL or GPL
 *   Version 2] license."  If you don't indicate a single choice of license, a
 *   recipient has the option to distribute your version of this file under
 *   either the CDDL, the GPL Version 2 or to extend the choice of license to
 *   its licensees as provided above.  However, if you add GPL Version 2 code
 *   and therefore, elected the GPL Version 2 license, then the option applies
 *   only if the new code is made subject to such option by the copyright
 *   holder.
 */

package org.glassfish.enterprise.admin.ncli;

import org.glassfish.enterprise.admin.ncli.metadata.CommandDesc;
import org.glassfish.enterprise.admin.ncli.comm.TargetServer;

import java.util.Arrays;


/**  The main class that is designed to work on arguments that are presented on a <i> command line </i>.
 *   It is designed for remote commands as of now.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since  GlassFish v3 (September 2009)
 */
public class RemoteCommandDriver {
    private final String[] args;
    private final Parser parser;
    private final CommandMetadataCache cache;
    
    public RemoteCommandDriver(String[] args) {
        this.args = args;
        parser = new Parser(args);
        cache  = new CommandMetadataCache();
    }

    public static void main(String... args) throws ParserException, GenericCommandException {
        new RemoteCommandDriver(args).run();
    }

    // ALL Private
    
    private void run() throws ParserException, GenericCommandException {
        FirstPassResult fpr = parser.firstPass();
        String cmdName      = fpr.getCommandName();
        TargetServer ts     = fpr.getTargetServer();
        debug(fpr);
        CommandDesc desc    = cache.get(cmdName, ts);

        if (desc == null)
            desc = getCommandMetadata(cmdName, fpr.getTargetServer()); //goes to server

        SecondPassResult spr = parser.secondPass(desc, fpr.getCommandArguments());
        NewCommand command = spr.getFinalCommand();
        //at this point, there are no syntax errors, server is running and command is fully formed
        //now, only command execution errors can occur
        assert command != null : "Command is null!";
        CommandExecutionResult er = command.execute(ts);
        cache.put(cmdName, ts, desc);
    }

    private static void debug(FirstPassResult fpr) {
        //TODO remove this method
        out("First Pass is Successful");
        out("****Command uses NEW Syntax? " + !fpr.usesDeprecatedSyntax());
        out("Explicit/Default asadmin program options:");
        for (Option op : fpr.getProgramOptions()) {
            out("name: " + op.getName());
            out("effective value: " + op.getEffectiveValue());
        }
        out("CmdName: " + fpr.getCommandName());
        out("Command Options/Operands:" + Arrays.toString(fpr.getCommandArguments()));

    }

    private static void out(String s) {
        System.out.println(s);
    }

    private CommandDesc getCommandMetadata(String cmdName, TargetServer from) {
        //TODO
        return null;
    }
}