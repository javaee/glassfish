/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package samples.i18n.simple.servlet;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Simple filter to set the character encoding to be used to parse the request.
 * The character encoding we use here is defined as initialization parameter
 */

public class SimpleFilter implements Filter {

    private FilterConfig filterConfig	= null;
    private boolean usefilter			= false;
    private String encoding				= null;

    /**
     * Called by the web container to indicate to a filter that it is being placed into service
     *
     * @param filterConfig The filter configuration object
     */
    public void init(FilterConfig filterConfig) throws ServletException {
		try {
			this.filterConfig	=	filterConfig;
			String param		=	filterConfig.getInitParameter("usefilter");
			this.encoding		=	filterConfig.getInitParameter("encoding");
			if (param.equals("true")) {
				this.usefilter	=	true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * The doFilter method of the Filter is called by the container each time a request/response pair
     * is passed through the chain due to a client request for a resource at the end of the chain.
     * @param request The servlet request upon which the filter is acting
     * @param result The servlet response that we are creating from the filter
     * @param chain The filter chain we are processing
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
	throws IOException, ServletException {
		try {
        	if (usefilter) {
        	    String encoding = getEncoding(req);
        	    if (encoding != null) {
        	        req.setCharacterEncoding(encoding);
				}
			}
        	chain.doFilter(req, res);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * Gets the encoding from initialization parameter and sets it to this filter
     *
     * @param request The servlet request
     */
    protected String getEncoding(ServletRequest req) {
        return this.encoding;
    }

    /**
     * Called by the web container to indicate to a filter that it is being taken out of service.
     */
    public void destroy() {
        this.encoding		= null;
        this.filterConfig	= null;
    }
}
