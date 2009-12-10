/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates all the details of handling the ACC agent arguments.
 * <p>
 * The agent accepts agent arguments, mostly to find out:
 * <ul>
 * <li>what type of client
 * the user specified (JAR, directory, class, or class file) and the path or
 * name for the file, and
 * <li>the appclient command arguments from the appclient command.
 * </ul>
 * Each word that is an appclient script argument appears in the agent
 * arguments as "arg=..." which distinguishes them from arguments intended
 * directly for the agent.  Note that the appclient script does not pass
 * through the user's -client xxx option and value.  Instead it passes
 * client=... intended for the agent (note the lack of the - sign).
 *
 * @author tjquinn
 */
public class CommandLaunchInfo {

    /* agent argument names */
    private static final String CLIENT_AGENT_ARG_NAME = "client=";
    private static final String COMMAND_AGENT_ARG = "arg=";
    private static final String MODE_AGENT_ARG = "mode=";
    private static final String APPCPATH = "appcpath=";

    /* records the type of launch the user requested: jar, directory, class, or class file*/
    private ClientLaunchType clientLaunchType;

    /* records the client JAR file path, directory path, class name, or class file path */
    private String clientName;

    private String mode = null;

    private String appcPath = null;

//    /*
//     * main class as specified in the client=class=(class-name) or
//     * client=classfile=(class-file-name) agent argument
//     */
//    private String clientMainClassName = null;

    /* info about the appclient script options */
    private AppclientCommandArguments appclientCommandLaunchInfo = null;

    /**
     * Creates and returns a new CommandLaunchInfo instance.
     * <p>
     * Typically the caller is the agent which will pass the agent arguments
     * it received when the VM invoked its premain method.
     *
     * @param agentArgs string of agent arguments
     * @return new CommandLaunchInfo object
     */
    public static CommandLaunchInfo newInstance(final String agentArgs) throws UserError {
        CommandLaunchInfo result = new CommandLaunchInfo(agentArgs);
        return result;
    }


    private CommandLaunchInfo(final String agentArgs) throws UserError {
        clientLaunchType = saveArgInfo(agentArgs);
    }

    /**
     * Returns the name part of the client selection expression.  This can be
     * the file path to a JAR, a file path to a directory, the fully-qualified
     * class name for the main class, or the file path to a .class file.
     *
     * @return the name
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Returns which type of launch the user has triggered given the combination
     * of options he or she specified.
     * @return
     */
    public ClientLaunchType getClientLaunchType() {
        return clientLaunchType;
    }

    public AppclientCommandArguments getAppclientCommandArguments() {
        return appclientCommandLaunchInfo;
    }

    public String getAppcPath() {
        return appcPath;
    }
    
    private ClientLaunchType saveArgInfo(
            final String agentArgs) throws UserError {
        if (agentArgs == null){
            return ClientLaunchType.UNKNOWN;
        }
        ClientLaunchType result = null;
        List<String> appclientCommandArgs = new ArrayList<String>();

        final String[] args = agentArgs.split(",");
        for (String arg : args) {
            if (arg.startsWith(CLIENT_AGENT_ARG_NAME)) {
                result = processClientArg(arg.substring(CLIENT_AGENT_ARG_NAME.length()));
            } else if (arg.startsWith(COMMAND_AGENT_ARG)) {
                processAppclientArg(appclientCommandArgs, arg.substring(COMMAND_AGENT_ARG.length()));
            } else if (arg.startsWith(MODE_AGENT_ARG)) {
                processMode(arg.substring(MODE_AGENT_ARG.length()));
            } else if (arg.startsWith(APPCPATH)) {
                processAppcPath(arg.substring(APPCPATH.length()));
            }
        }
        /*
         * Now that we have all the appclient arguments, process them as a group.
         */
        appclientCommandLaunchInfo = AppclientCommandArguments.newInstance(
                appclientCommandArgs);

        return result;
    }

    private void processMode(final String mode) {
        this.mode = mode;
    }

    private ClientLaunchType processClientArg(final String clientSpec) {
        /*
         * We are in the process of handling the agent argument
         * "client=(type)=(value).  clientSpec contains (type)=(value).
         */
        final int equalsSign = clientSpec.indexOf('=');

        final String clientType = clientSpec.substring(0, equalsSign);
        clientName = clientSpec.substring(equalsSign + 1);
        if (clientName.startsWith("\"") && clientName.endsWith("\"")) {
            clientName = clientName.substring(1, clientName.length() - 1);
        }
        ClientLaunchType type = ClientLaunchType.byType(clientType);

        return type;
    }

    private void processAppclientArg(final List<String> commandArgs, String arg) {
        commandArgs.add(arg);
    }

    private void processAppcPath(final String appcPath) {
        this.appcPath = appcPath;
    }

    /**
     * Represents the types of client launches.
     */
    public enum ClientLaunchType {
        JAR,
        DIR(true),
        CLASSFILE(true),
        CLASS,
        URL,
        UNKNOWN;

        private final boolean usesAppClientCommandForMainProgram;

        ClientLaunchType() {
            this(false);
        }

        ClientLaunchType(final boolean usesAppClientCommandForMainProgram) {
            this.usesAppClientCommandForMainProgram = usesAppClientCommandForMainProgram;
        }

        boolean usesAppClientCommandForMainProgram() {
            return usesAppClientCommandForMainProgram;
        }

        /**
         * Returns the ClientLaunchType for the specified launch type name.
         * @param lowerCaseType launch type name (in lower-case)
         * @return relevant ClientLaunchType for the type; null if no match
         */
        static ClientLaunchType byType(final String lowerCaseType) {
            for (ClientLaunchType t : values()) {
                if (t.name().equalsIgnoreCase(lowerCaseType)) {
                    return t;
                }
            }
            return UNKNOWN;
        }
    }
}
