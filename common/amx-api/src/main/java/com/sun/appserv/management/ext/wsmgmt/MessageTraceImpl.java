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

import com.sun.appserv.management.base.MapCapableBase;

import java.io.Serializable;
import java.util.Map;

/**
 * Provides details of a SOAP invocation. 
 *
 * @since AppServer 9.0
 */
public final class MessageTraceImpl extends MapCapableBase
    implements MessageTrace {

    /**
     * Constructor
     */
    public MessageTraceImpl(
        final Map<String,Serializable> m,
        final String className) {
        super(m, className);
    }

    /**
     * Constructor
     * 
     * @param id    Message ID
     */
    public MessageTraceImpl(String id, String className) {
        super(null, className);
        putField(MESSAGE_ID,id );
    }

    /**
     * Constructor
     * 
     * @param id    Message ID
     */
    public MessageTraceImpl(String id) {
        super( null, MessageTrace.CLASS_NAME);
        putField(MESSAGE_ID,id );
    }

    protected boolean validate() {
        return (true);
    }

    /**
     * Returns the total size in bytes of the request content. This is 
     * SOAP content size including body and headers. 
     *
     * @return  SOAP request content size in bytes
     */
    public int getRequestSize() {
         return getInteger(REQUEST_SIZE).intValue();
    }

    public void setRequestSize(int reqSize) {
        putField(REQUEST_SIZE, new Integer(reqSize));
    }

    /**
     * Returns the total size in bytes of the response content. This is 
     * SOAP content size including body and headers. 
     *
     * @return  SOAP response content size in bytes
     */
    public int getResponseSize() {
         return getInteger(RESPONSE_SIZE).intValue();
    }

    public void setResponseSize(int resSize) {
        putField(RESPONSE_SIZE, new Integer(resSize));
    }

    /**
     * Returns the SOAP request content. This contains SOAP body and headers.
     *
     * @return  SOAP request content
     */
    public String getRequestContent() {
         return getString(REQUEST_CONTENT);
    }

    public void setRequestContent(String reqContent) {
        putField(REQUEST_CONTENT, reqContent);
    }

    /**
     * Returns the SOAP response content. This contains SOAP body and headers. 
     * 
     * @return SOAP response content
     */
    public String getResponseContent() {
         return getString(RESPONSE_CONTENT);
    }

    public void setResponseContent(String resContent) {
        putField(RESPONSE_CONTENT, resContent);
    }

    /**
     * Returns the transport type. Known types are "HTTP", "JMS" and "SNMP".
     *
     * @return  transport type
     */
    public String getTransportType() {
         return getString(TRANSPORT_TYPE);
    }

    public void setTransportType(String transport) {
        putField(TRANSPORT_TYPE, transport);
    }

    /**
     * Returns the comma separated list of HTTP request header names and its 
     * values. Null if transport type is not HTTP. 
     *
     * @return  HTTP request headers
     */
    public String getHTTPRequestHeaders() {
         return getString(HTTP_REQUEST_HEADERS);
    }

    public void setHTTPRequestHeaders(String reqHeaders) {
         putField(HTTP_REQUEST_HEADERS, reqHeaders);
    }

    /**
     * Returns the comma separated list of HTTP response header names and its
     * values. Null if transport type is not HTTP. 
     *
     * @return  HTTP response headers
     */
    public String getHTTPResponseHeaders() {
         return getString(HTTP_RESPONSE_HEADERS);
    }

    public void setHTTPResponseHeaders(String resHeaders) {
         putField(HTTP_RESPONSE_HEADERS, resHeaders);
    }

    /**
     * Returns the IP address or host name of the client machine. 
     *
     * @return  IP address or host name of client
     */
    public String getClientHost() {
         return getString(CLIENT_HOST);
    }

    public void setClientHost(String clientHost) {
         putField(CLIENT_HOST,clientHost );
    }

    /**
     * Returns the caller principal name. 
     *
     * @return  caller principal name
     */
    public String getPrincipalName() {
         return getString(PRINCIPAL_NAME);
    }

    public void setPrincipalName(String principal) {
        putField(PRINCIPAL_NAME, principal);
    }

    /**
     * Returns the response time in milli seconds for this web service 
     * operation. 
     * 
     * @return  reponse time in milli seconds
     */
    public long getResponseTime() {
         return new Long(getString(RESPONSE_TIME)).longValue();
    }

    public void setResponseTime(long resTime) {
        putField(RESPONSE_TIME, new Long(resTime).toString());
    }

    /**
     * Returns the fault code. 
     *
     * @return  fault code for this web service or null
     */
    public String getFaultCode() {
        return getString(FAULT_CODE);
    }

    public void setFaultCode(String faultCode) {
        putField(FAULT_CODE, faultCode);
    }

    /**
     * Returns the fault string. 
     *
     * @return  fault string for this web service or null
     */
    public String getFaultString() {
        return getString(FAULT_STRING);
    }

    public void setFaultString(String faultString) {
        putField(FAULT_STRING, faultString);
    }

    /**
     * Returns the fault actor. 
     *
     * @return  fault actor for this web service
     */
    public String getFaultActor() {
        return getString(FAULT_ACTOR);
    }

    public void setFaultActor(String faultActor) {
        putField(FAULT_ACTOR,faultActor );
    }

    /**
     * Returns the message id.
     *
     * @return  message id
     */
    public String getMessageID() {
        return getString(MESSAGE_ID);
    }

    /**
     * Returns the name of the application.
     *
     * @return application name
     */
    public String getApplicationID() {
        return getString(APPLICATION_ID);
    }

    public void setApplicationID(String id) {
        putField(APPLICATION_ID,id );
    }

    /**
     * Returns the fully qualified name of the webservice endpoint
     */
    public String getEndpointName() {
        return getString(ENDPOINT_NAME);
    }

    public void setEndpointName(String name) {
         putField(ENDPOINT_NAME, name);
    }

    /**
     * Returns the time stamp when the SOAP message was received.
     *
     * @return the time stamp when the SOAP message was received
     */
    public long getTimestamp() {
         return new Long(getString(TIME_STAMP)).longValue();
    }

    /**
     * Returns if during this web service invocation call flow was enabled or
     * not
     *
     * @return true, if during this web service invocation call flow was
     * enabled otherwise flase.
     */
    public boolean isCallFlowEnabled() {
        return new Boolean(getString(CALL_FLOW_ENABLED)).booleanValue();
    }

    /**
     * Returns if during this web service invocation call flow was enabled or
     * not
     */
    public void setCallFlowEnabled(boolean enabled) {
        putField(CALL_FLOW_ENABLED, new Boolean(enabled).toString());
    }

    /**
     * Returns the time stamp when the SOAP message was received.
     *
     * @param ts the time stamp when the SOAP message was received
     */
    public void setTimeStamp(long ts) {
         putField(TIME_STAMP, new Long(ts).toString());
    }

    /** Request Size */
    static final String REQUEST_SIZE = "RequestSize";

    /** Response Size */
    static final String RESPONSE_SIZE = "ResponseSize";

    /** Response Content */
    static final String RESPONSE_CONTENT = "ResponseContent";

    /** Request Content */
    static final String REQUEST_CONTENT = "RequestContent";

    /** Transport Type */
    static final String TRANSPORT_TYPE = "TransportType";

    /** HTTP Request Headers */
    static final String HTTP_REQUEST_HEADERS = "HTTPRequestHeaders";

    /** HTTP Response Headers */
    static final String HTTP_RESPONSE_HEADERS = "HTTPResponseHeaders";

    /** Client Host */
    static final String CLIENT_HOST = "ClientHost";

    /** Principal Name */
    static final String PRINCIPAL_NAME = "PrincipalName";

    /** Response Time */
    static final String RESPONSE_TIME = "ResponseTime";

    /** Fault Code */
    static final String FAULT_CODE = "FaultCode";

    /** Fault String */
    static final String FAULT_STRING = "FaultString";

    /** Fault Actor */
    static final String FAULT_ACTOR = "FaultActor";

    /** Message ID */
    static final String MESSAGE_ID = "MessageID";

    /** Application ID */
    static final String APPLICATION_ID = "ApplicationID";

    /** Endpoint Name */
    static final String ENDPOINT_NAME = "EndpointName";

    /** Time Stamp */
    static final String TIME_STAMP = "Timestamp";

    /** CallFlow Enabled */
    static final String CALL_FLOW_ENABLED = "CallFlowEnabled";
}
