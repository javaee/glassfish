/*
 * SimpleFilter.java
 *
 * =================================================================================================
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * =================================================================================================
 *
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
