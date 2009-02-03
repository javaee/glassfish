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
 * InteractiveInputImpl.java
 *
 * Created on November 19, 2007, 12:23 PM
 *
 */

package com.sun.enterprise.tools.upgrade.common;

import java.util.Map;
import java.util.List;
import java.util.logging.*;

import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_adminuser;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_adminpassword;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_masterpassword;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_source;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_target;
import com.sun.enterprise.tools.upgrade.common.arguments.ArgumentHandler;

/**
 * Utility to evaluate the CLI input arguments and prompt
 * the user for missing data or data determined to be invalid.
 *
 * @author rebeccas
 */
public class InteractiveInputImpl implements InteractiveInput{
	private CommonInfoModel commonInfo = CommonInfoModel.getInstance();
	private Map<String, ArgumentHandler> inputMap;
	private StringManager sm = StringManager.getManager(InteractiveInputImpl.class);
	
	/** Creates a new instance of InteractiveInputImpl */
	public InteractiveInputImpl() {
	}
	
	public void processArguments(Map<String, ArgumentHandler> inputMap){
		this.inputMap = inputMap;
		
		try {
			System.out.println(CLIConstants.CLI_USER_INSTRUCTIONS);
			sourcePrompt();
			targetPrompt();
			if (!CommonInfoModel.getInstance().isUpgradeSupported()){
				System.exit(1);
			}
			adminPrompt();
			adminPasswordPrompt();
			masterPasswordPrompt();
		}catch(Exception e) {
			getLogger().log(Level.SEVERE,
			sm.getString("enterprise.tools.upgrade.cli.unexpectedException"),
			e);
		}
		
		//- verify user credentials
		Credentials c = commonInfo.getSource().getDomainCredentials(); 
		verifyUserAndPasswords(c.getAdminUserName(), c.getAdminPassword(), 
			c.getMasterPassword());		
	}
	
	/**
	 *  Collect the users response from stnd input.
	 */
	private String getResponse() throws Exception{
		String response = null;
		byte b[] = new byte[1024];
		int c = System.in.read(b);
		if (c == -1) { // input stream closed, maybe by ^C
			System.exit(1);
		}
		response = new String(b,0,c);
		return response.trim();
	}
	
	private void sourcePrompt()throws Exception {
		ArgumentHandler tmpA = inputMap.get(CLIConstants.SOURCE_SHORT);
		if (tmpA == null){
			tmpA = inputMap.get(CLIConstants.SOURCE);
		}
		if(tmpA == null) {
			System.out.print(
				sm.getString("enterprise.tools.upgrade.cli.Source_input"));
			
			String source = getResponse();
			tmpA = new ARG_source();
			tmpA.setRawParameters(source);
			inputMap.put(CLIConstants.SOURCE,tmpA);
		}
		//Check if input is a valid source directory input
		if (tmpA.isValidParameter()){
			tmpA.exec();
		} else {
			getLogger().severe(
				sm.getString("enterprise.tools.upgrade.cli.not_valid_source_install"));
			inputMap.remove(CLIConstants.SOURCE_SHORT);
			inputMap.remove(CLIConstants.SOURCE);
			sourcePrompt();
		}
	}
	
	private void targetPrompt()throws Exception{
		ArgumentHandler tmpA = inputMap.get(CLIConstants.TARGET_SHORT);
		if (tmpA == null){
			tmpA = inputMap.get(CLIConstants.TARGET);
		}
		if(tmpA == null) {
			System.out.print(
				sm.getString("enterprise.tools.upgrade.cli.Target_input"));
			
			String target = getResponse();
			tmpA = new ARG_target();
			tmpA.setRawParameters(target);
			inputMap.put(CLIConstants.TARGET,tmpA);
		}
		
		if (tmpA.isValidParameter()){
			tmpA.exec();
		} else {
			getLogger().severe(sm.getString("" +
				"enterprise.tools.upgrade.cli.not_valid_target_install"));
			inputMap.remove(CLIConstants.TARGET_SHORT);
			inputMap.remove(CLIConstants.TARGET);
			targetPrompt();
		}
	}
	
	private void adminPrompt()throws Exception{
		ArgumentHandler tmpA = inputMap.get(CLIConstants.ADMINUSER_SHORT);
		if (tmpA == null){
			tmpA = inputMap.get(CLIConstants.ADMINUSER);
		}
		if(tmpA == null) {
			System.out.print(
				sm.getString("enterprise.tools.upgrade.cli.adminuser_input"));
			
			String admiuser = getResponse();
			tmpA = new ARG_adminuser();
			tmpA.setRawParameters(admiuser);
			inputMap.put(CLIConstants.ADMINUSER,tmpA);
		}
		tmpA.exec();
	}
	
	private void adminPasswordPrompt()throws Exception{
		ArgumentHandler tmpA = inputMap.get(CLIConstants.ADMINPASSWORD_SHORT);
		if (tmpA == null){
			tmpA = inputMap.get(CLIConstants.ADMINPASSWORD);
		}
		//Getting source edition since target domain's profile will be based on this.
		String sourceEdition = commonInfo.getSource().getEdition();
		if(tmpA == null) {
			System.out.print(
				sm.getString("enterprise.tools.upgrade.cli.adminpassword_input"));
			
			String adminPassword =  new CliUtil().getPassword();
			tmpA = new ARG_adminpassword();
			tmpA.setRawParameters(adminPassword);
			inputMap.put(CLIConstants.ADMINPASSWORD,tmpA);
		}
		tmpA.exec();
	}
	
	private void masterPasswordPrompt()throws Exception{
		ArgumentHandler tmpA = inputMap.get(CLIConstants.MASTERPASSWORD_SHORT);
		if (tmpA == null){
			tmpA = inputMap.get(CLIConstants.MASTERPASSWORD);
		}
		if(tmpA == null) {
			System.out.print(
				sm.getString("enterprise.tools.upgrade.cli.MasterPW_input"));
			String password =  new CliUtil().getPassword();
			tmpA = new ARG_masterpassword();
			tmpA.setRawParameters(password);
			inputMap.put(CLIConstants.MASTERPASSWORD, tmpA);
		}
		tmpA.exec();
	}
	
	private void verifyUserAndPasswords(String adminUser, String adminPassword,
		String masterPassword) {
		if(!UpgradeUtils.getUpgradeUtils(commonInfo).
			validateUserDetails(adminUser,adminPassword,masterPassword)) {
			getLogger().severe(sm.getString(
				"enterprise.tools.upgrade.cli.wrong_adminuser_or_adminpassword_or_masterpassword"));
			//- cleanup and try again
			commonInfo.getSource().getDomainCredentials().setAdminUserName(null);
			commonInfo.getSource().getDomainCredentials().setAdminPassword(null);
			commonInfo.getSource().getDomainCredentials().setMasterPassword(null);
 			inputMap.remove(CLIConstants.ADMINUSER);
			inputMap.remove(CLIConstants.ADMINPASSWORD);
			inputMap.remove(CLIConstants.MASTERPASSWORD);
			inputMap.remove(CLIConstants.ADMINUSER_SHORT);
			inputMap.remove(CLIConstants.ADMINPASSWORD_SHORT);
			inputMap.remove(CLIConstants.MASTERPASSWORD_SHORT);
			try{
				adminPrompt();
				adminPasswordPrompt();
				masterPasswordPrompt();
			}catch(Exception e) {
				getLogger().log(Level.SEVERE,
					sm.getString("enterprise.tools.upgrade.cli.unexpectedException"),
					e);
		}
			verifyUserAndPasswords(commonInfo.getSource().getDomainCredentials().getAdminUserName(),
			commonInfo.getSource().getDomainCredentials().getAdminPassword(), commonInfo.getSource().getDomainCredentials().getMasterPassword());
		}
	}
	
	private Logger getLogger() {
		return LogService.getLogger(LogService.UPGRADE_LOGGER);
	}
	
	public void helpUsage(String str){
		System.out.println("\n" + str + "\n");
		helpUsage();
	}
	
	public void helpUsage(){
		helpUsage(0);
	}
	
	public void helpUsage(int exitCode) {
		System.out.println("(FIX THIS) InteractiveInputImpl:helpUsage:exitcode: " + exitCode);
	}
}
