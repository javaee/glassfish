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

package com.sun.enterprise.admin.cli.optional;

import com.sun.enterprise.admin.cli.CLIProcessExecutor;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import com.sun.enterprise.util.OS;
import java.io.File;
import java.io.IOException;


/**
 *  start-database command
 *  This command class will invoke DerbyControl to first ping
 *  if the database is running.  If not then it will start
 *  the database.  If the database is already started, then
 *  a message will be displayed to the user.
 *  @author <a href="mailto:jane.young@sun.com">Jane Young</a>
 *  @version  $Revision: 1.13 $
 */
public final class StartDatabaseCommand extends DatabaseCommand
{
    private final static String DB_HOME         = "dbhome";
    private final static String DATABASE_DIR_NAME = "databases";
    private String dbHome;

    /**
     *  An method that validates the options 
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
     *  defines the command to start the derby database
     *  Note that when using Darwin (Mac), the property,
     *  "-Dderby.storage.fileSyncTransactionLog=True" is defined.
     */
    public String[] startDatabaseCmd() throws Exception
    {
        if (OS.isDarwin()) {
            return new String [] {
                sJavaHome+File.separator+"bin"+File.separator+"java",
                "-Djava.library.path="+sInstallRoot+File.separator+"lib",
                "-Dderby.storage.fileSyncTransactionLog=True",
                "-cp",
                sClasspath + File.pathSeparator + sDatabaseClasspath,
                "com.sun.enterprise.admin.cli.optional.DerbyControl",
                "start",
                dbHost, dbPort, "true", dbHome
            };
        }
        else {
            return new String [] {
                sJavaHome+File.separator+"bin"+File.separator+"java",
                "-Djava.library.path="+sInstallRoot+File.separator+"lib",
                "-cp",
                sClasspath + File.pathSeparator + sDatabaseClasspath,
                "com.sun.enterprise.admin.cli.optional.DerbyControl",
                "start",
                dbHost, dbPort, "true", dbHome
            };
        }
    }


    /**
     *  defines the command to print out the database sysinfo
     *  Note that when using Darwin (Mac), the property,
     *  "-Dderby.storage.fileSyncTransactionLog=True" is defined.
     */
    public String[] sysinfoCmd() throws Exception
    {
        if (OS.isDarwin()) {
            return new String [] {
                sJavaHome+File.separator+"bin"+File.separator+"java",
                "-Djava.library.path="+sInstallRoot+File.separator+"lib",
                "-Dderby.storage.fileSyncTransactionLog=True",
                "-cp",
                sClasspath + File.pathSeparator + sDatabaseClasspath,
                "com.sun.enterprise.admin.cli.optional.DerbyControl",
                "sysinfo",
               dbHost, dbPort, "false"
            };
        }
        else {
            return new String [] {
                sJavaHome+File.separator+"bin"+File.separator+"java",
                "-Djava.library.path="+sInstallRoot+File.separator+"lib",
                "-cp",
                sClasspath + File.pathSeparator + sDatabaseClasspath,
                "com.sun.enterprise.admin.cli.optional.DerbyControl",
                "sysinfo",
                dbHost, dbPort, "false"
            };
        }
    }


    /**
     *  This method returns dbhome.
     *  If dbhome option is specified, then the option value is returned.
     *  If not, then go through series of conditions to determine the
     *  default dbhome directory.
     *  The conditions are as follow:
     *    1.  if derby.log exists in the current directory, then that is
     *        the default dbhome directory.
     *    2.  if derby.log does not exist in the current directory, then
     *        create a "databases" in <parent directory of domains>.  This
     *        is usually <install-dir> in filebased installation.  In
     *        package based installation this directory is /var/SUNWappserver.
     */
    private String getDatabaseHomeDir()
    {
        //dbhome is specified then return the dbhome option value
        if (getOption(DB_HOME) !=null) return getOption(DB_HOME);

            //check if current directory contains derby.log
            //for now we are going to rely on derby.log file to ascertain
            //whether the current directory is where databases were created.
            //However, this may not always be right.
            //The reason for this behavior is so that it is
            //compatible with 8.2PE and 9.0 release.
            //In 8.2PE and 9.0, the current directory is the
            //default dbhome.  
            final String currentDir = System.getProperty("user.dir");
            if ((new File(currentDir, DerbyControl.DB_LOG_FILENAME)).exists())
                return currentDir;
            //the default dbhome is <AS_INSTALL>/databases
            final File installPath = GFLauncherUtils.getInstallDir();

            if (installPath != null) {
                final File dbDir = new File(installPath, DATABASE_DIR_NAME);
                dbDir.mkdir();
                try {
                    return dbDir.getCanonicalPath();
                }
                catch (IOException ioe) {
                    //if unable to get canonical path, then return the absolute path
                    return dbDir.getAbsolutePath();
                }
            }
        //hopefully it'll never get here.  if installPath is null then
        //asenv.conf is incorrect.
        return null;
    }
    


    /**
     *  method that execute the command
     *  @throws CommandException
    */
    public void runCommand() throws CommandException, CommandValidationException
    {
        validateOptions();
        final CLIProcessExecutor cpe = new CLIProcessExecutor();
	    String dbLog = "";
        try {
            prepareProcessExecutor();
            dbHome = getDatabaseHomeDir();
            if (dbHome != null)
                dbLog = dbHome + File.separator + DerbyControl.DB_LOG_FILENAME;

            CLILogger.getInstance().printDebugMessage("Ping Database");
            cpe.execute("pingDatabaseCmd", pingDatabaseCmd(true), true);
            //if ping is unsuccesfull then database is not up and running
            if (cpe.exitValue() > 0) {
	            CLILogger.getInstance().printDebugMessage("Start Database");
                cpe.execute("startDatabaseCmd", startDatabaseCmd(), false);
                if (cpe.exitValue() != 0) {
                    throw new CommandException(getLocalizedString("UnableToStartDatabase",
                                                     new Object[]{dbLog}));
                }
            }
            else if (cpe.exitValue() < 0) {
                    // Something terribly wrong!
                throw new CommandException(getLocalizedString("CommandUnSuccessful",
                                                              new Object[] {name} ));
            }
            else {
                    //database already started
                CLILogger.getInstance().printMessage(getLocalizedString(
                                                     "StartDatabaseStatus",
                                                     new Object[]{dbHost, dbPort}));
            }
        }
        catch (IllegalThreadStateException ite) {
            // IllegalThreadStateException is thrown if the 
            // process has not yet teminated and is still running.
	        // see http://java.sun.com/j2se/1.5.0/docs/api/java/lang/Process.html#exitValue()
            // This is good since that means the database is up and running.

            if (!getBooleanOption(TERSE)) {
                try {
	                CLILogger.getInstance().printDetailMessage(
                                            getLocalizedString("database.info.msg",
                                            new Object[]{dbHost, dbPort}));
                    //try getting sysinfo
	                CLILogger.getInstance().printDebugMessage("Database SysInfo");
		            new CLIProcessExecutor().execute("sysinfoCmd", sysinfoCmd(), true);
                }
                catch (Exception e) {
                    throw new CommandException(getLocalizedString("CommandUnSuccessful",
                                                           new Object[] {name}), e);
                }
            }
            CLILogger.getInstance().printMessage(getLocalizedString("DatabaseStartMsg"));
	        if ((new File(dbLog)).canWrite()) {
	            CLILogger.getInstance().printMessage(getLocalizedString("LogRedirectedTo",
                                                     new Object[]{dbLog}));
	        }
        }
        catch (Exception e) {
            throw new CommandException(getLocalizedString("CommandUnSuccessful",
                                                              new Object[] {name}), e);
        }
    }

}
