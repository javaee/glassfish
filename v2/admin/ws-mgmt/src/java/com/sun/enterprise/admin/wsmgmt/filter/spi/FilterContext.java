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
package com.sun.enterprise.admin.wsmgmt.filter.spi;

import com.sun.enterprise.webservice.monitoring.Endpoint;
import com.sun.enterprise.webservice.monitoring.TransportInfo;
import com.sun.enterprise.admin.wsmgmt.SOAPMessageContext;

/**
 * Runtime context needed for Filter's execution 
 */
public class FilterContext {

    /**
     * Public constructor.
     *
     * @param ep        Endpoint information
     * @param callFlow  true, if CallFlow is enabled, otherwise false
     * @param info      Physical transport information
     * @param ctx       Message context for this web service invocation
     * @param id        Id of the message
     */
    public FilterContext(Endpoint ep, boolean callFlow, TransportInfo info, 
        SOAPMessageContext ctx, String id, String f) {

        endpoint = ep;
        tInfo = info;
        mCtx = ctx;
        mId = id;
        fqn = f;
        mIsCallFlowEnabled = callFlow;
        // note down when the filter context is created
        execTime = System.currentTimeMillis();
    }
    
    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint e) {
        endpoint = e;
    }

    public TransportInfo getTransportInfo() {
        return tInfo;
    }

    public void setTransportInfo(TransportInfo t) {
        tInfo = t;
    }

    public SOAPMessageContext getMessageContext() {
        return mCtx;
    }

    public void setMessageContext(SOAPMessageContext ctx) {
        mCtx = ctx;
    }

    public String getFullyQualifiedName() {
        return fqn;
    }

    public void setFullyQualifiedName(String f) {
        fqn = f;
    }

    public String getMessageId() {
        return mId;
    }

    public boolean isCallFlowEnabled() {
        return mIsCallFlowEnabled;
    }

    public long getExecutionTime() {
        return execTime;
    }

    public void setExecutionTime(long e) {
        execTime = e;
    }

    // PRIVATE VARIABLES

    private Endpoint endpoint;
    private TransportInfo tInfo;
    private SOAPMessageContext mCtx;
    private String mId;
    private String fqn;
    private boolean mIsCallFlowEnabled;
    private long execTime;
}
