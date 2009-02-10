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
 * ConfigTransfers.java
 *
 * Created on September 8, 2003, 9:05 AM
 */

package com.sun.enterprise.tools.upgrade.miscconfig;

/**
 *
 * @author  prakash
 * @author hans hrasna
 * This class is used to transfer config files
 * server.policy, sun-acc.xml, default-web.xml, secmod.db
 * with minor modifications wherever necessary.
 */
import com.sun.enterprise.tools.upgrade.common.*;
import java.io.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.*;
import java.util.logging.*;
import java.util.Enumeration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Vector;

public class ConfigTransfers implements BaseModule{
    
    private static final String SECMODDB = "secmod.db";
    
    private StringManager stringManager = StringManager.getManager(ConfigTransfers.class);
    private Logger logger = CommonInfoModel.getDefaultLogger();
    private Vector recoveryList = new Vector();
   
    private CommonInfoModel commonInfo = null;
    
    /** Creates a new instance of ConfigTransfers
     *
     */
    public ConfigTransfers() {
        
    }
    
    /**
     * Method to start upgrade of miscellaneous configuration
     */
    public boolean upgrade(CommonInfoModel commonInfo) {
        this.commonInfo = commonInfo;
        
        //Transfer Server.policy file
        String sourceServerPolicy = commonInfo.getSource().getServerPolicyFileName();
        String targetServerPolicy = commonInfo.getTarget().getServerPolicyFileName();
        transferServerPolicy(sourceServerPolicy, targetServerPolicy);
        
        //Transform wss-server-config xml file is upgrade is from EE
         if(commonInfo.isEnterpriseEdition(commonInfo.getSource().getEdition())) {  
			String targetWssServerConfig = commonInfo.getTarget().getWssServerConfigXML();
            new WssServerConfigXMLTransfer().transform(targetWssServerConfig);
            return true;
        }
   
        //Transform default-web xml file
        String targetDefaultWebXML = commonInfo.getTarget().getDefaultWebXMLFileName();
        new DefaultWebXMLTransfer(commonInfo).transform(targetDefaultWebXML);
        
        //Transform sun-acc xml file
        String sourceSunACCFile = commonInfo.getSource().getSunACCFileName();
        String targetSunACCFile = commonInfo.getTarget().getSunACCFileName();
        new SunACCTransfer().transform(sourceSunACCFile, targetSunACCFile);	
        return true;
    }
    
    public void recovery(CommonInfoModel commonInfo) {
        Enumeration e = recoveryList.elements();
        while(e.hasMoreElements()){
            String recoverPath = (String)e.nextElement();
            String backupPath = recoverPath + ".bak";
            try {
                UpgradeUtils.copyFile(backupPath, recoverPath);
                new File(backupPath).delete();
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, stringManager.getString("upgrade.realm.recoveryFailureMessage",ioe.getMessage()),new Object[]{recoverPath,ioe});
            }
        }
    }
    
    /**
     * transferSeverPolicy uses sun.security.provider.PolicyParser from the jdk 
     * to parse the source and target server.policy files
     * Also transfers user added grants and permissions
     * @author  hans
     */
    private void transferServerPolicy(String sourcePolicyFileName, 
            String destPolicyFileName){
        //Backup the existing server.policy file
        if (! backup(destPolicyFileName)) {
            logger.log(Level.SEVERE, stringManager.getString(
                    "upgrade.configTransfers.serverPolicy.backupFailureMessage"));
            logger.log(Level.SEVERE, stringManager.getString(
                    "upgrade.configTransfers.serverPolicy.startFailureMessage"));
            return;
        }
        logger.log(Level.INFO, stringManager.getString(
                "upgrade.configTransfers.serverPolicy.startMessage"));
        
        //Read source and target policy files into respective PolicyParser objects
        PolicyParser sourcePolicy = new PolicyParser();
        PolicyParser targetPolicy = new PolicyParser();
        try {
            sourcePolicy.read(new FileReader(sourcePolicyFileName));
            targetPolicy.read(new FileReader(destPolicyFileName));
        } catch (PolicyParser.ParsingException pe) {
            logger.log(Level.SEVERE, stringManager.getString(
                    "upgrade.configTransfers.serverPolicy.startFailureMessage") 
                    + pe.getLocalizedMessage());
            return;
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, stringManager.getString(
                    "upgrade.configTransfers.serverPolicy.startFailureMessage"),
                    ioe.getMessage());
            return;
        }
        
        //Get source and target grant elements
        Enumeration sourceElements = sourcePolicy.grantElements();
        Enumeration targetElements = targetPolicy.grantElements();
        
        //Get matching grant entries and add required permissions
        while(sourceElements.hasMoreElements()) {
            PolicyParser.GrantEntry sourceGrantEntry = 
                    (PolicyParser.GrantEntry)sourceElements.nextElement();
            boolean matchedGrantEntry = false;
            while (targetElements.hasMoreElements()) {
                PolicyParser.GrantEntry targetGrantEntry = 
                        (PolicyParser.GrantEntry)targetElements.nextElement();
                if(targetGrantEntry.codeBase == null && 
                        sourceGrantEntry.codeBase == null) {
                    matchedGrantEntry = true;
                } else if (targetGrantEntry.codeBase != null && 
                        sourceGrantEntry.codeBase != null) {
                    if (targetGrantEntry.codeBase.equals(sourceGrantEntry.codeBase)) {
                        //found a matched codeBase
                        matchedGrantEntry = true;
                    }
                }
                if(matchedGrantEntry) {
                    //Check if target has all the permissions of the source
                    //If not, add the missing permissions
                    Enumeration sourcePermissions = sourceGrantEntry.permissionElements();
                    while(sourcePermissions.hasMoreElements()) {
                        boolean matchedPermission = false;
                        PolicyParser.PermissionEntry sourcePermission = 
                                (PolicyParser.PermissionEntry)sourcePermissions.nextElement();
                        Enumeration targetPermissions = targetGrantEntry.permissionElements();
                        while(targetPermissions.hasMoreElements()) {
                            PolicyParser.PermissionEntry targetPermission = 
                                    (PolicyParser.PermissionEntry)targetPermissions.nextElement();
                            if(targetPermission.equals(sourcePermission)) {
                                matchedPermission = true;
                                break;
                            }
                        }
                        if(!matchedPermission){
                            targetGrantEntry.add(sourcePermission);
                        }
                    }
                    
                    //Check if target has all the principals of the source
                    //If not, add the missing principals
                    Iterator sourcePrincipalIterator = sourceGrantEntry.principals.iterator();
                    while(sourcePrincipalIterator.hasNext()) {
                        boolean matchedPrincipal = false;
                        PolicyParser.PrincipalEntry sourcePrincipalEntry = 
                                (PolicyParser.PrincipalEntry)sourcePrincipalIterator.next();
                        Iterator targetPrincipalIterator = targetGrantEntry.principals.iterator();
                        while(targetPrincipalIterator.hasNext()) {
                            PolicyParser.PrincipalEntry targetPrincipalEntry = 
                                    (PolicyParser.PrincipalEntry)targetPrincipalIterator.next();
                            if(targetPrincipalEntry.equals(sourcePrincipalEntry)) {
                                matchedPrincipal = true;
                                break;
                            }
                        }
                        if(!matchedPrincipal) {
                            targetGrantEntry.principals.add(sourcePrincipalEntry);
                        }
                    }
                    break;
                }
            }
            if (!matchedGrantEntry) {
                targetPolicy.add(sourceGrantEntry);
            }
        }       
        try {
            targetPolicy.write(new FileWriter(destPolicyFileName));
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, stringManager.getString(
                    "upgrade.configTransfers.serverPolicy.startFailureMessage"),
                    ioe.getMessage());
            return;
        }
    }
    
    private void transferSecModDb(CommonInfoModel commonInfo) {
        String sourcePath = commonInfo.getSource().getDomainDir();
        String targetPath = commonInfo.getTarget().getDomainDir();
        File sourceFile = new File(sourcePath + "/" + "config" + "/" + SECMODDB);
        File targetFile = new File(targetPath + "/" + "config" + "/" + SECMODDB);
        if(!sourceFile.exists()) return;
        if(targetFile.exists()) {
            backup(targetFile.getAbsolutePath());
        }
        try {
            UpgradeUtils.copyFile(sourceFile.getAbsolutePath(), 
                    targetFile.getAbsolutePath());
        } catch(IOException e) {
            logger.log(Level.WARNING, stringManager.getString("upgrade.configTransfers.secModDb.failureMessage") + e.getLocalizedMessage());
        }
    }
    
    /**
     * Method to backup the server.policy file before processing
     */
    private boolean backup(String filename) {
        try{
            File targetFile = new File(filename);
            boolean renamed = targetFile.renameTo(new File(filename +".bak"));
            if(!renamed){
                // This is possible if user is running the upgrade again 
                //and .bak is already created.
                renamed = targetFile.delete();
            }
            if(renamed){
                targetFile = new File(filename);
                targetFile.createNewFile();
                BufferedReader reader = new BufferedReader(new InputStreamReader
                        (new FileInputStream(filename + ".bak")));
                PrintWriter writer = new PrintWriter(new FileOutputStream(targetFile));
                String readLine = null;
                while((readLine = reader.readLine()) != null){
                    writer.println(readLine);
                }
                writer.flush();
                writer.close();
                reader.close();
                return true;
            } else {
                //Log a error message : Rename Failure
                logger.log(Level.SEVERE, stringManager.getString(
                        "upgrade.configTransfers.serverPolicy.renameFailureMessage"));
            }
        }catch(Exception ex){
            // Log a error message
            logger.log(Level.SEVERE, stringManager.getString(
                    "upgrade.configTransfers.serverPolicy.startFailureMessage"),ex);
        }
        return false;
    }
    
    public String getName() {
        return stringManager.getString("upgrade.configTransfers.moduleName");
    }
    
}
