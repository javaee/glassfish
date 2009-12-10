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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.module.common_impl.Tokenizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.Inhabitants;

/**
 * Simple admin command to list all existing commands.
 *
 * @author Jerome Dochez
 * 
 */
@Service(name="list-commands")
public class ListCommandsCommand implements AdminCommand {

    private static final String DEBUG_PAIR = "mode=debug";
    @Inject
    Habitat habitat;


    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {

        context.getActionReport().setActionExitCode(ActionReport.ExitCode.SUCCESS);
        ActionReport report = context.getActionReport();
        report.setMessage("List of Commands");
        report.getTopMessagePart().setChildrenType("Command");
        for (String name : sortedAdminCommands()) {
            ActionReport.MessagePart part = report.getTopMessagePart().addChild();
            part.setMessage(name);
        }
    }
    
    private List<String> sortedAdminCommands() {
        List<String> names = new ArrayList<String>();
        for (Inhabitant<?> command : habitat.getInhabitantsByContract(AdminCommand.class.getName())) {
            for (String name : Inhabitants.getNamesFor(command, AdminCommand.class.getName())) {
                //see 6161 -- I thought we should ensure that a command found in habitat should
                //return a valid Command Object, but it was decided that we don't need to instantiate
                //each object satisfying AdminCommand contract to get a list of all commands
                if (debugCommand(command)) { //it's a debug command, add only if debug is set
                    if (debugSet())
                        names.add(name);
                } else { //always add non-debug commands     \
                    names.add(name);
                }
            }
        }
        Collections.sort(names);
        return (names);
    }
    
    private static boolean debugCommand(Inhabitant command) {
        return !Inhabitants.getNamesFor(command, "mode").isEmpty();
    }
    
    private static boolean metadataContains(String md, String nev) {
        boolean contains = false;
        Tokenizer st = new Tokenizer(md, ","); //TODO
        for (String pair : st) {
            if (pair.trim().equals(nev)) {
                contains = true;
                break;
            }
        }
        return ( contains );
    }
    
    private static boolean debugSet() { //TODO take into a/c debug-enabled?
        String s = System.getenv("AS_DEBUG");
        return ( Boolean.valueOf(s) );
    }
}
