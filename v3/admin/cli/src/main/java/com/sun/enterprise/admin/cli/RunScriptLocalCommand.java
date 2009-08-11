/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.admin.cli;

import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.cli.remote.*;
import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.ValidOption;
import java.net.*;
import java.io.*;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Establishes a connection with Comet server (Provided by servlet)
 *  and also initiates a server execution of the deployed javascript.
 * 
 * @author Prashanth Abbagani
 */

public final class RunScriptLocalCommand extends RemoteCommand {

    public static final String TERSE = "terse";
    public static final String HTTP_PORT = "httpport";
    public static final String UPLOAD = "upload";

    //private String host = "localhost";
    private boolean upload = true;
    private int httpPort = 8080;
    private String scriptName;

    /**
     * Constructor used by subclasses to save the name, program options,
     * and environment information into corresponding protected fields.
     * Finally, this constructor calls the initializeLogger method.
     */
    public RunScriptLocalCommand(String name, ProgramOptions programOpts, Environment env)
                throws CommandException {
        super(name, programOpts, env);
        //logger.printMessage("In the RunScriptLocalCommand.constructor()");
    }

    /**
     * The prepare method must ensure that the commandOpts,
     * operandType, operandMin, and operandMax fields are set.
     */
    
    @Override
    protected void prepare() throws CommandException {
        Set<ValidOption> opts = new LinkedHashSet<ValidOption>();
        addOption(opts, TERSE, '\0', "BOOLEAN", false, "true");
        addOption(opts, UPLOAD, '\0', "BOOLEAN", false, "true");
        addOption(opts, HTTP_PORT, '\0', "STRING", false, "8080");
        commandOpts = Collections.unmodifiableSet(opts);
        operandName = "script";
        operandType = "FILE";
        operandMin = 1;
        operandMax = 1;

        if (!isValidCommand()) {
            throw new CommandException("Command " + this.name + " is not supported");
        }
        try {
            processProgramOptions();
        } catch (Exception e) {
            throw new CommandException(e.getMessage());
        }
    }
    

    /**
     * The validate method validates that the type and quantity of
     * parameters and operands matches the requirements for this
     * command.  The validate method supplies missing options from
     * the environment.  It also supplies passwords from the password
     * file or prompts for them if interactive.
     */
    @Override
    protected void validate()
            throws CommandException, CommandValidationException {
        super.validate();
        String shttpPort = getOption(HTTP_PORT);
        if (ok(shttpPort))
            httpPort = Integer.parseInt(shttpPort);
        upload = Boolean.getBoolean(getOption(UPLOAD));
        String scriptPath = operands.get(0);
        int i = scriptPath.lastIndexOf(File.separator);
        scriptName = scriptPath.substring(i+1, scriptPath.length());
    }

    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {
        DataInputStream in = null;
        boolean isContinue = true;
        CLILogger.getInstance().printDebugMessage("In the run-script command");
        try {
            super.executeCommand();
            String urlStr = "http://" + programOpts.getHost() + ":" +
                        httpPort + "/comet/cometServlet";
            CLILogger.getInstance().printDebugMessage("URL = " + urlStr);
            
            URL url1 = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //conn.setReadTimeout(100);
            String data = URLEncoder.encode("script", "UTF-8") + "=" +
                URLEncoder.encode(scriptName, "UTF-8");
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            wr.close();

            BufferedReader rd = new BufferedReader( new InputStreamReader(conn.getInputStream()));
            int c;
            while ((c = rd.read()) >= 0)
                System.out.print((char)c);
            //rd.close();
            return 0;
        } catch(Exception e) {
            //suppress all output and infer that the server is not running
            //e.printStackTrace();
            printRemoteException(e);
            return 1;
        }
    }

    /**
     * Get the list of commands from the remote server.
     *
     * @return the commands as a String array, sorted
     */
    private boolean isValidCommand() {
        //System.out.println("Checking to see if its valid command");
        RemoteCommand cmd;
        try {
            String cmds;
            try {
                cmd = new RemoteCommand("list-commands", programOpts, env);
                cmds = cmd.executeAndReturnOutput("list-commands");
            } catch (CommandException ce) {
                //System.out.println ("The server might not be running at all");
                //ce.printStackTrace();
                return false;
            }
            BufferedReader r = new BufferedReader(new StringReader(cmds));
            String line;

        /*
         * The output of the remote list-commands command is a bunch of
         * lines of the form:
         * Command : cmd-name
         * We extract the command name from each such line.
         * XXX - depending on this output format is gross;
         * should be able to send --terse to remote command
         * to cause it to produce exactly the output we want.
         */
            while ((line = r.readLine()) != null) {
                if (line.contains("run-script")) {
                    return true;
                }
            }
            return false;

        } catch (CommandValidationException ex) {
            //ex.printStackTrace();
            //System.out.println("Caught CommandValidationException");
            return false;
        } catch (IOException ioex) {
            //ioex.printStackTrace();
            //System.out.println(" Cannot find run-script command on the remote end");
            return false;
        }
    }

    private void printRemoteException(Exception e) {
        //e.printStackTrace();
        CLILogger.getInstance().printMessage("Command " + this.name + " is not supported");
        CLILogger.getInstance().printDebugMessage(e.getMessage());
        CLILogger.getInstance().printExceptionStackTrace(e);
    }

}
