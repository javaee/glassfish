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
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.*;
import java.text.*;
import java.util.*;


/**
 * my v3 main, basically some throw away code
 */
public class AsadminMain {
    public static void main(String[] args) {
        if(CLIConstants.debugMode) {
            System.setProperty(CLIConstants.WALL_CLOCK_START_PROP, "" + System.currentTimeMillis());
            CLILogger.getInstance().printDebugMessage("CLASSPATH= " +
                    System.getProperty("java.class.path") +
                    "\nCommands: " + Arrays.toString(args));
        }

        AsadminMain main = new AsadminMain();
        int exitCode;

        if(args.length <= 0) {
             String msg = strings.get("AsadminUsageMessage");
             System.out.println(msg);
             System.exit(0);
        }
        copyOfArgs = new String[args.length];
        classPath = SmartFile.sanitizePaths(System.getProperty("java.class.path"));
        className = main.getClass().getName();

        System.arraycopy(args, 0, copyOfArgs, 0, args.length);
        String command = args[0];
        try {
            exitCode = main.local(args);
        }
        catch(InvalidCommandException e) {
            // Transform 'asadmin help remote-command' to 'asadmin remote-command --help'.
            // Otherwise, you'll get a CommandNotFoundException: Command help not found.
            if (args[0].equals("help")) {
                exitCode = main.remote(new String[] {args[1], "--help"});
                command = args[1];
            } else {
                CLILogger.getInstance().printDebugMessage(e.getMessage());
                exitCode = main.remote(args);
            }
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
        writeCommandToDebugLog(args, exitCode);
        System.exit(exitCode);
    }

    public int local(String[] args) throws InvalidCommandException{
        try {
            CLIMain cli = new com.sun.enterprise.cli.framework.CLIMain();
            cli.invokeCommand(args);
            return SUCCESS;
        }
        catch(CommandException ce) {
            CLILogger.getInstance().printError(ce.getMessage());
            return ERROR;
        }
        catch(CommandValidationException cve) {
            CLILogger.getInstance().printError(cve.getMessage());
            return ERROR;
        }
        catch(NoClassDefFoundError ncdfe)
        {
            CLILogger.getInstance().printError(ncdfe.toString());
            return ERROR;
        }
        catch(InvalidCommandException ice) {
            throw ice;
        }
        catch (Throwable ex) {
            CLILogger.getInstance().printExceptionStackTrace(ex);
            CLILogger.getInstance().printError(ex.toString());
            return ERROR;
        }
    }
    public int remote(String[] args) {
        try {
            CLIRemoteCommand rc = new CLIRemoteCommand(args);
            rc.runCommand();
            return SUCCESS;
        }
        catch (Throwable ex) {
            CLILogger.getInstance().printExceptionStackTrace(ex);
            CLILogger.getInstance().printMessage(ex.getMessage());
            if (ex.getCause() instanceof java.net.ConnectException) {
                return CONNECTION_ERROR;
            }
            if (ex.getCause() instanceof InvalidCommandException) {
                return INVALID_COMMAND_ERROR;
            }
            return ERROR;
        }
    }

    /**
     * We avoid using real System properties.  Instead we use our own Map
     * @return a read-only Map of our system-wide properties                                                           
     */
    public static Map<String, String> getSystemProps() {
        return Collections.unmodifiableMap(systemProps);
    }
    /*pkg-priv*/ static String[] getArgs() {
        return copyOfArgs;
    }
    /*pkg-priv*/ static String getClassPath() {
        return classPath;
    }
    /*pkg-priv*/ static String getClassName() {
        return className;
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

    private static void writeCommandToDebugLog(String[] args, int exit) {
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

    private final static int ERROR = 1;
    private final static int CONNECTION_ERROR = 2;
    private final static int INVALID_COMMAND_ERROR = 3;
    private final static int SUCCESS = 0;
    private final static LocalStringsImpl strings = new LocalStringsImpl(AsadminMain.class);
    private       static String[] copyOfArgs;
    private       static String classPath;
    private       static String className;
    private       static Map<String, String> systemProps;

    static {
        systemProps = new ASenvPropertyReader().getProps();
        final String ir = SystemPropertyConstants.INSTALL_ROOT_PROPERTY;
        final String cr = SystemPropertyConstants.CONFIG_ROOT_PROPERTY;
        final String irVal = systemProps.get(ir);
        final String crVal = systemProps.get(cr);

        if(ok(irVal))
            System.setProperty(ir, irVal);

        if(ok(crVal))
            System.setProperty(cr, crVal);
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

