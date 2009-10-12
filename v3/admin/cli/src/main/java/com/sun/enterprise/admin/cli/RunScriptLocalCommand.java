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

import com.sun.enterprise.admin.cli.remote.*;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import java.net.*;
import java.io.*;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import java.util.UUID;
import java.rmi.*;

/** Establishes a connection with Comet server (Provided by servlet)
 *  and also initiates a server execution of the deployed javascript.
 * 
 * @author Prashanth Abbagani
 * @author Byron Nevins
 */

@Service(name = "run-script")
@Scoped(PerLookup.class)
public final class RunScriptLocalCommand extends RemoteCommand {

    //public static final String TERSE = "terse";
    public static final String HTTP_PORT = "httpport";
    public static final String UPLOAD = "upload";
    public static final String SCRIPT_ID = "scriptid";
    private static final int DEFAULT_HTTP_PORT   = 8080;

    //private String host = "localhost";
    private boolean upload = true;
    private int httpPort = -1;
    private String scriptName;
    private static final LocalStringsImpl strings =
            new LocalStringsImpl(RunScriptLocalCommand.class);


    public RunScriptLocalCommand() throws CommandException {
        super();
    }

    /**
     * The prepare method must ensure that the commandOpts,
     * operandType, operandMin, and operandMax fields are set.
     */
    
    @Override
    protected void prepare()
            throws CommandException, CommandValidationException {
        super.prepare();
        Set<ValidOption> opts = new LinkedHashSet<ValidOption>();
        addOption(opts, HTTP_PORT, '\0', "STRING", false, "8080");
        addOption(opts, UPLOAD, '\0', "BOOLEAN", false, "false");
        //Need to add an option on the fly for the remote version of the command
        commandOpts = Collections.synchronizedSet(opts);
        operandName = "script";
        operandType = "STRING";
        operandMin = 1;
        operandMax = 1;

        processProgramOptions();
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
        validatePort();
        validateScriptName();
    }

    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {
        DataInputStream in = null;
        boolean isContinue = true;
        logger.printDebugMessage("In the run-script command");
        try {
            String scriptID = scriptName+UUID.randomUUID();
            //Adding an option on the fly for the remote version of the command
            commandOpts.add(new ValidOption(SCRIPT_ID, "STRING", ValidOption.OPTIONAL,
                                            scriptID));
            options.put(SCRIPT_ID, scriptID);
            super.executeCommand();
            // Check if its --help which is already taken care of by RemoteCommand
            if (programOpts.isHelp() || getBooleanOption("help")) {
                return 0;
            }
            String urlStr = "http://" + programOpts.getHost() + ":" +
                        httpPort + "/comet/cometServlet";
            logger.printDebugMessage("URL = " + urlStr);
            
            URL url1 = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //conn.setReadTimeout(100);
            String data = URLEncoder.encode("script", "UTF-8") + "=" +
                URLEncoder.encode(scriptID, "UTF-8");
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            wr.close();

            try {
                BufferedReader rd = new BufferedReader(
                                        new InputStreamReader(conn.getInputStream()));
                int c;
                while ((c = rd.read()) >= 0)
                    System.out.print((char)c);
            } catch (java.rmi.ConnectException ce) {
                ce.printStackTrace();
                logger.printMessage("\nConnection terminated by server");
                return 1;
            } catch (FileNotFoundException fnfe) {
                logger.printMessage("\nConnection terminated by server, Comet servlet is not found");
                return 1;
            } catch (IOException ioe) {
                logger.printMessage("\nConnection terminated by server, exiting the client");
                return 1;
            }
            //rd.close();
            return 0;
        } catch(Exception e) {
            //suppress all output and infer that the server is not running
            e.printStackTrace();
            printRemoteException(e);
            return 1;
        }
    }

    private void printRemoteException(Exception e) {
        //e.printStackTrace();
        logger.printMessage("Command " + this.name + " is not supported");
        logger.printDebugMessage(e.getMessage());
        logger.printExceptionStackTrace(e);
    }

    private void validatePort() throws CommandValidationException {
        httpPort = DEFAULT_HTTP_PORT;
        String port = getOption(HTTP_PORT);

        if(!ok(port))
            return;

        try {
            httpPort = Integer.parseInt(port);

            if(httpPort > 0 && httpPort < 65536)
                return;
            else
                httpPort = DEFAULT_HTTP_PORT; // not necessary --> paranoia
        }
        catch(NumberFormatException nfe) {
            // handled below...
        }

        throw new CommandValidationException(strings.get("runscript.badport", port));
    }

    private void validateScriptName() throws CommandValidationException {
        if (operands.isEmpty() || !ok(operands.get(0)))
            throw new CommandValidationException(strings.get("runscript.noscriptname"));

        // we have a scriptname which is at least one character long
        // several possibilities:
        // 1. An absolute path on this client machine
        // 2. A relative path that is relative to the current dir on this client machine
        // 3. An absolute path on the server machine
        // 4. A path relative to the server's current directory

        // As of today, Oct 11, 2009, we only support 1 and 2 above.
        // In the future it would be easy to support 3 and 4 -- we just send the paths
        // off to the server

        String op = operands.get(0);
        File f = SmartFile.sanitize(new File(op));

        if(!f.isFile())
            throw new CommandValidationException(strings.get("runscript.badscriptname", op));

        scriptName = f.getName();
        operands.set(0, f.getPath().replace('\\', '/'));
    }
}
