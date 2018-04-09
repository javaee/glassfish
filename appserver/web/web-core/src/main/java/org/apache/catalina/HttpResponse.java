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

package org.apache.catalina;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.Collection;

/**
 * An <b>HttpResponse</b> is the Catalina-internal facade for an
 * <code>HttpServletResponse</code> that is to be produced,
 * based on the processing of a corresponding <code>HttpRequest</code>.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:16 $
 */

public interface HttpResponse
    extends Response {


    // --------------------------------------------------------- Public Methods


    /**
     * Return the value for the specified header, or <code>null</code> if this
     * header has not been set.  If more than one value was added for this
     * name, only the first is returned; use {@link #getHeaders(String)} to
     * retrieve all of them.
     *
     * @param name Header name to look up
     */
    public String getHeader(String name);


    /**
     * @return a (possibly empty) <code>Collection</code> of the names
     * of the headers of this response
     */
    public Collection<String> getHeaderNames();


    /**
     * @param name the name of the response header whose values to return
     *
     * @return a (possibly empty) <code>Collection</code> of the values
     * of the response header with the given name
     */
    public Collection<String> getHeaders(String name);


    /**
     * Special method for adding a session cookie as we should be overriding 
     * any previous 
     * @param cookie
     */
    public void addSessionCookieInternal(final Cookie cookie);


    /**
     * Return the error message that was set with <code>sendError()</code>
     * for this Response.
     */
    public String getMessage();


    /**
     * Return the HTTP status code associated with this Response.
     */
    public int getStatus();


    /**
     * Reset this response, and specify the values for the HTTP status code
     * and corresponding message.
     *
     * @exception IllegalStateException if this response has already been
     *  committed
     */
    public void reset(int status, String message);


    /**
     * Send an acknowledgment of a request.   An acknowledgment in this
     * case is simply an HTTP response status line, i.e.
     * <code>HTTP/1.1 [STATUS] [REASON-PHRASE]<code>.
     *
     * @exception java.io.IOException if an input/output error occurs
     */
    public void sendAcknowledgement() throws IOException;
}
