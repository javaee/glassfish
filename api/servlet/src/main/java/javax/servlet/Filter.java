

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

package javax.servlet;

import java.io.IOException;

	/** 
	* A filter is an object that performs filtering tasks on either the request to a resource (a servlet or static content), or on the response from a resource, or both.
        * <br><br>
	* Filters perform filtering in the <code>doFilter</code> method. Every Filter has access to 
	** a FilterConfig object from which it can obtain its initialization parameters, a
	** reference to the ServletContext which it can use, for example, to load resources
	** needed for filtering tasks.
	** <p>
	** Filters are configured in the deployment descriptor of a web application
	** <p>
	** Examples that have been identified for this design are<br>
	** 1) Authentication Filters <br>
	** 2) Logging and Auditing Filters <br>
	** 3) Image conversion Filters <br>
    	** 4) Data compression Filters <br>
	** 5) Encryption Filters <br>
	** 6) Tokenizing Filters <br>
	** 7) Filters that trigger resource access events <br>
	** 8) XSL/T filters <br>
	** 9) Mime-type chain Filter <br>
	 * @since	Servlet 2.3
	*/

public interface Filter {

	/** 
	* Called by the web container to indicate to a filter that it is being placed into
	* service. The servlet container calls the init method exactly once after instantiating the
	* filter. The init method must complete successfully before the filter is asked to do any
	* filtering work. <br><br>

     	* The web container cannot place the filter into service if the init method either<br>
        * 1.Throws a ServletException <br>
        * 2.Does not return within a time period defined by the web container 
	*/
	public void init(FilterConfig filterConfig) throws ServletException;
	
	
	/**
	* The <code>doFilter</code> method of the Filter is called by the container
	* each time a request/response pair is passed through the chain due
	* to a client request for a resource at the end of the chain. The FilterChain passed in to this
	* method allows the Filter to pass on the request and response to the next entity in the
	* chain.<p>
	* A typical implementation of this method would follow the following pattern:- <br>
	* 1. Examine the request<br>
	* 2. Optionally wrap the request object with a custom implementation to
	* filter content or headers for input filtering <br>
	* 3. Optionally wrap the response object with a custom implementation to
	* filter content or headers for output filtering <br>
	* 4. a) <strong>Either</strong> invoke the next entity in the chain using the FilterChain object (<code>chain.doFilter()</code>), <br>   
	** 4. b) <strong>or</strong> not pass on the request/response pair to the next entity in the filter chain to block the request processing<br>
	** 5. Directly set headers on the response after invocation of the next entity in the filter chain.
	**/
    public void doFilter ( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException;

	/**
	* Called by the web container to indicate to a filter that it is being taken out of service. This 
	* method is only called once all threads within the filter's doFilter method have exited or after
	* a timeout period has passed. After the web container calls this method, it will not call the
	* doFilter method again on this instance of the filter. <br><br>
	* 
     	* This method gives the filter an opportunity to clean up any resources that are being held (for
	* example, memory, file handles, threads) and make sure that any persistent state is synchronized
	* with the filter's current state in memory.
	*/

	public void destroy();


}

