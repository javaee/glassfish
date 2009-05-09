/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.v3.services.impl;

import com.sun.grizzly.Context;
import com.sun.grizzly.Controller;
import com.sun.grizzly.http.HttpProtocolChain;
import com.sun.grizzly.ProtocolFilter;

import java.util.List;
import java.util.logging.Level;

/**
 * Customized GlassFish's ProtocolChain.
 *
 * @author Jeanfrancois Arcand
 */
public class GlassfishProtocolChain extends HttpProtocolChain {
    // Allow disabling the continuous execution optimization from Grizzly.
    protected final static boolean CONTINUOUS_EXECUTION = Boolean.valueOf(
        System.getProperty("v3.grizzly.readFilter.continuousExecution",
            "false"));

    public GlassfishProtocolChain() {
        setContinuousExecution(CONTINUOUS_EXECUTION);
    }

    private List<ProtocolFilter> dynamicProtocolFilters;

    /**
     * Execute the ProtocolFilter.execute method. If a ProtocolFilter.execute
     * return false, avoid invoking the next ProtocolFilter. Override its parent
     * as the ProtocolFilter can dynamically be added.
     *
     * @param ctx <code>Context</code>
     *
     * @return position of next <code>ProtocolFilter</code> to exexute
     */
    @Override
    protected int executeProtocolFilter(Context ctx, int firstFilter) {
        boolean invokeNext = true;
        int currentPosition = 0;
        ProtocolFilter protocolFilter = null;
        for (int i = firstFilter; ; i++) {
            try {
                protocolFilter = getProtocolFilter(i);
                if (protocolFilter == null) {
                    break;
                }
                invokeNext = protocolFilter.execute(ctx);
            } catch (Exception ex) {
                invokeNext = false;
                i--;
                Controller.logger().log(Level.SEVERE,
                    "ProtocolChain exception", ex);
                notifyException(Phase.EXECUTE, protocolFilter, ex);
            }
            currentPosition = i;
            if (!invokeNext) {
                break;
            }
        }
        return currentPosition;
    }

    @Override
    protected boolean postExecuteProtocolFilter(int currentPosition,
        Context ctx) {
        boolean invokeNext = true;
        ProtocolFilter tmpHandler = null;
        boolean reinvokeChain = false;
        for (int i = currentPosition; i > -1; i--) {
            try {
                tmpHandler = getProtocolFilter(i);
                if (tmpHandler == null) {
                    break;
                }
                invokeNext = tmpHandler.postExecute(ctx);
            } catch (Exception ex) {
                Controller.logger().log(Level.SEVERE,
                    "ProtocolChain exception", ex);
                notifyException(Phase.POST_EXECUTE, tmpHandler, ex);
            }
            if (!invokeNext) {
                break;
            }
        }
        if (continousExecution
            && currentPosition >= protocolFilters.size() - 1
            && (Boolean) ctx.removeAttribute(ProtocolFilter.SUCCESSFUL_READ)
            == Boolean.TRUE) {
            reinvokeChain = true;
        }
        dynamicProtocolFilters = null;
        return reinvokeChain;
    }

    /**
     * Get's ProtocolFilter either from basic or dynamic filter list depending
     * on index
     *
     * @param index
     *
     * @return <code>ProtocolFilter</code>
     */
    protected ProtocolFilter getProtocolFilter(int index) {
        int basicFiltersNum = protocolFilters.size();
        if (index < basicFiltersNum) {
            return protocolFilters.get(index);
        } else if (dynamicProtocolFilters != null) {
            int dynamicFilterIndex = index - basicFiltersNum;
            if (dynamicFilterIndex < dynamicProtocolFilters.size()) {
                return dynamicProtocolFilters.get(dynamicFilterIndex);
            }
        }
        return null;
    }

    public List<ProtocolFilter> protocolFilters() {
        return protocolFilters;
    }

    public List<ProtocolFilter> getDynamicProtocolFilters() {
        return dynamicProtocolFilters;
    }

    public void setDynamicProtocolFilters(
        List<ProtocolFilter> dynamicProtocolFilters) {
        this.dynamicProtocolFilters = dynamicProtocolFilters;
    }
}
