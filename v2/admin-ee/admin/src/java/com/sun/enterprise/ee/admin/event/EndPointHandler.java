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
package com.sun.enterprise.ee.admin.event;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.admin.event.CommandEvent;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.ee.admin.proxy.InstanceProxy;
import com.sun.enterprise.ee.admin.mbeanapi.ServerRuntimeMBean;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.ConfigContext;

import com.sun.enterprise.admin.servermgmt.InstanceException;

/**
 * Forwards an event to an end point.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class EndPointHandler implements Runnable {

    /**
     * Constructor.
     *
     * @param  event  event that needs to be sent
     * @param  ep     end point for this event
     */
    EndPointHandler(AdminEvent event, EndPoint ep) {
        _endPoint  = ep;
        _event     = event;
    }

    /**
     * Returns logger for this class.
     */
    private static Logger getLogger() {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }
 
    /**
     * Calls the server runtime MBean to forward the event to the 
     * remote server. Events are only forwarded when dynamic 
     * reconfiguration is enbled for the server.
     */
    public void run() {

        String serverName  = _endPoint.getHost();
        ConfigContext ctx  = _endPoint.getConfigContext();
        try {

            Config config = ServerHelper.getConfigForServer(ctx, serverName);

            // forwards the event if dynamic reconfiguration is enabled
            if ((config != null && config.isDynamicReconfigurationEnabled())
                    || (_event instanceof CommandEvent)) {
                ServerRuntimeMBean sRuntime = 
                    InstanceProxy.getInstanceProxy( _endPoint.getHost() );
                _result = sRuntime.forwardEvent(_event);
            } else {
                // dynamic reconfiguration is not enabled
                getLogger().log(Level.WARNING, 
                    "eeadmin.eventDynamicReconfigDisabled.Warning",
                    new Object[] {serverName, _event.toString()});
            }
        } catch (InstanceException e) {
            getLogger().log(Level.WARNING, 
                "eeadmin.eventInstanceUnreachable.Warning", 
                serverName); 
                _result = new AdminEventResult(_event.getSequenceNumber());
                _result.setResultCode(AdminEventResult.TRANSMISSION_ERROR);
                _result.addException(serverName, e);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, 
                "eeadmin.eventInstanceError.Exception", e); 
                _result = new AdminEventResult(_event.getSequenceNumber());
                _result.setResultCode(AdminEventResult.ERROR);
                _result.addException(serverName, e);
        } 
    }

    /**
     * Returns the result from this end point.
     * 
     * @return  result from this end point
     */
    AdminEventResult getResult() {
        return _result;
    }

    // ---- VARIABLE(S) - PRIVATE -----------------------
    private EndPoint _endPoint                  = null;
    private AdminEvent _event                   = null;
    private AdminEventResult _result            = null;
    private static Logger _logger               = null;             
    private static final StringManager _strMgr  = 
            StringManager.getManager(EndPointHandler.class);
}
