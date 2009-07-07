/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.appclient.server.core.jws;

import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.grizzly.util.http.MimeType;
import com.sun.logging.LogDomains;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;

/**
 * GrizzlyAdapter for serving static and dynamic content.
 *
 * @author tjquinn
 */
public class AppClientHTTPAdapter extends GrizzlyAdapter {

    private final static String LAST_MODIFIED_HEADER_NAME = "Last-Modified";
    private final static String DATE_HEADER_NAME = "Date";

    private enum State {
        RESUMED,
        SUSPENDED
    }

    private volatile State state = State.RESUMED;

    private Logger jwsLogger = LogDomains.getLogger(getClass(),
            LogDomains.ACC_LOGGER);

    private final Map<String,File> staticContent;
    private final Map<String,DynamicContent> dynamicContent;
    private final Properties tokens;

    public AppClientHTTPAdapter(
            final Map<String,File> staticContent,
            final Map<String,DynamicContent> dynamicContent,
            final Properties tokens) {
        this.staticContent = staticContent;
        this.dynamicContent = dynamicContent;
        this.tokens = tokens;

        /*
         * Preload the adapter's cache with the static content.  This helps
         * performance but is essential to the operation of the adapter.
         * Normally the Grizzly logic will qualify the URI in the request
         * with the root folder of the adapter.  But the files this
         * adapter needs to serve can come from a variety of places that
         * might not share any common parent directory for the root folder.
         * Preloading the cache with the known static content lets the Grizzly
         * logic serve the files we want, from whereever they are.
         */
        cache.putAll(staticContent);

        /*
         * Turn off the default static resource handling.  We do our own from
         * our service method, rather than letting the StaticResourcesAdapter
         * have a try at each request first.
         */
        setHandleStaticResources(false);

        setUseSendFile(true);
        commitErrorResponse = true;
    }

    /**
     * Responds to all requests routed to the context root with which this
     * adapter was registered to the RequestDispatcher.
     *
     * @param gReq
     * @param gResp
     */
    @Override
    public void service(GrizzlyRequest gReq, GrizzlyResponse gResp) {
        /*
         * "Forbidden" seems like a more helpful response than "not found"
         * if the corresponding app client has been suspended.
         */
        if (state == State.SUSPENDED) {
            gResp.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String uriString = gReq.getRequestURI();
        /*
         * The Grizzly-managed cache could contain entries for non-existent
         * files that users request.  If the URI indicates it's a request for
         * static content make sure the requested URI is in the predefined staticContent
         * before having Grizzly serve it.
         *
         * Alternatively, if the URI indicates the request is for dynamic content
         * then handle that separately.
         *
         * If the request is for a URI in neither the static nor dynamic
         * content this adapter should serve, then just return a 404.
         */
        if (staticContent.containsKey(uriString)) {
            processStaticContent(uriString, gReq, gResp);
        } else if (dynamicContent.containsKey(uriString)) {
            processDynamicContent(tokens, uriString, gReq, gResp);
        } else {
            gResp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void processStaticContent(final String uriString,
            final GrizzlyRequest gReq, final GrizzlyResponse gResp) {
        try {
            super.service(uriString, gReq.getRequest(), gResp.getResponse());
            return;
        } catch (Exception e) {
            gResp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            gResp.getResponse().setErrorException(e);
            return;
        }
    }

    private void processDynamicContent(final Properties tokens,
            final String uriString,
            final GrizzlyRequest gReq, final GrizzlyResponse gResp) {
        final DynamicContent dc = dynamicContent.get(uriString);
        final String rawContent = dc.content();

        if (rawContent == null) {
            gResp.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Date instanceTimestamp = dc.timestamp();
        gResp.setDateHeader(LAST_MODIFIED_HEADER_NAME, instanceTimestamp.getTime());
        gResp.setDateHeader(DATE_HEADER_NAME, System.currentTimeMillis());
        gResp.setContentType(mimeType(uriString));
        gResp.setStatus(HttpServletResponse.SC_OK);

        /*
         *Only for GET should the response actually contain the content.
         */
        final String methodType = gReq.getMethod();
        if (methodType.equalsIgnoreCase("GET")) {
            final String processedContent = Util.replaceTokens(rawContent, tokens);
            writeData(uriString, processedContent.toString(),
                    gReq, gResp);
        }
        try {
            gResp.finishResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void suspend() {
        state = State.SUSPENDED;
    }

    public void resume() {
        state = State.RESUMED;
    }

    /**
     * Some stolen from Grizzly's StaticResourcesAdapter -- maybe it'll get
     * refactored out later?
     *
     * @param resource
     * @param req
     * @param res
     */
    private void writeData(final String uri, final String data,
            final GrizzlyRequest req, final GrizzlyResponse res) {
        try {
            res.setStatus(200);


            res.setContentLength(data.length());
            res.getResponse().sendHeaders();

            PrintWriter pw = res.getWriter();
            pw.println(data);
            pw.flush();
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getResponse().setErrorException(e);
            return;
        }
    }

    /**
     * Stolen from Grizzly.
     * 
     * @param uri
     * @return
     */
    private String mimeType(final String uri) {
        int dot=uri.lastIndexOf(".");
        if( dot > 0 ) {
            String ext=uri.substring(dot+1);
            String ct= MimeType.get(ext);
            if( ct!=null) {
                return ct;
            }
        } else {
            return MimeType.get("html");
        }
        return "";
    }

    public static class DynamicContent {
        private final String content;
        private final Date timestamp;

        public DynamicContent(final String content, final Date timestamp) {
            this.content = content;
            this.timestamp = timestamp;
        }

        public String content() {
            return content;
        }

        public Date timestamp() {
            return timestamp;
        }
    }
}
