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
package com.sun.enterprise.admin.wsmgmt.msg;

import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import com.sun.enterprise.admin.wsmgmt.config.spi.Constants;
import com.sun.enterprise.admin.wsmgmt.config.spi.ConfigFactory;
import com.sun.enterprise.admin.wsmgmt.config.spi.ConfigProvider;
import com.sun.enterprise.admin.wsmgmt.config.spi.WebServiceConfig;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Keeps track of SOAP messages per application or stand alone module.
 */
class ApplicationMediator {

    /**
     * Constructor.
     *
     * @param  id  name of the application
     */
    ApplicationMediator(String id) throws MessageTraceException {

        _applicationId = id;
        _endpoints     = new Hashtable();

        // initializes the endpoint handler
        try {
            ConfigFactory cf = ConfigFactory.getConfigFactory();
            ConfigProvider cp = cf.getConfigProvider();
            WebServiceConfig[] wsc = cp.getWebserviceConfigs(id);

            for (int i=0; i<wsc.length; i++) {
                String mLevel = wsc[i].getMonitoringLevel();

                // SOAP message visualization is only enabled for level HIGH
                if (Constants.HIGH.equals(mLevel)) {
                    EndpointHandler eph = new EndpointHandler(wsc[i], id);
                    _endpoints.put(eph.getEndpointName(), eph);
                }
            }
        } catch (Exception e) {
            String msg=_stringMgr.getString("ApplicationMediator_ConfigEx",id);
            throw new MessageTraceException(msg, e);
        }
    }

    /**
     * Sets the number of messages stored in memory for this application. 
     * This method is called to dynamically reconfigure the size.
     *
     * @param  wsEndpoint  name of the webservice endpoint
     * @param  size  number of message stored in memory
     */
    void setMessageHistorySize(String wsEndpoint, int size) {
        EndpointHandler eph = (EndpointHandler) _endpoints.get(wsEndpoint);
        if (eph != null) {
            eph.setMessageHistorySize(size);
        }
    }

    /**
     * Disables monitoring for the endpoint.
     *
     * @param  wsEndpoint  name of the webservice endpoint
     */
    void disable(String wsEndpoint) {
        EndpointHandler eph = (EndpointHandler) _endpoints.remove(wsEndpoint);
        if (eph != null) {
            eph.destroy();
        }
    }

    /**
     * Enables monitoring for the endpoint.
     *
     * @param  wsEndpoint  name of the webservice endpoint
     * @param  size  max size of the messages in history
     */
    void enable(String wsEndpoint, int size) {
        EndpointHandler eph = 
            new EndpointHandler(wsEndpoint, size, _applicationId);
        _endpoints.put(wsEndpoint, eph);
    }

    /**
     * Returns true if there are no endpoints in this application mediator.
     *
     * @return  true if mediator is empty
     */
    boolean isEmpty() {
        Collection c = _endpoints.values();
        return c.isEmpty();
    }

    /**
     * Returns messages for the given endpoint.
     *
     * @param  wsEndpoint  web service endpoint
     * @return  messages for the given endpoint
     */
    Collection getMessages(String wsEndpoint) {
        EndpointHandler eph = (EndpointHandler) _endpoints.get(wsEndpoint);
        if (eph != null) {
            return eph.getMessages();
        }
        return null;
    }

    /**
     * Returns all messages for this application.
     *
     * @return  messages associated for this application
     */
    Collection getMessages() {

        Collection c = new ArrayList();
        Collection endpoints = _endpoints.values();
        for (Iterator iter=endpoints.iterator(); iter.hasNext();) {
            EndpointHandler eph = (EndpointHandler) iter.next();
            if (eph != null) {
                c.addAll( eph.getMessages() );
            }
        }

        return c;
    }

    /**
     * Stops message visualization for this application.
     */
    void destroy() {
        Collection endpoints = _endpoints.values();
        for (Iterator iter=endpoints.iterator(); iter.hasNext();) {
            EndpointHandler eph = (EndpointHandler) iter.next();
            if (eph != null) {
                eph.destroy();
            }
        }
        _endpoints.clear();
        _endpoints = null;
        _logger.finer("Message trace mediator destroyed for " + _applicationId);
    }

    // ---- VARIABLES - PRIVATE ---------------------------------------
    private Map _endpoints         = null;
    private String _applicationId  = null;
    private static final Logger _logger = 
        Logger.getLogger(LogDomains.ADMIN_LOGGER);
    private static final StringManager _stringMgr = 
        StringManager.getManager(ApplicationMediator.class);

}
