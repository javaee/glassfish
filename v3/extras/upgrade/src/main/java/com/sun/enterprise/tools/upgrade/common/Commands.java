/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * Commands.java
 *
 * Created on June 7, 2004, 2:41 PM
 */

package com.sun.enterprise.tools.upgrade.common;

import java.io.*;
import java.util.ArrayList;

//import com.sun.enterprise.cli.framework.CLIMain;
import com.sun.enterprise.admin.cli.AsadminMain;
import com.sun.enterprise.cli.framework.InputsAndOutputs;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.util.i18n.StringManager;

/**
 *
 * @author  hans
 */
public class Commands {
    private static StringManager stringManager = StringManager.getManager(Commands.class);
    
    /** Creates a new instance of Commands */
    public Commands() {
    }
    
    public static boolean startDomain(String domainName, CommonInfoModel commonInfo) {
        Credentials c = commonInfo.getSource().getDomainCredentials();
		String adminUser = c.getAdminUserName();

        ArrayList<String> tmpC = new ArrayList<String>();
        tmpC.add("start-domain");
        tmpC.add("--domaindir");
        tmpC.add(commonInfo.getTarget().getInstallDir());

        //- V3 allows for anonymous user credentials. skip passing credentials
        if (adminUser != null && adminUser.length() > 0){
            tmpC.add("--user");
            tmpC.add(adminUser);
            String adminPassword = c.getAdminPassword();
            if(adminPassword != null && adminPassword.length() > 0){
                tmpC.add("--passwordfile ");
                tmpC.add(c.getPasswordFile());
            }
        }
        tmpC.add(domainName);

        String command[] = new String[tmpC.size()];
        command = tmpC.toArray(command);
        return executeCommand(command);
        
    }
    
    public static boolean stopDomain(String domainName, CommonInfoModel commonInfo) {
		String command[] = {"stop-domain", "--domaindir",  
				commonInfo.getTarget().getInstallDir(),  domainName
        };	
		
        return executeCommand(command);
    }
    
    public static boolean executeCommand(String commandStrings[]){
        try {
            StringBuffer commandOneString = new StringBuffer();
            for(int i = 0; i < commandStrings.length; i++) {
                commandOneString.append(commandStrings[i]).append(" ");
            }			
            InputsAndOutputs io = InputsAndOutputs.getInstance();
            PipedOutputStream pos = new PipedOutputStream();
            io.setErrorOutput(pos);
            io.setUserOutput(pos);
            CommandOutputReader cor = new CommandOutputReader(pos);
            ((Thread)cor).start();
            CommonInfoModel.getDefaultLogger().info(stringManager.
                    getString("commands.executingCommandMsg") + commandOneString);
            com.sun.enterprise.admin.cli.AsadminMain.main(commandStrings);
            pos.flush();
            return true;
        }
        catch(Exception e) {
            Throwable t = e.getCause();
            CommonInfoModel.getDefaultLogger().warning(stringManager.getString(
				"upgrade.common.general_exception") + (t==null?e.getMessage():t.getMessage()));
        }
        return false;
    }
    
    
    static class CommandOutputReader extends Thread {
        PipedInputStream pis = new PipedInputStream();
        public CommandOutputReader(PipedOutputStream pout) throws IOException {
            pis.connect(pout);
        }
        
        public void run() {
            String s;
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(
				pis));
            try {
                while((s = buffReader.readLine()) != null) {
                    CommonInfoModel.getDefaultLogger().info(s);
                }
                buffReader.close();
            } catch (Exception ioe) {
                try {
                    buffReader.close();
                } catch (Exception e) {
                    CommonInfoModel.getDefaultLogger().info(e.getMessage());
                }
            }
        }
        
        protected void finalize() throws Throwable {
            pis.close();
        }
    }
}


