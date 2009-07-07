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
import com.sun.logging.LogDomains;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import org.glassfish.appclient.server.core.jws.servedcontent.StaticContent;

/**
 *
 * @author tjquinn
 */
public class RestrictedContentAdapter extends GrizzlyAdapter {

    private final Logger logger = LogDomains.getLogger(
            RestrictedContentAdapter.class, LogDomains.ACC_LOGGER);

    private enum State {
        RESUMED,
        SUSPENDED
    }

    private volatile State state = State.RESUMED;


    private final Map<String,StaticContent> content;

    public RestrictedContentAdapter(final Map<String,StaticContent> content) throws IOException {
        this.content = content;
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
        for (Map.Entry<String,StaticContent> sc : content.entrySet()) {
            cache.put(sc.getKey(), sc.getValue().file());
        }

        /*
         * Turn off the default static resource handling.  We do our own from
         * our service method, rather than letting the StaticResourcesAdapter
         * have a try at each request first.
         */
        setHandleStaticResources(false);

        setUseSendFile(true);
        commitErrorResponse = true;
    }

    @Override
    public void service(GrizzlyRequest gReq, GrizzlyResponse gResp) {
        if ( ! serviceContent(gReq, gResp) ) {
            respondNotFound(gResp);
        }
    }

    protected boolean serviceContent(GrizzlyRequest gReq, GrizzlyResponse gResp) {

        /*
         * "Forbidden" seems like a more helpful response than "not found"
         * if the corresponding app client has been suspended.
         */
        if (state == State.SUSPENDED) {
            gResp.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
            return true;
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
        if (content.containsKey(uriString)) {
            processContent(uriString, gReq, gResp);
            return true;
        } else {
            return false;
        }
    }

    private void processContent(final String uriString,
            final GrizzlyRequest gReq, final GrizzlyResponse gResp) {
        try {
            super.service(uriString, gReq.getRequest(), gResp.getResponse());
            finishResponse(gResp, HttpServletResponse.SC_OK);
        } catch (Exception e) {
            gResp.getResponse().setErrorException(e);
            finishResponse(gResp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    
    public void suspend() {
        state = State.SUSPENDED;
    }

    public void resume() {
        state = State.RESUMED;
    }

    @Override
    public String toString() {
        return content.toString();
    }


    protected void finishResponse(final GrizzlyResponse gResp, final int status) {
        gResp.setStatus(status);
        try {
            gResp.finishResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void respondNotFound(final GrizzlyResponse gResp) {
        finishErrorResponse(gResp, HttpServletResponse.SC_NOT_FOUND);
    }

    protected void finishErrorResponse(final GrizzlyResponse gResp, final int status) {
        gResp.setStatus(status);
        try {
            if (commitErrorResponse) {
                customizedErrorPage(gResp.getRequest().getRequest(), gResp.getResponse());
            }
            gResp.finishResponse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
