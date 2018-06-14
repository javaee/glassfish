/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.valves;

import org.apache.catalina.Request;
import org.apache.catalina.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Valve that attempts to force MS WebDAV clients connecting on port 80 to use
 * a WebDAV client that actually works. Other workarounds that might help
 * include:
 * <ul>
 *   <li>Specifing the port, even if it is port 80, when trying to connect.</li>
 *   <li>Canceling the first authentication dialog box and then trying to
 *       reconnect.</li>
 * </ul>
 * To use this valve add the following <code>&lt;Valve
 * className="org.apache.catalina.valves.WebdavFixValve" /&gt;</code>
 * to your <code>Engine</code>, <code>Host</code> or <code>Context</code> as
 * required. Normally, this valve would be used at the <code>Context</code>
 * level.
 *
 * @version $Revision: 420067 $, $Date: 2006-07-08 09:16:58 +0200 (sub, 08 srp 2006) $
 */

public class WebdavFixValve
    extends ValveBase {

    /**
     * Check for the broken MS WebDAV client and if detected issue a re-direct
     * that hopefully will cause the non-broken client to be used.
     */
    public int invoke(Request request, Response response)
        throws IOException, ServletException {

        HttpServletRequest hreq = (HttpServletRequest)request.getRequest();
        HttpServletResponse hres = (HttpServletResponse)response.getResponse();
        String ua = hreq.getHeader("User-Agent");
        if (ua != null && ua.contains("MiniRedir")) {
            hres.sendRedirect(buildRedirect(hreq));
            return END_PIPELINE;
        } else {
            return INVOKE_NEXT;
        }
    }

    private String buildRedirect(HttpServletRequest request) {
        StringBuilder location =
            new StringBuilder(request.getRequestURL().length());
        location.append(request.getScheme());
        location.append("://");
        location.append(request.getServerName());
        location.append(':');
        // If we include the port, even if it is 80, then MS clients will use
        // a WebDAV client that works rather than the MiniRedir that has
        // problems with BASIC authentication
        location.append(request.getServerPort());
        location.append(request.getRequestURI());
        return location.toString();
    }
}
