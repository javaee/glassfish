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

import com.sun.enterprise.cli.framework.CLIMain;
import com.sun.enterprise.cli.framework.InputsAndOutputs;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.LogService;

/**
 *
 * @author  hans
 */
public class Commands {
    private static StringManager stringManager = StringManager.getManager(Commands.class);
    
    /** Creates a new instance of Commands */
    public Commands() {
    }
    
    public static boolean deploy(String modulePath, CommonInfoModel commonInfo, ArrayList parameters) {
		String trgDomainXMLfile = commonInfo.getTarget().getConfigXMLFile();
		String adminPort = DomainsProcessor.getTargetDomainPort(trgDomainXMLfile);
        String adminSecurity = DomainsProcessor.getTargetDomainSecurity(trgDomainXMLfile);
		Credentials c = commonInfo.getSource().getDomainCredentials();
		
        ArrayList deployList = new ArrayList();
        deployList.add("deploy");
        deployList.add("--user");
        deployList.add(c.getAdminUserName());
        deployList.add("--passwordfile");
        deployList.add("\"" + c.getPasswordFile()+ "\"");
        deployList.add("--port");
        deployList.add(adminPort);
        deployList.add("--secure=" + adminSecurity);
        if(parameters != null) {
            for(int i=0; i< parameters.size(); i++){
                deployList.add(parameters.get(i));
            }
        }
        deployList.add("\"" + modulePath + "\"");
        String[] deployArray = new String[1];
        String[] deploy = (String[])deployList.toArray(deployArray);
        
        try {
            return executeCommand(deploy);
        } catch (CommandException ce) {
            Throwable t = ce.getCause();
			CommonInfoModel.getDefaultLogger().warning(
				stringManager.getString("upgrade.common.general_exception")
				+ " " + (t==null?ce.getMessage():t.getMessage()));
        }
        CommonInfoModel.getDefaultLogger().warning(
			stringManager.getString("commands.errorDeployingMsg") + modulePath);
        return false;
    }
    
    public static boolean startDomain(String domainName, CommonInfoModel commonInfo) {
        Credentials c = commonInfo.getSource().getDomainCredentials();
		String adminUser = c.getAdminUserName();
		String command[] = {
            "start-domain", 
                "--domaindir", "\"" + commonInfo.getTarget().getInstallDir() + "\"", 
                "--user", adminUser, 
                "--passwordfile ", "\"" + c.getPasswordFile() +"\"", 
                domainName
        };
        
        try {
            boolean b = executeCommand(command);
            return b;
        } catch (CommandException ce) {
            Throwable t = ce.getCause();
            CommonInfoModel.getDefaultLogger().severe(stringManager.
                    getString("upgrade.common.general_exception") + ce.getMessage());
            if (t != null) {
                String message = t.getMessage();
                if(message != null){
                    CommonInfoModel.getDefaultLogger().severe(stringManager.
                            getString("upgrade.common.general_exception") + message);
                    if ( message.indexOf(stringManager.
                            getString("commands.DomainRunningFragment")) != -1 || 
                        (stringManager.getString("commands.DomainRunningFragment").
                            equalsIgnoreCase("No local string defined") && 
                        message.indexOf("running") != -1 )) {
                        CommonInfoModel.getDefaultLogger().severe(stringManager.
                                getString("commands.DomainRunningMsg", domainName));   
                    }
                }
            }
        }
        return false;
    }
    
    public static boolean stopDomain(String domainName, CommonInfoModel commonInfo) {
		String command[] = {"stop-domain", "--domaindir", "\"" + 
				commonInfo.getTarget().getInstallDir() +"\"", domainName
        };	
		
        try {
            boolean b = executeCommand(command);
            return b;
        } catch (CommandException ce) {
            Throwable t = ce.getCause();
            if (t != null && t.getMessage().indexOf("is not running") != -1) {
                return true;
            }
            CommonInfoModel.getDefaultLogger().warning(stringManager.getString(
				"upgrade.common.general_exception") + ce.getMessage());
        }
        return false;
    }
    
    public static boolean executeCommand(String commandStrings[]) 
            throws CommandException {
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
            CLIMain.invokeCLI(commandOneString.toString(), io);
            pos.flush();
            return true;
        }
        catch(CommandException ce) {
            throw ce;
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


