/*
 * AjaxValidatorPhaseListener.java
 *
 * Created on June 6, 2005, 12:17 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.javaee.blueprints.components.ui.components;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.faces.FactoryFinder;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>Handles the server side validation and conversion processing for
 * the AjaxValidator component.  Sends the validation message down to
 * the client in XML that is understood by the client javascript.</p>
 *
 * @author edburns
 */
public class AjaxValidatorPhaseListener implements PhaseListener {
    
    
    
    /** Creates a new instance of AjaxValidatorPhaseListener */
    public AjaxValidatorPhaseListener() {
    }
    
    public PhaseId getPhaseId() {
        return PhaseId.PROCESS_VALIDATIONS;
    }

    public ResponseWriter getResponseWriter(FacesContext context) throws IOException {
        ResponseWriter curWriter = context.getResponseWriter();
        if (null == curWriter) {
            HttpServletRequest request = (HttpServletRequest)
                context.getExternalContext().getRequest();
            HttpServletResponse response = (HttpServletResponse)
                context.getExternalContext().getResponse();
            
            RenderKitFactory rkf = (RenderKitFactory)
                FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
            RenderKit renderKit = rkf.getRenderKit(context,
                    context.getViewRoot().getRenderKitId());
            
            curWriter = renderKit.createResponseWriter(response.getWriter(),
                    "text/html", request.getCharacterEncoding());
            
            context.setResponseWriter(curWriter);
        }
        return curWriter;
    }    
    
    /**
     * <p>Go through each component in the AJAX_VALIDATOR_COMPONENTS
     * list in request scope.
     */
    public void afterPhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        ExternalContext extContext = context.getExternalContext();
        
        List ajaxValidatorComponents = (List)
            extContext.getRequestMap().get(AJAX_VALIDATOR_COMPONENTS);
        
        // Don't take action for non AJAX requests
        if (null == ajaxValidatorComponents) {
            return;
        }
        
        Iterator messageIter = null, 
                components = ajaxValidatorComponents.iterator();
        UIComponent child = null;
        AjaxValidatorComponent curComponent = null;
        FacesMessage curMessage = null;
        String summary = null, detail = null, clientId = null, messageId = null;
        StringBuffer messageString = new StringBuffer();
        
        // Don't take action unless we have at least
        // one AjaxValidatorComponent
        if (!components.hasNext()) {
            return;
        }
        context.responseComplete();
        
        HttpServletResponse response = (HttpServletResponse)
        extContext.getResponse();
        response.setContentType("text/xml");
        response.setHeader("Cache-control", "no-cache");
        ResponseWriter writer = null;
        try {
            writer = this.getResponseWriter(context);
        }
        catch (IOException e) {
            // log error
            return;
        }
        
        while (components.hasNext()) {
            curComponent = (AjaxValidatorComponent) components.next();
            child = (UIComponent) curComponent.getChildren().get(0);
            assert(null != child);
            
            // get the messageId to send to the client.
            messageId = (String) curComponent.getMessageId();
            assert(null != messageId);

            clientId = child.getClientId(context);

            // get any validation messages for the component
            // we're trying to ajaxvalidate
            messageIter = context.getMessages(clientId);
            curMessage = null;
            messageString.delete(0, messageString.length());
        
            // Just stuff them into a StringBuffer.  Applying rendering
            // attributes can be done as an enhancement.
            while (messageIter.hasNext()) {
                curMessage = (FacesMessage) messageIter.next();
                if (0 < messageString.length()) {
                    messageString.append(" ");
                }
                summary = curMessage.getSummary();
                detail = curMessage.getDetail();
                if (null == summary) {
                    if (null != detail) {
                        messageString.append(detail);
                    }
                } else {
                    messageString.append(summary);
                }
            }
            
            assert(null != messageString);
            if (0 < messageString.length()) {
                
                try {
                    writer.startElement("message", curComponent);
                    writer.startElement("validationMessage", curComponent);
                    writer.writeText(messageString.toString(), null);
                    writer.endElement("validationMessage");
                    writer.startElement("clientId", curComponent);
                    writer.writeText(clientId, null);
                    writer.endElement("clientId");
                    writer.startElement("messageId", curComponent);
                    writer.writeText(messageId, null);
                    writer.endElement("messageId");
                    writer.endElement("message");
                    
                } catch (IOException e) {
                    // log message.
                }
            }
            else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
        ajaxValidatorComponents.clear();
    }
    
    public void beforePhase(PhaseEvent event) { }

    static final String AJAX_VALIDATOR_COMPONENTS = "vtrainer.AJAX_VALIDATOR_COMPONENTS";
    
    
}
