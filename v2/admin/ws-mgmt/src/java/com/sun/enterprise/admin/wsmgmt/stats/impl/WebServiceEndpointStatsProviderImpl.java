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
package com.sun.enterprise.admin.wsmgmt.stats.impl;

import com.sun.enterprise.admin.wsmgmt.stats.spi.WebServiceEndpointStatsProvider;

/**
 * A Class for providing stats for Web Service Endpoint.
 *
 * @author Satish Viswanatham
 */
public class WebServiceEndpointStatsProviderImpl 
    implements WebServiceEndpointStatsProvider
{

    public WebServiceEndpointStatsProviderImpl() {
        faultActor = faultString = faultCode = clientHost = clientUser = null;
        responseSize = requestSize = totalFaults =
        totalSuccess = totalAuthFailures =  totalAuthSuccess = 0;
        responseTime = avgResponseTime =
                totalResponseTime = maxResponseTime = (long)0;
        resetTime = System.currentTimeMillis();
        minResponseTime = -1;
        throughput = 0.0;

    }

    // Setters -- that change the state

    public void setRequestTimeStamp(long t , String host, String user, 
        int rSize) {

        enterTime = t;
        reqSize = rSize;
        cHost = host;
        cUser = user;
    }

    public void setSuccess(int resSize, long exitTime, long respTime) {
        
        precheck();

        responseTime =  respTime;
        calculateResponseTimes (responseTime);
        clientHost = cHost;
        clientUser = cUser;
        requestSize = reqSize;
        responseSize = resSize;
        totalSuccess++;
        totalAuthSuccess++;
    
        // since this is success reset the fault related values
        faultCode = faultString = faultActor = null;

        cleanup();
    }

    void calculateResponseTimes (long curRespTime) {
        if ( curRespTime > maxResponseTime) {
            maxResponseTime = curRespTime;
        }

        if (minResponseTime < 0 ) {
            minResponseTime = curRespTime;
        } else if ( curRespTime < minResponseTime) {
            minResponseTime = curRespTime;
        }
        totalResponseTime += curRespTime;
    }

    public void setFault(int resSize, long exitTime, long rTime, String fCode, String fString,
    String fActor) {
        precheck();

        responseTime = rTime;
        calculateResponseTimes (responseTime);
        clientHost = cHost;
        clientUser = cUser;
        requestSize = reqSize;
        responseSize = resSize;
        totalFaults++;
        totalAuthSuccess++;
    
        // since this is success reset the fault related values
        faultCode = fCode;
        faultString = fString;
        faultActor = fActor;

        cleanup();

    }

    public void setAuthFailure (long t) {

        // precheck(); -- precheck may not be called for auth failures

        clientHost = cHost;
        clientUser = cUser;
        requestSize = reqSize;
        totalFaults++;
        totalAuthFailures++;
    
        // since this is success reset the fault related values
        faultCode = faultString = faultActor = null;

        cleanup();

    }

    public void precheck() {
        if ( enterTime == 0 ) {
            throw new RuntimeException(
            "Request method should also update the request stats");
        }

        // XXX store this stat into message store if history size is > 0

    }

    public void cleanup() {
        enterTime = 0;
        cHost = cUser = null;
    }

    public void reset() {
        faultActor = faultString = faultCode = clientHost = clientUser = null;
        responseSize = requestSize = totalFaults =
        totalSuccess = totalAuthFailures =  totalAuthSuccess = 0;
        responseTime = totalResponseTime = maxResponseTime =
        avgResponseTime = (long) 0;
        resetTime = System.currentTimeMillis();
        throughput = 0.0;
        minResponseTime = -1;
    }

    // Getters -- used by the monioring MBeans
    public long getLastResetTime() {
        return resetTime;
    }

    public long getAverageResponseTime() {
        int totalInv =  totalSuccess + totalFaults;
        if  (totalInv == 0 ) {
            return (long)0;
        } else {
            return totalResponseTime/ (totalInv);
        }
    }

    public long getMinResponseTime() {
        if (minResponseTime < 0 ) {
            return 0;
        } else {
            return minResponseTime;
        }
    }

    public long getMaxResponseTime() {
        return maxResponseTime;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public int getTotalFailures() {
        return totalFaults;
    }

    public int getTotalSuccesses() {
        return totalSuccess;
    }

    public int getTotalAuthFailures() {
        return totalAuthFailures;
    }

    public int getTotalAuthSuccesses() {
        return totalAuthSuccess;
    }

    public double getThroughput() {
        long avgRespTime = getAverageResponseTime();
        double dValue = avgRespTime;
        if ( avgRespTime == 0 ) {
            return 0;
        } else {
            return 1000/dValue;
        }
    }

    public int getRequestSize() {
        return requestSize;
    }

    public int getResponseSize() {
        return responseSize;
    }

    public String getClientHost() {
        return clientHost;
    }

    public String getClientUser() {
        return clientUser;
    }

    public String getFaultCode() {
        return faultCode;
    }

    public String getFaultString() {
        return faultString;
    }

    public String getFaultActor() {
        return faultActor;
    }

    // PRIVATE VARS

    String faultActor, faultString, faultCode, clientHost, clientUser;

    int responseSize, requestSize, totalFaults, totalSuccess,
        totalAuthFailures, totalAuthSuccess;
    long responseTime, totalResponseTime, avgResponseTime, minResponseTime, 
        maxResponseTime;
    double throughput;

    // Temp data to hold between request and reponse stage, before this
    // Statistic is posted

    long enterTime =0;
    long resetTime =0;
    int    reqSize = 0;
    String    cHost =  null, cUser = null;
}
