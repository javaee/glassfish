/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.appserv.management.ext.wsmgmt;

import com.sun.appserv.management.base.MapCapable;

/**
 * Provides details of a SOAP invocation. 
 *
 * @since AppServer 9.0
 */
public interface MessageTrace extends MapCapable
 {

    /** This interface's class name */
    public final static String  CLASS_NAME    =
                "com.sun.appserv.management.ext.wsmgmt.MessageTrace";

    /**
     * Returns the total size in bytes of the request content. This is 
     * SOAP content size including body and headers. 
     *
     * @return  SOAP request content size in bytes
     */
    public int getRequestSize();

    /**
     * Returns the total size in bytes of the response content. This is 
     * SOAP content size including body and headers. 
     *
     * @return  SOAP response content size in bytes
     */
    public int getResponseSize();

    /**
     * Returns the SOAP request content. This contains SOAP body and headers.
     *
     * @return  SOAP request content
     */
    public String getRequestContent();

    /**
     * Returns the SOAP response content. This contains SOAP body and headers. 
     * 
     * @return SOAP response content
     */
    public String getResponseContent();

    /**
     * Returns the transport type. Known types are "HTTP", "JMS" and "SNMP".
     *
     * @return  transport type
     */
    public String getTransportType();

    /**
     * Returns the comma separated list of HTTP request header names and its 
     * values. Null if transport type is not HTTP. 
     *
     * @return  HTTP request headers
     */
    public String getHTTPRequestHeaders();

    /**
     * Returns the comma separated list of HTTP response header names and its
     * values. Null if transport type is not HTTP. 
     *
     * @return  HTTP response headers
     */
    public String getHTTPResponseHeaders();

    /**
     * Returns the IP address or host name of the client machine. 
     *
     * @return  IP address or host name of client
     */
    public String getClientHost();

    /**
     * Returns the caller principal name. 
     *
     * @return  caller principal name
     */
    public String getPrincipalName();

    /**
     * Returns the response time in milli seconds for this web service 
     * operation. 
     * 
     * @return  reponse time in milli seconds
     */
    public long getResponseTime();

    /**
     * Returns the fault code. 
     *
     * @return  fault code for this web service or null
     */
    public String getFaultCode();

    /**
     * Returns the fault string. 
     *
     * @return  fault string for this web service or null
     */
    public String getFaultString();

    /**
     * Returns the fault actor. 
     *
     * @return  fault actor for this web service
     */
    public String getFaultActor();

    /**
     * Returns the message id.
     *
     * @return  message id
     */
    public String getMessageID();

    /**
     * Returns the name of the application.
     *
     * @return  application name
     */
    public String getApplicationID();

    /**
     * Returns the name of the webservice endpoint.
     *
     * @return  name of the webservice endpoint
     */
    public String getEndpointName();

    /**
     * Returns the time stamp when the SOAP message was received.
     *
     * @return the time stamp when the SOAP message was received
     */
    public long getTimestamp();

    /**
     * Returns if during this web service invocation call flow was enabled or
     * not
     *
     * @return true, if during this web service invocation call flow was
     * enabled otherwise flase.
     */
    public boolean isCallFlowEnabled();
}
