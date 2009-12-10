/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.security.jauth;

import javax.security.auth.message.MessageInfo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An HTTP Servlet authentication parameter that encapsulates
 * HTTP Servlet request and response objects.
 *
 * <p> HttpServletAuthParam may be created with null request or response
 * objects.  The following table describes when it is appropriate to pass null:
 *
 * <pre>
 *                                        Request   Response
 *                                        -------   --------
 *
 * ClientAuthModule.secureRequest         non-null  null
 * ClientAuthModule.validateResponse      null      non-null
 *
 * ServerAuthModule.validateRequest       non-null  null
 * ServerAuthModule.secureResponse        null      non-null
 * </pre>
 *
 * <p> As noted above, in the case of
 * <code>ServerAuthModule.validateRequest</code> the module receives
 * a null response object.  If the implementation of
 * <code>validateRequest</code> encounters an authentication error,
 * it may construct the appropriate response object itself and set it
 * into the HttpServletAuthParam via the <code>setResponse</code> method.
 *
 * @version %I%, %G%
 */
public class HttpServletAuthParam implements AuthParam {

    private HttpServletRequest request;
    private HttpServletResponse response;
    //private static final MessageLayer layer =
    //      new MessageLayer(MessageLayer.HTTP_SERVLET);
    
    /**
     * Create an HttpServletAuthParam with HTTP request and response objects.
     *
     * @param request the HTTP Servlet request object, or null.
     * @param response the HTTP Servlet response object, or null.
     */
    public HttpServletAuthParam(HttpServletRequest request,
				HttpServletResponse response) {
	this.request = request;
	this.response = response;
    }

    /**
     * Create an HttpServletAuthParam with MessageInfo object.
     * @param messageInfo
     *
     */
    public HttpServletAuthParam(MessageInfo messageInfo) {
        this.request = (HttpServletRequest)messageInfo.getRequestMessage();
        this.response = (HttpServletResponse)messageInfo.getResponseMessage();
    }

    /**
     * Get the HTTP Servlet request object.
     *
     * @return the HTTP Servlet request object, or null.
     */
    public HttpServletRequest getRequest() {
	return this.request;
    }

    /**
     * Get the HTTP Servlet response object.
     *
     * @return the HTTP Servlet response object, or null.
     */
    public HttpServletResponse getResponse() {
	return this.response;
    }

    /**
     * Set a new HTTP Servlet response object.
     *
     * <p> If a response has already been set (it is non-null),
     * this method returns.  The original response is not overwritten.
     *
     * @param response the HTTP Servlet response object.
     *
     * @exception IllegalArgumentException if the specified response is null.
     */
    public void setResponse(HttpServletResponse response) {
	if (response == null) {
	    throw new IllegalArgumentException("invalid null response");
	}

	if (this.response == null) {
	    this.response = response;
	}
    }

    /**
     * Get a MessageLayer instance that identifies HttpServlet
     * as the message layer.
     *
     * @return a MessageLayer instance that identifies HttpServlet
     *          as the message layer.
     */
    //public MessageLayer getMessageLayer() {
    //    return layer;
    //};

    /**
     * Get the operation related to the encapsulated HTTP Servlet
     * request and response objects.
     *
     * @return the operation related to the encapsulated request and response
     *          objects, or null.
     */
    public String getOperation() {
	return null;
    }
}
