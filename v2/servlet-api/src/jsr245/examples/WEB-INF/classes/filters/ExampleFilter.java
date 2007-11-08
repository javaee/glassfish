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
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


/**
 * Example filter that can be attached to either an individual servlet
 * or to a URL pattern.  This filter performs the following functions:
 * <ul>
 * <li>Attaches itself as a request attribute, under the attribute name
 *     defined by the value of the <code>attribute</code> initialization
 *     parameter.</li>
 * <li>Calculates the number of milliseconds required to perform the
 *     servlet processing required by this request, including any
 *     subsequently defined filters, and logs the result to the servlet
 *     context log for this application.
 * </ul>
 *
 * @author Craig McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:13:49 $
 */

public final class ExampleFilter implements Filter {


    // ----------------------------------------------------- Instance Variables


    /**
     * The request attribute name under which we store a reference to ourself.
     */
    private String attribute = null;


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

        this.attribute = null;
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

	// Store ourselves as a request attribute (if requested)
	if (attribute != null)
	    request.setAttribute(attribute, this);

	// Time and log the subsequent processing
	long startTime = System.currentTimeMillis();
        chain.doFilter(request, response);
	long stopTime = System.currentTimeMillis();
	filterConfig.getServletContext().log
	    (this.toString() + ": " + (stopTime - startTime) +
	     " milliseconds");

    }


    /**
     * Place this filter into service.
     *
     * @param filterConfig The filter configuration object
     */
    public void init(FilterConfig filterConfig) throws ServletException {

	this.filterConfig = filterConfig;
        this.attribute = filterConfig.getInitParameter("attribute");

    }


    /**
     * Return a String representation of this object.
     */
    public String toString() {

	if (filterConfig == null)
	    return ("InvokerFilter()");
	StringBuffer sb = new StringBuffer("InvokerFilter(");
	sb.append(filterConfig);
	sb.append(")");
	return (sb.toString());

    }


}

