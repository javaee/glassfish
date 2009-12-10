/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.cli.util.*;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 * The help command will display the help text for all the commands and their
 * options
 */
@Service(name = "help")
@Scoped(PerLookup.class)
public class HelpCommand extends CLICommand {
    @Inject
    private Habitat habitat;

    private static final int DEFAULT_PAGE_LENGTH = 50;
    private static final int NO_PAGE_LENGTH = -1;
    private static final String DEFAULT_HELP_PAGE = "help";

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(HelpCommand.class);

    /**
     * The prepare method must ensure that the commandOpts,
     * operandType, operandMin, and operandMax fields are set.
     */
    @Override
    protected void prepare()
            throws CommandException, CommandValidationException {
        commandOpts = Collections.emptySet();
        operandName = "command-name";
        operandType = "STRING";
        operandMin = 0;
        operandMax = 1;
    }

    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {
	try {
            new More(getPageLength(),
                getSource(),
                getDestination(),
                getUserInput(),
                getUserOutput(),
                getQuitChar(),
                getPrompt());
	} catch (IOException ioe) {
	    throw new CommandException(ioe);
	}
        return 0;
    }

    private String getCommandName() {
	return (operands.size() > 0 ? operands.get(0) : DEFAULT_HELP_PAGE);
    }

    private Writer getDestination() {
	return new OutputStreamWriter(System.out);
    }

    private int getPageLength() {
      if (programOpts.isInteractive())
          return DEFAULT_PAGE_LENGTH;
      else
          return NO_PAGE_LENGTH;
    }

    private String getPrompt() {
        return strings.get("ManpagePrompt");
    }
 
    private String getQuitChar() {
        return strings.get("ManpageQuit");
    }

    private Reader getSource()
            throws CommandException, CommandValidationException {
        CLICommand cmd = CLICommand.getCommand(habitat, getCommandName());
        Reader r = cmd.getManPage();
        if (r == null)
            throw new CommandException(
                        strings.get("ManpageMissing", getCommandName()));
        return r;
    }

    private Reader getUserInput() {
	return new InputStreamReader(System.in);
    }

    private Writer getUserOutput() {
	return new OutputStreamWriter(System.err);
    }
}
