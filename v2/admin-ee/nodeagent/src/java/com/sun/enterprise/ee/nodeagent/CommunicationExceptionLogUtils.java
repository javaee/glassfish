/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.ee.nodeagent;

import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.synchronization.DASCommunicationException;
import com.sun.enterprise.security.LoginException;
import java.io.IOException;
import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** A class to log the communication exceptions. There could be various types
 * of Exception and we should not throw away the information stored in
 * the exceptions. The purpose of this class is to log appropriate messages
 * that help users to recover from errors.
 * @since 8.2
 */
class CommunicationExceptionLogUtils {
    
    private final String configDasHost;
    private final String configDasPort;
    private final String configDasProtocol;
    private final Logger logger;
    private final Level level;
    public CommunicationExceptionLogUtils(final Logger logger, final Level level,
    final DASPropertyReader dr) {
        this.configDasHost = dr.getHost();
        this.configDasPort = dr.getPort();
        this.configDasProtocol = dr.getProtocol();
        this.logger = logger;
        if (level == null) {
            this.level = Level.SEVERE;
        }
        else {
            this.level = level;
        }
    }
    /** This method is meant for handling various communication errors during 
     * synchronization. It is not meant to do anything with the exception handling. 
     * See bug: 6324223. Also, note that Synchronization does not rethrow the 
     * actual exceptions during the process of synchronization and wraps them 
     * (unnecessarily) in DASCommunicationException. To unwrap it, is the
     * basic purpose of this method. None of the parameters may be null.
     * @param de an instance of DASCommunicationException from synchronization code
     * @since Appserver 8.2
     */
    public void handleDASCommunicationException(final DASCommunicationException de) {
        Throwable wrapped = null;
        if (de != null) {
            wrapped = de.getCause();
            if (wrapped instanceof ConnectException)
                logConnectException((ConnectException)wrapped);
            else if (wrapped instanceof LoginException)
                logLoginException((LoginException)wrapped);
            else if (wrapped instanceof IOException)
                logIOException((IOException)wrapped);
            else 
                logThrowable(wrapped);
        }
    }
    private void logConnectException(final ConnectException c) {
        final String[] hp = new String[] {configDasHost, configDasPort};
        logger.log(level, "nodeagent.connectException", hp);
    }
    private void logLoginException(final LoginException l) {
        final String[] hp = new String[] {configDasHost, configDasPort};
        logger.log(level, "nodeagent.loginException", hp);
    }
    private void logIOException(final IOException c) {
        //javax.naming.serviceunavailable-java.net.connectexception-java.rmi.connectexception------ server is down, stub has different address
        //javax.naming.CommunicationException-java.rmi.ConnectIOException -- the endpoint may not be JRMP  (HTTP port and RMI Connection)
        Throwable pc = c.getCause();
        final String[] hpp = new String[]{configDasHost, configDasPort, configDasProtocol};
        if (pc instanceof javax.naming.ServiceUnavailableException) {
            if (pc.getCause() instanceof java.net.ConnectException ||
                pc.getCause() instanceof java.rmi.ConnectException) {
                logger.log(level, "nodeagent.serverEndDown", hpp);
            }
        } else if (pc instanceof javax.naming.CommunicationException) {
            pc = pc.getCause();
            if (pc instanceof java.rmi.ConnectIOException) {
                final String[] hp = new String[]{configDasHost, configDasPort};
                logger.log(level, "nodeagent.nonJrmpEndPoint", hp);
            }
        }
    }
    private void logThrowable(final Throwable t) {
        logger.log(level, "nodeagent.unknownCommunicationError", t);
    }
}
