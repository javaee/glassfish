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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import org.glassfish.appclient.server.core.jws.servedcontent.DynamicContent;
import org.glassfish.appclient.server.core.jws.servedcontent.StaticContent;

/**
 * GrizzlyAdapter for serving static and dynamic content.
 *
 * @author tjquinn
 */
public class AppClientHTTPAdapter extends RestrictedContentAdapter {

    private final static String LAST_MODIFIED_HEADER_NAME = "Last-Modified";
    private final static String DATE_HEADER_NAME = "Date";

    private static final String ARG_QUERY_PARAM_NAME = "arg";
    private static final String PROP_QUERY_PARAM_NAME = "prop";
    private static final String VMARG_QUERY_PARAM_NAME = "vmarg";
    private final String lineSep = System.getProperty("line.separator");

    private final Logger logger = LogDomains.getLogger(getClass(),
            LogDomains.ACC_LOGGER);

    private final Map<String,DynamicContent> dynamicContent;
    private final Properties tokens;

    public AppClientHTTPAdapter(
            final String contextRoot,
            final Map<String,StaticContent> staticContent,
            final Map<String,DynamicContent> dynamicContent,
            final Properties tokens) throws IOException {
        super(contextRoot, staticContent);
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
        final String relativeURIString =
                relativizeURIString(gReq.getRequestURI());
        if (relativeURIString == null) {
            respondNotFound(gResp);
        } else if (dynamicContent.containsKey(relativeURIString)) {
            processDynamicContent(tokens, relativeURIString, gReq, gResp);
        } else if ( ! serviceContent(gReq, gResp) ) {
            respondNotFound(gResp);
        }
    }

    public void addContentIfAbsent(final Map<String,StaticContent> staticAdditions,
            final Map<String,DynamicContent> dynamicAdditions) throws IOException {
        addContentIfAbsent(staticAdditions);
        addDynamicContentIfAbsent(dynamicAdditions);
    }

    private void addDynamicContentIfAbsent(final Map<String,DynamicContent> additions) {
        for (Map.Entry<String,DynamicContent> entry : additions.entrySet()) {
            addContentIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    private void addContentIfAbsent(final String relativeURIString, final DynamicContent addition) {
        if ( ! dynamicContent.containsKey(relativeURIString)) {
            dynamicContent.put(relativeURIString, addition);
        }
    }

    private void processDynamicContent(final Properties tokens,
            final String relativeURIString,
            final GrizzlyRequest gReq, final GrizzlyResponse gResp) {
        final DynamicContent dc = dynamicContent.get(relativeURIString);
        if (dc == null) {
            respondNotFound(gResp);
            return;
        }
        if ( ! dc.isAvailable()) {
            finishErrorResponse(gResp, contentStateToResponseStatus(dc));
            return;
        }
        final Properties allTokens = prepareRequestPlaceholders(tokens, gReq);

        final DynamicContent.Instance instance = dc.getOrCreateInstance(allTokens);

        final Date instanceTimestamp = instance.getTimestamp();
        gResp.setDateHeader(LAST_MODIFIED_HEADER_NAME, instanceTimestamp.getTime());
        gResp.setDateHeader(DATE_HEADER_NAME, System.currentTimeMillis());
        gResp.setContentType(mimeType(relativeURIString));
        gResp.setStatus(HttpServletResponse.SC_OK);

        /*
         *Only for GET should the response actually contain the content.
         */
        final String methodType = gReq.getMethod();
        if (methodType.equalsIgnoreCase("GET")) {
            writeData(instance.getText(), gResp);
        }
        finishResponse(gResp, HttpServletResponse.SC_OK);
    }

    /**
     * Initializes a Properties object with the token names and values for
     * substitution in the dynamic content template.
     *
     * @param the incoming request
     * @return Properties object containing the token names and values
     * @throws ServletException in case of an error preparing the placeholders
     */
    private Properties prepareRequestPlaceholders(
            final Properties adapterTokens,
            GrizzlyRequest request) {
        final Properties answer = new Properties(adapterTokens);

        answer.setProperty("request.scheme", request.getScheme());
        answer.setProperty("request.host", request.getServerName());
        answer.setProperty("request.port", Integer.toString(request.getServerPort()));

        /*
         *Treat query parameters with the name "arg" as command line arguments to the
         *app client.
         */

        final String queryString = request.getQueryString();
        final StringBuilder queryStringPropValue = new StringBuilder();
        if (queryString != null && queryString.length() > 0) {
            queryStringPropValue.append("?").append(queryString);
        }
        answer.setProperty("request.web.app.context.root",
                NamingConventions.JWSAPPCLIENT_SYSTEM_PREFIX);
        answer.setProperty("request.path", getPathInfo(request));
        answer.setProperty("request.query.string", queryStringPropValue.toString());

        processQueryParameters(queryString, answer);

        return answer;
    }

    private String getPathInfo(final GrizzlyRequest gReq) {
        return gReq.getRequestURI();
    }

    private void processQueryParameters(String queryString, final Properties answer) {
        if (queryString == null) {
            queryString = "";
        }
        String [] queryParams = null;
        try {
            queryParams = URLDecoder.decode(queryString, "UTF-8").split("&");
        } catch (UnsupportedEncodingException e) {
            // This should never happen.  We'd better know about UTF-8!
            throw new RuntimeException(e);
        }

        QueryParams arguments = new ArgQueryParams();
        QueryParams properties = new PropQueryParams();
        QueryParams vmArguments = new VMArgQueryParams();
        QueryParams [] paramTypes = new QueryParams[] {arguments, properties, vmArguments};

        for (String param : queryParams) {
            for (QueryParams qpType : paramTypes) {
                if (qpType.processParameter(param)) {
                    break;
                }
            }
        }

        answer.setProperty("request.arguments", arguments.toString());
        answer.setProperty("request.properties", properties.toString());
        answer.setProperty("request.vmargs", vmArguments.toString());
    }

    /**
     * Some stolen from Grizzly's StaticResourcesAdapter -- maybe it'll get
     * refactored out later?
     *
     * @param resource
     * @param req
     * @param res
     */
    private void writeData(final String data,
            final GrizzlyResponse res) {
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
        final int dot=uri.lastIndexOf(".");
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

//    public static class DynamicContent {
//        private final String content;
//        private final Date timestamp;
//
//        public DynamicContent(final String content, final Date timestamp) {
//            this.content = content;
//            this.timestamp = timestamp;
//        }
//
//        public String content() {
//            return content;
//        }
//
//        public Date timestamp() {
//            return timestamp;
//        }
//    }
    private abstract class QueryParams {
        private String prefix;

        protected QueryParams(String prefix) {
            this.prefix = prefix;
        }

        private boolean handles(String prefix) {
            return prefix.equals(this.prefix);
        }

        protected abstract void processValue(String value);

        public abstract String toString();

        public boolean processParameter(String param) {
            boolean result = false;
            final int equalsSign = param.indexOf("=");
            String value = "";
            String prefix;
            if (equalsSign != -1) {
                prefix = param.substring(0, equalsSign);
            } else {
                prefix = param;
            }
            if (handles(prefix)) {
                result = true;
                if ((equalsSign + 1) < param.length()) {
                    value = param.substring(equalsSign + 1);
                }
                processValue(value);
            }
            return result;
        }
    }

    private class ArgQueryParams extends QueryParams {
        private StringBuilder arguments = new StringBuilder();

        public ArgQueryParams() {
            super(ARG_QUERY_PARAM_NAME);
        }

        public void processValue(String value) {
            if (value.length() == 0) {
                value = "#missing#";
            }
            arguments.append("<argument>").append(value).append("</argument>").append(lineSep);
        }

        public String toString() {
            return arguments.toString();
        }
    }

    private class PropQueryParams extends QueryParams {
        private StringBuilder properties = new StringBuilder();

        public PropQueryParams() {
            super(PROP_QUERY_PARAM_NAME);
        }

        public void processValue(String value) {
            if (value.length() > 0) {
                final int equalsSign = value.indexOf('=');
                String propValue = "";
                String propName;
                if (equalsSign > 0) {
                    propName = value.substring(0, equalsSign);
                    if ((equalsSign + 1) < value.length()) {
                        propValue = value.substring(equalsSign + 1);
                    }
                    properties.append("<property name=\"" + propName + "\" value=\"" + propValue + "\"/>").append(lineSep);
                }
            }
        }

        public String toString() {
            return properties.toString();
        }

    }

    private class VMArgQueryParams extends QueryParams {
        private StringBuilder vmArgs = new StringBuilder();

        public VMArgQueryParams() {
            super(VMARG_QUERY_PARAM_NAME);
        }

        public void processValue(String value) {
            vmArgs.append(value).append(" ");
        }

        public String toString() {
            return vmArgs.length() > 0 ? " java-vm=args=\"" + vmArgs.toString() + "\"" : "";
        }
    }

}
