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
 *
 */

package org.glassfish.embed;

import org.glassfish.embed.util.LoggerHelper;
//import  com.sun.enterprise.util.ObjectAnalyzer;
import java.io.*;
import java.net.*;
import javax.net.ServerSocketFactory;
import org.glassfish.embed.util.StringUtils;
import static org.glassfish.embed.util.ServerConstants.*;

/**
 * <code>EmbeddedInfo</code> contains the {@link Server} configuration.  Server
 * name, port, logging, and autodeploy configuration may be set.  The file
 * system, {@link EmbeddedFileSystem}, used by <code>Server</code> can be
 * retrieved.
 *
 * If the HTTP port is not set, no default port will be used, and the HTTP listener
 * will not be created.  In order to start the server the HTTP port must be set
 * using <code>setHttpPort(int port)</code>
 * 
 * @author Byron Nevins
 */
public class EmbeddedInfo {
    /**
     * Default constructor sets server name to "server" by default
     */
    public EmbeddedInfo() {

    }

    /**
     * pkg-private convenience method which is used by the special
     * convenience constructor of Server
     * @param port
     */
    EmbeddedInfo(int port) {
       setHttpPort(port);
    }

    /**
     * Get the file system used by the server
     *
     * @return {@link EmbeddedFileSystem}
     */
    public synchronized EmbeddedFileSystem getFileSystem() {
        if(efs == null) {
            efs = new EmbeddedFileSystem();
        }
        return efs;
    }

    /**
     * Set the HTTP port
     * @param port valid port number
     */
    public void setHttpPort(int port) {
        httpPort = port;
    }


    /**
     * Set the Admin HTTP port
     * @param port valid port number
     */
    public void setAdminHttpPort(int port) {
        adminHttpPort = port;
    }

    /**
     * Set the system JMX Connector port
     * @param port valid port number
     */
    public void setJmxConnectorPort(int port) {
        jmxConnectorPort = port;
    }

    /**
     * Set the server name
     * @param newName server name
     */
    public void setServerName(String newName) {
        if(StringUtils.ok(newName))
            name = newName;
    }


    /**
     * Set the HTTP listener name.
     * @param newName HTTP listener name
     */
    public void setHttpListenerName(String newName) {
        if(StringUtils.ok(newName))
            httpListenerName = newName;
    }

    /**
     * Turn verbose mode on or off.
     * If verbose is set to true, log messages output to the console.
     * If verbose is set to false, log messages output to the console.
     *
     * @param b true to turn verbose mode on
     *          false to turn verbose mode off
     */
    public void setVerbose(boolean b) {
        verbose = b;
    }

    /**
     * Turn on the autodeploy service.
     */
    public void enableAutoDeploy() {
        // TODO also check that domain.xml has it enabled.
        autoDeploy = true;
    }
    
    /**
     * Checks that the HTTP port and Admin port are valid port numbers.
     * Checks if an {@link EmbeddedFileSystem} has been set.
     * If none has been set, a default <code>EmbeddedFileSystem</code>
     * is created.
     * Sets up logging.
     *
     * @throws org.glassfish.embed.EmbeddedException
     */
    void validate() throws EmbeddedException {
        validatePorts();
        validateFilesystem();
        validateLogging();
    }

    /**
     * Turn logging on or off.
     *
     * @param b true to turn logging on
     *          false to turn logging off
     */
    public void setLogging(boolean b) {
        logging = b;
    }

/* Commented out temporarily because the Reporter class in common-util is not available.
    @Override
    public String toString() {
        return ObjectAnalyzer.toString(this);
    }
*/
    //////////////////////  pkg-private  //////////////////////
    // bnevins
    // This class is really just a buffer for storing a bunch of data
    // That means that if we wrote getters they would have to return a reference
    // to the long-lived object here.  In that case all encapsulation is gone anyway
    // so I went with this simpler cleaner route...

    String                  name                    = DEFAULT_SERVER_NAME;
    int                     httpPort                = DEFAULT_HTTP_PORT;
    int                     adminHttpPort           = DEFAULT_ADMIN_HTTP_PORT;
    int                     jmxConnectorPort        = DEFAULT_JMX_CONNECTOR_PORT;
    EmbeddedFileSystem      efs;
    String                  httpListenerName        = DEFAULT_HTTP_LISTENER_NAME;
    String                  adminHttpListenerName   = DEFAULT_ADMIN_HTTP_LISTENER_NAME;
    String                  adminVSName             = DEFAULT_ADMIN_VIRTUAL_SERVER_ID;
    boolean                 logging                 = true;
    boolean                 verbose                 = false;
    boolean                 autoDeploy              = false;


    //////////////////////  all private below //////////////////////

    private void validateFilesystem() throws EmbeddedException {
        getFileSystem().initialize();
    }

    private void validateLogging() throws EmbeddedException {
        if(logging) {
            File logFile = efs.getLogFile();
            LoggerHelper.info("log_msg", logFile);
            
            if(!verbose)
                LoggerHelper.stopConsoleLogging();

            LoggerHelper.setLogFile(logFile.getPath());
        }

        LoggerHelper.info(toString());
    }
    
    private void validatePorts() throws EmbeddedException {
        if(httpPort != DEFAULT_HTTP_PORT) {
            if(httpPort < MIN_PORT || httpPort > MAX_PORT)
                throw new EmbeddedException("bad_port", MIN_PORT, MAX_PORT, httpPort);
            if (!isPortAvailable(httpPort))
                throw new EmbeddedException("port_in_use", Integer.toString(httpPort));
        }
        if(adminHttpPort != DEFAULT_ADMIN_HTTP_PORT) {
            if(adminHttpPort < MIN_PORT || adminHttpPort > MAX_PORT)
                throw new EmbeddedException("bad_port", MIN_PORT, MAX_PORT, adminHttpPort);
            if (!isPortAvailable(adminHttpPort))
                throw new EmbeddedException("port_in_use", Integer.toString(adminHttpPort));
        }
        
        // todo TODO
        // todo TODO
        // TODO todo Here is where we can see if the port is in use and assign another
        // todo TODO
        // todo TODO
    }

    private boolean isPortAvailable(int port) {
        boolean isAvailable = true;
        try {
          ServerSocket socket = ServerSocketFactory.getDefault().createServerSocket(port);
          socket.close();
          socket = null;
        } catch (IOException ioe) {
           isAvailable = false;
        }
        return isAvailable;
    }

    //////////////////////   private variables  ////////////////////////////////
}
