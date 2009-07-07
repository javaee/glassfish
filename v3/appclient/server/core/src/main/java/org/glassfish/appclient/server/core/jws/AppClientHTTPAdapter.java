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

import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.grizzly.util.http.MimeType;
import com.sun.logging.LogDomains;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import org.glassfish.appclient.server.core.jws.servedcontent.StaticContent;

/**
 * GrizzlyAdapter for serving static and dynamic content.
 *
 * @author tjquinn
 */
public class AppClientHTTPAdapter extends RestrictedContentAdapter {

    private final static String LAST_MODIFIED_HEADER_NAME = "Last-Modified";
    private final static String DATE_HEADER_NAME = "Date";

    private Logger jwsLogger = LogDomains.getLogger(getClass(),
            LogDomains.ACC_LOGGER);

    private final Map<String,DynamicContent> dynamicContent;
    private final Properties tokens;

    public AppClientHTTPAdapter(
            final Map<String,StaticContent> staticContent,
            final Map<String,DynamicContent> dynamicContent,
            final Properties tokens) throws IOException {
        super(staticContent);
        this.dynamicContent = dynamicContent;
        this.tokens = tokens;
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
        if ( ! serviceContent(gReq, gResp) ) {
            final String uriString = gReq.getRequestURI();
            if (dynamicContent.containsKey(uriString)) {
                processDynamicContent(tokens, uriString, gReq, gResp);
            } else {
                respondNotFound(gResp);
            }
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
