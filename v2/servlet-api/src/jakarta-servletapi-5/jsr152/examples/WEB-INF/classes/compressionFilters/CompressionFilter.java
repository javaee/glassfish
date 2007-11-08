

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

package compressionFilters;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Implementation of <code>javax.servlet.Filter</code> used to compress
 * the ServletResponse if it is bigger than a threshold.
 *
 * @author Amy Roh
 * @author Dmitri Valdin
 * @version $Revision: 1.2 $, $Date: 2005/12/08 01:13:19 $
 */

public class CompressionFilter implements Filter{

    /**
     * The filter configuration object we are associated with.  If this value
     * is null, this filter instance is not currently configured.
     */
    private FilterConfig config = null;

    /**
     * Minimal reasonable threshold
     */
    private int minThreshold = 128;


    /**
     * The threshold number to compress
     */
    protected int compressionThreshold;

    /**
     * Debug level for this filter
     */
    private int debug = 0;

    /**
     * Place this filter into service.
     *
     * @param filterConfig The filter configuration object
     */

    public void init(FilterConfig filterConfig) {

        config = filterConfig;
        if (filterConfig != null) {
            String value = filterConfig.getInitParameter("debug");
            if (value!=null) {
                debug = Integer.parseInt(value);
            } else {
                debug = 0;
            }
            String str = filterConfig.getInitParameter("compressionThreshold");
            if (str!=null) {
                compressionThreshold = Integer.parseInt(str);
                if (compressionThreshold != 0 && compressionThreshold < minThreshold) {
                    if (debug > 0) {
                        System.out.println("compressionThreshold should be either 0 - no compression or >= " + minThreshold);
                        System.out.println("compressionThreshold set to " + minThreshold);
                    }
                    compressionThreshold = minThreshold;
                }
            } else {
                compressionThreshold = 0;
            }

        } else {
            compressionThreshold = 0;
        }

    }

    /**
    * Take this filter out of service.
    */
    public void destroy() {

        this.config = null;

    }

    /**
     * The <code>doFilter</code> method of the Filter is called by the container
     * each time a request/response pair is passed through the chain due
     * to a client request for a resource at the end of the chain.
     * The FilterChain passed into this method allows the Filter to pass on the
     * request and response to the next entity in the chain.<p>
     * This method first examines the request to check whether the client support
     * compression. <br>
     * It simply just pass the request and response if there is no support for
     * compression.<br>
     * If the compression support is available, it creates a
     * CompressionServletResponseWrapper object which compresses the content and
     * modifies the header if the content length is big enough.
     * It then invokes the next entity in the chain using the FilterChain object
     * (<code>chain.doFilter()</code>), <br>
     **/

    public void doFilter ( ServletRequest request, ServletResponse response,
                        FilterChain chain ) throws IOException, ServletException {

        if (debug > 0) {
            System.out.println("@doFilter");
        }

        if (compressionThreshold == 0) {
            if (debug > 0) {
                System.out.println("doFilter gets called, but compressionTreshold is set to 0 - no compression");
            }
            chain.doFilter(request, response);
            return;
        }

        boolean supportCompression = false;
        if (request instanceof HttpServletRequest) {
            if (debug > 1) {
                System.out.println("requestURI = " + ((HttpServletRequest)request).getRequestURI());
            }

            // Are we allowed to compress ?
            String s = (String) ((HttpServletRequest)request).getParameter("gzip");
            if ("false".equals(s)) {
                if (debug > 0) {
                    System.out.println("got parameter gzip=false --> don't compress, just chain filter");
                }
                chain.doFilter(request, response);
                return;
            }

            Enumeration e =
                ((HttpServletRequest)request).getHeaders("Accept-Encoding");
            while (e.hasMoreElements()) {
                String name = (String)e.nextElement();
                if (name.indexOf("gzip") != -1) {
                    if (debug > 0) {
                        System.out.println("supports compression");
                    }
                    supportCompression = true;
                } else {
                    if (debug > 0) {
                        System.out.println("no support for compresion");
                    }
                }
            }
        }

        if (!supportCompression) {
            if (debug > 0) {
                System.out.println("doFilter gets called wo compression");
            }
            chain.doFilter(request, response);
            return;
        } else {
            if (response instanceof HttpServletResponse) {
                CompressionServletResponseWrapper wrappedResponse =
                    new CompressionServletResponseWrapper((HttpServletResponse)response);
                wrappedResponse.setDebugLevel(debug);
                wrappedResponse.setCompressionThreshold(compressionThreshold);
                if (debug > 0) {
                    System.out.println("doFilter gets called with compression");
                }
                try {
                    chain.doFilter(request, wrappedResponse);
                } finally {
                    wrappedResponse.finishResponse();
                }
                return;
            }
        }
    }

    /**
     * Set filter config
     * This function is equivalent to init. Required by Weblogic 6.1
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        init(filterConfig);
    }

    /**
     * Return filter config
     * Required by Weblogic 6.1
     */
    public FilterConfig getFilterConfig() {
        return config;
    }

}

