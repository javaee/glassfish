/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.admin.cli;

import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.util.OS;
import java.io.File;

/**
 *  stop-database command
 *  This command class will invoke DerbyControl to stop
 *  the database.
 *  @author <a href="mailto:jane.young@sun.com">Jane Young</a>
 *  @version  $Revision: 1.11 $
 */
public final class StopDatabaseCommand extends DatabaseCommand
{

    /**
     *  An  method that validates the options 
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
       defines the command to stop the derby database
       Note that when using Darwin (Mac), the property,
       "-Dderby.storage.fileSyncTransactionLog=True" is defined.
    */
    public String[] stopDatabaseCmd() throws Exception
    {
        if (OS.isDarwin()) {
            return new String [] {
                sJavaHome+File.separator+"bin"+File.separator+"java",
                "-Djava.library.path="+sInstallRoot+File.separator+"lib",
                "-Dderby.storage.fileSyncTransactionLog=True",
                "-cp",
                sClasspath + File.pathSeparator + sDatabaseClasspath,
                "com.sun.enterprise.admin.cli.DerbyControl",
                "shutdown",
                dbHost, dbPort, "false"
             };
        }
        return new String [] {
            sJavaHome+File.separator+"bin"+File.separator+"java",
            "-Djava.library.path="+sInstallRoot+File.separator+"lib",
            "-cp",
            sClasspath + File.pathSeparator + sDatabaseClasspath,
            "com.sun.enterprise.admin.cli.DerbyControl",
            "shutdown",
            dbHost, dbPort, "false"
       };
    }
    

    
    /**
     *  Method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        if (!validateOptions())
            throw new CommandValidationException("Validation is false");

        try {
            prepareProcessExecutor();
            CLIProcessExecutor cpe = new CLIProcessExecutor();
            cpe.execute(pingDatabaseCmd(false), true);
            if (cpe.exitValue() > 0) {
                    //if ping is unsuccesfull then database is not up and running
                throw new CommandException(getLocalizedString("StopDatabaseStatus", new Object[]{dbHost, dbPort}));
            }
            else if (cpe.exitValue() <0) {
                    // Something terribly wrong!
                throw new CommandException(getLocalizedString("CommandUnSuccessful",
                                                              new Object[] {name} ));
            }
            else {
                    //database is running so go ahead and stop the database
                cpe.execute(stopDatabaseCmd(), true);
                if (cpe.exitValue() > 0) {
                    throw new CommandException(getLocalizedString("CommandUnSuccessful",
                                                              new Object[] {name} ));
                }
                CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                           "CommandSuccessful",
                                                           new Object[] {name}));
            }
        }
        catch (Exception e) {
            throw new CommandException(getLocalizedString("CommandUnSuccessful",
                                                              new Object[] {name}), e);
        }
    }
}
