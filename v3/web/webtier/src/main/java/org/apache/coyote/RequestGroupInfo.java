

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

import java.util.ArrayList;

/** This can be moved to top level ( eventually with a better name ).
 *  It is currently used only as a JMX artifact, to agregate the data
 *  collected from each RequestProcessor thread.
 */
public class RequestGroupInfo {
    ArrayList<RequestInfo> processors=new ArrayList<RequestInfo>();
    private long deadMaxTime = 0;
    private long deadProcessingTime = 0;
    private int deadRequestCount = 0;
    private int deadErrorCount = 0;
    private long deadBytesReceived = 0;
    private long deadBytesSent = 0;

    // START S1AS
    private long deadCount2xx;
    private long deadCount3xx;
    private long deadCount4xx;
    private long deadCount5xx;
    private long deadCountOther;
    private long deadCount200;
    private long deadCount302;
    private long deadCount304;
    private long deadCount400;
    private long deadCount401;
    private long deadCount403;
    private long deadCount404;
    private long deadCount503;
    private long countOpenConnections;
    private long maxOpenConnections;
    // END S1AS


    public synchronized void addRequestProcessor( RequestInfo rp ) {
        processors.add( rp );
    }

    public synchronized void removeRequestProcessor( RequestInfo rp ) {
        if( rp != null ) {
            if( deadMaxTime < rp.getMaxTime() )
                deadMaxTime = rp.getMaxTime();
            deadProcessingTime += rp.getProcessingTime();
            deadRequestCount += rp.getRequestCount();
            deadErrorCount += rp.getErrorCount();
            deadBytesReceived += rp.getBytesReceived();
            deadBytesSent += rp.getBytesSent();

            // START S1AS
            deadCount2xx += rp.getCount2xx();
            deadCount3xx += rp.getCount3xx();
            deadCount4xx += rp.getCount4xx();
            deadCount5xx += rp.getCount5xx();
            deadCountOther += rp.getCountOther();
            deadCount200 += rp.getCount200();
            deadCount302 += rp.getCount302();
            deadCount304 += rp.getCount304();
            deadCount400 += rp.getCount400();
            deadCount401 += rp.getCount401();
            deadCount403 += rp.getCount403();
            deadCount404 += rp.getCount404();
            deadCount503 += rp.getCount503();
            // END S1AS

            processors.remove( rp );
        }
    }

    public synchronized long getMaxTime() {
        long maxTime=deadMaxTime;
        for( int i=0; i<processors.size(); i++ ) {
            RequestInfo rp = processors.get( i );
            if( maxTime < rp.getMaxTime() ) maxTime=rp.getMaxTime();
        }
        return maxTime;
    }

    // Used to reset the times
    public synchronized void setMaxTime(long maxTime) {
        deadMaxTime = maxTime;
        for( int i=0; i<processors.size(); i++ ) {
            RequestInfo rp = processors.get( i );
            rp.setMaxTime(maxTime);
        }
    }

    public synchronized long getProcessingTime() {
        long time=deadProcessingTime;
        for( int i=0; i<processors.size(); i++ ) {
            RequestInfo rp = processors.get( i );
            time += rp.getProcessingTime();
        }
        return time;
    }

    public synchronized void setProcessingTime(long totalTime) {
        deadProcessingTime = totalTime;
        for( int i=0; i<processors.size(); i++ ) {
            RequestInfo rp = processors.get( i );
            rp.setProcessingTime( totalTime );
        }
    }

    public synchronized int getRequestCount() {
        int requestCount=deadRequestCount;
        for( int i=0; i<processors.size(); i++ ) {
            RequestInfo rp = processors.get( i );
            requestCount += rp.getRequestCount();
        }
        return requestCount;
    }

    public synchronized void setRequestCount(int requestCount) {
        deadRequestCount = requestCount;
        for( int i=0; i<processors.size(); i++ ) {
            RequestInfo rp = processors.get( i );
            rp.setRequestCount( requestCount );
        }
    }

    public synchronized int getErrorCount() {
        int requestCount=deadErrorCount;
        for( int i=0; i<processors.size(); i++ ) {
            RequestInfo rp = processors.get( i );
            requestCount += rp.getErrorCount();
        }
        return requestCount;
    }

    public synchronized void setErrorCount(int errorCount) {
        deadErrorCount = errorCount;
        for( int i=0; i<processors.size(); i++ ) {
            RequestInfo rp = processors.get( i );
            rp.setErrorCount( errorCount);
        }
    }

    public synchronized long getBytesReceived() {
        long bytes=deadBytesReceived;
        for( int i=0; i<processors.size(); i++ ) {
            RequestInfo rp = processors.get( i );
            bytes += rp.getBytesReceived();
        }
        return bytes;
    }

    public synchronized void setBytesReceived(long bytesReceived) {
        deadBytesReceived = bytesReceived;
        for( int i=0; i<processors.size(); i++ ) {
            RequestInfo rp = processors.get( i );
            rp.setBytesReceived( bytesReceived );
        }
    }

    public synchronized long getBytesSent() {
        long bytes=deadBytesSent;
        for( int i=0; i<processors.size(); i++ ) {
            RequestInfo rp = processors.get( i );
            bytes += rp.getBytesSent();
        }
        return bytes;
    }

    public synchronized void setBytesSent(long bytesSent) {
        deadBytesSent = bytesSent;
        for( int i=0; i<processors.size(); i++ ) {
            RequestInfo rp = processors.get( i );
            rp.setBytesSent( bytesSent );
        }
    }

    // START S1AS
    public synchronized long getCount2xx() {
        long ret = deadCount2xx;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            ret += rp.getCount2xx();
        }
        return ret;
    }

    public synchronized void setCount2xx(long count) {
        deadCount2xx = count;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            rp.setCount2xx(count);
        }
    }

    public synchronized long getCount3xx() {
        long ret = deadCount3xx;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            ret += rp.getCount3xx();
        }
        return ret;
    }

    public synchronized void setCount3xx(long count) {
        deadCount3xx = count;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            rp.setCount3xx(count);
        }
    }

    public synchronized long getCount4xx() {
        long ret = deadCount4xx;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            ret += rp.getCount4xx();
        }
        return ret;
    }

    public synchronized void setCount4xx(long count) {
        deadCount4xx = count;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            rp.setCount4xx(count);
        }
    }

    public synchronized long getCount5xx() {
        long ret = deadCount5xx;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            ret += rp.getCount5xx();
        }
        return ret;
    }

    public synchronized void setCount5xx(long count) {
        deadCount5xx = count;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            rp.setCount5xx(count);
        }
    }

    public synchronized long getCountOther() {
        long ret = deadCountOther;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            ret += rp.getCountOther();
        }
        return ret;
    }

    public synchronized void setCountOther(long count) {
        deadCountOther = count;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            rp.setCountOther(count);
        }
    }

    public synchronized long getCount200() {
        long ret = deadCount200;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            ret += rp.getCount200();
        }
        return ret;
    }

    public synchronized void setCount200(long count) {
        deadCount200 = count;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            rp.setCount200(count);
        }
    }

    public synchronized long getCount302() {
        long ret = deadCount302;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            ret += rp.getCount302();
        }
        return ret;
    }

    public synchronized void setCount302(long count) {
        deadCount302 = count;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            rp.setCount302(count);
        }
    }

    public synchronized long getCount304() {
        long ret = deadCount304;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            ret += rp.getCount304();
        }
        return ret;
    }

    public synchronized void setCount304(long count) {
        deadCount304 = count;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            rp.setCount304(count);
        }
    }

    public synchronized long getCount400() {
        long ret = deadCount400;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            ret += rp.getCount400();
        }
        return ret;
    }

    public synchronized void setCount400(long count) {
        deadCount400 = count;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            rp.setCount400(count);
        }
    }

    public synchronized long getCount401() {
        long ret = deadCount401;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            ret += rp.getCount401();
        }
        return ret;
    }

    public synchronized void setCount401(long count) {
        deadCount401 = count;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            rp.setCount401(count);
        }
    }

    public synchronized long getCount403() {
        long ret = deadCount403;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            ret += rp.getCount403();
        }
        return ret;
    }

    public synchronized void setCount403(long count) {
        deadCount403 = count;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            rp.setCount403(count);
        }
    }

    public synchronized long getCount404() {
        long ret = deadCount404;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            ret += rp.getCount404();
        }
        return ret;
    }

    public synchronized void setCount404(long count) {
        deadCount404 = count;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            rp.setCount404(count);
        }
    }

    public synchronized long getCount503() {
        long ret = deadCount503;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            ret += rp.getCount503();
        }
        return ret;
    }

    public synchronized void setCount503(long count) {
        deadCount503 = count;
        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            rp.setCount503(count);
        }
    }

    public synchronized long getCountOpenConnections() {
        return countOpenConnections;
    }

    public synchronized void setCountOpenConnections(long count) {
        countOpenConnections = count;
        if (countOpenConnections > maxOpenConnections) {
            maxOpenConnections = countOpenConnections;
        }
    }

    public synchronized void increaseCountOpenConnections() {
        countOpenConnections++;
        if (countOpenConnections > maxOpenConnections) {
            maxOpenConnections = countOpenConnections;
        }
    }

    public synchronized void decreaseCountOpenConnections() {
        countOpenConnections--;
    }

    public synchronized long getMaxOpenConnections() {
        return maxOpenConnections;
    }

    public synchronized void setMaxOpenConnections(long count) {
        maxOpenConnections = count;
    }
    // END S1AS


    // START SJSAS 6338793
    /**
     * Gets the URI of the last request serviced.
     *
     * @return The URI of the last request serviced
     */
    public String getLastRequestURI() {

        long lastRequestCompletionTime = 0;
        String lastRequestURI = null;

        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            if (rp.getLastRequestCompletionTime() > lastRequestCompletionTime) {
                lastRequestCompletionTime = rp.getLastRequestCompletionTime();
                lastRequestURI = rp.getLastRequestURI();
            }
        }

        return lastRequestURI;
    }

    /**
     * Gets the HTTP method of the last request serviced.
     *
     * @return The HTTP method of the last request serviced
     */
    public String getLastRequestMethod() {

        long lastRequestCompletionTime = 0;
        String lastRequestMethod = null;

        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            if (rp.getLastRequestCompletionTime() > lastRequestCompletionTime) {
                lastRequestCompletionTime = rp.getLastRequestCompletionTime();
                lastRequestMethod = rp.getLastRequestMethod();
            }
        }

        return lastRequestMethod;
    }

    /**
     * Gets the time when the last request was completed.
     *
     * @return The time when the last request was completed.
     */
    public long getLastRequestCompletionTime() {

        long lastRequestCompletionTime = 0;

        for (int i=0; i<processors.size(); i++) {
            RequestInfo rp = processors.get(i);
            if (rp.getLastRequestCompletionTime() > lastRequestCompletionTime) {
                lastRequestCompletionTime = rp.getLastRequestCompletionTime();
            }
        }

        return lastRequestCompletionTime;
    }
    // END SJSAS 6338793


    public void resetCounters() {
        this.setBytesReceived(0);
        this.setBytesSent(0);
        this.setRequestCount(0);
        this.setProcessingTime(0);
        this.setMaxTime(0);
        this.setErrorCount(0);
        // START S1AS
        this.setCount2xx(0);
        this.setCount3xx(0);
        this.setCount4xx(0);
        this.setCount5xx(0);
        this.setCountOther(0);
        this.setCount200(0);
        this.setCount302(0);
        this.setCount304(0);
        this.setCount400(0);
        this.setCount401(0);
        this.setCount403(0);
        this.setCount404(0);
        this.setCount503(0);
        this.setCountOpenConnections(0);
        this.setMaxOpenConnections(0);
        // END S1AS
    }
}
