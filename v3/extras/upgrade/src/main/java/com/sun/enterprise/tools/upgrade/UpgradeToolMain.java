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
import java.util.ArrayList;

import com.sun.enterprise.tools.upgrade.cli.*;
import com.sun.enterprise.tools.upgrade.common.*;
import com.sun.enterprise.tools.upgrade.gui.MainFrame;
import com.sun.enterprise.tools.upgrade.gui.util.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.tools.upgrade.common.arguments.*;

public class UpgradeToolMain {

    private static final String AS_DOMAIN_ROOT = "com.sun.aas.domainRoot";
    static{
        String domainRoot = System.getProperty(AS_DOMAIN_ROOT);
        if(domainRoot == null){
            System.out.println("Configuration Error: AS_DEFS_DOMAINS_PATH is not set.");
            System.exit(1);
        }

        LogService.initialize();
 
    }
    
    static Logger _logger=LogService.getLogger(LogService.UPGRADE_LOGGER);

    private StringManager sm = StringManager.getManager(UpgradeToolMain.class);
    private CommonInfoModel commonInfo = CommonInfoModel.getInstance();
    private CliLogMessageListener stdoutMsgs = null;

    public UpgradeToolMain() {
        //- print tool's msgs to stnd out
        stdoutMsgs = new CliLogMessageListener();
        LogService.addLogMessageListener(stdoutMsgs);
        _logger.log(Level.FINE, sm.getString("enterprise.tools.upgrade.start_upgrade_tool"));

        //- Have GF sets asenv.conf properties to system properties
        new ASenvPropertyReader();

        //- Default location of all traget server domains
        String rawTargetDomainRoot = System.getProperty(AS_DOMAIN_ROOT);
        if(rawTargetDomainRoot == null) {
            rawTargetDomainRoot = "";
        }
        String targetDomainRoot = null;
        try {
            targetDomainRoot = new File(rawTargetDomainRoot).getCanonicalPath();
        } catch (IOException ioe) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine(String.format(
                    "Will not create canonical path for target: %s",
                    ioe.getLocalizedMessage()));
            }
            targetDomainRoot = new File(rawTargetDomainRoot).getAbsolutePath();
        }
		commonInfo.getTarget().setInstallDir(targetDomainRoot);
    }
    
    public void startGUI(String [] args){
        _logger.log(Level.FINE,sm.getString("enterprise.tools.upgrade.start_upgrade_tool_gui"));
        if(args.length > 0){
			//- set all vaild options user provided on cmd-line
			GUICmdLineInput guiIn = new GUICmdLineInput();
			guiIn.processArguments(guiIn.parse(args));
        }

		//- disable writing to stnd out when in GUI mode
        LogService.removeLogMessageListener(stdoutMsgs);

		MainFrame gui = new MainFrame();
        LogService.addLogMessageListener(gui);
        gui.addDialogListener(new DialogListener(){
            public void dialogProcessed(DialogEvent evt){
                processUIEvent(evt);
            }
        });
        UpdateProgressManager.getProgressManager().addUpgradeUpdateListener(gui);
        gui.setVisible(true);

        //- enable writing to stndout
        LogService.addLogMessageListener(stdoutMsgs);
    }
    
    public void startCLI(String [] args){
        _logger.log(Level.FINE, sm.getString("enterprise.tools.upgrade.start_upgrade_tool_cli"));
        try{
			cliParse(args);
        }catch(Exception e){
            _logger.log(Level.INFO, sm.getString("enterprise.tools.upgrade.unexpected_parsing"),e);
            System.exit(1);
        }
        this.upgrade();
    }
    
	private void cliParse(String[] args){
		ArgsParser ap = new ArgsParser();
		ArrayList<ArgumentHandler> aList = ap.parse(args);
		
		InteractiveInput tmpI = new InteractiveInputImpl();
		if (commonInfo.isNoprompt()){
			tmpI = new NopromptInput();
		} 
        tmpI.processArguments(aList);
        printArgs(aList);
	}

	private void printArgs(ArrayList<ArgumentHandler> aList){
		StringBuffer buff = new StringBuffer();
		for(ArgumentHandler tmpAh : aList){
			if (tmpAh instanceof ARG_w || tmpAh instanceof ARG_adminpassword ||
				tmpAh instanceof ARG_m || tmpAh instanceof ARG_masterpassword){
				//- don't reveal passwords
				buff.append("-" + tmpAh.getCmd() + " " +
					tmpAh.getRawParameter().replaceAll(".","*"));
			} else if(tmpAh instanceof ARG_c || tmpAh instanceof ARG_console ||
					tmpAh instanceof ARG_h || tmpAh instanceof ARG_help ||
					tmpAh instanceof ARG_V || tmpAh instanceof ARG_version ||
                    tmpAh instanceof ARG_noprompt){
				buff.append("-" + tmpAh.getCmd());
			}else {
				buff.append("-" + tmpAh.getCmd() + " " + tmpAh.getRawParameter());
			}
			buff.append(" ");
		}
		_logger.fine(UpgradeConstants.ASUPGRADE + " " + buff.toString());
	}
	
    private void processUIEvent(DialogEvent evt){
        if(evt.getAction() == DialogEvent.FINISH_ACTION ||
           evt.getAction() == DialogEvent.CANCEL_ACTION){
           System.exit(0);
        }else if(evt.getAction() == DialogEvent.UPGRADE_ACTION){
            this.upgrade();
        }
    }
    
    private void upgrade() {
        try {
            commonInfo.setupTasks();


            try {
                // preform upgrade
                DomainsProcessor dProcessor = new DomainsProcessor(commonInfo);
                TargetAppSrvObj _target = commonInfo.getTarget();

                // Find the endpoint of an existing server log so
                // when we look for error msgs we will not get
                // pre-existing ones.
                File serverLog = null;
                LogParser logParser = null;
                try {
                    serverLog = LogFinder.getServerLogFile();
                    if (serverLog != null) {
                        logParser = new LogParser(serverLog);
                    }
                } catch (FileNotFoundException fe) {
                    _logger.log(Level.WARNING, sm.getString(
                            "enterprise.tools.upgrade.domain_log_file_not_found", fe.getMessage()));
                } catch (IOException e) {
                    _logger.log(Level.WARNING, sm.getString(
                            "enterprise.tools.upgrade.domain_log_read_failure", e.getMessage()));
                }

                int exitValue = dProcessor.startDomain(_target.getDomainName());
                UpdateProgressManager.getProgressManager().processUpgradeUpdateEvent(100);
                if (exitValue == 0){
                    dProcessor.stopDomain(_target.getDomainName());
                }

                //- There should be a new server log file.
                if (serverLog == null) {
                    try {
                        serverLog = LogFinder.getServerLogFile();
                        logParser = new LogParser(serverLog);
                        logParser.setStartPoint(0);
                    } catch (FileNotFoundException fe) {
                        _logger.log(Level.WARNING, sm.getString(
                                "enterprise.tools.upgrade.domain_log_file_not_found", fe.getMessage()));
                    } catch (IOException e) {
                        _logger.log(Level.WARNING, sm.getString(
                                "enterprise.tools.upgrade.domain_log_read_failure", e.getMessage()));
                    }
                }
                //- broadcast all upgrade error found
                if (logParser != null){
                    StringBuilder sbuf = logParser.parseLog();
                    if (sbuf.length() > 0){
                        _logger.log(Level.INFO, sm.getString("enterprise.tools.upgrade.not_successful_mgs"));
                         _logger.log(Level.INFO, sm.getString("enterprise.tools.upgrade.logs_mgs_title"));
                         _logger.log(Level.INFO, sbuf.toString());

                    } else {
                        _logger.log(Level.INFO, sm.getString("enterprise.tools.upgrade.success_mgs"));

                    }

                } else {
                    _logger.log(Level.WARNING, sm.getString("enterprise.tools.upgrade.could_not_process_server_log"));

                }

            } catch (HarnessException he) {
                _logger.log(Level.INFO, sm.getString(
                        "enterprise.tools.upgrade.generalException", he.getMessage()));
                UpdateProgressManager.getProgressManager().processUpgradeUpdateEvent(-1);
                commonInfo.recover();
            }

            //Delete temporary files (if any) created during the process
            _logger.log(Level.FINE, sm.getString(
                    "enterprise.tools.upgrade.deletingTempPasswordFiles"));
            commonInfo.getSource().getDomainCredentials().deletePasswordFile();
        } catch (Exception e) {
            _logger.log(Level.INFO, e.getMessage());
        }
    }

    
    public static void main(String [] args) {
        UpgradeToolMain main = new UpgradeToolMain();
        boolean isCLIcmd = false;
        for (int i = 0; i < args.length; i++) {
            //- -c/--console option is not position dependent
            if (args[i].equals(CLIConstants.CLI_OPTION_CONSOLE_SHORT) ||
                args[i].equals(CLIConstants.CLI_OPTION_CONSOLE_LONG)) {
                isCLIcmd = true;
            }

            if (args[i].equals(CLIConstants.CLI_OPTION_HELP_SHORT) ||
                args[i].equals(CLIConstants.CLI_OPTION_HELP_LONG)) {
                ARG_help tmpH = new ARG_help();
                tmpH.exec();
                System.exit(0);
            }
        }
        
        if (isCLIcmd) {
            main.startCLI(args);
            System.exit(0);
        } else {
            main.startGUI(args);
        }

    }
}
