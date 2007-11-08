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
package com.sun.enterprise.admin.wsmgmt.filter.impl;

import com.sun.enterprise.admin.wsmgmt.config.spi.Constants;
import com.sun.enterprise.admin.wsmgmt.filter.spi.Filter;
import com.sun.enterprise.admin.wsmgmt.filter.spi.FilterContext;
import com.sun.enterprise.admin.wsmgmt.stats.spi.StatsProviderManager;
import com.sun.enterprise.admin.wsmgmt.stats.impl.WebServiceEndpointStatsProviderImpl;

import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPFault;
import com.sun.enterprise.admin.wsmgmt.SOAPMessageContext;


/**
 * Filter that can implement or collect web services management information 
 */
public class AggregateStatsFilter implements Filter {

    /**
     * Public Constructor.
     *
     * @param epName    End point name for which stats are collected
     */
    public AggregateStatsFilter() {
    }

    /**
     * Returns the unique name for this filter
     */
    public String getName() {
        return Constants.AGGREGATE_STATS_FILTER;
    }

    /**
     * Invoke the filter.
     */
    public void process(String stage, String endpoint, FilterContext context) {

        WebServiceEndpointStatsProviderImpl impl = (
            WebServiceEndpointStatsProviderImpl) StatsProviderManager.
            getInstance().getEndpointStatsProvider(endpoint);

        if ( stage.equals(Filter.PRE_PROCESS_REQUEST) ) {
            impl.setRequestTimeStamp( System.currentTimeMillis(),null, null, 0);
        } else {
            if ( stage.equals(Filter.POST_PROCESS_RESPONSE) ) {

               SOAPMessageContext smc = context.getMessageContext(); 
               SOAPMessage sm = null;
               SOAPFault fault = null;
               try {
                   if (smc != null)
                       sm = smc.getMessage();
                    if (sm != null) {
                        SOAPBody sb = sm.getSOAPBody();
                        if (sb != null) {
                            fault = sb.getFault();
                        }
                    }
                } catch ( Exception e) {
                    // if body can not be obtained, consider this as failure
                    // (fault) case, however fault information is not available
                    impl.setFault(0,
                    System.currentTimeMillis(), context.getExecutionTime(), null,null, null);
                    return;
                }
 
                if ( fault == null) { 
                    impl.setSuccess(0, System.currentTimeMillis(),
                    context.getExecutionTime());
                } else {
                    impl.setFault(0,
                    System.currentTimeMillis(),context.getExecutionTime(),
                    fault.getFaultCode(), fault.getFaultString(), 
                    fault.getFaultString() );
                }
            } else {
                throw new RuntimeException(" Should not be called for this stage of execution of web service end point " + endpoint);
            }
        }

    }

}
