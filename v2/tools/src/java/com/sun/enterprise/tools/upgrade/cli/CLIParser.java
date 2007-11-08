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

package com.sun.enterprise.tools.upgrade.cli;

/**
 *
 * author : Gautam Borah
 *
 */

import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.common.*;
import com.sun.enterprise.cli.framework.*;

public class CLIParser extends ArgsParser implements InteractiveInput {
    
    
    private boolean interactiveInput = false;
    private int index=0;
    private Map interactiveInputMap;
    private String currentDomain;
    private StringManager sm;
    private CommonInfoModel commonInfo;
    private ArgsParser parser;

    /**
     * CLIParser constructor
     */
    public CLIParser(){
        this(new CommonInfoModel(), new String [] {});
    }
    
    /**
     * CLIParser constructor with arguments
     */
    public CLIParser(CommonInfoModel cim, String [] args) {
        super(args, cim);
        commonInfo = cim;
        sm = StringManager.getManager(LogService.UPGRADE_CLI_LOGGER);
        
        //Parse the inputs and collect any missing arguments
        interactiveInputMap = parse(this);
        
        //Verify if all inputs have been provided
        verifyCommonInfo(commonInfo);
        
        //Build source domains mapping if not already done so.
        commonInfo.enlistDomainsFromSource();
        
        //Check if this is a valid upgrade path.
        if(!commonInfo.isUpgradeSupported()) {
            getLogger().info(sm.getString(
                    "enterprise.tools.upgrade.cli.upgrade_not_supported"));
            cim.recover();	    
            System.exit(1);
        }
    }
    
    public void setCommonInfoModel(CommonInfoModel commonInfo){
        this.commonInfo=commonInfo;
    }
    
    public CommonInfoModel getCommonInfoModel(CommonInfoModel commonInfo){
        return commonInfo;
    }
    
    public void printInfo(){
        commonInfo.printInfo();
    }
    
    public Logger getLogger() {
        return LogService.getLogger(LogService.UPGRADE_LOGGER);
    }
    
    public void parseComandLineArguments(String[] args) {
        parser = new ArgsParser(args, commonInfo);
        interactiveInputMap = parser.parse(this);
        verifyCommonInfo(commonInfo);
        if(!commonInfo.isUpgradeSupported()) {
            helpUsage(sm.getString("enterprise.tools.upgrade.cli.upgrade_not_supported"));
            getLogger().info(sm.getString("enterprise.tools.upgrade.cli.upgrade_not_supported"));
            commonInfo.recover();	    
            System.exit(1);
        } else {
            String src = (String)interactiveInputMap.get(SOURCE);
	    String targ = (String)commonInfo.getTargetInstallDir();
	    if(commonInfo.getSourceVersion().equals(UpgradeConstants.VERSION_7X)) {
		if(targ != null && !targ.equals("") && src!= null && !src.equals("")) {
		    if(targ.equals(src)) {
		        helpUsage(sm.getString("enterprise.tools.upgrade.cli.upgrade_not_supported"));
                        commonInfo.recover();			
		        System.exit(1);
	            }
		}
	    }
	}
        commonInfo.enlistDomainsFromSource();
    }

    /**
     * Method to verfiy if all inputs have been provided by the user.
     */
    public void verifyCommonInfo(CommonInfoModel commonInfo){
        //Check not valid since source and target will be validated for
        //domain directory and domains root already and they will never be equal
        /*if(commonInfo.getSourceInstallDir().equals(
                commonInfo.getTargetInstallDir())) {
            String msg = sm.getString(
                    "enterprise.tools.upgrade.cli.Invalid_input_directory");
            helpUsage(msg);
            commonInfo.recover();	    
            System.exit(1);
        }*/
        if(!( (interactiveInputMap.containsKey(SOURCE) || interactiveInputMap.containsKey(SOURCE_SHORT)) &&
              (interactiveInputMap.containsKey(TARGET) || interactiveInputMap.containsKey(TARGET_SHORT)) &&
              (interactiveInputMap.containsKey(ADMINUSER) || interactiveInputMap.containsKey(ADMINUSER_SHORT)) &&
              (interactiveInputMap.containsKey(ADMINPASSWORD) || interactiveInputMap.containsKey(ADMINPASSWORD_SHORT))))
        {
            commonInfo.recover();
            helpUsage(1);
        }
    }
    
    /**
     * Method to collect missing arguments from the user in CLI mode.
     */
    public void collectMissingArguments(Map inputMap)  {
        //If no other instructions are to be received, return.    
        if(inputMap.containsKey(UpgradeConstants.CLI_OPTION_NOPROMPT))
            return;
        
        List domainList = commonInfo.getDomainList();
        
        // Following are Invalid Inputs for Interaction
        //Source is null but domain name is not null
        //Domain name is null but nsspwd, jkdpwd, capwd not null
        Iterator itr =  domainList.iterator();
        while(itr.hasNext()) {
            String domainName = (String)itr.next();
            String suffix = "-" + domainName;
            if( inputMap.get(SOURCE) == null && 
                    inputMap.get(SOURCE_SHORT) == null &&
                    inputMap.get(DOMAIN+suffix) != null)  {
                commonInfo.recover();		
                helpUsage(1);
            }
            if( (inputMap.get(NSSPWD+suffix) != null ||
                    inputMap.get(JKSPWD+suffix) != null || 
                    inputMap.get(CAPWD+suffix) != null) &&
                    inputMap.get(DOMAIN+suffix) == null) {
                commonInfo.recover();		
                helpUsage(1);
            }
        }
       
        System.out.println(UpgradeConstants.CLI_USER_INSTRUCTIONS);
        if(inputMap.get(SOURCE) == null && inputMap.get(SOURCE_SHORT) == null) {
            System.out.print(
                    sm.getString("enterprise.tools.upgrade.cli.Source_input"));
            byte b[] = new byte[1024];
            try {
                int c = System.in.read(b);
                if (c == -1) { // input stream closed, maybe by ^C
                    System.exit(1);
                }
                String sourceDir = new String(b,0,c);
                String source = sourceDir.trim();
                
                //Check if input is a valid source directory input
                checkValidSourceDir(source);
                
                //Set the source input in source install dir
                source = normalizePath(source); // temp fix for 6567417
                commonInfo.setSourceInstallDir(source);
                
                //Create any backups needed and build the source domain mapping
                commonInfo.enlistDomainsFromSource();
                inputMap.put(SOURCE,source);
                interactiveInput = true;
            }catch(Exception e) {
                getLogger().log(Level.SEVERE, 
                        sm.getString("enterprise.tools.upgrade.cli.unexpectedException"), 
                        e);
            }
        }
        if(inputMap.get(TARGET) == null && inputMap.get(TARGET_SHORT) == null) {
            System.out.print(
                    sm.getString("enterprise.tools.upgrade.cli.Target_input"));
            byte b[] = new byte[1024];
            try {
                int c = System.in.read(b);
                if (c == -1) { // input stream closed, maybe by ^C
                    System.exit(1);
                }
                String targetDir = new String(b,0,c);
                String target = targetDir.trim();
                
                //Check if input is a valid target directory input
                if (!UpgradeUtils.getUpgradeUtils(commonInfo).
                        isValidTargetPath(target)) {
                    getLogger().severe(sm.getString("" +
                            "enterprise.tools.upgrade.cli.not_valid_target_install"));
                    commonInfo.recover();		    
                    helpUsage(1);
                }
                
                //Input can only be domains root. Set it in commonInfo
                target = normalizePath(target); // temp fix for 6567417
                commonInfo.setTargetDomainRoot(target);
                
                //Build domains mapping of source
                commonInfo.enlistDomainsFromSource();
                
                inputMap.put(TARGET,target);
                interactiveInput = true;
            }catch(Exception e) {
                getLogger().log(Level.SEVERE, 
                        sm.getString("enterprise.tools.upgrade.cli.unexpectedException"), 
                        e);
            }
        }
        if(inputMap.get(ADMINUSER) == null && inputMap.get(ADMINUSER_SHORT) == null) {
            System.out.print(
                    sm.getString("enterprise.tools.upgrade.cli.adminuser_input"));
            byte b[] = new byte[1024];
            try {
                int c = System.in.read(b);
                if (c == -1) { // input stream closed, maybe by ^C
                    System.exit(1);
                }
                String adminUser = new String(b,0,c);
                String admiuser = adminUser.trim();
                
                //Set admin user in commonInfo
                commonInfo.setAdminUserName(admiuser);
                
                inputMap.put(ADMINUSER,admiuser);
                interactiveInput = true;
            }catch(Exception e) {
                getLogger().log(Level.SEVERE, 
                        sm.getString("enterprise.tools.upgrade.cli.unexpectedException"), 
                        e);
            }
        }
	
        //Getting source edition since target domain's profile will be based on this.	
	String sourceEdition = commonInfo.getSourceEdition();
			
        if(inputMap.get(ADMINPASSWORD) == null && 
                inputMap.get(ADMINPASSWORD_SHORT) == null) {
            System.out.print(
                    sm.getString("enterprise.tools.upgrade.cli.adminpassword_input"));
            byte b[] = new byte[1024];
            try {
                String adminPassword =  new CliUtil().getPassword();
                
                //Set admin password in commonInfo
                commonInfo.setAdminPassword(adminPassword);
                
                inputMap.put(ADMINPASSWORD,adminPassword);
                interactiveInput = true;
                
                //Validate admin credentials if source is PE
                if(UpgradeConstants.EDITION_PE.equals(sourceEdition))
                    verifyUserAndPasswords(commonInfo.getAdminUserName(),
                            adminPassword, null);
            }catch(Exception e) {
                getLogger().log(Level.SEVERE, sm.getString(
                        "enterprise.tools.upgrade.cli.unexpectedException"), e);
            }
        }
        
        //Collect Master Password if source is not PE
        if(!(UpgradeConstants.EDITION_PE.equals(sourceEdition))){
            if(inputMap.get(MASTERPASSWORD) == null && 
                    inputMap.get(MASTERPASSWORD_SHORT) == null) {
                System.out.print(
                        sm.getString("enterprise.tools.upgrade.cli.MasterPW_input"));
                String password =  new CliUtil().getPassword();
                
                //Set master password in commonInfo
                commonInfo.setMasterPassword(password);
                
                inputMap.put(MASTERPASSWORD, password);
                
                //Validate admin credentials and master password
                verifyUserAndPasswords(commonInfo.getAdminUserName(),
                        commonInfo.getAdminPassword(),
                        password);
                interactiveInput = true;
            }
        } else {
			commonInfo.setMasterPassword(commonInfo.getDefaultMasterPassword());
		}
    }

    public static void main(String[] args) throws Exception{
        CLIParser parser = new CLIParser();
        parser.setCommonInfoModel(new CommonInfoModel());
        parser.parseComandLineArguments(args);
        parser.printInfo();
    }
    
    private void checkValidSourceDir(String sourceDir) {
        if(!UpgradeUtils.getUpgradeUtils(commonInfo).isValidSourcePath(sourceDir)) {
            getLogger().severe(
                    sm.getString("enterprise.tools.upgrade.cli.not_valid_source_install"));
            commonInfo.recover();	    
            helpUsage(1);
        }
    }
    
    private void verifyUserAndPasswords(String adminUser, String adminPassword, 
            String masterPassword) {
        
        if(!UpgradeUtils.getUpgradeUtils(commonInfo).
                validateUserDetails(adminUser,adminPassword,masterPassword)) {
            if(!UpgradeConstants.EDITION_PE.equals(commonInfo.getSourceEdition())){
                getLogger().severe(sm.getString(
                        "enterprise.tools.upgrade.cli.wrong_adminuser_or_adminpassword_or_masterpassword"));
            } else {
                getLogger().severe(sm.getString(
                        "enterprise.tools.upgrade.cli.wrong_adminuser_or_adminpassword"));
            }
            commonInfo.recover();	    
            System.exit(1);
        }
        
    }
    
    /*
     * This method is a temporary fix for issue 6567417.  
     *
     * If a user enters:
     *
     *    d:/foo/bar\domains/domain1
     *
     * the code validates this by calling (new File(...)).exists(), but then stores
     * the actual path in CommonInfoModel.  Then when CommonInfoModel.enlistDomainsFromSource
     * is called, it assumes that the path is properly using File.separator which generates
     * NPE.
     *
     * This method assumes that checkValidSourceDir(source) or isValidTargetPath has been 
     * called and returned true.
     */
    private String normalizePath(String path) {
        try {
            return (new File(path)).getCanonicalPath();
        } catch (IOException e) {
            // can't happen if preconditions described above returned true...
            return path;
        }
    }
}



