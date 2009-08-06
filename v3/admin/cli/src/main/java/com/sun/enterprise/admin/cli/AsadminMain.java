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

package com.sun.enterprise.admin.cli;

import java.io.*;
import java.text.*;
import java.util.*;

import com.sun.enterprise.admin.cli.remote.*;
import com.sun.enterprise.admin.cli.commands.ListCommandsCommand;
import com.sun.enterprise.cli.framework.ValidOption;
import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.InvalidCommandException;
import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.cli.framework.StringEditDistance;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.util.JDK;
import com.sun.enterprise.util.SystemPropertyConstants;

/**
 * The asadmin main program.
 */
public class AsadminMain {

    private       static String[] copyOfArgs;
    private       static String classPath;
    private       static String className;
    private       static String command;
    private       static Map<String, String> systemProps;
    private       static ProgramOptions po;

    private final static int ERROR = 1;
    private final static int CONNECTION_ERROR = 2;
    private final static int INVALID_COMMAND_ERROR = 3;
    private final static int SUCCESS = 0;
    private static final int MAX_COMMANDS_TO_DISPLAY = 75;
    private static final LocalStringsImpl strings =
                                new LocalStringsImpl(AsadminMain.class);

    static {
        systemProps = new ASenvPropertyReader().getProps();
        final String ir = SystemPropertyConstants.INSTALL_ROOT_PROPERTY;
        final String cr = SystemPropertyConstants.CONFIG_ROOT_PROPERTY;
        final String irVal = systemProps.get(ir);
        final String crVal = systemProps.get(cr);

        if (ok(irVal))
            System.setProperty(ir, irVal);

        if (ok(crVal))
            System.setProperty(cr, crVal);
    }

    public static void main(String[] args) {
        int minor = JDK.getMinor();

        if (minor < 6) {
            CLILogger.getInstance().printError(
                strings.get("OldJdk", "" + minor));
            System.exit(ERROR);
        }

        if (CLIConstants.debugMode) {
            System.setProperty(CLIConstants.WALL_CLOCK_START_PROP,
                "" + System.currentTimeMillis());
            CLILogger.getInstance().printDebugMessage("CLASSPATH= " +
                    System.getProperty("java.class.path") +
                    "\nCommands: " + Arrays.toString(args));
        }

        AsadminMain main = new AsadminMain();
        int exitCode;

        if (args.length <= 0)
             args = new String[] { "multimode" };

        copyOfArgs = new String[args.length];
        System.arraycopy(args, 0, copyOfArgs, 0, args.length);
        classPath =
            SmartFile.sanitizePaths(System.getProperty("java.class.path"));
        className = main.getClass().getName();

        command = args[0];
        exitCode = executeCommand(args);

        switch (exitCode) {
        case SUCCESS:
            if (!po.isTerse())
                CLILogger.getInstance().printDetailMessage(
                    strings.get("CommandSuccessful", command));
            break;

        case ERROR:
            CLILogger.getInstance().printDetailMessage(
                strings.get("CommandUnSuccessful", command));
            break;

        case INVALID_COMMAND_ERROR:
            CLILogger.getInstance().printDetailMessage(
                strings.get("CommandUnSuccessful", command));
            break;

        case CONNECTION_ERROR:
            CLILogger.getInstance().printDetailMessage(
                strings.get("CommandUnSuccessful", command));
            break;
        }
        writeCommandToDebugLog(args, exitCode);
        System.exit(exitCode);
    }

    public static int executeCommand(String[] argv) {
        CLICommand cmd = null;
        Environment env = new Environment();
        try {

            // if the first argument is an option, we're using the new form
            if (argv.length > 0 && argv[0].startsWith("-")) {
                /*
                 * Parse all the asadmin options, stopping at the first
                 * non-option, which is the command name.
                 */
                Parser rcp = new Parser(argv, 0,
                                ProgramOptions.getValidOptions(), false);
                Map<String, String> params = rcp.getOptions();
                po = new ProgramOptions(params, env);
                List<String> operands = rcp.getOperands();
                argv = operands.toArray(new String[operands.size()]);
            } else
                po = new ProgramOptions(env);
            po.toEnvironment(env);
            po.setProgramArguments(copyOfArgs);
            po.setClassPath(classPath);
            po.setClassName(className);
            if (argv.length == 0)
                argv = new String[] { "multimode" };
            command = argv[0];
            cmd = CLICommand.getCommand(command, po, env);
            return cmd.execute(argv);
        } catch (CommandException ce) {
            if (ce.getCause() instanceof InvalidCommandException) {
                // find closest match with local or remote commands
                CLILogger.getInstance().printError(ce.getMessage());
                try {
                    displayClosestMatch(command, getAllCommands(po, env),
                        strings.get("ClosestMatchedLocalAndRemoteCommands"));
                } catch (InvalidCommandException e) {
                    // not a big deal if we cannot help
                }
            } else if (ce.getCause() instanceof java.net.ConnectException) {
                // find closest match with local commands
                CLILogger.getInstance().printError(ce.getMessage());
                try {
                    displayClosestMatch(command, getLocalCommands(po, env),
                        strings.get("ClosestMatchedLocalCommands"));
                } catch (InvalidCommandException e) {
                    CLILogger.getInstance().printMessage(
                            strings.get("InvalidRemoteCommand", command));
                }
            } else
                CLILogger.getInstance().printError(ce.getMessage());
            return ERROR;
        } catch (CommandValidationException cve) {
            CLILogger.getInstance().printError(cve.getMessage());
            if (cmd == null)    // error parsing program options
                printUsage();
            else
                CLILogger.getInstance().printError(cmd.getUsage());
            return ERROR;
        }
    }

    /**
     * Print usage message for asadmin command.
     * XXX - should be derived from ProgramOptions.
     */
    private static void printUsage() {
        CLILogger.getInstance().printError(
"Usage: asadmin [-H|--host localhost] [-p|--port 4848] [-u|--user anonymous]\n"+
"\t[-W|--passwordfile file] [-s|--secure=false] [-e|--echo=false]\n" +
"\t[-I|--interactive=true] [-?|--help] [command [options] [operands]]");
    }

    /**
     * We avoid using real System properties.  Instead we use our own Map
     * @return a read-only Map of our system-wide properties
     */
    // XXX - not used?
    /*
    public static Map<String, String> getSystemProps() {
        return Collections.unmodifiableMap(systemProps);
    }
    */

    /**
     * Return all commands, local and remote.
     */
    // XXX - public only because it's used by MultimodeCommand
    public static String[] getAllCommands(ProgramOptions po,
                                Environment env) {
        try {
            ListCommandsCommand lcc =
                new ListCommandsCommand("list-commands", po, env);
            String[] remoteCommands = lcc.getRemoteCommands();
            String[] localCommands = lcc.getLocalCommands();
            String[] allCommands =
                    new String[localCommands.length + remoteCommands.length];
            System.arraycopy(localCommands, 0,
                allCommands, 0, localCommands.length);
            System.arraycopy(remoteCommands, 0,
                allCommands, localCommands.length, remoteCommands.length);
            return allCommands;
        } catch (CommandValidationException cve) {
            return null;
        } catch (CommandException ce) {
            return null;
        }
    }

    /**
     * Return all the known local commands.
     */
    // XXX - public only because it's used by MultimodeCommand
    public static String[] getLocalCommands(ProgramOptions po,
                                Environment env) {
        try {
            ListCommandsCommand lcc =
                new ListCommandsCommand("list-commands", po, env);
            return lcc.getLocalCommands();
        } catch (CommandException ce) {
            return new String[0];       // should never happen
        }
    }

    /**
     * Display the command from the list that are the closest match
     * to the specified command.
     */
    // XXX - public only because it's used by MultimodeCommand
    public static void displayClosestMatch(final String commandName,
                               final String[] commands, final String msg)
                               throws InvalidCommandException {
        try {
            // remove leading "*" and ending "*" chars
            int beginIndex = 0;
            int endIndex = commandName.length();
            if (commandName.startsWith("*"))
                beginIndex = 1;
            if (commandName.endsWith("*"))
                endIndex = commandName.length() - 1;
            final String trimmedCommandName =
                    commandName.substring(beginIndex, endIndex);

            // sort commands in alphabetical order
            Arrays.sort(commands);

            // add all matches to the search String since we want
            // to search all the commands that match the string
            final String[] matchedCommands =
                    getMatchedCommands(trimmedCommandName, commands);
                    //".*"+trimmedCommandName+".*", commands);
            // don't want to display more than 50 commands
            if (matchedCommands.length > 0 &&
                    matchedCommands.length < MAX_COMMANDS_TO_DISPLAY) {
                System.out.println(msg != null ? msg :
                                   strings.get("ClosestMatchedCommands"));
                for (String eachCommand : matchedCommands)
                    System.out.println("    " + eachCommand);
            } else {
                // find the closest distance
                final String nearestString = StringEditDistance.findNearest(
                        commandName, commands);
                // don't display the string if the edit distance is too large
                if (StringEditDistance.editDistance(
                        commandName, nearestString) < 5) {
                    System.out.println(msg != null? msg :
                                       strings.get("ClosestMatchedCommands"));
                    System.out.println("    " + nearestString);
                } else
                    throw new InvalidCommandException(commandName);
            }
        } catch (Exception e) {
            throw new InvalidCommandException(commandName);
        }
    }

    /**
     * Return all the commands that include pattern (just a literal
     * string, not really a pattern) as a substring.
     */
    private static String[] getMatchedCommands(final String pattern,
                                final String[] commands) {
        List<String> matchedCommands = new ArrayList<String>();
        for (int i = 0; i < commands.length; i++) {
            if (commands[i].indexOf(pattern) >= 0)
                matchedCommands.add(commands[i]);
        }
        return matchedCommands.toArray(new String[matchedCommands.size()]);
    }

    // XXX - public only because it's used by MultimodeCommand
    public static void writeCommandToDebugLog(String[] args, int exit) {
        File log = getDebugLogfile();

        if(log == null)
            return;

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(log, true));
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date date = new Date();
            out.write(dateFormat.format(date));
            out.write(" EXIT: " + exit);

            out.write(" asadmin ");

            if(args != null) {
               for (int i = 0; args != null && i < args.length; ++i) {
                    out.write(args[i] + " ");
               }
            }
        } catch (IOException e) {
            //It is just a debug file.
        }
        finally {
            if(out != null) {
                try {
                    out.write("\n");
                    out.close();
                }
                catch(Exception e) {
                    // ignore
                }
            }
        }
    }

    private static File getDebugLogfile() {
        String fname =  getEnvOrSysProp(CLIConstants.CLI_RECORD_ALL_COMMANDS_PROP);

        if(fname == null)
            return null;

        File f = new File(fname);

        if(!f.exists())
            try { f.createNewFile(); } catch(Exception e) { /* ignore */ }

        if(f.isFile() && f.canWrite())
            return f;
        else
            return null;
    }

    private static String getEnvOrSysProp(String index) {
        String s1 = System.getProperty(index);
        String s2 = System.getenv(index);

        // System Prop trumps environmental variable
        if(s1 != null)
            return s1;
        else
            return s2;
    }

    private static boolean ok(String s) {
        return s!= null && s.length() > 0;
    }

}



    /** Turned off for now -- it takes ~200 msec on a laptop!
    private final static boolean foundClass(String s) {
        try {
            Class.forName(s);
            return true;
        }
        catch (Throwable t) {
            System.out.println("Can not find class: " + s);
            return false;
        }
    }
    
    private final static String[] requiredClassnames =
            {
        // one from launcher jar        
        "com.sun.enterprise.admin.launcher.GFLauncher",
        // one from universal jar
        "com.sun.enterprise.universal.xml.MiniXmlParser",
        // one from cli-framework jar
        "com.sun.enterprise.cli.framework.CLIMain",
        // one from glassfish bootstrap jar
        "com.sun.enterprise.glassfish.bootstrap.ASMain",
        // one from stax-api
        "javax.xml.stream.XMLInputFactory",
        // one from server-mgmt
        "com.sun.enterprise.admin.servermgmt.RepositoryException",
        // one from common-utils
        "com.sun.enterprise.util.net.NetUtils",
        // one from admin/util
        "com.sun.enterprise.admin.util.TokenValueSet",
        // here's one that server-mgmt is dependent on
        "com.sun.enterprise.security.auth.realm.file.FileRealm",
        // dol
        "com.sun.enterprise.deployment.PrincipalImpl",
        // kernel
        //"com.sun.appserv.server.util.Version",
    };
    static {
        // check RIGHT NOW to make sure all the classes we need are
        // available
        long start = System.currentTimeMillis();
        boolean gotError = false;
        for (String s : requiredClassnames) {
            if(!foundClass(s))
                gotError = true;
        }
        // final test -- see if sjsxp is available
        try {
            javax.xml.stream.XMLInputFactory.newInstance().getXMLReporter();
        }
        catch(Throwable t) {
            gotError = true;
            System.out.println("Can't access STAX classes");
        }
        if(gotError) {
            // messages already sent to stdout...
            System.exit(1);
        }
        long stop = System.currentTimeMillis();
        System.out.println("Time to pre-load classes = " + (stop-start) + " msec");
    }
     */
