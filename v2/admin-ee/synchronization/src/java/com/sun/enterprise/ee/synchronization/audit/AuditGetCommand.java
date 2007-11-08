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
package com.sun.enterprise.ee.synchronization.audit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.sun.enterprise.ee.admin.proxy.MBeanServerProxy;
import com.sun.enterprise.ee.admin.clientreg.MBeanServerConnectionInfo;
import com.sun.enterprise.ee.synchronization.Command;
import com.sun.enterprise.ee.synchronization.SynchronizationMBean;
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.SynchronizationResponse;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.ee.synchronization.SynchronizationException;

/**
 * Represents a concrete implementation of audit GET command using JMX.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class AuditGetCommand implements Command {
    
    /**
     * Initializes the synchronization GET command.
     *
     * @param  req    synchronization requests
     * @param  conn   connector impl that provides connectivity 
     *                to the repository management entity
     */
    AuditGetCommand(SynchronizationRequest[] req, 
            MBeanServerConnectionInfo connInfo) {

        this._requests = req;
        this._connInfo = connInfo;

        // sets the timestamp type to none
        for (int i=0; i<req.length; i++) {
            req[i].setTimestampType(SynchronizationRequest.TIMESTAMP_NONE);
        }
    }

    /**
     * Executes a synchronization request.
     *
     * @throws  SynchronizationException   if an error occurred during the
     *                                     execution of this command
     */
    public void execute() throws AuditException {

        try {            
            SynchronizationMBean proxy = (SynchronizationMBean)
                MBeanServerProxy.getMBeanServerProxy(
                    SynchronizationMBean.class, 
                    new ObjectName(MBEAN_NAME), _connInfo);

            _result = proxy.audit(_requests);            

        } catch (Exception e) {
            String msg = _localStrMgr.getString("fileRetrieveError",
                            _requests[0].getMetaFileName());
            throw new AuditException(msg, e);
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
    private SynchronizationRequest[] _requests  = null;
    private SynchronizationResponse _result     = null;

    private static final String NAME = "Audit-Get-Command";    
    private static final String MBEAN_NAME = 
            "com.sun.appserv:type=synchronization,category=config";

    private static Logger _logger = Logger.getLogger(EELogDomains.
                                    SYNCHRONIZATION_LOGGER);
    private static final StringManager _localStrMgr = StringManager.getManager(
                    AuditGetCommand.class);
}
