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

package com.sun.enterprise.tools.upgrade;

import java.io.*;
import java.util.logging.*;

import com.sun.enterprise.tools.upgrade.cli.*;
import com.sun.enterprise.tools.upgrade.common.*;
import com.sun.enterprise.tools.upgrade.gui.MainFrame;
import com.sun.enterprise.tools.upgrade.gui.util.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.util.ASenvPropertyReader;
import com.sun.enterprise.util.SystemPropertyConstants;

public class UpgradeToolMain {
    
    static{
        String domainRoot = System.getProperty("com.sun.aas.domainRoot");
        if(domainRoot == null){
            System.out.println("Configuration Error: AS_DEFS_DOMAINS_PATH is not set.");
            System.exit(1);
        }
        String upgradeLogPath =domainRoot+File.separator+"upgrade.log";
        try{
            File f = new File(domainRoot);
            if(!f.exists()){
                System.out.println("Configuration Error: AS_DEFS_DOMAINS_PATH: " + 
                        domainRoot + " does not exist.");
                System.exit(1);
            }
            LogService.initialize(upgradeLogPath);
        }catch(Exception e){
            System.out.println("Could not create upgrade.log file: " + 
                    e.getLocalizedMessage());
        }
    }
    
    static Logger _logger=LogService.getLogger(LogService.UPGRADE_LOGGER);
    private CommonInfoModel commonInfo;
    private UpgradeHarness harness;
    private String certDbPassword;
    private String aliasname;
    private String keyStorePassword;
    private StringManager sm;
    
    public UpgradeToolMain() {
        final ASenvPropertyReader reader = new ASenvPropertyReader(
           System.getProperty(SystemPropertyConstants.CONFIG_ROOT_PROPERTY));
        reader.setSystemProperties();

        sm = StringManager.getManager(LogService.UPGRADE_LOGGER);
        _logger.log(Level.INFO, 
                sm.getString("enterprise.tools.upgrade.start_upgrade_tool"));
        
        commonInfo = new CommonInfoModel();
        
        String targetDomainRoot = System.getProperty(UpgradeConstants.AS_DOMAIN_ROOT);
        if(targetDomainRoot == null) {
           targetDomainRoot = new File("").getAbsolutePath();
        }
        commonInfo.setTargetDomainRoot(targetDomainRoot);
        
        harness = new UpgradeHarness();
        String targetInstallPath = System.getProperty(UpgradeConstants.AS_INSTALL_ROOT);
        String asadmin = null;
        String osName = System.getProperty(UpgradeConstants.OS_NAME_IDENTIFIER);
        
        commonInfo.setOSName(osName);
        
        //Test for valid configuration by checking installRoot
        if(osName.indexOf(UpgradeConstants.OS_NAME_WINDOWS) != -1) {
            asadmin = targetInstallPath + File.separator + 
                    UpgradeConstants.AS_BIN_DIRECTORY + 
                    File.separator + UpgradeConstants.ASUPGRADE_BAT;
        } else {
            asadmin = targetInstallPath + File.separator + 
                    UpgradeConstants.AS_BIN_DIRECTORY + 
                    File.separator + UpgradeConstants.ASUPGRADE;
        }
        try {
            if(! new File(asadmin).exists()) {
                _logger.log(Level.WARNING,
                        sm.getString("enterprise.tools.upgrade.configError"));
                System.exit(1);
            }
        }catch (Exception e) {
            _logger.log(Level.WARNING,
                    sm.getString("enterprise.tools.upgrade.unknownError"),e);
        }
        commonInfo.setTargetInstallDir(targetInstallPath);
    }
    
    public void startGUI(String [] args){
        _logger.log(Level.INFO,sm.getString(
                "enterprise.tools.upgrade.start_upgrade_tool_gui"));
        if(args.length > 0){
            CliLogMessageListener l = new CliLogMessageListener();
            LogService.addLogMessageListener(l);  //to log and output command line parsing
            new ArgsParser(args, commonInfo).parse();
            LogService.removeLogMessageListener(l);
        }
        MainFrame gui = new MainFrame(commonInfo);
        LogService.addLogMessageListener(gui);
        gui.addDialogListener(new DialogListener(){
            public void dialogProcessed(DialogEvent evt){
                processUIEvent(evt);
            }
        });
        UpdateProgressManager.getProgressManager().addUpgradeUpdateListener(gui);
        gui.setVisible(true);
    }
    
    public void startCLI(String [] args){
        _logger.log(Level.INFO,
                sm.getString("enterprise.tools.upgrade.start_upgrade_tool_cli"));
        LogService.addLogMessageListener(new CliLogMessageListener()); 
        commonInfo.setCliMode(true);
        try{
            new CLIParser(commonInfo, args);
        }catch(Exception e){
            _logger.log(Level.INFO,sm.getString(
                    "enterprise.tools.upgrade.unexpected_parsing"),e);
            System.exit(1);
        }
        this.upgrade();
    }
    
    private void processUIEvent(DialogEvent evt){
        if(evt.getAction() == DialogEvent.FINISH_ACTION ||
        evt.getAction() == DialogEvent.CANCEL_ACTION){
            System.exit(0);
        }else if(evt.getAction() == DialogEvent.UPGRADE_ACTION){
            this.upgrade();
        }
    }
    
    private void upgrade(){
        harness.setCommonInfoModel(commonInfo);
        
        //Start Upgrade
        _logger.log(Level.INFO, sm.getString(
                "enterprise.tools.upgrade.start_upgrade_harness"));
        harness.startUpgrade();
        
        //Delete temporary files (if any) created during the process
        _logger.log(Level.INFO, sm.getString(
                "enterprise.tools.upgrade.deletingTempPasswordFiles"));
        commonInfo.deletePasswordFile();
        if(commonInfo.getCliMode())
            System.exit(0);
    }
    
    public static void main(String [] args) {
        UpgradeToolMain main = new UpgradeToolMain();
        StringBuffer argsStringBuff = new StringBuffer();
        for(int i=0;i<args.length;i++){
            argsStringBuff.append(args[i]);
            argsStringBuff.append(" ");
        }
        _logger.info(UpgradeConstants.ASUPGRADE + argsStringBuff.toString());
        // CR 6568819 - added support for -v option (CLI_OPTION_VERSION_UC/LC_SHORT)
        if(args.length==0){
            main.startGUI(args);
        } else if (args[0].equals(UpgradeConstants.CLI_OPTION_CONSOLE_SHORT) || 
                args[0].equals(UpgradeConstants.CLI_OPTION_CONSOLE_LONG) || 
                args[0].equals(UpgradeConstants.CLI_OPTION_VERSION_UC_SHORT) || 
                args[0].equals(UpgradeConstants.CLI_OPTION_VERSION_LC_SHORT) || 
                args[0].equals(UpgradeConstants.CLI_OPTION_VERSION_LONG)){
            main.startCLI(args);
        } else {
            main.startGUI(args);
        }
    }
}
