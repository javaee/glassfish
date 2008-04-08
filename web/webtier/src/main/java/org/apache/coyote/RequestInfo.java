

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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


package org.apache.coyote;


/**
 * Structure holding the Request and Response objects. It also holds statistical
 * informations about request processing and provide management informations
 * about the requests beeing processed.
 *
 * Each thread uses a Request/Response pair that is recycled on each request.
 * This object provides a place to collect global low-level statistics - without
 * having to deal with synchronization ( since each thread will have it's own
 * RequestProcessorMX ).
 *
 * TODO: Request notifications will be registered here.
 *
 * @author Costin Manolache
 */
public class RequestInfo  {

    private RequestGroupInfo global=null;

    private Request req;

    private int stage = Constants.STAGE_NEW;

    /*
     * Statistical data collected at the end of each request.
     */
    private long bytesSent;

    private long bytesReceived;

    // Total time = divide by requestCount to get average.
    private long processingTime;

    // The longest response time for a request
    private long maxTime;

    // URI of the request that took maxTime
    private String maxRequestUri;

    private int requestCount;

    // number of response codes >= 400
    private int errorCount;

    // START S1AS
    // Number of responses with a status code in the 2xx range
    private long count2xx;

    // Number of responses with a status code in the 3xx range
    private long count3xx;

    // Number of responses with a status code in the 4xx range
    private long count4xx;

    // Number of responses with a status code in the 5xx range
    private long count5xx;

    // Number of responses with a status code outside the 2xx, 3xx, 4xx,
    // and 5xx range
    private long countOther;

    // Number of responses with a status code equal to 200
    private long count200;

    // Number of responses with a status code equal to 302
    private long count302;

    // Number of responses with a status code equal to 304
    private long count304;

    // Number of responses with a status code equal to 400
    private long count400;

    // Number of responses with a status code equal to 401
    private long count401;

    // Number of responses with a status code equal to 403
    private long count403;

    // Number of responses with a status code equal to 404
    private long count404;

    // Number of responses with a status code equal to 503
    private long count503;

    // Worker thread ID that processes the associated request
    private long workerThreadID = 0;

    // Request completion time
    private long requestCompletionTime;
    // END S1AS

    // START SJSAS 6338793
    private String lastURI;
    private String lastMethod;
    private long lastCompletionTime;
    // END SJSAS 6338793

    /**
     * Constructor
     */
    public RequestInfo( Request req) {
        this.req=req;
    }

    public RequestGroupInfo getGlobalProcessor() {
        return global;
    }
    
    public void setGlobalProcessor(RequestGroupInfo global) {
        if( global != null) {
            this.global=global;
            global.addRequestProcessor( this );
        } else {
            if (this.global != null) {
                this.global.removeRequestProcessor( this ); 
                this.global = null;
            }
        }
    }


    // ------------------- Information about the current request  -----------
    // This is useful for long-running requests only

    public String getMethod() {
        return req.method().toString();
    }

    public String getCurrentUri() {
        return req.requestURI().toString();
    }

    public String getCurrentQueryString() {
        return req.queryString().toString();
    }

    public String getProtocol() {
        return req.protocol().toString();
    }

    public String getVirtualHost() {
        return req.serverName().toString();
    }

    public int getServerPort() {
        return req.getServerPort();
    }

    public String getRemoteAddr() {
        req.action(ActionCode.ACTION_REQ_HOST_ADDR_ATTRIBUTE, null);
        return req.remoteAddr().toString();
    }

    public int getContentLength() {
        return req.getContentLength();
    }

    public long getRequestBytesReceived() {
        return req.getBytesRead();
    }

    public long getRequestBytesSent() {
        return req.getResponse().getBytesWritten();
    }

    public long getRequestProcessingTime() {
        return (System.currentTimeMillis() - req.getStartTime());
    }


    /**
     * Called by the processor before recycling the request. It'll collect
     * statistic information.
     */
    void updateCounters() {
        bytesReceived+=req.getBytesRead();
        bytesSent+=req.getResponse().getBytesWritten();

        requestCount++;

        int responseStatus = req.getResponse().getStatus();

        // START S1AS
        if (responseStatus >= 200 && responseStatus < 299) {
            // 2xx
            count2xx++;
            if (responseStatus == 200) {
                count200++;
            }
        } else if (responseStatus >= 300 && responseStatus < 399) {
            // 3xx
            count3xx++;
            if (responseStatus == 302) {
                count302++;
            } else if (responseStatus == 304) {
                count304++;
            }
        } else if (responseStatus >= 400 && responseStatus < 499) {
            // 4xx
            count4xx++;
            if (responseStatus == 400) {
                count400++;
            } else if (responseStatus == 401) {
                count401++;
            } else if (responseStatus == 403) {
                count403++;
            } else if (responseStatus == 404) {
                count404++;
            }
        } else if (responseStatus >= 500 && responseStatus < 599) {
            // 5xx
            count5xx++;
            if (responseStatus == 503) {
                count503++;
            }
        } else {
            // Other
            countOther++;
        }
        // END S1AS

        if (responseStatus >= 400) {
            errorCount++;
        }

        long t0=req.getStartTime();
        long t1=System.currentTimeMillis();

        requestCompletionTime = t1-t0;
        processingTime+=requestCompletionTime;
        if( maxTime < requestCompletionTime ) {
            maxTime=requestCompletionTime;
            maxRequestUri=req.requestURI().toString();
        }

        // START SJSAS 6338793
        lastURI = req.requestURI().toString();
        lastMethod = req.method().toString();
        lastCompletionTime = t1;
        // END SJAS 6338793
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public void setBytesSent(long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public long getBytesReceived() {
        return bytesReceived;
    }

    public void setBytesReceived(long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    public String getMaxRequestUri() {
        return maxRequestUri;
    }

    public void setMaxRequestUri(String maxRequestUri) {
        this.maxRequestUri = maxRequestUri;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    // START S1AS
    public long getCount2xx() {
        return count2xx;
    }

    public void setCount2xx(long count2xx) {
        this.count2xx = count2xx;
    }

    public long getCount3xx() {
        return count3xx;
    }

    public void setCount3xx(long count3xx) {
        this.count3xx = count3xx;
    }

    public long getCount4xx() {
        return count4xx;
    }

    public void setCount4xx(long count4xx) {
        this.count4xx = count4xx;
    }

    public long getCount5xx() {
        return count5xx;
    }

    public void setCount5xx(long count5xx) {
        this.count5xx = count5xx;
    }

    public long getCountOther() {
        return countOther;
    }

    public void setCountOther(long countOther) {
        this.countOther = countOther;
    }

    public long getCount200() {
        return count200;
    }

    public void setCount200(long count200) {
        this.count200 = count200;
    }

    public long getCount302() {
        return count302;
    }

    public void setCount302(long count302) {
        this.count302 = count302;
    }

    public long getCount304() {
        return count304;
    }

    public void setCount304(long count304) {
        this.count304 = count304;
    }

    public long getCount400() {
        return count400;
    }

    public void setCount400(long count400) {
        this.count400 = count400;
    }

    public long getCount401() {
        return count401;
    }

    public void setCount401(long count401) {
        this.count401 = count401;
    }

    public long getCount403() {
        return count403;
    }

    public void setCount403(long count403) {
        this.count403 = count403;
    }

    public long getCount404() {
        return count404;
    }

    public void setCount404(long count404) {
        this.count404 = count404;
    }

    public long getCount503() {
        return count503;
    }

    public void setCount503(long count503) {
        this.count503 = count503;
    }

    /**
     * Gets the worker thread ID which is processing the request associated
     * with this RequestInfo. Return 0 if no thread ID has been associated.
     * 
     * @return The worker thread id
     */
    public long getWorkerThreadID() {
        return workerThreadID;
    }

    /**
     * Sets the worker thread ID responsible for processing the request 
     * associated with this RequestInfo. 
     *
     * @param workerThread The worker thread is
     */
    public void setWorkerThreadID(long workerThreadID) {
        this.workerThreadID = workerThreadID;
    }
  
    /**
     * Gets the time taken to complete the request associated
     * with this RequestInfo.
     */
    public long getRequestCompletionTime() {
        return requestCompletionTime;
    }

    /**
     * Sets the time taken to complete the request associated
     * with this RequestInfo.
     */
    public void setRequestCompletionTime(long completionTime) {
        this.requestCompletionTime = completionTime;
    }
    // END S1AS


    // START SJSAS 6338793
    /**
     * Gets the URI of the last request serviced.
     *
     * @return The URI of the last request serviced
     */
    public String getLastRequestURI() {
        return lastURI;
    }

    /**
     * Gets the HTTP method of the last request serviced.
     *
     * @return The HTTP method of the last request serviced
     */
    public String getLastRequestMethod() {
        return lastMethod;
    }

    /**
     * Gets the time when the last request was completed.
     *
     * @return The time when the last request was completed.
     */
    public long getLastRequestCompletionTime() {
        return lastCompletionTime;
    }
    // END SJSAS 6338793


    // START S1AS
    /**
     * Resets this <code>RequestInfo</code>.
     */
    public void reset() {
        setBytesSent(0);
        setBytesReceived(0);
        setProcessingTime(0);
        setMaxTime(0);
        setMaxRequestUri(null);
        setRequestCount(0);
        setErrorCount(0);
        setCount2xx(0);
        setCount3xx(0);
        setCount4xx(0);
        setCount5xx(0);
        setCountOther(0);
        setCount200(0);
        setCount302(0);
        setCount304(0);
        setCount400(0);
        setCount401(0);
        setCount403(0);
        setCount404(0);
        setCount503(0);
        setWorkerThreadID(0);
        setRequestCompletionTime(0);
        // START SJSAS 6338793
        lastMethod = null;
        lastURI = null;
        lastCompletionTime = 0;
        // END SJSAS 6338793
    }
    // END S1AS
}
