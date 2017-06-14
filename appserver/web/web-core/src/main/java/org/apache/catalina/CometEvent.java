/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The CometEvent interface.
 * 
 * @author Filip Hanik
 * @author Remy Maucherat
 */
public interface CometEvent {

    /**
     * Enumeration describing the major events that the container can invoke 
     * the CometProcessors event() method with
     * BEGIN - will be called at the beginning 
     *  of the processing of the connection. It can be used to initialize any relevant 
     *  fields using the request and response objects. Between the end of the processing 
     *  of this event, and the beginning of the processing of the end or error events,
     *  it is possible to use the response object to write data on the open connection.
     *  Note that the response object and dependent OutputStream and Writer are still 
     *  not synchronized, so when they are accessed by multiple threads, 
     *  synchronization is mandatory. After processing the initial event, the request 
     *  is considered to be committed.
     * READ - This indicates that input data is available, and that one read can be made
     *  without blocking. The available and ready methods of the InputStream or
     *  Reader may be used to determine if there is a risk of blocking: the servlet
     *  should read while data is reported available, and can make one additional read
     *  without blocking. When encountering a read error or an EOF, the servlet MUST
     *  report it by either returning false or throwing an exception such as an 
     *  IOException. This will cause the error event to be invoked, and the connection
     *  will be closed. It is not allowed to attempt reading data from the request object
     *  outside of the execution of this method.
     * END - End may be called to end the processing of the request. Fields that have
     *  been initialized in the begin method should be reset. After this event has
     *  been processed, the request and response objects, as well as all their dependent
     *  objects will be recycled and used to process other requests.
     * ERROR - Error will be called by the container in the case where an IO exception
     *  or a similar unrecoverable error occurs on the connection. Fields that have
     *  been initialized in the begin method should be reset. After this event has
     *  been processed, the request and response objects, as well as all their dependent
     *  objects will be recycled and used to process other requests.
     */
    public enum EventType {BEGIN, READ, END, ERROR}
    
    
    /**
     * Event details
     * TIMEOUT - the connection timed out (sub type of ERROR); note that this ERROR type is not fatal, and
     *   the connection will not be closed unless the servlet uses the close method of the event
     * CLIENT_DISCONNECT - the client connection was closed (sub type of ERROR)
     * IOEXCEPTION - an IO exception occurred, such as invalid content, for example, an invalid chunk block (sub type of ERROR)
     * WEBAPP_RELOAD - the webapplication is being reloaded (sub type of END)
     * SERVER_SHUTDOWN - the server is shutting down (sub type of END)
     * SESSION_END - the servlet ended the session (sub type of END)
     */
    public enum EventSubType { TIMEOUT, CLIENT_DISCONNECT, IOEXCEPTION, WEBAPP_RELOAD, SERVER_SHUTDOWN, SESSION_END }
    
    
    /**
     * Returns the HttpServletRequest.
     * 
     * @return HttpServletRequest
     */
    public HttpServletRequest getHttpServletRequest();
    
    /**
     * Returns the HttpServletResponse.
     * 
     * @return HttpServletResponse
     */
    public HttpServletResponse getHttpServletResponse();
    
    /**
     * Returns the event type.
     * 
     * @return EventType
     */
    public EventType getEventType();
    
    /**
     * Returns the sub type of this event.
     * 
     * @return EventSubType
     */
    public EventSubType getEventSubType();
    
    /**
     * Ends the Comet session. This signals to the container that 
     * the container wants to end the comet session. This will send back to the
     * client a notice that the server has no more data to send as part of this
     * request. The servlet should perform any needed cleanup as if it had received
     * an END or ERROR event. 
     * 
     * @throws IOException if an IO exception occurs
     */
    public void close() throws IOException;
    
    /**
     * Sets the timeout for this Comet connection. Please NOTE, that the implementation 
     * of a per connection timeout is OPTIONAL and MAY NOT be implemented.<br/>
     * This method sets the timeout in milliseconds of idle time on the connection.
     * The timeout is reset every time data is received from the connection or data is flushed
     * using <code>response.flushBuffer()</code>. If a timeout occurs, the 
     * <code>error(HttpServletRequest, HttpServletResponse)</code> method is invoked. The 
     * web application SHOULD NOT attempt to reuse the request and response objects after a timeout
     * as the <code>error(HttpServletRequest, HttpServletResponse)</code> method indicates.<br/>
     * This method should not be called asynchronously, as that will have no effect.
     * 
     * @param timeout The timeout in milliseconds for this connection, must be a positive value, larger than 0
     * @throws IOException An IOException may be thrown to indicate an IO error, 
     *         or that the EOF has been reached on the connection
     * @throws ServletException An exception has occurred, as specified by the root
     *         cause
     * @throws UnsupportedOperationException if per connection timeout is not supported, either at all or at this phase
     *         of the invocation.
     */
    public void setTimeout(int timeout)
        throws IOException, ServletException, UnsupportedOperationException;

}
