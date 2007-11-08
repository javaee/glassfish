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
package com.sun.enterprise.admin.wsmgmt.transform;

import java.util.Collection;
import java.util.List;
import java.util.Iterator;

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
public class TransformHandler {

    /**
     * Constructor.
     *
     * @param  endpoint  name of the endpoint
     */
    public TransformHandler(WebServiceConfig wsc, String appId) throws
    TransformException {

        _applicationId  = appId;
        _endpointId     = wsc.getName();

    }

    /**
     * Constructor.
     *
     * @param  endpoint  name of the endpoint
     */
    public TransformHandler(String endpoint, String appId) {
        _applicationId  = appId;
        _endpointId     = endpoint;

    }

    /**
     * Registers a filter with the filter manager for this endpoint.
     */
    public Filter registerFilter(WebServiceConfig wsc) 
        throws TransformException {

        _filter = new TransformFilter(_applicationId, wsc);
        FilterRegistry fr = FilterRegistry.getInstance();
        String endpoint = getFQEndpointName();

        // registers the filter
        fr.registerFilter(Filter.PROCESS_REQUEST, endpoint, _filter);
        fr.registerFilter(Filter.PROCESS_RESPONSE, endpoint, _filter);
        return _filter;
    }

    /**
     * Registers a filter with the filter manager for this endpoint.
     */
    private void registerFilter() {

        // msg filter
        _filter = new TransformFilter(_applicationId, _endpointId);
        FilterRegistry fr = FilterRegistry.getInstance();
        String endpoint = getFQEndpointName();

        // registers the filter
        fr.registerFilter(Filter.PROCESS_REQUEST, endpoint, _filter);
        fr.registerFilter(Filter.PROCESS_RESPONSE, endpoint, _filter);
    }

    /**
     * Disables transformation for the endpoint and deregisters the filters.
     */
    void destroy() {
        if (_filter != null) {
            FilterRegistry fr = FilterRegistry.getInstance();
            String endpoint = getFQEndpointName();

            // unregister filters
            fr.unregisterFilter(Filter.PROCESS_REQUEST, endpoint, _filter);
            fr.unregisterFilter(Filter.PROCESS_RESPONSE, endpoint, _filter);
            _filter = null;
        }
        _logger.finer("Transform handler destroyed for " 
            + getEndpointName());
    }

    /**
     * Disables transformation for the endpoint and deregisters the filters.
     */
    void unregisterFilter(String appId, WebServiceConfig wsc) {
            String fn =  TransformFilter.getName(appId, wsc);
            FilterRegistry fr = FilterRegistry.getInstance();
            String endpoint = getFQEndpointName(appId, wsc);

            // unregister filters
            fr.unregisterFilterByName(Filter.PROCESS_REQUEST, endpoint, fn);
            fr.unregisterFilterByName(Filter.PROCESS_RESPONSE, endpoint, fn);
        _logger.finer("Transform handler destroyed for " 
            + getEndpointName());
    }

    /**
     * Gets the registered filter for transformation.
     */
    public Filter getFilter(String appId, WebServiceConfig wsc) {
            FilterRegistry fr = FilterRegistry.getInstance();
            String endpoint = getFQEndpointName(appId, wsc);

        _logger.finer("getFilter called for " 
            + getEndpointName());
           List filterList= fr.getFilters(Filter.PROCESS_REQUEST, endpoint);
           if ( filterList == null) {
                return null;
           }
           Iterator filterItr = filterList.iterator();
           if ( filterItr == null) {
                return null;
           }
          while ( filterItr.hasNext()) {
                Filter f = (Filter) filterItr.next();
                if ( f instanceof TransformFilter) {
                return f;
          }
        }
        return null;
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

    /**
     * Returns the fully qualified name of this endpoint.
     *
     * @return  fully qualified name of this endpoint
     */
    String getFQEndpointName(String appId, WebServiceConfig wsc) {
        return appId + DELIM + wsc.getName();
    }

    // ---- VARIABLES - PRIVATE ---------------------------------------
    private TransformFilter _filter     = null;
    private String _endpointId        = null;
    private String _applicationId     = null;
    private static final String DELIM = "#";
    private static final Logger _logger = 
        Logger.getLogger(LogDomains.ADMIN_LOGGER);
    private static final StringManager _stringMgr = 
        StringManager.getManager(TransformHandler.class);
}
