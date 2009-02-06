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

package com.sun.enterprise.admin.cli.remote;

import com.sun.enterprise.cli.framework.CommandException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A generic class that invokes a given command with given set of options.
 *  Note that it is supposed to invoke remote commands only.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3
 */
final public class CommandInvoker {
    Map<String, String> options = new HashMap<String, String>();
    private final String cmdName;
    
    public CommandInvoker(String cmdName) {
        if (cmdName == null || cmdName.length() == 0) {
            throw new IllegalArgumentException();
        }
        this.cmdName = cmdName;
    }
    public void put(String name, String value) {
        //IMPORTANT: ignores the null names or null values.
        if (name != null && value != null)
            options.put(name, value);
    }
    
    public void invoke() throws CommandException {
        String[] args = asArray();
        CLIRemoteCommand rc = new CLIRemoteCommand(args);
        try {
            rc.runCommand();
        } catch(Exception e) {
            throw new CommandException(e);
        }
    }
    
    private String[] asArray() {
        List<String> line = new ArrayList<String>();
        line.add(cmdName);
        for (String on:options.keySet()) {
            line.add("--" + on);
            line.add(options.get(on));
        }
        return line.toArray(new String[0]);
    }
}
