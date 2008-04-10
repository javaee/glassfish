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
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;

/**
 * Simple admin command to list all existing commands.
 *
 * @author Jerome Dochez
 * 
 */
@Service(name="list-commands")
@Scoped(PerLookup.class)
@I18n("list.commands")

public class ListCommandsCommand implements AdminCommand {

    private static final String DEBUG_PAIR = "mode=debug";
    @Inject
    Habitat habitat;

    @Param
    Boolean verbose = Boolean.FALSE;

    @Param
    Boolean debug = Boolean.TRUE;

    @Param
    Boolean xyz;
    
    @Param(defaultValue="true")
    Boolean qbert;
    
    @Param
    Boolean noDefValue = true;
    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        setDebug();
        context.getActionReport().setActionExitCode(ActionReport.ExitCode.SUCCESS);
        ActionReport report = context.getActionReport();
        report.setMessage("List of Commands XYZ = " + xyz );
        report.getTopMessagePart().setChildrenType("Command");
        setAdminCommands();
        sortAdminCommands();
        
        for (AdminCommand cmd : cmds) {
            if(!debug && debugCommand(cmd)) {
                continue;
            }
            String name = cmd.getClass().getAnnotation(Service.class).name();
            ActionReport.MessagePart part = report.getTopMessagePart().addChild();
            part.setMessage(name);
            if(verbose) {
                part.addProperty("Class", cmd.getClass().getName());
                Annotation[] anns = cmd.getClass().getDeclaredAnnotations();
                for(int i = 0; i < anns.length; i++) {
                    Annotation ann = anns[i]; 
                    part.addProperty("Annotation " + i, ann.toString());
                }
            }
        }
    }
    
    private static boolean debugCommand(AdminCommand command) {
        String metadata = command.getClass().getAnnotation(Service.class).metadata();
        boolean dc = false;
        if (metadata != null) {
            if (metadataContains(metadata, DEBUG_PAIR)) {
                dc = true;
            }
        }
        return ( dc );
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
    
    private void setDebug() { //TODO take into a/c debug-enabled?
        // debug is set if either or both the param is true or AS_DEBUG is true
        String s = System.getenv("AS_DEBUG");
        debug = debug || Boolean.valueOf(s);
    }

    private void setAdminCommands() {
        cmds = new ArrayList<AdminCommand>();
        for (AdminCommand command : habitat.getAllByContract(AdminCommand.class)) {
            cmds.add(command);
        }
    }

    private void sortAdminCommands() {
        Collections.sort(cmds, new Comparator<AdminCommand>() {
            public int compare(AdminCommand c1, AdminCommand c2) {
                String name1 = c1.getClass().getAnnotation(Service.class).name();
                String name2 = c2.getClass().getAnnotation(Service.class).name();
                return name1.compareTo(name2);
            }
        }
        );
    }
    
    private List<AdminCommand> cmds;
}
