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

import com.sun.enterprise.admin.wsmgmt.config.spi.Constants;
import com.sun.enterprise.admin.wsmgmt.filter.spi.Filter;
import com.sun.enterprise.admin.wsmgmt.filter.spi.FilterContext;
import com.sun.enterprise.admin.wsmgmt.msg.MessageTraceFactory;
import com.sun.appserv.management.ext.wsmgmt.MessageTrace;

/**
 * Filter that can implement or collect web services management information 
 */
public class MessageFilter implements Filter {

    /**
     * Public Constructor.
     *
     * @param appId   name of the application
     * @param endpoint   end point name for which stats are collected
     * @param h  endpoint handler
     */
    public MessageFilter(String appId, String endpoint, EndpointHandler h) {
        _applicationId = appId;
        _endpointId    = endpoint;
        _handler       = h;
    }

    /**
     * Returns the unique name for this filter
     */
    public String getName() {
        return (NAME_PREFIX + _applicationId + DELIM + _endpointId);
    }

    /**
     * Invoke the filter.
     * 
     * @param  stage   stage of the execution
     * @param  endpoint  name of the endpoint
     * @param  context  filter context 
     */
    public void process(String stage, String endpoint, FilterContext context) {

        MessageTraceFactory mtf = MessageTraceFactory.getInstance();

        // SOAP request
        if ( stage.equals(Filter.PROCESS_REQUEST) ) {

            // delegates to message trace factory for holding 
            mtf.processRequest(context, _applicationId);

        // SOAP response
        } else if ( stage.equals(Filter.PROCESS_RESPONSE) ) {
            mtf.processResponse(context);

        } else if (stage.equals(Filter.POST_PROCESS_RESPONSE) ) {
            MessageTrace mt = mtf.postProcessResponse(context);

            // adds the newly created message trace to the pool
            _handler.addMessage(mt);
        }
    }

    // -- PRIVATE - VARIABLES -------------------------
    private String _applicationId            = null;
    private String _endpointId               = null;
    public EndpointHandler _handler         = null;
    private static final String DELIM        = "#";
    private static final String NAME_PREFIX  = "MSGFILTER_";
}
