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

package com.sun.enterprise.cli.commands;

import com.sun.enterprise.cli.framework.*;

import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.server.Constants;
import com.sun.enterprise.admin.common.JMXFileTransfer;
import com.sun.enterprise.util.FileUtil;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.admin.common.constant.DeploymentConstants;


// jdk imports
import java.io.File;
import java.util.Iterator;
import java.util.Properties;


/**
 *  This is the GetClientStubs command
 */
public class GetClientStubsCommand extends S1ASCommand
{
    private String downloadDir;
    private String appName;
    public static final String APP_NAME = "appname";

    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() 
            throws CommandException, CommandValidationException
    {
        validateOptions();

	//use http connector
	final MBeanServerConnection mbsc = getMBeanServerConnection(getHost(), 
								    getPort(), 
								    getUser(), 
								    getPassword());

        String path = getClientStub(mbsc);

	// getClientStub(mbsc,retrievePath, type);

        CLILogger.getInstance().printDetailMessage(getLocalizedString(
        				     "CommandSuccessful",
	 				     new Object[] {name} ) );
    }


    /**
     *  An abstract method that validates the options 
     *  on the specification in the xml properties file
     *  This method verifies for the correctness of number of 
     *  operands and if all the required options are supplied by the client.
     *  @return boolean returns true if success else returns false
     */
    public boolean validateOptions() throws CommandValidationException
    {
        super.validateOptions();
        downloadDir = (String) getOperands().get(0);
	validateDirectory();
        appName = getAppName();
	return true;
    }


    /** 
     *  check if file path exist on the local file system
     *  @throws CommandValidationExcetion if file not found
     */
    private void validateDirectory() throws CommandValidationException
    {
        File dlDir = new File(downloadDir);
	if (!dlDir.exists() )
        {
            dlDir.mkdirs();
        }
        if(!dlDir.exists() || !dlDir.canWrite() || !dlDir.isDirectory() ) {
            throw new CommandValidationException(getLocalizedString(
						 "InvalidDirectory",new Object [] {downloadDir}));
        }
    }
    
    private String getAppName() throws CommandValidationException
    {
        String name = getOption(APP_NAME);
        return name;
    }



	
    /**
     *  retrieve client stub from server
     */
    private String getClientStub(MBeanServerConnection mbsc)
	throws CommandException
    {
	try 
	{

	    final String fileName = new JMXFileTransfer(mbsc).downloadClientStubs(
							 appName, 
							 downloadDir);
	    CLILogger.getInstance().printDebugMessage("Downloaded client stubs to: " + fileName);
        return fileName;
	}
	catch (Exception e)
	{
            Throwable t = e.getCause();
            while(t!=null && t.getCause()!=null)
                t=t.getCause();
            if(t==null)
                t=e;
	    throw new CommandException(t.getLocalizedMessage(),t);
	}
    }


}
