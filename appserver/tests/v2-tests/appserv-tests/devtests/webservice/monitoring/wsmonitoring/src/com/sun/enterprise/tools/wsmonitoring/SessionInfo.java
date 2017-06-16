/*
 * SessionBean1.java
 *
 * Created on November 17, 2004, 11:03 AM
 * Copyright dochez
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
