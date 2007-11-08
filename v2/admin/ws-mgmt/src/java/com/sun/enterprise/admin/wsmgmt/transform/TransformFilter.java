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

import com.sun.enterprise.admin.wsmgmt.config.spi.Constants;
import com.sun.enterprise.admin.wsmgmt.config.spi.TransformationRule;
import com.sun.enterprise.admin.wsmgmt.filter.spi.Filter;
import com.sun.enterprise.admin.wsmgmt.filter.spi.FilterContext;

import com.sun.enterprise.admin.wsmgmt.config.spi.WebServiceConfig;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;



/**
 * Filter that can implement XSLT transformations.
 */
public class TransformFilter implements Filter {

    /**
     * Public Constructor.
     *
     * @param appId   name of the application
     * @param endpoint   end point name for which stats are collected
     */
    public TransformFilter(String appId, String endpoint) {
        _applicationId = appId;
        _endpointId    = endpoint;
        _rwl = new ReentrantReadWriteLock();
        _readLock = _rwl.readLock();
        _writeLock = _rwl.writeLock();
    }

    /**
     * Public Constructor.
     *
     * @param appId   name of the application
     * @param endpoint   end point name for which stats are collected
     */
    public TransformFilter(String appId, WebServiceConfig wsc) 
        throws TransformException {
        _applicationId = appId;
        _endpointId    = wsc.getName();
        TransformationRule[] reqRules = wsc.getRequestTransformationRule();
        if ( reqRules != null) {
            reqChain = new FilterChain(reqRules, false);
            //reqChain.addFilter(reqRules, false);
        } else {
            reqChain = null;
        }
        TransformationRule[] resRules = wsc.getResponseTransformationRule();
        if (resRules != null) {
            resChain = new FilterChain(resRules, true);
            //resChain.addFilter(resRules, true);
        } else {
            resChain = null;
        }
        _rwl = new ReentrantReadWriteLock();
        _readLock = _rwl.readLock();
        _writeLock = _rwl.writeLock();
    }

    // XXX optimize these resetXXXChain, rather than creating the new chain
    // modify the existing RequestChain. 

    public void resetRequestChain(TransformationRule[] tRules) throws
    TransformException {
        _writeLock.lock();  
        try {
            reqChain = new FilterChain(tRules, false);
        } finally {
           _writeLock.unlock();
        }
    }

    public void resetResponseChain(TransformationRule[] tRules)  throws
    TransformException {
        _writeLock.lock();  
        try {
            resChain = new FilterChain(tRules, true);
        } finally {
           _writeLock.unlock();
        }
    }

    /**
     * Returns the unique name for this filter
     */
    public String getName() {
        return (NAME_PREFIX + _applicationId + DELIM + _endpointId);
    }

    /**
     * Returns the unique name for this filter
     */
    static public String getName(String appId, WebServiceConfig wsc) {
        return (NAME_PREFIX + appId + DELIM + wsc.getName());
    }

    /**
     * Invoke the filter.
     * 
     * @param  stage   stage of the execution
     * @param  endpoint  name of the endpoint
     * @param  context  filter context 
     */
    public void process(String stage, String endpoint, FilterContext context) {

        _readLock.lock();
        try {
            // SOAP request
            if ( stage.equals(Filter.PROCESS_REQUEST) ) {
                // optmize this XXX
                if ( reqChain != null) {
                        reqChain.process(context);
                }
            // SOAP response
            } else if ( stage.equals(Filter.PROCESS_RESPONSE) ) {
                if ( resChain != null) {
                        resChain.process(context);
                }
            }
        } catch(Exception e) {
            // log a warning
            e.printStackTrace();
            String msg = _stringMgr.getString("transform_failed", endpoint);
            _logger.log(Level.INFO, msg, e.getMessage());
        } finally {
            _readLock.unlock();
        }

    }

    // -- PRIVATE - VARIABLES -------------------------
    private String _applicationId            = null;
    private String _endpointId               = null;
    private static final String DELIM        = "#";
    private static final String NAME_PREFIX  = "TRANSFORMFILTER_";
    private FilterChain reqChain = null;
    private FilterChain resChain= null;
    private ReentrantReadWriteLock _rwl = null;
    private Lock _readLock = null;
    private Lock _writeLock = null;
    private static final Logger _logger = 
        Logger.getLogger(LogDomains.ADMIN_LOGGER);
    private static final StringManager _stringMgr = 
        StringManager.getManager(TransformFilter.class);


}
