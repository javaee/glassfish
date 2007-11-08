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

import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.io.FileUtils;
import java.io.File;


/**
 *  This is an abstract class to be inherited by
 *  StartDatabaseCommand and StopDatabaseCommand.
 *  This classes prepares the variables that is used to
 *  to invoke DerbyControl.  It also contains a pingDatabase
 *  method that is used by both start/stop database command.
 *  @author <a href="mailto:jane.young@sun.com">Jane Young</a> 
 *  @version  $Revision: 1.4 $
 */
public abstract class DatabaseCommand extends S1ASCommand
{
    private final static String DB_HOST       = "dbhost";
    private final static String DB_PORT       = "dbport";
    
    protected String dbHost;
    protected String dbPort;
    protected String dbLocation;
    protected String sJavaHome;
    protected String sInstallRoot;
    protected String sClasspath;
    protected String sDatabaseClasspath;

    /**
     * Prepare variables to invoke start/ping database command
     * DatabaseCommand.
     */
    protected void prepareProcessExecutor() throws Exception
    {
        dbHost = getOption(DB_HOST);
        dbPort = getOption(DB_PORT);
	checkIfPortIsValid(dbPort);
        dbLocation = System.getProperty(SystemPropertyConstants.DERBY_ROOT_PROPERTY);
        sJavaHome = System.getProperty(SystemPropertyConstants.JAVA_ROOT_PROPERTY);
        sInstallRoot = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
	//	sClasspath = System.getProperty("java.class.path");
	sClasspath = sInstallRoot+File.separator+"lib"+File.separator+
                     "appserv-rt.jar"+File.pathSeparator+sInstallRoot+
	             File.separator+"lib"+File.separator+"admin-cli.jar";
        sDatabaseClasspath = dbLocation+File.separator+"lib"+
                                       File.separator+"derby.jar"+
                                       File.pathSeparator+dbLocation+
                                       File.separator+"lib"+File.separator+
                                       "derbytools.jar"+File.pathSeparator+
                                       dbLocation+File.separator+"lib"+
                                       File.separator+"derbynet.jar"+
                                       File.pathSeparator+dbLocation+File.separator+
                                       "lib"+File.separator+"derbyclient.jar";
    }


  /** check if database port is valid.
   *  Derby does not check this so need to add code to check the port number.
   */
    private void checkIfPortIsValid(final String port) throws CommandValidationException
    {
        try
        {
            Integer.parseInt(port);
        }
        catch(Exception e)
        {
            throw new CommandValidationException(getLocalizedString("InvalidPortNumber", new Object[] {port}));
        }
    }
    


    /**
       defines the command to ping the derby database
       Note that when using Darwin (Mac), the property,
       "-Dderby.storage.fileSyncTransactionLog=True" is defined.
    */
    protected String[] pingDatabaseCmd(boolean bRedirect) throws Exception
    {
        if (OS.isDarwin()) {
            return new String[]{sJavaHome + File.separator + "bin" + File.separator +
                         "java",
                    "-Djava.library.path=" + sInstallRoot + File.separator +
                    "lib", "-Dderby.storage.fileSyncTransactionLog=True", "-cp",
                    sClasspath + File.pathSeparator + sDatabaseClasspath,
                    "com.sun.enterprise.cli.commands.DerbyControl", "ping",
                    dbHost, dbPort, Boolean.valueOf(bRedirect).toString()};
        }
        else {
            return new String[]{sJavaHome + File.separator + "bin" + File.separator +
                         "java",
                    "-Djava.library.path=" + sInstallRoot + File.separator +
                    "lib", "-cp",
                    sClasspath + File.pathSeparator + sDatabaseClasspath,
                    "com.sun.enterprise.cli.commands.DerbyControl", "ping",
                    dbHost, dbPort, Boolean.valueOf(bRedirect).toString()};
        }
    }
}
