/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

import java.net.URL;
import javax.faces.*;
import com.sun.jsfcl.app.*;
import java.util.ArrayList;
import java.util.Iterator;

import javax.faces.model.SelectItem;

// AppServer imports

import com.sun.enterprise.webservice.monitoring.WebServiceEngineFactory;
import com.sun.enterprise.webservice.monitoring.WebServiceEngine;
import com.sun.enterprise.webservice.monitoring.Endpoint;
import com.sun.enterprise.webservice.monitoring.MessageTrace;
import com.sun.enterprise.webservice.monitoring.EndpointLifecycleListener;
import com.sun.enterprise.deployment.WebServiceEndpoint;

public class SessionInfo extends AbstractSessionBean
        implements EndpointLifecycleListener {

    // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Definition">

    private int __placeholder;

    /**
     * Holds value of property endpointsListItems.
     */
    private ArrayList endpointsListItems;

    /**
     * Holds value of property endpointListSelection.
     */
    private String endpointListSelection;

    private String wsdlURL;

    /**
     * Holds value of property messageListItems.
     */
    private ArrayList messageListItems;

    /**
     * Holds value of property messageListSelection.
     */
    private String messageListSelection;

    /**
     * Holds value of property endpointInfo.
     */
    private String endpointInfo;

    /**
     * Holds value of property displayEnv.
     */
    private Boolean displayEnv;

    // </editor-fold>    
    
    // </editor-fold>
    public SessionInfo() {
        // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Initialization">
        try {
        } catch (Exception e) {
            log("SessionBean1 Initialization Failure", e);
            throw e instanceof javax.faces.FacesException ? (FacesException) e : new FacesException(e);
        }
        // </editor-fold>
        // Additional user provided initialization code
        endpointsListItems = new ArrayList();
        messageListItems = new ArrayList();
        WebServiceEngine wsEngine = WebServiceEngineFactory.getInstance().getEngine();
        if (wsEngine==null) {
            // display error
            java.lang.System.out.println("No WebServiceEngin defined");
            return;
        }
        wsEngine.addLifecycleListener(this);
        refreshEndpointList();
        displayEnv = new Boolean(true);        
    }

    void refreshEndpointList() {

        getEndpointsListItems().clear();
        WebServiceEngine wsEngine = WebServiceEngineFactory.getInstance().getEngine();        
        if (wsEngine==null)
            return;
        
        Iterator<Endpoint> endpoints = wsEngine.getEndpoints();
        if (endpoints.hasNext()) {
            do{
                Endpoint endpoint = endpoints.next();
                SelectItem si =new SelectItem(endpoint.getEndpointSelector());
                getEndpointsListItems().add(si);
                if (getEndpointsListItems().size()==1){
                    endpointListSelection=endpoint.getEndpointSelector();
                }
            } while (endpoints.hasNext());
        } else {
            java.lang.System.out.println("No endpoint monitored");
            return;
        }
     }

    /** 
     * Bean cleanup.
     */
    protected void afterRenderResponse() {
    }

    /**
     * Getter for property endpointsListItems.
     * @return Value of property endpointsListItems.
     */
    public ArrayList getEndpointsListItems() {
        return this.endpointsListItems;
    }

    /**
     * Setter for property endpointsListItems.
     * @param endpointsListItems New value of property endpointsListItems.
     */
    public void setEndpointsListItems(ArrayList endpointsListItems) {
        this.endpointsListItems = endpointsListItems;
    }

    /**
     * Getter for property endpointListSelection.
     * @return Value of property endpointListSelection.
     */
    public String getEndpointListSelection() {
        return this.endpointListSelection;
    }

    /**
     * Setter for property endpointListSelection.
     * @param endpointListSelection New value of property endpointListSelection.
     */
    public void setEndpointListSelection(String endpointListSelection) {
        this.endpointListSelection = endpointListSelection;
    }

    public String getWsdlURL() {
         return wsdlURL;
    }
    
    public void setWsdlURL(String wsdl) {
         wsdlURL = wsdl;
    }

    /**
     * Getter for property messageListItems.
     * @return Value of property messageListItems.
     */
    public ArrayList getMessageListItems() {
        return this.messageListItems;
    }

    /**
     * Setter for property messageListItems.
     * @param messageListItems New value of property messageListItems.
     */
    public void setMessageListItems(ArrayList messageListItems) {
        this.messageListItems = messageListItems;
    }

    /**
     * Getter for property messageListSelection.
     * @return Value of property messageListSelection.
     */
    public String getMessageListSelection() {
        return this.messageListSelection;
    }

    /**
     * Setter for property messageListSelection.
     * @param messageListSelection New value of property messageListSelection.
     */
    public void setMessageListSelection(String messageListSelection) {
        this.messageListSelection = messageListSelection;
    }

    /**
     * Getter for property endpointInfo.
     * @return Value of property endpointInfo.
     */
    public String getEndpointInfo() {
        return this.endpointInfo;
    }

    /**
     * Setter for property endpointInfo.
     * @param endpointInfo New value of property endpointInfo.
     */
    public void setEndpointInfo(String endpointInfo) {
        this.endpointInfo = endpointInfo;
    }

    /**
     * Getter for property displayEnv.
     * @return Value of property displayEnv.
     */
    public Boolean getDisplayEnv() {
        return this.displayEnv;
    }

    /**
     * Setter for property displayEnv.
     * @param displayEnv New value of property displayEnv.
     */
    public void setDisplayEnv(Boolean displayEnv) {
        this.displayEnv = displayEnv;
    }

    public void endpointAdded(Endpoint em) {
        refreshEndpointList();
    }

    public void endpointRemoved(Endpoint em) {
        refreshEndpointList();
    }
}
