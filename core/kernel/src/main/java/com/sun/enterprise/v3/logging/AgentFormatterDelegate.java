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

package com.sun.enterprise.v3.logging;

import com.sun.enterprise.server.logging.FormatterDelegate;
import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.admin.monitor.callflow.ThreadLocalData;

import static com.sun.enterprise.server.logging.UniformLogFormatter.*;

import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: May 29, 2007
 * Time: 4:13:03 PM
 * To change this template use File | Settings | File Templates.
 */

public class AgentFormatterDelegate implements FormatterDelegate {

    Agent agent;

    public AgentFormatterDelegate(Agent agent) {
        this.agent = agent;
    }

    public void format(StringBuilder buf, Level level) {


        ThreadLocalData tld = agent.getThreadLocalData();
        if (tld==null) {
            return;
        }

        if (level.equals(Level.INFO) || level.equals(Level.CONFIG)) {

            if (tld.getApplicationName() != null) {
                buf.append("_ApplicationName").append(NV_SEPARATOR).
                        append(tld.getApplicationName()).
                        append(NVPAIR_SEPARATOR);
            }

        } else {

            if (tld.getRequestId() != null) {
                buf.append("_RequestID").append(NV_SEPARATOR).
                        append(tld.getRequestId()).append(NVPAIR_SEPARATOR);
            }

            if (tld.getApplicationName() != null) {
                buf.append("_ApplicationName").append(NV_SEPARATOR).
                        append(tld.getApplicationName()).
                        append(NVPAIR_SEPARATOR);
            }

            if (tld.getModuleName() != null) {
                buf.append("_ModuleName").append(NV_SEPARATOR).
                        append(tld.getModuleName()).append(NVPAIR_SEPARATOR);
            }

            if (tld.getComponentName() != null) {
                buf.append("_ComponentName").append(NV_SEPARATOR).
                        append(tld.getComponentName()).append(NVPAIR_SEPARATOR);
            }

            if (tld.getComponentType() != null) {
                buf.append("_ComponentType").append(NV_SEPARATOR).
                        append(tld.getComponentType()).append(NVPAIR_SEPARATOR);
            }

            if (tld.getMethodName() != null) {
                buf.append("_MethodName").append(NV_SEPARATOR).
                        append(tld.getMethodName()).append(NVPAIR_SEPARATOR);
            }

            if (tld.getTransactionId() != null) {
                buf.append("_TransactionId").append(NV_SEPARATOR).
                        append(tld.getTransactionId()).append(NVPAIR_SEPARATOR);
            }

            if (tld.getSecurityId() != null) {
                buf.append("_CallerId").append(NV_SEPARATOR).
                        append(tld.getSecurityId()).append(NVPAIR_SEPARATOR);
            }
        }
    }    
}
