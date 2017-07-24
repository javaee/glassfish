/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.tools.wsmonitoring;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;

import com.sun.logging.LogDomains;

import com.sun.enterprise.webservice.monitoring.MessageListener;
import com.sun.enterprise.webservice.monitoring.MessageTrace;
import com.sun.enterprise.webservice.monitoring.Endpoint;

/**
 * This class is responsible for monitoring a particular
 * endpoint.
 *
 * @author dochez
 */
public class EndpointMonitor implements MessageListener {
    
    static final Logger sLogger=Logger.getLogger(LogDomains.WEB_LOGGER);        
    
    MessageExchange[] traces;
    int traceIndex=0;
        
    Endpoint endpoint;
        
    /** Creates a new instance of EndpointMonitor */
    public EndpointMonitor(Endpoint endpoint) {
        this.endpoint = endpoint;
        traces = new MessageExchange[5];
    }
        
    public Endpoint getEndpoint() {
        return endpoint;
    }                          
    
    public void invocationProcessed(MessageTrace request, MessageTrace response) {
        
        MessageExchange newExchange = new MessageExchange();
        newExchange.request = request;
        newExchange.response = response;
        
        traces[traceIndex++] = newExchange;
        if (traceIndex==traces.length) {
            traceIndex=0;
        }        
    }
    
        
    public MessageExchange[] getInvocationTraces() {
        
        Vector<MessageExchange> v = new Vector<MessageExchange>();
        if (traces[traceIndex]!=null) {
            for (int i=traceIndex;i<traces.length;i++) {
                v.add(traces[i]);
            }
        }
        for (int i=0;i<traceIndex;i++) {
            v.add(traces[i]);
        }
        if (v.isEmpty()) {
            return null;
        } else {
            return (MessageExchange[]) v.toArray(new MessageExchange[0]);
        }
    }
    
}
