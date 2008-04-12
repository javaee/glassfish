/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.admin.cli;

import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import java.io.*;

public class IntroductionCommand extends S1ASCommand {

    public void runCommand() throws CommandException, CommandValidationException {
        try {
            validateOptions();
            printHeader();
            printCommands();
            System.out.println("");
        }
        catch (IOException ex) {
            throw new CommandException("Internal Error: " + ex);
        }
    }
    public void printHeader() throws IOException{
        BufferedReader reader = new BufferedReader(
                new CLIManFileFinder().getCommandManFile("introduction-header"));

        String s;
        while ((s = reader.readLine()) != null) {
            System.out.println(s);
        }
    }

    private void printCommands() throws CommandValidationException {

        ListCommandsCommand lc = new ListCommandsCommand();
        lc.getLocalCommands();
        lc.printLocalCommands();

        try {
            lc.getRemoteCommands();
            lc.printRemoteCommands();
        }
        catch (Exception e) {
            // DAS is not up - ignore and print local commands
            CLILogger.getInstance().printMessage(strings.get("introduction.noremote"));
        }
        
    }
    private final static LocalStringsImpl strings = new LocalStringsImpl(IntroductionCommand.class);
}
