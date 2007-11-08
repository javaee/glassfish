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

/**
 * Statistics.java
 *   Statistics data model class
 * 
 *  @author - ylee
 */
package com.sun.jbi.jsf.statistics;

import com.sun.jbi.jsf.statistics.DisplayStatistics;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Each endpoint currently has 8 counters
 * sentDones, receivedDones, sentErrors, receivedErrors,
 * sentRequests, receivedRequests, sentReplies, receivedReplies
 */
public class Statistics implements Serializable {

    private String endpoint;
    private long sentRequests;
    private long sentReplies;
    private long sentErrors;
    private long sentDones;

    private long receivedRequests;
    private long receivedReplies;
    private long receivedErrors;
    private long receivedDones;

    /**
     *
     */
    public Statistics() {
    }

    /**
     * @param endpoint
     * @param sentrequests
     * @param sentreplies
     * @param senterrors
     * @param sentdones
     * @param receivedrequests
     * @param receivedreplies
     * @param receivederrors
     * @param receiveddones
     */
    public Statistics(String endpoint,
                            long sentrequests, long sentreplies, long senterrors, long sentdones,
                            long receivedrequests, long receivedreplies, long receivederrors, long receiveddones) {
        super();
        
        this.endpoint = endpoint;
        this.sentRequests = sentrequests;
        this.sentReplies = sentreplies;
        this.sentErrors = senterrors;
        this.sentDones = sentdones;
        this.receivedRequests = receivedrequests;
        this.receivedReplies = receivedreplies;
        this.receivedErrors = receivederrors;
        this.receivedDones = receiveddones;
    }

    /**
     * @param sentrequests
     * @param sentreplies
     * @param senterrors
     * @param sentdones
     * @param receivedrequests
     * @param receivedreplies
     * @param receivederrors
     * @param receiveddones
     */
    public Statistics(long sentrequests, long sentreplies, long senterrors, long sentdones,
                            long receivedrequests, long receivedreplies, long receivederrors, long receiveddones) {
        super();
        this.sentRequests = sentrequests;
        this.sentReplies = sentreplies;
        this.sentErrors = senterrors;
        this.sentDones = sentdones;
        this.receivedRequests = receivedrequests;
        this.receivedReplies = receivedreplies;
        this.receivedErrors = receivederrors;
        this.receivedDones = receiveddones;
    }

    /**
     *
     * @param endpoint
     * @param sentrequests
     * @param sentreplies
     * @param senterrors
     * @param sentdones
     * @param receivedrequests
     * @param receivedreplies
     * @param receivederrors
     * @param receiveddones
     */
    public void setValues(String endpoint,
            long sentrequests, long sentreplies, long senterrors, long sentdones,
            long receivedrequests, long receivedreplies, long receivederrors, long receiveddones) {
        this.endpoint = endpoint;
        this.sentRequests = sentrequests;
        this.sentReplies = sentreplies;
        this.sentErrors = senterrors;
        this.sentDones = sentdones;
        this.receivedRequests = receivedrequests;
        this.receivedReplies = receivedreplies;
        this.receivedErrors = receivederrors;
        this.receivedDones = receiveddones;
    }

    /**
     * @return Returns the endpointURL.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    public void add( long sentrequests, long sentreplies, long senterrors, long sentdones,
            long receivedrequests, long receivedreplies, long receivederrors, long receiveddones) {
        this.sentRequests += sentrequests;
        this.sentReplies += sentreplies;
        this.sentErrors += senterrors;
        this.sentDones += sentdones;
        this.receivedRequests += receivedrequests;
        this.receivedReplies += receivedreplies;
        this.receivedErrors += receivederrors;
        this.receivedDones += receiveddones;
    }


    /**
     * @param endpoint The endpointURL to set.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }


    /**
     * @return Returns the receivedDones.
     */
    public long getReceivedDones() {
        return this.receivedDones;
    }


    /**
     * @param receivedDones The receivedDones to set.
     */
    public void setReceivedDones(long receivedDones) {
        this.receivedDones = receivedDones;
    }


    /**
     * @return Returns the receivedErrors.
     */
    public long getReceivedErrors() {
        return this.receivedErrors;
    }


    /**
     * @param receivedErrors The receivedErrors to set.
     */
    public void setReceivedErrors(long receivedErrors) {
        this.receivedErrors = receivedErrors;
    }



    /**
     * @return Returns the receivedReplies.
     */
    public long getReceivedReplies() {
        return this.receivedReplies;
    }


    /**
     * @param receivedReplies The receivedReplies to set.
     */
    public void setReceivedReplies(long receivedReplies) {
        this.receivedReplies = receivedReplies;
    }



    /**
     * @return Returns the receivedRequests.
     */
    public long getReceivedRequests() {
        return this.receivedRequests;
    }



    /**
     * @param receivedRequests The receivedRequests to set.
     */
    public void setReceivedRequests(long receivedRequests) {
        this.receivedRequests = receivedRequests;
    }


    /**
     * @return Returns the sentDones.
     */
    public long getSentDones() {
        return this.sentDones;
    }


    /**
     * @param sentDones The sentDones to set.
     */
    public void setSentDones(long sentDones) {
        this.sentDones = sentDones;
    }


    /**
     * @return Returns the sentErrors.
     */
    public long getSentErrors() {
        return this.sentErrors;
    }


    /**
     * @param sentErrors The sentErrors to set.
     */
    public void setSentErrors(long sentErrors) {
        this.sentErrors = sentErrors;
    }


    /**
     * @return Returns the sentReplies.
     */
    public long getSentReplies() {
        return this.sentReplies;
    }



    /**
     * @param sentReplies The sentReplies to set.
     */
    public void setSentReplies(long sentReplies) {
        this.sentReplies = sentReplies;
    }



    /**
     * @return Returns the sentRequests.
     */
    public long getSentRequests() {
        return this.sentRequests;
    }



    /**
     * @param sentRequests The sentRequests to set.
     */
    public void setSentRequests(long sentRequests) {
        this.sentRequests = sentRequests;
    }

    /**
     * generate a DisplayStatistics object from statistics gathered by this class
     * @return List<DisplayStatistics>
     */
    public List<DisplayStatistics> generateDisplayStatistics() {
        List<DisplayStatistics> list = new ArrayList<DisplayStatistics>();
        DisplayStatistics displayStats;

        displayStats = new DisplayStatistics(endpoint,receivedRequests,receivedReplies,receivedErrors,receivedDones,
                sentRequests,sentReplies,sentErrors,sentDones);
        list.add(displayStats);
        return list;
    }


}
