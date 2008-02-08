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
import com.sun.grizzly.DefaultProtocolChain;
import com.sun.grizzly.ProtocolFilter;
import java.util.List;
import java.util.logging.Level;

/**
 * Customized GlassFish's ProtocolChain.
 * 
 * @author Jeanfrancois Arcand
 */
class GlassfishProtocolChain extends DefaultProtocolChain{
    
    /**
     * Execute the ProtocolFilter.execute method. If a ProtocolFilter.execute
     * return false, avoid invoking the next ProtocolFilter. Override its parent
     * as the ProtocolFilter can dynamically be added.
     * 
     * @param ctx <code>Context</code>
     * @return position of next <code>ProtocolFilter</code> to exexute
     */
    @Override
    protected int executeProtocolFilter(Context ctx) {
        boolean invokeNext = true;
        int currentPosition = 0;
        ProtocolFilter protocolFilter = null;
        
        for (int i=0; i < protocolFilters.size(); i++) {
            try {
                protocolFilter = protocolFilters.get(i);
                invokeNext = protocolFilter.execute(ctx);
            } catch (Exception ex){
                invokeNext = false;
                i--;
                Controller.logger().log(Level.SEVERE,
                        "ProtocolChain exception",ex);
                notifyException(Phase.EXECUTE, protocolFilter, ex);
            }
            
            currentPosition = i;
            if ( !invokeNext ) break;
        }
        return currentPosition;
    }
    
    
    public List<ProtocolFilter> protocolFilters(){
        return protocolFilters;
    }

}
