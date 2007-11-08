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
package com.sun.enterprise.ee.synchronization;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.sun.enterprise.ee.admin.proxy.MBeanServerProxy;
import com.sun.enterprise.ee.admin.clientreg.MBeanServerConnectionInfo;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Represents a concrete implementation of synchronization GET 
 * command using JMX.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class JmxGetCommand implements Command {
    
    private static Logger _logger = Logger.getLogger(EELogDomains.
                                    SYNCHRONIZATION_LOGGER);

    // The following string manager is used for exception messages
    private static final StringManager _localStrMgr = StringManager.getManager(
                    JmxGetCommand.class);

    /**
     * Initializes the synchronization GET command.
     *
     * @param  req    synchronization request
     * @param  conn   connector impl that provides connectivity 
     *                to the repository management entity
     */
    JmxGetCommand(SynchronizationRequest req,MBeanServerConnectionInfo connInfo) {
        this._request = req;
        this._connInfo = connInfo;
    }

    /**
     * Executes a synchronization request.
     *
     * @throws  SynchronizationException   if an error occurred during the
     *                                     execution of this command
     */
    public void execute() throws SynchronizationException {

        try {            
            SynchronizationRequest[] reqArray = 
                new SynchronizationRequest[] { _request };

            SynchronizationMBean proxy = (SynchronizationMBean)MBeanServerProxy.                getMBeanServerProxy(
                SynchronizationMBean.class, new ObjectName(MBEAN_NAME),
                    _connInfo);
            _logger.log(Level.FINE,
                "synchronization.request_start", _request.getMetaFileName());
            _result = proxy.synchronize(reqArray);            
            _logger.log(Level.FINE,
                "synchronization.file_received", _request.getMetaFileName());
        } catch (Exception e) {
            String msg = _localStrMgr.getString("fileRetrieveError"
                       , _request.getMetaFileName());
            throw new SynchronizationException(msg, e);
        }

        if (_result == null) {
            String msg = _localStrMgr.getString("fileRetrieveError"
                       , _request.getMetaFileName());
            throw new SynchronizationException(msg);
        }
    }

    /**
     * Returns the name of this command.
     *
     * @return  the name of this command
     */
    public String getName() {
        return NAME;
    }

    /**
     * Returns the response for this synchronization request.
     *
     * @return  the synchronization response of type 
     *          com.sun.enterprise.ee.synchronization.SynchronizationResponse
     */
    public Object getResult() {
        return _result;
    }

    // ---- INSTANCE VARIABLE(S) - PRIVATE -----------------------
    private MBeanServerConnectionInfo _connInfo = null;
    private SynchronizationRequest _request   = null;
    private SynchronizationResponse _result   = null;

    private static final String NAME = "Synchronization-Get-Command";    
    private static final String MBEAN_NAME = 
            "com.sun.appserv:type=synchronization,category=config";
}
