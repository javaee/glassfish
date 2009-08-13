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

package com.sun.enterprise.tools.upgrade.common;

import com.sun.enterprise.tools.upgrade.logging.LogService;
import com.sun.enterprise.util.i18n.StringManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.enterprise.tools.upgrade.UpgradeToolMain;

/**
 *
 * @author  hans
 */
public class Commands {

    private static final Logger logger = LogService.getLogger();
    
    private static final StringManager stringManager =
        StringManager.getManager(Commands.class);
    
    /** Creates a new instance of Commands */
    public Commands() {
    }
    
    public static int startDomain(String domainName, CommonInfoModel commonInfo) {
        Credentials c = commonInfo.getSource().getDomainCredentials();
		String adminUser = c.getAdminUserName();

        String installRoot = System.getProperty(UpgradeToolMain.AS_DOMAIN_ROOT);
        File installRootF = new File(installRoot);
        File asadminF = new File(installRootF.getParentFile(), "bin/asadmin");
        String asadminScript = asadminF.getAbsolutePath();
        try {
            asadminScript = asadminF.getCanonicalPath();
        } catch(IOException e){
            //- no action needed use absolutePath
        }
        String ext = "";
        String osName = System.getProperty("os.name");
        if(osName.indexOf("Windows") != -1){
           ext = ".bat";
        }
        ArrayList<String> tmpC = new ArrayList<String>();
        //-tmpC.add("bin/asadmin");
        tmpC.add(asadminScript+ext);
        tmpC.add("start-domain");
        tmpC.add("--upgrade");
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
 
  
    private static int executeCommand(String[] commandStrings) {
        int exitValue=0;

        StringBuffer commandOneString = new StringBuffer();
            for(int i = 0; i < commandStrings.length; i++) {
                commandOneString.append(commandStrings[i]).append(" ");
            }
            logger.info(stringManager.
                    getString("commands.executingCommandMsg") + commandOneString);

        try {
            Process asadminProcess = Runtime.getRuntime().exec(commandOneString.toString());
            BufferedReader pInReader =
                    new BufferedReader(new InputStreamReader(
                    asadminProcess.getInputStream()));
            BufferedReader eInReader =
                    new BufferedReader(new InputStreamReader(
                    asadminProcess.getErrorStream()));
            String inLine = null;
            String eLine = null;
            while ((eLine = eInReader.readLine()) != null && (inLine = pInReader.readLine()) != null) {
                if(eLine != null){
                    logger.log(Level.INFO,eLine);
                    exitValue++;
                }
                if(inLine != null){
                    logger.log(Level.INFO,inLine);
                }
            }
            asadminProcess.destroy();
            pInReader.close();
            eInReader.close();
        } catch (Exception e) {
            Throwable t = e.getCause();
            logger.warning(stringManager.getString(
				"upgrade.common.general_exception") + (t==null?e.getMessage():t.getMessage()));
        }
        return exitValue;
    }
}


