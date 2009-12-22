/* Copyright 2005 Sun Microsystems, Inc. All rights reserved.
   You may not modify, use, reproduce, or distribute this software except in
   compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
   $Id: SlideshowPhaseListener.java,v 1.1 2005/11/19 00:53:50 inder Exp $  */

package com.sun.javaee.blueprints.components.ui.slider_navigator;

import java.io.*;
import java.net.*;
import javax.faces.event.*;
import javax.servlet.http.*;
import javax.servlet.*;
import com.sun.faces.util.Util;

/**
 * Phase listener which responds to requests for the script tag
 * 
 */
public class SlideshowPhaseListener implements PhaseListener {
    
    public static final String PATH_PREFIX = "/META-INF/slider-navigator/";
    public static final String SCRIPT_VIEW_ID = "ajax-slideshow-script.js";
    public static final String AJAX_SCRIPT_VIEW_ID = "ajax-common-script.js";
    public static final String XSL_SCRIPT_VIEW_ID = "xsl-script.js";
    public static final String CAT_XSL_VIEW_ID = "cats.xsl";
    public static final String CSS_VIEW_ID = "ajax-slideshow.css";
    public static final String GIF_SUFFIX = ".gif";
    public static final String JPG_SUFFIX = ".jpg";
    public static final String PNG_SUFFIX = ".png";

    public SlideshowPhaseListener() {
    }
    
    
    public void afterPhase(PhaseEvent event) {
        String rootId = event.getFacesContext().getViewRoot().getViewId();
        String realPath = null;
        
        if (rootId.endsWith(SCRIPT_VIEW_ID)) {
            System.out.println("IN AFTERPHASE - SLIDESHOW.JS");
            handleResourceRequest(event, PATH_PREFIX + "slideshow.js", "text/javascript");
        } else if (rootId.endsWith(XSL_SCRIPT_VIEW_ID)) {
            handleResourceRequest(event, PATH_PREFIX + "xslt.js", "text/javascript");
        } else if (rootId.endsWith(AJAX_SCRIPT_VIEW_ID)) {
            handleResourceRequest(event, PATH_PREFIX + "xmlhttprequest.js", "text/javascript");
        } else if (rootId.endsWith(CAT_XSL_VIEW_ID)) {
            handleResourceRequest(event, PATH_PREFIX + "cats.xsl", "application/xml;charset=UTF-8");
        } else if (rootId.endsWith(CSS_VIEW_ID)) {
            handleResourceRequest(event, "styles.css", "text/css");
        } else if (rootId.endsWith(GIF_SUFFIX)) {
            //realPath = "../" + rootId;
            handleResourceRequest(event, realPath, "image/gif");
        } else if (rootId.endsWith(JPG_SUFFIX)) {
            //realPath = "../" + rootId;
            handleResourceRequest(event, realPath, "image/jpeg");
        } else if (rootId.endsWith(PNG_SUFFIX)) {
            //realPath = "../" + rootId;
            handleResourceRequest(event, realPath, "image/x-png");
        }
    }
     
    /**
     * The URL looks like a request for a resource, such as a JavaScript or CSS file. Write
     * the given resource to the response writer.
     * @param event 
     * @param resource 
     * @param contentType 
     */
    private void handleResourceRequest(PhaseEvent event, String resource, String contentType) {
    
        URL url = SlideshowPhaseListener.class.getResource(resource);
        URLConnection conn = null;
        InputStream stream = null;
        BufferedReader bufReader = null;
        HttpServletResponse response =
            (HttpServletResponse)event.getFacesContext().getExternalContext().getResponse();
        OutputStreamWriter outWriter = null;
        String curLine = null;

        try {
            outWriter =
                new OutputStreamWriter(response.getOutputStream(), response.getCharacterEncoding());
            conn = url.openConnection();
            conn.setUseCaches(false);
            stream = conn.getInputStream();
            bufReader = new BufferedReader(new InputStreamReader(stream));
            response.setContentType(contentType);
            response.setStatus(200);

            while (null != (curLine = bufReader.readLine())) {
                outWriter.write(curLine + "\n");
            }

            outWriter.flush();
            outWriter.close();
            event.getFacesContext().responseComplete();
        } catch (Exception e) {
            String message = "Can't load resource:" + url.toExternalForm();
            System.err.println(message);
            e.printStackTrace();
        }
    }

    public void beforePhase(PhaseEvent event) {
    }

    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }
    
}
