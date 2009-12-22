/*
 * PopupPhaseListener.java
 *
 * Created on October 17, 2005, 3:44 PM
 */
package com.sun.javaee.blueprints.components.ui.popup;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.webapp.UIComponentTag;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * <p>
 * Phase listener that responds to requests for the JavaScript files, 
 * images and text templates
 *
 * @author Mark Basler
 */
public class PopupPhaseListener implements PhaseListener {
    
    public static final String PATH_PREFIX = "/META-INF/popup";
    public static final String SCRIPT_SUFFIX = ".js";
    public static final String CSS_SUFFIX = ".css";
    public static final String GIF_SUFFIX = ".gif";
    public static final String JPG_SUFFIX = ".jpg";
    public static final String PNG_SUFFIX = ".png";
    public static boolean bDebug=false;
    
    public PopupPhaseListener() {
    }

    /*
     * After phase listener that serves resources
     */
    public void afterPhase(PhaseEvent event) {
        String rootId = event.getFacesContext().getViewRoot().getViewId();

        if(bDebug) System.out.println("PhaseListener - Root ID " + rootId);
        
        // see what suffix is used for mapping to content type
        if (rootId.endsWith(SCRIPT_SUFFIX)) {
            handleResourceRequest(event, PATH_PREFIX + rootId, "text/javascript");
        } else if (rootId.endsWith(CSS_SUFFIX)) {
            handleResourceRequest(event, PATH_PREFIX + rootId, "text/css");
        } else if (rootId.endsWith(GIF_SUFFIX)) {
            handleResourceRequest(event, PATH_PREFIX + rootId, "image/gif");
        } else if (rootId.endsWith(JPG_SUFFIX)) {
            handleResourceRequest(event, PATH_PREFIX + rootId, "image/jpeg");
        } else if (rootId.endsWith(PNG_SUFFIX)) {
            handleResourceRequest(event, PATH_PREFIX + rootId, "image/x-png");
        }
    }

    
    /**
     * The URL looks like a request for a resource, such as a JavaScript or CSS file. Write
     * the given resource to the response writer.
     */
    private void handleResourceRequest(PhaseEvent event, String resource, String contentType) {
        
        // get full qualified path of class
        //String sxURL=getClass().getResource("PopupPhaseListener.class").toString();
        URL sxURL = PopupPhaseListener.class.getResource(resource);

        HttpServletResponse response=(HttpServletResponse)event.getFacesContext().getExternalContext().getResponse();
        OutputStream outStream=null;
        try {
            response.setContentType(contentType);
            response.setStatus(200);
            outStream=response.getOutputStream();
            // use util to write binary to output stream
            PopupUtil.readWriteBinaryUtil(sxURL, outStream);
        } catch (Exception e) {
            String message = "Can't find resource \"" + resource + "\" in the same directory as the class - " + getClass().getResource("PopupPhaseListener.class");
            System.out.println(message);
            e.printStackTrace();
        } finally {
            try {
                outStream.flush();
                outStream.close();
            } catch (Exception ee) {}
            event.getFacesContext().responseComplete();
        }
    }

    
    
    public void beforePhase(PhaseEvent event) {
    }

    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }
}
