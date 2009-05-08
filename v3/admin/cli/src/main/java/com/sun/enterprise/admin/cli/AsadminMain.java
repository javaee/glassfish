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

import com.sun.enterprise.admin.cli.remote.*;
import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import java.util.Arrays;
import java.util.Map;
import java.util.Hashtable;


/**
 *
 */
public class AsadminMain {
    public static void main(String[] args) {
        if(args.length <= 0) {
             //String msg = AsadminMain.strings.get("AsadminUsageMessage");
             //System.out.println(msg);
             System.exit(0);
        }

        int exitCode = ERROR;

        String command = args[0];
        AsadminMain main = new AsadminMain(args);
        exitCode = main.runCommand();

        Throwable t = main.getErrorThrowable();

        if(exitCode != SUCCESS && t instanceof InvalidCommandException && command.equals("help")) {
            // Transform 'asadmin help remote-command' to 'asadmin remote-command --help'.
            // Otherwise, you'll get a CommandNotFoundException: Command help not found.
            command = args[1];
            AsadminMain help = new AsadminMain(command, "--help");
            exitCode = help.runCommand();
        }

        if(exitCode == SUCCESS) {
            CLILogger.getInstance().printDetailMessage(
                strings.get("CommandSuccessful", command));
        }
        if(exitCode == ERROR) {
            CLILogger.getInstance().printDetailMessage(
                strings.get("CommandUnSuccessful", command));
        }
        if(exitCode == INVALID_COMMAND_ERROR) {
            try {
                CLIMain.displayClosestMatch(command, main.getRemoteCommands(),
                                            strings.get("ClosestMatchedLocalAndRemoteCommands"));
            } catch (InvalidCommandException e) {
                // not a big deal if we cannot help
            }
            CLILogger.getInstance().printDetailMessage(
                strings.get("CommandUnSuccessful", command));
        }
        if (exitCode == CONNECTION_ERROR) {
            try {
                CLIMain.displayClosestMatch(command, null,
                                            strings.get("ClosestMatchedLocalCommands"));
            } catch (InvalidCommandException e) {
                CLILogger.getInstance().printMessage(strings.get("InvalidRemoteCommand",
                                                                 command));
            }
            CLILogger.getInstance().printDetailMessage(
                strings.get("CommandUnSuccessful", command));
        }
        System.exit(exitCode);
    }
    public AsadminMain(String... args) {
        this(null, args);
    }

    public AsadminMain(InputsAndOutputs io, String... args) {
        
        if(io != null)
            InputsAndOutputs.setInstance(io);

        info = new CallingInfo(args, AsadminMain.class);
        debug();
    }

    /*
     * Does not throw Exceptions.
     * call getErrorThrowable() to see if there was an error
     * or if the return value is not == SUCCESS then that is an error.
     * Get the error message with getErrorMessage()
     */
    public final int runCommand(){
        if(info.copyOfArgs.length <= 0) {
            exitCode = ERROR;
        }
        else if((exitCode = runLocalCommand()) != SUCCESS)
            exitCode = runRemoteCommand();

        return exitCode;
    }

    public Throwable getErrorThrowable() {
        return errorThrowable;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    

    public int runLocalCommand(){
        errorThrowable = null;
        errorMessage = "";

        try {
            CLIMain cli = new com.sun.enterprise.cli.framework.CLIMain();
            cli.invokeCommand(info.copyOfArgs, this);
        }
        // special case to help debug
        catch(NoClassDefFoundError e) {
            errorThrowable = e;
            errorMessage = e.toString();
        }
        catch(Exception e) {
            errorThrowable = e;
            errorMessage = e.getMessage();
        }
        catch (Throwable t) {
            errorThrowable = t;
            printStack(t);
            errorMessage = t.getMessage();
        }

        if(errorThrowable != null) {
            if(CLIConstants.debugMode)
                printError(errorThrowable.getMessage());
            return ERROR;
        }
        else
            return SUCCESS;
    }
    public int runRemoteCommand() {
        try {
            CLIRemoteCommand rc = new CLIRemoteCommand(info.copyOfArgs);
            rc.runCommand();
            return SUCCESS;
        }
        catch (Throwable ex) { // there is a good reason for Throwable.
            CLILogger.getInstance().printExceptionStackTrace(ex);
            CLILogger.getInstance().printMessage(ex.getMessage());
            
            errorThrowable = ex;
            errorMessage = ex.getMessage();

            if (ex.getCause() instanceof java.net.ConnectException) {
                return CONNECTION_ERROR;
            }
            if (ex.getCause() instanceof InvalidCommandException) {
                return INVALID_COMMAND_ERROR;
            }
            return ERROR;
        }
    }

    String[] getArgs() {
        return info.copyOfArgs;
    }
    String getClassPath() {
        return info.classPath;
    }
    String getClassName() {
        return info.className;
    }

    private void printError(String s) {
        //TODO TODO --> use interactive flag!
        CLILogger.getInstance().printError(s);
    }

    private void printStack(Throwable t) {
        CLILogger.getInstance().printExceptionStackTrace(t);
    }

    private void debug() {
        if(CLIConstants.debugMode) {
            System.setProperty(CLIConstants.WALL_CLOCK_START_PROP, "" + System.currentTimeMillis());
            CLILogger.getInstance().printDebugMessage("CLASSPATH= " +
                    System.getProperty("java.class.path") +
                    "\nCommands: " + Arrays.toString(info.copyOfArgs));
        }
    }

    private Map<String, String> getRemoteCommands() {
        try {
            ListCommandsCommand lcc = new ListCommandsCommand();
            String[] remoteCommands = lcc.getRemoteCommands();
            Map<String, String> remoteCommandsMap = new Hashtable<String, String>();
            for (String rc : remoteCommands) {
                remoteCommandsMap.put(rc, "remote command");
            }
            return remoteCommandsMap;
        }
        catch (CommandException ce) {
            return null;
        }
    }
    

    private final static int ERROR = 1;
    private final static int CONNECTION_ERROR = 2;
    private final static int INVALID_COMMAND_ERROR = 3;
    private final static int SUCCESS = 0;
    private final static LocalStringsImpl strings = new LocalStringsImpl(AsadminMain.class);

    private         CallingInfo info;
    private         Throwable   errorThrowable;
    private         String      errorMessage = "";
    private         int         exitCode = -1;

    private static class CallingInfo {
        String[]    copyOfArgs;
        String      classPath;
        String      className;

        private CallingInfo(String[] args, Class caller) {
            classPath = System.getProperty("java.class.path");
            className = caller.getName();

            if(args != null && args.length > 0) {
                copyOfArgs = new String[args.length];
                System.arraycopy(args, 0, copyOfArgs, 0, args.length);
            }
            else {
                copyOfArgs = new String[0];
            }
        }
    }
}
