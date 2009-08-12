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

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_adminuser;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_adminpassword;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_masterpassword;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_source;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_target;
import com.sun.enterprise.tools.upgrade.common.arguments.ArgumentHandler;
import com.sun.enterprise.tools.upgrade.logging.LogService;

/**
 * Utility to evaluate the CLI input arguments and prompt
 * the user for missing data or data determined to be invalid.
 *
 * @author rebeccas
 */
public class InteractiveInputImpl implements DirectoryMover, InteractiveInput {

    private static final Logger logger = LogService.getLogger();
    
    private Map<String, ArgumentHandler> inputMap;
    private static final StringManager sm =
        StringManager.getManager(InteractiveInputImpl.class);
    private static final CommonInfoModel commonInfoModel =
        CommonInfoModel.getInstance();

	
    ///-public void processArguments(Map<String, ArgumentHandler> inputMap){
	public void processArguments(ArrayList<ArgumentHandler> aList){
        int cnt = aList.size();
		this.inputMap = new HashMap<String, ArgumentHandler>();
		for (int i =0; i < cnt; i++){
			ArgumentHandler tmpAh = aList.get(i);
			inputMap.put(tmpAh.getCmd(), tmpAh);
		}

		try {
			sourcePrompt();
			targetPrompt();
			if (!CommonInfoModel.getInstance().isUpgradeSupported()){
				System.exit(1);
			}
			adminPrompt();
			adminPasswordPrompt();
			masterPasswordPrompt();
		}catch(IOException e) {
            logger.log(Level.SEVERE,
                sm.getString(
                "enterprise.tools.upgrade.cli.unexpectedException"), e);
		}		
	}
	
	/**
	 *  Collect the users response from stnd input.
	 */
	private String getResponse() throws IOException {
		String response = null;
		byte b[] = new byte[1024];
		int c = System.in.read(b);
		if (c == -1) { // input stream closed, maybe by ^C
			System.exit(1);
		}
		response = new String(b,0,c);
		return response.trim();
	}
	
	private void sourcePrompt() throws IOException {
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
			logger.severe(
				sm.getString("enterprise.tools.upgrade.cli.not_valid_source_install"));
			inputMap.remove(CLIConstants.SOURCE_SHORT);
			inputMap.remove(CLIConstants.SOURCE);
			sourcePrompt();
		}
	}
	
	private void targetPrompt() throws IOException{
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

        // in the interactive CLI case, we'll allow users to fix name clashes
        tmpA.getCommonInfo().getTarget().setDirectoryMover(this);
		if (tmpA.isValidParameter()){
			tmpA.exec();
		} else {
			logger.severe(sm.getString("" +
				"enterprise.tools.upgrade.cli.not_valid_target_install"));
			inputMap.remove(CLIConstants.TARGET_SHORT);
			inputMap.remove(CLIConstants.TARGET);
			targetPrompt();
		}
	}
	
	private void adminPrompt() throws IOException{
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
	
	private void adminPasswordPrompt() throws IOException{
		ArgumentHandler tmpA = inputMap.get(CLIConstants.ADMINPASSWORD_SHORT);
		if (tmpA == null){
			tmpA = inputMap.get(CLIConstants.ADMINPASSWORD);
		}
		
		if(tmpA == null) {
            String adminPassword = getPasswordResponse(sm.getString("enterprise.tools.upgrade.cli.adminpassword_input"));
			tmpA = new ARG_adminpassword();
			tmpA.setRawParameters(adminPassword);
			inputMap.put(CLIConstants.ADMINPASSWORD,tmpA);
		}
		tmpA.exec();
	}
	
	private void masterPasswordPrompt() throws IOException{
		ArgumentHandler tmpA = inputMap.get(CLIConstants.MASTERPASSWORD_SHORT);
		if (tmpA == null){
			tmpA = inputMap.get(CLIConstants.MASTERPASSWORD);
		}
		if(tmpA == null) {
            String password = getPasswordResponse(sm.getString("enterprise.tools.upgrade.cli.MasterPW_input"));
			tmpA = new ARG_masterpassword();
			tmpA.setRawParameters(password);
			inputMap.put(CLIConstants.MASTERPASSWORD, tmpA);
		}
		tmpA.exec();
	}

    private String getPasswordResponse(String prompt){
        String optionValue;
        try {
            InputsAndOutputs.getInstance().getUserOutput().print(prompt);
            InputsAndOutputs.getInstance().getUserOutput().flush();
            optionValue = new CliUtil().getPassword();
        } catch (java.lang.NoClassDefFoundError e) {
            optionValue = readInput();
        } catch (java.lang.UnsatisfiedLinkError e) {
            optionValue = readInput();
        } catch (Exception e) {
            optionValue=null;
        }
        return optionValue;
    }

    private String readInput() {
        try {
            return InputsAndOutputs.getInstance().getUserInput().readLine();
        } catch (IOException ioe) {
            return null;
        }
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

    /**
     * Ask the user whether or not to move the
     * conflicting directory.
     */
    public boolean moveDirectory(File dir) {
        try {
            System.out.print(sm.getString(
                "enterprise.tools.upgrade.cli.move_dir",
                dir.getName()));
            String response = getResponse();
            String yesOption =
                sm.getString("enterprise.tools.upgrade.cli.yes_option");
            boolean move = yesOption.equalsIgnoreCase(response);
            if (move) {
                UpgradeUtils.getUpgradeUtils(commonInfoModel).rename(dir);
            }
            return move;
        } catch (IOException ioe) {
            // if this was going to happen, it would have before now
            logger.warning(ioe.getLocalizedMessage());
            return false;
        }
    }
}
