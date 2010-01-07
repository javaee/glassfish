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


package com.sun.javaee.blueprints.components.ui.textfield;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
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

import com.sun.faces.util.Util;


/**
 * <p>
 * Phase listener which handles two types of requests:
 *  <ol>
 *  <li> Responds to requests for the JavaScript file referenced by the rendered textfield component markup</li>
 *  <li> Responds to autocompletion requests</li>
 *  </ol>
 *
 * @author Tor Norbye
 * @author Ed Burns
 */
public class AjaxPhaseListener implements PhaseListener {
    /** Max number of results returned in a single completion request. */
    static final int MAX_RESULTS_RETURNED = 10;
    private static final String AJAX_VIEW_ID = "ajax-autocomplete";
    public static final String SCRIPT_VIEW_ID = "ajax-textfield.js";
    public static final String CSS_VIEW_ID = "ajax-textfield.css";

    public AjaxPhaseListener() {
    }

    public void afterPhase(PhaseEvent event) {
        String rootId = event.getFacesContext().getViewRoot().getViewId();

        if (rootId.endsWith(SCRIPT_VIEW_ID)) {
            handleResourceRequest(event, "/META-INF/textfield/script.js", "text/javascript");
        } else if (rootId.endsWith(CSS_VIEW_ID)) {
            handleResourceRequest(event, "/META-INF/textfield/styles.css", "text/css");
        } else if (rootId.indexOf(AJAX_VIEW_ID) != -1) {
            handleAjaxRequest(event);
        }
    }

    /** 
     * The URL is identified as an "ajax" request, e.g. an asynchronous request,
     * so we need to extract the arguments from the request, invoke the completion
     * method, and return the results in the form of an XML response that the
     * browser JavaScript can handle.
     */
    private void handleAjaxRequest(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        HttpServletResponse response =
            (HttpServletResponse)context.getExternalContext().getResponse();

        Object object = context.getExternalContext().getRequest();

        if (!(object instanceof HttpServletRequest)) {
            // PortletRequest? Handle that here?
            return;
        }

        HttpServletRequest request = (HttpServletRequest)object;
        String prefix = request.getParameter("prefix");
        String method = request.getParameter("method");
        StringBuffer sb = new StringBuffer();
        boolean namesAdded = false;

        try {
            CompletionResult results = getCompletionItems(context, method, prefix);
            List items = results.getItems();

            // Chop off results at a max -- in case client methods
            // do the wrong thing and generate tons of data. I ought
            // to make this configurable on the component...
            int n = Math.min(MAX_RESULTS_RETURNED, items.size());

            if (n > 0) {
                sb.append("<items>");

                Iterator it = items.iterator();

                while (it.hasNext()) {
                    sb.append("<item>");
                    sb.append(it.next().toString());
                    sb.append("</item>");
                }

                sb.append("</items>");

                response.setContentType("text/xml");
                response.setHeader("Cache-Control", "no-cache");
                response.getWriter().write(sb.toString());
            } else {
                //nothing to show
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }

            event.getFacesContext().responseComplete();

            return;
        } catch (EvaluationException ee) {
            // log(ee.toString());
            ee.printStackTrace();
        } catch (IOException ioe) {
            // log(ioe.toString());
            ioe.printStackTrace();
        }
    }

    private CompletionResult getCompletionItems(FacesContext context, String methodExpr,
        String prefix) {
        // Find the user/component-specified completion method and invoke it. That
        // method should populate the CompletionResult object we'return passing in to it.
        if (UIComponentTag.isValueReference(methodExpr)) {
            Class[] argTypes = { FacesContext.class, String.class, CompletionResult.class };
            MethodBinding vb = context.getApplication().createMethodBinding(methodExpr, argTypes);
            CompletionResult result = new CompletionResult();
            Object[] args = { context, prefix, result };

            vb.invoke(context, args);

            return result;
        } else {
            Object[] params = { methodExpr };
            throw new javax.faces.FacesException(Util.getExceptionMessageString(
                    Util.INVALID_EXPRESSION_ID, params));
        }
    }

    /**
     * The URL looks like a request for a resource, such as a JavaScript or CSS file. Write
     * the given resource to the response writer.
     */
    private void handleResourceRequest(PhaseEvent event, String resource, String contentType) {
        URL url = AjaxPhaseListener.class.getResource(resource);

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
