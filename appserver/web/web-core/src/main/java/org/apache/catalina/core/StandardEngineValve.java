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

package org.apache.catalina.core;


import org.apache.catalina.Host;
import org.apache.catalina.LogFacade;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.valves.ValveBase;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;


/**
 * Valve that implements the default basic behavior for the
 * <code>StandardEngine</code> container implementation.
 * <p>
 * <b>USAGE CONSTRAINT</b>:  This implementation is likely to be useful only
 * when processing HTTP requests.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:36 $
 */

final class StandardEngineValve
    extends ValveBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardEngineValve/1.0";

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Select the appropriate child Host to process this request,
     * based on the requested server name.  If no matching Host can
     * be found, return an appropriate HTTP error.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     * @param valveContext Valve context used to forward to the next Valve
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    @Override
    public int invoke(Request request, Response response)
            throws IOException, ServletException {

        Host host = preInvoke(request, response);
        if (host == null) {
            return END_PIPELINE;
        }

        if (host.getPipeline().hasNonBasicValves() ||
                host.hasCustomPipeline()) {
            // Invoke pipeline
            host.getPipeline().invoke(request, response);
        } else {
            // Invoke basic valve only
            host.getPipeline().getBasic().invoke(request, response);
        }

        return END_PIPELINE;
    }


    /**
     * Tomcat style invocation.
     */
    @Override
    public void invoke(org.apache.catalina.connector.Request request,
                       org.apache.catalina.connector.Response response)
            throws IOException, ServletException {

        Host host = preInvoke(request, response);
        if (host == null) {
            return;
        }

        if (host.getPipeline().hasNonBasicValves() ||
                host.hasCustomPipeline()) {
            // Invoke pipeline
            host.getPipeline().invoke(request, response);
        } else {
            // Invoke basic valve only
            host.getPipeline().getBasic().invoke(request, response);
        }
    }


    private Host preInvoke(Request request, Response response)
            throws IOException, ServletException {

        // Select the Host to be used for this Request
        Host host = request.getHost();
        if (host == null) {

            // BEGIN S1AS 4878272
            ((HttpServletResponse) response.getResponse()).sendError
                (HttpServletResponse.SC_BAD_REQUEST);
            String msg = MessageFormat.format(rb.getString(LogFacade.NO_HOST_MATCH), request.getRequest().getServerName());
            response.setDetailMessage(msg);
            // END S1AS 4878272
            return null;
        }

        return host;
    }
}
