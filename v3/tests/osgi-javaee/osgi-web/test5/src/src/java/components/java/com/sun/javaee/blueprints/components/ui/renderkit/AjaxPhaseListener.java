/*
 * Copyright 2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
/*
 * AjaxPhaseListener.java
 *
 * Created on May 11, 2005, 11:04 AM
 */

package com.sun.javaee.blueprints.components.ui.renderkit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletResponse;


/**
 *
 * @author edburns
 */
public class AjaxPhaseListener implements PhaseListener {
    
    private static final String SCRIPT_VIEW_ID = "ajax-script";
    private static final String SCRIPT_RESOURCE_NAME = "/META-INF/ajax.js";
    
    public AjaxPhaseListener() {
    }
    
    public static ClassLoader getCurrentLoader(Object fallbackClass) {
        ClassLoader loader =
            Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = fallbackClass.getClass().getClassLoader();
        }
        return loader;
    }
    
    public void afterPhase(PhaseEvent event) {
        // If this is restoreView phase and the viewId is the script view id
        if (-1 != event.getFacesContext().getViewRoot().getViewId().indexOf(SCRIPT_VIEW_ID) &&
            PhaseId.RESTORE_VIEW == event.getPhaseId()) {
            // render the script
            renderScript(event);
        }
        else {
            // see if this is an abortPhase request
            String abortPhaseParam = (String)
                    event.getFacesContext().getExternalContext().
                      getRequestParameterMap().get("bpcatalog.abortPhase");
            int phaseOrdinal = -1;
            if (null != abortPhaseParam) {
                try {
                    phaseOrdinal = Integer.valueOf(abortPhaseParam).intValue();
                }
                catch (NumberFormatException e) {
                    // log error
                }
                if (phaseOrdinal == event.getPhaseId().getOrdinal()) {
                    event.getFacesContext().responseComplete();
                }
            }
            
        }
        
    }


    public void beforePhase(PhaseEvent event) {
    }

    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }
    
    private void renderScript(PhaseEvent event) {
        URL url = AjaxPhaseListener.class.getResource(SCRIPT_RESOURCE_NAME);
        URLConnection conn = null;
        InputStream stream = null;
        BufferedReader bufReader = null;
        HttpServletResponse response = (HttpServletResponse)event.getFacesContext().getExternalContext().getResponse();
        OutputStreamWriter outWriter = null;
        String curLine = null;

        try {
            outWriter = new OutputStreamWriter(response.getOutputStream(),
                                               response.getCharacterEncoding());
            conn = url.openConnection();
            conn.setUseCaches(false);
            stream = conn.getInputStream();
            bufReader = new BufferedReader(new InputStreamReader(stream));
            response.setContentType("text/javascript");
            response.setStatus(200);
            while (null != (curLine = bufReader.readLine())) {
                outWriter.write(curLine+"\n");
            }
            outWriter.flush();
            outWriter.close();
            event.getFacesContext().responseComplete();
            
        } catch (Exception e) {
            String message = null;
                message = "Can't load script file:" +
                    url.toExternalForm();
        }
        
    }
    
}
