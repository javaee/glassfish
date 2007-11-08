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

import java.util.Collection;
import com.sun.enterprise.admin.wsmgmt.config.spi.Constants;
import com.sun.enterprise.admin.wsmgmt.config.spi.ConfigFactory;
import com.sun.enterprise.admin.wsmgmt.config.spi.ConfigProvider;
import com.sun.enterprise.admin.wsmgmt.config.spi.WebServiceConfig;
import com.sun.enterprise.admin.wsmgmt.pool.spi.Pool;
import com.sun.enterprise.admin.wsmgmt.pool.impl.BoundedPool;
import com.sun.enterprise.admin.wsmgmt.filter.spi.Filter;
import com.sun.enterprise.admin.wsmgmt.filter.spi.FilterRegistry;
import com.sun.appserv.management.ext.wsmgmt.MessageTrace;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Keeps track of SOAP messages per endpoint.
 */
public class EndpointHandler {

    /**
     * Constructor.
     *
     * @param  endpoint  name of the endpoint
     */
    EndpointHandler(WebServiceConfig wsc, String appId) {

        _applicationId  = appId;
        _endpointId     = wsc.getName();
        String mLevel   = wsc.getMonitoringLevel();

        // SOAP message visualization is only enabled for level HIGH
        if (Constants.HIGH.equals(mLevel)) {
            _pool = new BoundedPool(wsc.getName(), wsc.getMaxHistorySize());
            registerFilter();
        }
    }

    /**
     * Constructor.
     *
     * @param  endpoint  name of the endpoint
     * @param  size  max size of the pool
     */
    EndpointHandler(String endpoint, int size, String appId) {
        _applicationId  = appId;
        _endpointId     = endpoint;
        _pool           = new BoundedPool(endpoint, size);

        registerFilter();
    }

    /**
     * Registers a filter with the filter manager for this endpoint.
     */
    private void registerFilter() {

        // msg filter
        _filter = new MessageFilter(_applicationId, _endpointId, this);
        FilterRegistry fr = FilterRegistry.getInstance();
        String endpoint = getFQEndpointName();

        // registers the filter
        fr.registerFilter(Filter.PROCESS_REQUEST, endpoint, _filter);
        fr.registerFilter(Filter.PROCESS_RESPONSE, endpoint, _filter);
        fr.registerFilter(Filter.POST_PROCESS_RESPONSE, endpoint, _filter);
    }

    /**
     * Sets the number of messages stored in memory for this endpoint. 
     * This method is called to dynamically reconfigure the size.
     *
     * @param  size  number of message stored in memory
     */
    void setMessageHistorySize(int size) {
        if (_pool != null) {
            _pool.resize(size);
            _logger.fine("Set message history size to " + size 
                + " for " + getEndpointName());
        }
    }

    /**
     * Disables monitoring for the endpoint and deregisters the filters.
     */
    void destroy() {
        if (_pool != null) {
            _pool.clear();
            _pool = null;
        }

        if (_filter != null) {
            FilterRegistry fr = FilterRegistry.getInstance();
            String endpoint = getFQEndpointName();

            // unregister filters
            fr.unregisterFilter(Filter.PROCESS_REQUEST, endpoint, _filter);
            fr.unregisterFilter(Filter.PROCESS_RESPONSE, endpoint, _filter);
            fr.unregisterFilter(Filter.POST_PROCESS_RESPONSE, endpoint, _filter);
            _filter = null;
        }
        _logger.finer("Message trace handler destroyed for " 
            + getEndpointName());
    }

    /**
     * Returns all messages for this endpoint.
     *
     * @return  messages associated for this endpoint
     */
    Collection getMessages() {
        return _pool.values();
    }

    /**
     * Adds a message trace to the pool.
     *
     * @param  msgTrace  a message trace object after the invocation
     */
    public void addMessage(MessageTrace msgTrace) {
        _pool.put(msgTrace.getMessageID(), msgTrace);
    }

    /**
     * Returns the name of the endpoint. 
     *
     * @return  name of the endpoint
     */
    String getEndpointName() {
        return _endpointId;
    }

    /**
     * Returns the fully qualified name of this endpoint.
     *
     * @return  fully qualified name of this endpoint
     */
    String getFQEndpointName() {
        return _applicationId + DELIM + _endpointId;
    }

    // ---- VARIABLES - PRIVATE ---------------------------------------
    private MessageFilter _filter     = null;
    private Pool _pool                = null;
    private String _endpointId        = null;
    private String _applicationId     = null;
    private static final String DELIM = "#";
    private static final Logger _logger = 
        Logger.getLogger(LogDomains.ADMIN_LOGGER);
    private static final StringManager _stringMgr = 
        StringManager.getManager(EndpointHandler.class);
}
