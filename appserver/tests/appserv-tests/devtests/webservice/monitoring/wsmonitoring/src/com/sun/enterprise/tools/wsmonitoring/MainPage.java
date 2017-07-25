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
import javax.faces.component.html.*;
import com.sun.jsfcl.data.*;
import javax.faces.component.*;
import javax.faces.event.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.model.SelectItem;

import javax.servlet.http.HttpServletRequest;

import com.sun.enterprise.webservice.monitoring.WebServiceEngine;
import com.sun.enterprise.webservice.monitoring.WebServiceEngineFactory;
import com.sun.enterprise.webservice.monitoring.Endpoint;
import com.sun.enterprise.webservice.monitoring.MessageTrace;
import com.sun.enterprise.deployment.WebServiceEndpoint;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

public class MainPage extends AbstractPageBean {
    // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Definition">

    private int __placeholder;

    private HtmlForm form1 = new HtmlForm();

    public HtmlForm getForm1() {
        return form1;
    }

    public void setForm1(HtmlForm hf) {
        this.form1 = hf;
    }

    private HtmlOutputText mainTitle = new HtmlOutputText();

    public HtmlOutputText getMainTitle() {
        return mainTitle;
    }

    public void setMainTitle(HtmlOutputText hot) {
        this.mainTitle = hot;
    }

    private HtmlOutputText chooseTitle = new HtmlOutputText();

    public HtmlOutputText getChooseTitle() {
        return chooseTitle;
    }

    public void setChooseTitle(HtmlOutputText hot) {
        this.chooseTitle = hot;
    }
    
    private HtmlOutputText info = new HtmlOutputText();

    public HtmlOutputText getInfo() {
        return info;
    }

    public void setInfo(HtmlOutputText hot) {
        this.info = hot;
    }    

    private HtmlOutputText selectedEndpoint = new HtmlOutputText();

    public HtmlOutputText getSelectedEndpoint() {
        return selectedEndpoint;
    }

    public void setSelectedEndpoint(HtmlOutputText hot) {
        this.selectedEndpoint = hot;
    }

    private HtmlOutputLink hyperlink1 = new HtmlOutputLink();

    public HtmlOutputLink getHyperlink1() {
        return hyperlink1;
    }

    public void setHyperlink1(HtmlOutputLink hol) {
        this.hyperlink1 = hol;
    }

    private HtmlOutputText wsdlLink = new HtmlOutputText();

    public HtmlOutputText getWsdlLink() {
        return wsdlLink;
    }

    public void setWsdlLink(HtmlOutputText hot) {
        this.wsdlLink = hot;
    }

    private HtmlOutputText wsdlTitle = new HtmlOutputText();

    public HtmlOutputText getWsdlTitle() {
        return wsdlTitle;
    }

    public void setWsdlTitle(HtmlOutputText hot) {
        this.wsdlTitle = hot;
    }

    private HtmlCommandButton refreshButton = new HtmlCommandButton();

    public HtmlCommandButton getRefreshButton() {
        return refreshButton;
    }

    public void setRefreshButton(HtmlCommandButton hcb) {
        this.refreshButton = hcb;
    }

    private HtmlSelectOneListbox messageList = new HtmlSelectOneListbox();

    public HtmlSelectOneListbox getMessageList() {
        return messageList;
    }

    public void setMessageList(HtmlSelectOneListbox hsol) {
        this.messageList = hsol;
    }

    private UISelectItems messageListItems = new UISelectItems();

    public UISelectItems getMessageListItems() {
        return messageListItems;
    }

    public void setMessageListItems(UISelectItems uisi) {
        this.messageListItems = uisi;
    }

    private HtmlOutputText soapRequest = new HtmlOutputText();

    public HtmlOutputText getSoapRequest() {
        return soapRequest;
    }

    public void setSoapRequest(HtmlOutputText hot) {
        this.soapRequest = hot;
    }

    private HtmlOutputText soapResponse = new HtmlOutputText();

    public HtmlOutputText getSoapResponse() {
        return soapResponse;
    }

    public void setSoapResponse(HtmlOutputText hot) {
        this.soapResponse = hot;
    }

    private HtmlOutputText processingTime = new HtmlOutputText();

    public HtmlOutputText getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(HtmlOutputText hot) {
        this.processingTime = hot;
    }

    private HtmlSelectBooleanCheckbox displayEnvCheckbox = new HtmlSelectBooleanCheckbox();

    public HtmlSelectBooleanCheckbox getDisplayEnvCheckbox() {
        return displayEnvCheckbox;
    }

    public void setDisplayEnvCheckbox(HtmlSelectBooleanCheckbox hsbc) {
        this.displayEnvCheckbox = hsbc;
    }

    private HtmlOutputText displayEnvText = new HtmlOutputText();

    public HtmlOutputText getDisplayEnvText() {
        return displayEnvText;
    }

    public void setDisplayEnvText(HtmlOutputText hot) {
        this.displayEnvText = hot;
    }

    private HtmlSelectOneListbox endpointsList = new HtmlSelectOneListbox();

    public HtmlSelectOneListbox getEndpointsList() {
        return endpointsList;
    }

    public void setEndpointsList(HtmlSelectOneListbox hsol) {
        this.endpointsList = hsol;
    }

    private UISelectItems listbox1SelectItems = new UISelectItems();

    public UISelectItems getListbox1SelectItems() {
        return listbox1SelectItems;
    }

    public void setListbox1SelectItems(UISelectItems uisi) {
        this.listbox1SelectItems = uisi;
    }

    private HtmlCommandButton refreshEndpointList = new HtmlCommandButton();

    public HtmlCommandButton getRefreshEndpointList() {
        return refreshEndpointList;
    }

    public void setRefreshEndpointList(HtmlCommandButton hcb) {
        this.refreshEndpointList = hcb;
    }
    // </editor-fold>

    private EndpointMonitorRegistry monitors =null;
    private StringManager stringManager = StringManager.getManager("com.sun.enterprise.tools.wsmonitoring");
    private Logger logger = LogDomains.getLogger(LogDomains.TOOLS_LOGGER);
  
    
    // </editor-fold>
    public MainPage() {
        // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Initialization">
        try {
        } catch (Exception e) {
            logger.severe("Page1 Initialization Failure : " + e.getMessage());
            throw e instanceof javax.faces.FacesException ? (FacesException) e : new FacesException(e);
        }
        // </editor-fold>
        // Additional user provided initialization code
        logger.finest("In page1 init");
        this.form1.setTitle("Webservices Montoring");
        displayEnvCheckbox.setValue(java.lang.Boolean.TRUE);
        monitors = EndpointMonitorRegistry.getInstance();
    }

    protected SessionInfo getSessionBean1() {
        return (SessionInfo) getBean("SessionBean1");
    }

    /** 
     * Bean cleanup.
     */
    protected void afterRenderResponse() {
    }

    public void endpointsList_processValueChange(ValueChangeEvent vce) {
        // User event code here...
        java.lang.System.out.println("Received event " + vce.getOldValue() + " -> " + vce.getNewValue());
        refreshEndpoint((String) vce.getNewValue());
    }

    public void refreshButton_processAction(ActionEvent ae) {
        // User event code here...
        refreshEndpoint(getSessionBean1().getEndpointListSelection());
    }

    private void refreshEndpoint(String endpointSelected) {
        getSessionBean1().setEndpointListSelection((String) endpointSelected);
        refreshMessageList();
    }

    private void refreshMessageList() {

        SessionInfo session = getSessionBean1();

        EndpointMonitor em = monitors.getEndpointMonitor(session.getEndpointListSelection());
        if (em!=null) {
            WebServiceEndpoint endpointDescriptor = em.getEndpoint().getDescriptor();
            String endpoint = em.getEndpoint().getEndpointSelector();
            session.setEndpointInfo("WebService name : " + endpointDescriptor.getWebService().getName() +
                " and port : " + endpointDescriptor.getWsdlPort().getLocalPart());
            session.setWsdlURL(endpoint + "?WSDL");
            session.getMessageListItems().clear();
            MessageExchange[] traces = em.getInvocationTraces();
            if (traces!=null && traces.length!=0) {
                for (int i =0;i<traces.length;i++) {
                    session.getMessageListItems().add(new SelectItem("" +i, traces[i].getTimeStamp().toString()));
                }
                // select the fist item
                messageList.setValue("0");
                refreshMessagePanes(0);
            } else {
                soapRequest.setValue("");
                soapResponse.setValue("");                    
            }
        } else {
            cleanEndpointInfo(session);
        }
     }

    private void cleanEndpointInfo(SessionInfo session) {
        session.getMessageListItems().clear();
        session.setWsdlURL("");
        session.setEndpointInfo("");        
        soapRequest.setValue("");
        soapResponse.setValue("");        
    }
    
    public void messageList_processValueChange(ValueChangeEvent vce) {
        // User event code here...
        refreshMessagePanes((String) vce.getNewValue());

    }

    private void refreshMessagePanes(String newMessage) {

        int index =0;
        try {
            index = java.lang.Integer.parseInt(newMessage);
        } catch (Exception e) {
        }
        refreshMessagePanes(index);
    }

    private void refreshMessagePanes(int index) {

        java.lang.System.out.println("Refreshing message panes with " + index);
        SessionInfo session = getSessionBean1();
        
        EndpointMonitor em = monitors.getEndpointMonitor(getSessionBean1().getEndpointListSelection());;
        if (em==null) {
            cleanEndpointInfo(session);
            return;
        }
        
        MessageExchange[] traces = em.getInvocationTraces();
        if (traces==null || index>=traces.length) {
            cleanEndpointInfo(session);
            return;
        }

        boolean includeHeaders = session.getDisplayEnv().booleanValue();
        soapRequest.setValue(traces[index].request.getMessage(includeHeaders));
        soapResponse.setValue(traces[index].response.getMessage(includeHeaders));
    }


    public void displayEnvCheckbox_processValueChange(ValueChangeEvent vce) {

        Boolean newValue = (Boolean) vce.getNewValue();

        if (newValue!=getSessionBean1().getDisplayEnv()) {
            getSessionBean1().setDisplayEnv(newValue);
        }
        String messageValue = (String) messageList.getValue();
        java.lang.System.out.println("Message Value is " + messageValue);
        int index =0;
        try {
            index = java.lang.Integer.parseInt(messageValue);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        refreshMessagePanes(index);
    }

    public void refreshEndpointList_processAction(ActionEvent ae) {
        getSessionBean1().refreshEndpointList();
        java.lang.System.out.println("Refreshing the list with " + endpointsList.getValue());
        refreshEndpoint((String) endpointsList.getValue());

    }
}
