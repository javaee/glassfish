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
 * DisplayStatistics.java
 *   A display data model class for displaying statistics in the DataProvider table
 * 
 *  @author ylee
 */

package com.sun.jbi.jsf.statistics;

import com.sun.jbi.jsf.framework.common.GenericConstants;
import com.sun.jbi.jsf.framework.common.Util;


public class DisplayStatistics {
    
    //
    private String endpoint = "";
    private String endpointShort = "";
    private String namespace = "";
    private String receivedRequests = "0";
    private String receivedReplies  = "0";
    private String receivedErrors   = "0";
    private String receivedDones    = "0";
    private String sentRequests     = "0";
    private String sentReplies      = "0";
    private String sentErrors       = "0";
    private String sentDones        = "0";
    
    public DisplayStatistics() {
    }
    
    public DisplayStatistics(String endpoint, String receivedRequests, String receivedReplies, String receivedErrors, 
            String receivedDones, String sentRequests, String sentReplies, String sentErrors, String sentDones) {
        this.endpoint = endpoint;
        this.receivedRequests = receivedRequests;
        this.receivedReplies = receivedReplies;
        this.receivedErrors = receivedErrors;
        this.receivedDones = receivedDones;
        this.sentRequests = sentRequests;
        this.sentReplies = sentReplies;
        this.sentErrors = sentErrors;
        this.sentDones = sentDones;
        this.namespace = Util.getNamespace(endpoint,GenericConstants.COMMA_SEPARATOR);
        this.endpointShort = Util.trimRight(endpoint,GenericConstants.COMMA_SEPARATOR);  //  //$NON-NLS-1$
        this.endpointShort = Util.trimLeft(endpointShort,GenericConstants.COMMA_SEPARATOR);
        
    }
    
    
    public DisplayStatistics(String endpoint, long receivedRequests, long receivedReplies, long receivedErrors, 
            long receivedDones, long sentRequests, long sentReplies, long sentErrors, long sentDones) {
        this(endpoint,receivedRequests+"",receivedReplies+"",receivedErrors+"",receivedDones+"",sentRequests+"",sentReplies+"",
                sentErrors+"",sentDones+"");
    }

    
    public String getEndpoint() {
        return endpoint;
    }

    public String getEndpointShort() {
        return endpointShort;
    }
    
    public void setEndpoint(String value) {
        this.endpoint = value;
    }
    
    public String getReceivedRequests() {
        return receivedRequests;
    }

    public void setReceivedRequests(String value) {
        this.receivedRequests = value;
    }
    
    public String getReceivedReplies() {
        return receivedReplies;
    }

    public void setReceivedReplies(String value) {
        this.receivedReplies = value;
    }    
    
       public String getReceivedErrors() {
        return receivedErrors;
    }

    public void setReceivedErrors(String value) {
        this.receivedErrors = value;
    }    
    
    public String getReceivedDones() {
        return receivedDones;
    }

    public void setReceivedDones(String value) {
        this.receivedDones = value;
    }        

    public String getSentRequests() {
        return sentRequests;
    }

    public void setSentRequests(String value) {
        this.sentRequests = value;
    }     
    

    public String getSentReplies() {
        return sentReplies;
    }

    public void setSentReplies(String value) {
        this.sentReplies = value;
    }         
    
    public String getSentErrors() {
        return sentErrors;
    }

    public void setSentErrors(String value) {
        this.sentErrors = value;
    }      

    public String getSentDones() {
        return sentDones;
    }

    public void setSentDones(String value) {
        this.sentDones = value;
    }      
        

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
}
