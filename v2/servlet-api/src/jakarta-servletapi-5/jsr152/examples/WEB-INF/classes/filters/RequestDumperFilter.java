

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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


package filters;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.Locale;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;


/**
 * Example filter that dumps interesting state information about a request
 * to the associated servlet context log file, before allowing the servlet
 * to process the request in the usual way.  This can be installed as needed
 * to assist in debugging problems.
 *
 * @author Craig McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:13:22 $
 */

public final class RequestDumperFilter implements Filter {


    // ----------------------------------------------------- Instance Variables


    /**
     * The filter configuration object we are associated with.  If this value
     * is null, this filter instance is not currently configured.
     */
    private FilterConfig filterConfig = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Take this filter out of service.
     */
    public void destroy() {

        this.filterConfig = null;

    }


    /**
     * Time the processing that is performed by all subsequent filters in the
     * current filter stack, including the ultimately invoked servlet.
     *
     * @param request The servlet request we are processing
     * @param result The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
	throws IOException, ServletException {

        if (filterConfig == null)
	    return;

	// Render the generic servlet request properties
	StringWriter sw = new StringWriter();
	PrintWriter writer = new PrintWriter(sw);
	writer.println("Request Received at " +
		       (new Timestamp(System.currentTimeMillis())));
	writer.println(" characterEncoding=" + request.getCharacterEncoding());
	writer.println("     contentLength=" + request.getContentLength());
	writer.println("       contentType=" + request.getContentType());
	writer.println("            locale=" + request.getLocale());
	writer.print("           locales=");
	Enumeration locales = request.getLocales();
	boolean first = true;
	while (locales.hasMoreElements()) {
	    Locale locale = (Locale) locales.nextElement();
	    if (first)
	        first = false;
	    else
	        writer.print(", ");
	    writer.print(locale.toString());
	}
	writer.println();
	Enumeration names = request.getParameterNames();
	while (names.hasMoreElements()) {
	    String name = (String) names.nextElement();
	    writer.print("         parameter=" + name + "=");
	    String values[] = request.getParameterValues(name);
	    for (int i = 0; i < values.length; i++) {
	        if (i > 0)
		    writer.print(", ");
		writer.print(values[i]);
	    }
	    writer.println();
	}
	writer.println("          protocol=" + request.getProtocol());
	writer.println("        remoteAddr=" + request.getRemoteAddr());
	writer.println("        remoteHost=" + request.getRemoteHost());
	writer.println("            scheme=" + request.getScheme());
	writer.println("        serverName=" + request.getServerName());
	writer.println("        serverPort=" + request.getServerPort());
	writer.println("          isSecure=" + request.isSecure());

	// Render the HTTP servlet request properties
	if (request instanceof HttpServletRequest) {
	    writer.println("---------------------------------------------");
	    HttpServletRequest hrequest = (HttpServletRequest) request;
	    writer.println("       contextPath=" + hrequest.getContextPath());
	    Cookie cookies[] = hrequest.getCookies();
            if (cookies == null)
                cookies = new Cookie[0];
	    for (int i = 0; i < cookies.length; i++) {
	        writer.println("            cookie=" + cookies[i].getName() +
			       "=" + cookies[i].getValue());
	    }
	    names = hrequest.getHeaderNames();
	    while (names.hasMoreElements()) {
	        String name = (String) names.nextElement();
		String value = hrequest.getHeader(name);
	        writer.println("            header=" + name + "=" + value);
	    }
	    writer.println("            method=" + hrequest.getMethod());
	    writer.println("          pathInfo=" + hrequest.getPathInfo());
	    writer.println("       queryString=" + hrequest.getQueryString());
	    writer.println("        remoteUser=" + hrequest.getRemoteUser());
	    writer.println("requestedSessionId=" +
			   hrequest.getRequestedSessionId());
	    writer.println("        requestURI=" + hrequest.getRequestURI());
	    writer.println("       servletPath=" + hrequest.getServletPath());
	}
	writer.println("=============================================");

	// Log the resulting string
	writer.flush();
	filterConfig.getServletContext().log(sw.getBuffer().toString());

	// Pass control on to the next filter
        chain.doFilter(request, response);

    }


    /**
     * Place this filter into service.
     *
     * @param filterConfig The filter configuration object
     */
    public void init(FilterConfig filterConfig) throws ServletException {

	this.filterConfig = filterConfig;

    }


    /**
     * Return a String representation of this object.
     */
    public String toString() {

	if (filterConfig == null)
	    return ("RequestDumperFilter()");
	StringBuffer sb = new StringBuffer("RequestDumperFilter(");
	sb.append(filterConfig);
	sb.append(")");
	return (sb.toString());

    }


}

