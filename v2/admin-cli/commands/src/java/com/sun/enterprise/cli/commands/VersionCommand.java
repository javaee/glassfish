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
import com.sun.appserv.server.util.Version;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.IOException;


/**
 *  This command will get the version of the application server
 *  @version  $Revision: 1.4 $ 
 */
public class VersionCommand extends S1ASCommand
{
    private static final String VERBOSE = "verbose";
    private static final String FULL_VERSION_ATTR = "applicationServerFullVersion";
    private static final String SHORT_VERSION_ATTR = "applicationServerVersion";

    /**
     *  An abstract method that validates the options
     *  on the specification in the xml properties file
     *  This method verifies for the correctness of number of
     *  operands and if all the required options are supplied by the client.
     *  @return boolean returns true if success else returns false
     */
    public boolean validateOptions() throws CommandValidationException
    {
        return super.validateOptions();
    }

    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        //if validateOptions is false, then it must  be that --help option
        //is provided and there is no need to execute the command since
        //either manpage or usage text is displayed
        if (!validateOptions())
            return;

        String objectName = getObjectName();        
        try
        {
            //use http connector
            MBeanServerConnection mbsc = getMBeanServerConnection(getHost(), getPort(),
                                                                  getUser(), getPassword());

            //if (System.getProperty("Debug") != null) 
            //    printDebug(mbsc, objectName);
            String attribute;
            if (getBooleanOption("verbose"))
                attribute = FULL_VERSION_ATTR;
            else
                attribute = SHORT_VERSION_ATTR;

            Object returnValue = mbsc.getAttribute(new ObjectName(objectName),
                                             attribute);
            // handleReturnValue(returnValue);
            CLILogger.getInstance().printMessage(getLocalizedString("Version",
                                                 new Object[] {returnValue}));
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                       "CommandSuccessful",
                                                       new Object[] {name}));
        }
        catch (IOException ioe)
        {
            displayVersionLocally();
        }
        catch (CommandValidationException cve)
        {
            displayVersionLocally();
        }
        catch(Exception e)
        {
            if (e.getLocalizedMessage() != null)
                CLILogger.getInstance().printDetailMessage(e.getLocalizedMessage());
            throw new CommandException(getLocalizedString("CommandUnSuccessful",
                                                     new Object[] {name} ), e);
        }
    }

    /**
     *  This method gets the Version locally
     */
    private void displayVersionLocally()
    {
        //if there is problem communicating with admin server, then get version
        //locally
        CLILogger.getInstance().printDetailMessage(getLocalizedString("UnableToCommunicateWithAdminServer"));
        
        if (getBooleanOption("verbose"))
            CLILogger.getInstance().printMessage(getLocalizedString("Version",
                                                 new Object[] {Version.getFullVersion()}));
        else
            CLILogger.getInstance().printMessage(getLocalizedString("Version",
                                                 new Object[] {Version.getVersion()}));
        CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                       "CommandSuccessful",
                                                       new Object[] {name}));
    }
    

    /**
     * This method prints the objecName info for debugging purpose
     */
    private void printDebug(MBeanServerConnection mbsc, String objectName)
        throws Exception
    {
        CLILogger.getInstance().printDebugMessage("********** getMBeanInfo **********");
        final javax.management.MBeanInfo mbinfo = mbsc.getMBeanInfo(new ObjectName(objectName));
        CLILogger.getInstance().printDebugMessage("Description = " + mbinfo.getDescription());
        CLILogger.getInstance().printDebugMessage("Classname = " + mbinfo.getClassName());
        final javax.management.MBeanOperationInfo[] mboinfo = mbinfo.getOperations();
        for (int ii=0; ii<mboinfo.length; ii++)
        {
            CLILogger.getInstance().printDebugMessage("("+ii+") Description = " +
                                                      mboinfo[ii].getDescription());
            CLILogger.getInstance().printDebugMessage("("+ii+") Name = " +
                                                      mboinfo[ii].getName());
            CLILogger.getInstance().printDebugMessage("****** TYPE *****");
            final javax.management.MBeanParameterInfo[]  mbpi  = mboinfo[ii].getSignature();
            for (int kk=0; kk<mbpi.length; kk++)
            {
                CLILogger.getInstance().printDebugMessage("type = " + mbpi[kk].getType());
            }
            CLILogger.getInstance().printDebugMessage("returnType = " + mboinfo[ii].getReturnType());


        }
    }

    
    /*
     * Returns the user option value
     * @return user returns user option value
     * @throws CommandValidationException if the following is true:
     *  1.  --user option not on command line
     *  2.  user option not specified in environment
     *  3.  user option not specified in ASADMINPREFS file
     *  4.  user option not specified in .asadminpass file
     */
    protected String getUser() throws CommandValidationException
    {
        String userValue = getOption(USER);
        if (userValue == null)
        {
            // read from .asadminpass
            userValue = getUserFromASADMINPASS();
            
            // read from .asadminprefs
            if (userValue == null)
                userValue= getValuesFromASADMINPREFS(USER);
            if (userValue != null)
            {
                CLILogger.getInstance().printDebugMessage(
                                "user value read from " + ASADMINPREFS);
            }
        }
        return userValue;
    }

    
    /**
     * Returns the password option value. This is used by all asadmin commands 
     * that accept the --password option.
     * @return password returns password option value
     */
    protected String getPassword() throws CommandValidationException, CommandException
    {
        //getPassword(optionName, allowedOnCommandLine, readPrefsFile, readPasswordOptionFromPrefs, readMasterPasswordFile, mgr, config,
        //promptUser, confirm, validate)
        return getPassword(PASSWORD, "AdminPasswordPrompt", "AdminPasswordConfirmationPrompt", 
                            true, true, false, false, null, null, false, false, false, true);       
    }
    
     
}
