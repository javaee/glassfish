/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.OS;


/**
 *  start-database command
 *  This command class will invoke DerbyControl to first ping
 *  if the database is running.  If not then it will start
 *  the database.  If the database is already started, then
 *  a message will be displayed to the user.
 *
 *  @author <a href="mailto:jane.young@sun.com">Jane Young</a>
 *  @author Bill Shannon
 */
@Service(name = "start-database")
@Scoped(PerLookup.class)
public final class StartDatabaseCommand extends DatabaseCommand {
    private final static String DB_HOME         = "dbhome";
    private final static String DATABASE_DIR_NAME = "databases";
    private String dbHome;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(StartDatabaseCommand.class);

    /**
     * The prepare method must ensure that the commandOpts,
     * operandType, operandMin, and operandMax fields are set.
     */
    @Override
    protected void prepare()
            throws CommandException, CommandValidationException {
        Set<ValidOption> opts = new LinkedHashSet<ValidOption>();
        addOption(opts, DB_HOST, '\0', "STRING", false, DB_HOST_DEFAULT);
        addOption(opts, DB_PORT, '\0', "STRING", false, DB_PORT_DEFAULT);
        addOption(opts, DB_HOME, '\0', "FILE", false, null);
        // not a remote command so have to process --terse and --echo ourselves
        addOption(opts, "terse", '\0', "BOOLEAN", false, "false");
        addOption(opts, "echo", '\0', "BOOLEAN", false, "false");
        addOption(opts, "help", '?', "BOOLEAN", false, "false");
        commandOpts = Collections.unmodifiableSet(opts);
        operandType = "STRING";
        operandMin = 0;
        operandMax = 0;
    }

    /**
     * The validate method validates that the type and quantity of
     * parameters and operands matches the requirements for this
     * command.  The validate method supplies missing options from
     * the environment.  It also supplies passwords from the password
     * file or prompts for them if interactive.
     */
    protected void validate()
            throws CommandException, CommandValidationException  {
        super.validate();

        // if --terse or -echo are supplied, copy them over to program options
        if (options.containsKey("echo"))
            programOpts.setEcho(getBooleanOption("echo"));
        if (options.containsKey("terse"))
            programOpts.setTerse(getBooleanOption("terse"));
        initializeLogger();     // in case program options changed
    }

    /**
     *  defines the command to start the derby database
     *  Note that when using Darwin (Mac), the property,
     *  "-Dderby.storage.fileSyncTransactionLog=True" is defined.
     */
    public String[] startDatabaseCmd() throws Exception {
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
    public String[] sysinfoCmd() throws Exception {
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
        } else {
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
    private String getDatabaseHomeDir() {
        // dbhome is specified then return the dbhome option value
        if (getOption(DB_HOME) != null)
            return getOption(DB_HOME);

        // check if current directory contains derby.log
        // for now we are going to rely on derby.log file to ascertain
        // whether the current directory is where databases were created.
        // However, this may not always be right.
        // The reason for this behavior is so that it is
        // compatible with 8.2PE and 9.0 release.
        // In 8.2PE and 9.0, the current directory is the
        // default dbhome.  
        final String currentDir = System.getProperty("user.dir");
        if ((new File(currentDir, DerbyControl.DB_LOG_FILENAME)).exists())
            return currentDir;
        // the default dbhome is <AS_INSTALL>/databases
        final File installPath = GFLauncherUtils.getInstallDir();

        if (installPath != null) {
            final File dbDir = new File(installPath, DATABASE_DIR_NAME);
            dbDir.mkdir();
            try {
                return dbDir.getCanonicalPath();
            } catch (IOException ioe) {
                // if unable to get canonical path, then return absolute path
                return dbDir.getAbsolutePath();
            }
        }
        // hopefully it'll never get here.  if installPath is null then
        // asenv.conf is incorrect.
        return null;
    }

    /**
     *  method that execute the command
     *  @throws CommandException
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {
        final CLIProcessExecutor cpe = new CLIProcessExecutor();
        String dbLog = "";
        int exitCode = 0;
        try {
            prepareProcessExecutor();
            dbHome = getDatabaseHomeDir();
            if (dbHome != null)
                dbLog = dbHome + File.separator + DerbyControl.DB_LOG_FILENAME;

            logger.printDebugMessage("Ping Database");
            cpe.execute("pingDatabaseCmd", pingDatabaseCmd(true), true);
            // if ping is unsuccesfull then database is not up and running
            if (cpe.exitValue() > 0) {
                logger.printDebugMessage("Start Database");
                cpe.execute("startDatabaseCmd", startDatabaseCmd(), false);
                if (cpe.exitValue() != 0) {
                    throw new CommandException(strings.get("UnableToStartDatabase", dbLog));
                }
            } else if (cpe.exitValue() < 0) {
                // Something terribly wrong!
                throw new CommandException(strings.get("CommandUnSuccessful", name));
            } else {
                // database already started
                logger.printMessage(strings.get("StartDatabaseStatus", dbHost, dbPort));
            }
        } catch (IllegalThreadStateException ite) {
            // IllegalThreadStateException is thrown if the 
            // process has not yet teminated and is still running.
            // see http://java.sun.com/j2se/1.5.0/docs/api/java/lang/Process.html#exitValue()
            // This is good since that means the database is up and running.
            CLIProcessExecutor cpePing = new CLIProcessExecutor();
            CLIProcessExecutor cpeSysInfo = new CLIProcessExecutor();
            try {
                if (!(programOpts.isTerse() || getBooleanOption("terse"))) {
                    // try getting sysinfo
                    logger.printDetailMessage(strings.get("database.info.msg", dbHost, dbPort));
                }
                cpePing.execute("pingDatabaseCmd", pingDatabaseCmd(true), true);
                int counter = 0;
                //give time for the database to be started
                while (cpePing.exitValue() != 0 && counter < 10) {
                    cpePing.execute("pingDatabaseCmd", pingDatabaseCmd(true), true);
                    Thread.sleep(500);
                    counter++;
                    //break out if start-database failed
                    try {
                        cpe.exitValue();
                        break;
                    } catch (IllegalThreadStateException itse) {
                        continue;
                    }
                }
                if (!(programOpts.isTerse() || getBooleanOption("terse"))) {
                    logger.printDebugMessage("Database SysInfo");
                    if (cpePing.exitValue() == 0) {
                        cpeSysInfo.execute("sysinfoCmd", sysinfoCmd(), true);
                        if (cpeSysInfo.exitValue() != 0) {
                            logger.printMessage(strings.get("CouldNotGetSysInfo"));
                        }
                    }
                }
            } catch (Exception e) {
                throw new CommandException(strings.get("CommandUnSuccessful", name), e);
            }
            if (cpePing.exitValue() == 0) {
                logger.printMessage(strings.get("DatabaseStartMsg"));
                if ((new File(dbLog)).canWrite()) {
                    logger.printMessage(strings.get("LogRedirectedTo", dbLog));
                }
            } else {
                throw new CommandException(strings.get("UnableToStartDatabase", dbLog));
            }
        } catch (Exception e) {
            throw new CommandException(strings.get("CommandUnSuccessful", name), e);
        }
        return exitCode;
    }
}
