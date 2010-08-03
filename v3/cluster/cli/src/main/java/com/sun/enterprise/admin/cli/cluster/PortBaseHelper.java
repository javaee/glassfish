/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.admin.cli.cluster;

import com.sun.enterprise.admin.cli.CLILogger;
import org.glassfish.api.admin.CommandException;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.net.NetUtils;

/**
 * Port base utilities used by create-local-instance.  Similar to create-domain.
 * @author Jennifer
 */
public class PortBaseHelper {
    
    public static final int PORT_MAX_VAL = 65535;
    public static final int PORTBASE_ADMINPORT_SUFFIX = 48;
    public static final int PORTBASE_HTTPSSL_SUFFIX = 81;
    public static final int PORTBASE_IIOPSSL_SUFFIX = 38;
    public static final int PORTBASE_IIOPMUTUALAUTH_SUFFIX = 39;
    public static final int PORTBASE_INSTANCE_SUFFIX = 80;
    public static final int PORTBASE_JMS_SUFFIX = 76;
    public static final int PORTBASE_IIOP_SUFFIX = 37;
    public static final int PORTBASE_JMX_SUFFIX = 86;

    public static final String ADMIN = "ASADMIN_LISTENER_PORT";
    public static final String HTTP = "HTTP_LISTENER_PORT";
    public static final String HTTPS = "HTTP_SSL_LISTENER_PORT";
    public static final String IIOP = "IIOP_LISTENER_PORT";
    public static final String IIOPM = "IIOP_SSL_MUTUALAUTH_PORT";
    public static final String IIOPS = "IIOP_SSL_LISTENER_PORT";
    public static final String JMS = "JMS_PROVIDER_PORT";
    public static final String JMX = "JMX_SYSTEM_CONNECTOR_PORT";

    final private static CLILogger logger = CLILogger.getInstance();
    final private static LocalStringsImpl strings = new LocalStringsImpl(PortBaseHelper.class);

    public PortBaseHelper(String portbase, boolean checkports) {
        portBase = portbase;
        checkPorts = checkports;
    }

    public void verifyPortBase() throws CommandException {
        if (usePortBase()) {
            final int portbase = convertPortStr(portBase);
            setOptionsWithPortBase(portbase);
        }
    }

    public String getAdminPort() {
        return adminPort;
    }

    public String getInstancePort() {
        return instancePort;
    }

    public String getHttpsPort() {
        return httpsPort;
    }

    public String getIiopPort() {
        return iiopPort;
    }

    public String getIiopsPort() {
        return iiopsPort;
    }

    public String getIiopmPort() {
        return iiopmPort;
    }

    public String getJmsPort() {
        return jmsPort;
    }

    public String getJmxPort() {
        return jmxPort;
    }

    /**
     * Converts the port string to port int
     *
     * @param port the port number
     * @return the port number as an int
     * @throws CommandValidationException if port string is not numeric
     */
    private int convertPortStr(final String port)
            throws CommandException {
        try {
            return Integer.parseInt(port);
        } catch (Exception e) {
            throw new CommandException(
                    strings.get("InvalidPortNumber", port));
        }
    }

    /**
     * Check if portbase option is specified.
     */
    private boolean usePortBase() throws CommandException {
        if (portBase != null) {
            return true;
        }
        return false;
    }

    private void setOptionsWithPortBase(final int portbase)
            throws CommandException {
        // set the option name and value in the options list
        verifyPortBasePortIsValid(ADMIN,
            portbase + PORTBASE_ADMINPORT_SUFFIX);
        adminPort = String.valueOf(portbase + PORTBASE_ADMINPORT_SUFFIX);

        verifyPortBasePortIsValid(HTTP,
            portbase + PORTBASE_INSTANCE_SUFFIX);
        instancePort = String.valueOf(portbase + PORTBASE_INSTANCE_SUFFIX);

        verifyPortBasePortIsValid(HTTPS,
            portbase + PORTBASE_HTTPSSL_SUFFIX);
        httpsPort = String.valueOf(portbase + PORTBASE_HTTPSSL_SUFFIX);

        verifyPortBasePortIsValid(IIOPS,
            portbase + PORTBASE_IIOPSSL_SUFFIX);
        iiopsPort = String.valueOf(portbase + PORTBASE_IIOPSSL_SUFFIX);

        verifyPortBasePortIsValid(IIOPM,
                portbase + PORTBASE_IIOPMUTUALAUTH_SUFFIX);
        iiopmPort = String.valueOf(portbase + PORTBASE_IIOPMUTUALAUTH_SUFFIX);

        verifyPortBasePortIsValid(JMS,
            portbase + PORTBASE_JMS_SUFFIX);
        jmsPort = String.valueOf(portbase + PORTBASE_JMS_SUFFIX);

        verifyPortBasePortIsValid(IIOP,
            portbase + PORTBASE_IIOP_SUFFIX);
        iiopPort = String.valueOf(portbase + PORTBASE_IIOP_SUFFIX);

        verifyPortBasePortIsValid(JMX,
            portbase + PORTBASE_JMX_SUFFIX);
        jmxPort = String.valueOf(portbase + PORTBASE_JMX_SUFFIX);
    }

    /**
     * Verify that the portbase port is valid
     * Port must be greater than 0 and less than 65535.
     * This method will also check if the port is in used.
     *
     * @param portNum the port number to verify
     * @throws CommandException if Port is not valid
     * @throws CommandException if port number is not a numeric value.
     */
    private void verifyPortBasePortIsValid(String portName, int portNum)
            throws CommandException {
        if (portNum <= 0 || portNum > PORT_MAX_VAL) {
            throw new CommandException(
                strings.get("InvalidPortBaseRange", portNum, portName));
        }
        if (checkPorts && !NetUtils.isPortFree(portNum)) {
            throw new CommandException(
                strings.get("PortBasePortInUse", portNum, portName));
        }
        logger.printDebugMessage("Port =" + portNum);
    }

    private String portBase;
    private boolean checkPorts;
    private String adminPort;
    private String instancePort;
    private String httpsPort;
    private String iiopPort;
    private String iiopmPort;
    private String iiopsPort;
    private String jmsPort;
    private String jmxPort;
}
