/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
