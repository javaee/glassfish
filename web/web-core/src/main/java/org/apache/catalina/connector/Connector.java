/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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


package org.apache.catalina.connector;

import java.lang.reflect.Constructor;
import java.net.URLEncoder;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

// START OF SJSAS 8.1 PE 6191830
import java.security.cert.X509Certificate;
// END OF SJSAS 8.1 PE 6191830
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;
import javax.management.MalformedObjectNameException;

import org.apache.tomcat.util.modeler.Registry;

import com.sun.grizzly.util.IntrospectionUtils;
import com.sun.grizzly.util.http.mapper.Mapper;

import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.tcp.ProtocolHandler;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Service;
// START OF SJSAS 8.1 PE 6191830
import org.apache.catalina.Globals;
// END OF SJSAS 8.1 PE 6191830
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.net.ServerSocketFactory;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;
import com.sun.appserv.ProxyHandler;
// START S1AS 6188932
// END S1AS 6188932
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Implementation of a Coyote connector for Tomcat 5.x.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.23 $ $Date: 2007/07/09 20:46:45 $
 */


public class Connector
    implements org.apache.catalina.Connector, Lifecycle, MBeanRegistration
{
    protected static final Logger log = Logger.getLogger(Connector.class.getName());

    // ---------------------------------------------- Adapter Configuration --//
    
    // START SJSAS 6363251
    /**
     * Coyote Adapter class name.
     * Defaults to the CoyoteAdapter.
     */
    private String defaultClassName =
        "org.apache.catalina.connector.CoyoteAdapter";
    // END SJSAS 6363251

    
    // ----------------------------------------------------- Instance Variables

    /**
     * Holder for our configured properties.
     */
    private HashMap properties = new HashMap();

    /**
     * The <code>Service</code> we are associated with (if any).
     */
    private Service service = null;


    /**
     * The accept count for this Connector.
     */
    private int acceptCount = 10;


    /**
     * The IP address on which to bind, if any.  If <code>null</code>, all
     * addresses on the server will be bound.
     */
    private String address = null;
                                                                           

    /**
     * Do we allow TRACE ?
     */
    private boolean allowTrace = true;


    /**
     * The input buffer size we should create on input streams.
     */
    private int bufferSize = 4096;


    /**
     * The Container used for processing requests received by this Connector.
     */
    protected Container container = null;


    /**
     * Compression value.
     */
    private String compression = "off";


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * The "enable DNS lookups" flag for this Connector.
     */
    private boolean enableLookups = false;


    /**
     * The server socket factory for this component.
     */
    private ServerSocketFactory factory = null;

    /**
     * Maximum size of a HTTP header. 4KB is the default.
     */
    private int maxHttpHeaderSize = 4 * 1024;

    /*
     * Is generation of X-Powered-By response header enabled/disabled?
     */
    private boolean xpoweredBy;


    /**
     * Descriptive information about this Connector implementation.
     */
    private static final String info =
        "org.apache.catalina.connector.Connector/2.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The minimum number of processors to start at initialization time.
     */
    protected int minProcessors = 5;


    /**
     * The maximum number of processors allowed, or <0 for unlimited.
     */
    private int maxProcessors = 20;


    /**
     * Linger value on the incoming connection.
     * Note : a value inferior to 0 means no linger.
     */
    private int connectionLinger = Constants.DEFAULT_CONNECTION_LINGER;


    /**
     * Timeout value on the incoming connection.
     * Note : a value of 0 means no timeout.
     */
    private int connectionTimeout = Constants.DEFAULT_CONNECTION_TIMEOUT;


    /**
     * Timeout value on the incoming connection during request processing.
     * Note : a value of 0 means no timeout.
     */
    private int connectionUploadTimeout = 
        Constants.DEFAULT_CONNECTION_UPLOAD_TIMEOUT;


    /**
     * Timeout value on the server socket.
     * Note : a value of 0 means no timeout.
     */
    private int serverSocketTimeout = Constants.DEFAULT_SERVER_SOCKET_TIMEOUT;


    /**
     * The port number on which we listen for requests.
     */
    private int port = 8080;


    /**
     * The server name to which we should pretend requests to this Connector
     * were directed.  This is useful when operating Tomcat behind a proxy
     * server, so that redirects get constructed accurately.  If not specified,
     * the server name included in the <code>Host</code> header is used.
     */
    private String proxyName = null;


    /**
     * The server port to which we should pretent requests to this Connector
     * were directed.  This is useful when operating Tomcat behind a proxy
     * server, so that redirects get constructed accurately.  If not specified,
     * the port number specified by the <code>port</code> property is used.
     */
    private int proxyPort = 0;


    /**
     * The redirect port for non-SSL to SSL redirects.
     */
    private int redirectPort = 443;


    // BEGIN S1AS 5000999
    /**
     * The default host.
     */
    private String defaultHost;
    // END S1AS 5000999


    /**
     * The request scheme that will be set on all requests received
     * through this connector.
     */
    private String scheme = "http";


    /**
     * The secure connection flag that will be set on all requests received
     * through this connector.
     */
    private boolean secure = false;

    
    // START SJSAS 6439313     
    /**
     * The blocking connection flag that will be set on all requests received
     * through this connector.
     */
    private boolean blocking = false;
    // END SJSAS 6439313     
    
    
    /** For jk, do tomcat authentication if true, trust server if false 
     */ 
    private boolean tomcatAuthentication = true;

    /**
     * The string manager for this package.
     */
    protected StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * Flag to disable setting a seperate time-out for uploads.
     * If <code>true</code>, then the <code>timeout</code> parameter is
     * ignored.  If <code>false</code>, then the <code>timeout</code>
     * parameter is used to control uploads.
     */
    private boolean disableUploadTimeout = true;
    

    /**
     * Maximum number of Keep-Alive requests to honor per connection.
     */
    private int maxKeepAliveRequests = 100;


    /**
     * Maximum size of a POST which will be automatically parsed by the 
     * container. 2MB by default.
     */
    private int maxPostSize = 2 * 1024 * 1024;


    /**
     * Has this component been initialized yet?
     */
    protected boolean initialized = false;


    /**
     * Has this component been started yet?
     */
    private boolean started = false;


    /**
     * The shutdown signal to our background thread
     */
    private boolean stopped = false;


    /**
     * The background thread.
     */
    private Thread thread = null;


    /**
     * Use TCP no delay ?
     */
    private boolean tcpNoDelay = true;


    /**
     * Coyote Protocol handler class name.
     * Defaults to the Coyote HTTP/1.1 protocolHandler.
     */
    private String protocolHandlerClassName =
    	"com.sun.enterprise.web.connector.grizzly.CoyoteConnectorLauncher";

    /**
     * Coyote protocol handler.
     */
    private ProtocolHandler protocolHandler = null;

    private String instanceName;

    /**
     * The name of this Connector
     */
    private String name;

    /**
     * Coyote adapter.
     */
    private Adapter adapter = null;


    /**
     * Mapper.
     */
    protected Mapper mapper;


    /**
     * Mapper listener.
     */
    protected MapperListener mapperListener;


    /**
     * URI encoding.
     */
    /* GlassFish Issue 2339
    private String uriEncoding = null;
     */
    // START GlassFish Issue 2339
    private String uriEncoding = "UTF-8";
    // END GlassFish Issue 2339


    // START SJSAS 6331392
    private boolean isEnabled = true;
    // END SJSAS 6331392


    // START S1AS 6188932
    /**
     * Flag indicating whether this connector is receiving its requests from
     * a trusted intermediate server
     */
    protected boolean authPassthroughEnabled = false;

    protected ProxyHandler proxyHandler = null;
    // END S1AS 6188932

    /**
     * The <code>SelectorThread</code> implementation class.
     */
    private String selectorThreadImpl = null; 
    
    
    // ------------------------------------------------------------- Properties

    /**
     * Return a configured property.
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Set a configured property.
     */
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    /** 
     * remove a configured property.
     */
    public void removeProperty(String name) {
        properties.remove(name);
    }

    /**
     * Return the <code>Service</code> with which we are associated (if any).
     */
    public Service getService() {

        return (this.service);

    }


    /**
     * Set the <code>Service</code> with which we are associated (if any).
     *
     * @param service The service that owns this Engine
     */
    public void setService(Service service) {

        this.service = service;
        setProperty("service", service);

    }


    /**
     * Get the value of compression.
     */
    public String getCompression() {

        return (compression);

    }


    /**
     * Set the value of compression.
     *
     * @param compression The new compression value, which can be "on", "off"
     * or "force"
     */
    public void setCompression(String compression) {

        this.compression = compression;
        setProperty("compression", compression);

    }


    /**
     * Return the connection linger for this Connector.
     */
    public int getConnectionLinger() {

        return (connectionLinger);

    }


    /**
     * Set the connection linger for this Connector.
     *
     * @param connectionLinger The new connection linger
     */
    public void setConnectionLinger(int connectionLinger) {

        this.connectionLinger = connectionLinger;
        setProperty("soLinger", String.valueOf(connectionLinger));

    }


    /**
     * Return the connection timeout for this Connector.
     */
    public int getConnectionTimeout() {

        return (connectionTimeout);

    }


    /**
     * Set the connection timeout for this Connector.
     *
     * @param connectionTimeout The new connection timeout
     */
    public void setConnectionTimeout(int connectionTimeout) {

        this.connectionTimeout = connectionTimeout;
        setProperty("soTimeout", String.valueOf(connectionTimeout));

    }


    /**
     * Return the connection upload timeout for this Connector.
     */
    public int getConnectionUploadTimeout() {

        return (connectionUploadTimeout);

    }


    /**
     * Set the connection upload timeout for this Connector.
     *
     * @param connectionUploadTimeout The new connection upload timeout
     */
    public void setConnectionUploadTimeout(int connectionUploadTimeout) {

        this.connectionUploadTimeout = connectionUploadTimeout;
        setProperty("timeout", String.valueOf(connectionUploadTimeout));

    }


    /**
     * Return the server socket timeout for this Connector.
     */
    public int getServerSocketTimeout() {

        return (serverSocketTimeout);

    }


    /**
     * Set the server socket timeout for this Connector.
     *
     * @param serverSocketTimeout The new server socket timeout
     */
    public void setServerSocketTimeout(int serverSocketTimeout) {

        this.serverSocketTimeout = serverSocketTimeout;
        setProperty("serverSoTimeout", String.valueOf(serverSocketTimeout));

    }


    /**
     * Return the accept count for this Connector.
     */
    public int getAcceptCount() {

        return (acceptCount);

    }


    /**
     * Set the accept count for this Connector.
     *
     * @param count The new accept count
     */
    public void setAcceptCount(int count) {

        this.acceptCount = count;
        setProperty("backlog", String.valueOf(count));

    }


    /**
     * Return the bind IP address for this Connector.
     */
    public String getAddress() {

        return (this.address);

    }


    /**
     * Set the bind IP address for this Connector.
     *
     * @param address The bind IP address
     */
    public void setAddress(String address) {

        this.address = address;
        setProperty("address", address);

    }

                                                                           
                                                                           
    /**
     * True if the TRACE method is allowed.  Default value is "false".
     */
    public boolean getAllowTrace() {
                                                                           
        return (this.allowTrace);
                                                                           
    }
                                                                           
                                                                           
    /**
     * Set the allowTrace flag, to disable or enable the TRACE HTTP method.     *
     * @param allowTrace The new allowTrace flag
     */
    public void setAllowTrace(boolean allowTrace) {
                                                                           
        this.allowTrace = allowTrace;
        setProperty("allowTrace", String.valueOf(allowTrace));
                                                                           
    }


    /**
     * Is this connector available for processing requests?
     */
    public boolean isAvailable() {

        return (started);

    }


    /**
     * Return the input buffer size for this Connector.
     */
    public int getBufferSize() {

        return (this.bufferSize);

    }


    /**
     * Set the input buffer size for this Connector.
     *
     * @param bufferSize The new input buffer size.
     */
    public void setBufferSize(int bufferSize) {

        this.bufferSize = bufferSize;
        setProperty("bufferSize", String.valueOf(bufferSize));

    }


    /**
     * Return the Container used for processing requests received by this
     * Connector.
     */
    public Container getContainer() {
        if( container==null ) {
            // Lazy - maybe it was added later
            findContainer();     
        }
        return (container);

    }


    /**
     * Set the Container used for processing requests received by this
     * Connector.
     *
     * @param container The new Container to use
     */
    public void setContainer(Container container) {

        this.container = container;

    }


    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {

        return (debug);

    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {

        this.debug = debug;

    }


    /**
     * Return the "enable DNS lookups" flag.
     */
    public boolean getEnableLookups() {

        return (this.enableLookups);

    }


    /**
     * Set the "enable DNS lookups" flag.
     *
     * @param enableLookups The new "enable DNS lookups" flag value
     */
    public void setEnableLookups(boolean enableLookups) {

        this.enableLookups = enableLookups;
        setProperty("enableLookups", String.valueOf(enableLookups));

    }


    /**
     * Return the server socket factory used by this Container.
     */
    public ServerSocketFactory getFactory() {

        return (this.factory);

    }


    /**
     * Set the server socket factory used by this Container.
     *
     * @param factory The new server socket factory
     */
    public void setFactory(ServerSocketFactory factory) {

        this.factory = factory;

    }


    /**
     * Return descriptive information about this Connector implementation.
     */
    public String getInfo() {

        return (info);

    }


     /**
      * Return the mapper.
      */
     public Mapper getMapper() {

         return (mapper);

     }
     
     
     /**
      * Set the {@link Mapper}.
      * @param mapper
      */
     public void setMapper(Mapper mapper){
         this.mapper = mapper;
     }     


    /**
     * Return the minimum number of processors to start at initialization.
     */
    public int getMinProcessors() {

        return (minProcessors);

    }


    /**
     * Set the minimum number of processors to start at initialization.
     *
     * @param minProcessors The new minimum processors
     */
    public void setMinProcessors(int minProcessors) {

        this.minProcessors = minProcessors;
        setProperty("minThreads", String.valueOf(minProcessors));

    }


    /**
     * Return the maximum number of processors allowed, or <0 for unlimited.
     */
    public int getMaxProcessors() {

        return (maxProcessors);

    }


    /**
     * Set the maximum number of processors allowed, or <0 for unlimited.
     *
     * @param maxProcessors The new maximum processors
     */
    public void setMaxProcessors(int maxProcessors) {

        this.maxProcessors = maxProcessors;
        setProperty("maxThreads", String.valueOf(maxProcessors));

    }


    /**
     * Return the maximum size of a POST which will be automatically
     * parsed by the container.
     */
    public int getMaxPostSize() {

        return (maxPostSize);

    }


    /**
     * Set the maximum size of a POST which will be automatically
     * parsed by the container.
     *
     * @param maxPostSize The new maximum size in bytes of a POST which will 
     * be automatically parsed by the container
     */
    public void setMaxPostSize(int maxPostSize) {

        this.maxPostSize = maxPostSize;
        setProperty("maxPostSize", String.valueOf(maxPostSize));
    }


    /**
     * Return the port number on which we listen for requests.
     */
    public int getPort() {

        return (this.port);

    }


    /**
     * Set the port number on which we listen for requests.
     *
     * @param port The new port number
     */
    public void setPort(int port) {

        this.port = port;
        setProperty("port", String.valueOf(port));

    }


    /**
     * Sets the name of this Connector.
     */
    public void setName(String name){
        this.name = name;
    }
    
    
    /**
     * Gets the name of this Connector.
     */
    public String getName(){
        return name;
    }


    /**
     * Sets the instance name for this Connector.
     * 
     * @param instanceName the instance name
     */
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }


    /**
     * Return the Coyote protocol handler in use.
     */
    public String getProtocol() {

        if ("com.sun.grizzly.tcp.http11.Http11Protocol".equals
            (getProtocolHandlerClassName())) {
            return "HTTP/1.1";
        } else if ("org.apache.jk.server.JkCoyoteHandler".equals
                   (getProtocolHandlerClassName())) {
            return "AJP/1.3";
        }
        return null;

    }


    /**
     * Set the Coyote protocol which will be used by the connector.
     *
     * @param protocol The Coyote protocol name
     */
    public void setProtocol(String protocol) {

        if (protocol.equals("HTTP/1.1")) {
            setProtocolHandlerClassName
                ("com.sun.grizzly.tcp.http11.Http11Protocol");
        } else if (protocol.equals("AJP/1.3")) {
            setProtocolHandlerClassName
                ("org.apache.jk.server.JkCoyoteHandler");
        } else {
            setProtocolHandlerClassName(null);
        }

    }


    /**
     * Return the class name of the Coyote protocol handler in use.
     */
    public String getProtocolHandlerClassName() {

        return (this.protocolHandlerClassName);

    }


    /**
     * Set the class name of the Coyote protocol handler which will be used
     * by the connector.
     *
     * @param protocolHandlerClassName The new class name
     */
    public void setProtocolHandlerClassName(String protocolHandlerClassName) {

        this.protocolHandlerClassName = protocolHandlerClassName;

    }


    /**
     * Return the protocol handler associated with the connector.
     */
    public ProtocolHandler getProtocolHandler() {

        return (this.protocolHandler);

    }


    /**
     * Return the proxy server name for this Connector.
     */
    public String getProxyName() {

        return (this.proxyName);

    }


    /**
     * Set the proxy server name for this Connector.
     *
     * @param proxyName The new proxy server name
     */
    public void setProxyName(String proxyName) {

        if(proxyName != null && proxyName.length() > 0) {
            this.proxyName = proxyName;
            setProperty("proxyName", proxyName);
        } else {
            this.proxyName = null;
            removeProperty("proxyName");
        }

    }


    /**
     * Return the proxy server port for this Connector.
     */
    public int getProxyPort() {

        return (this.proxyPort);

    }


    /**
     * Set the proxy server port for this Connector.
     *
     * @param proxyPort The new proxy server port
     */
    public void setProxyPort(int proxyPort) {

        this.proxyPort = proxyPort;
        setProperty("proxyPort", String.valueOf(proxyPort));

    }


    /**
     * Return the port number to which a request should be redirected if
     * it comes in on a non-SSL port and is subject to a security constraint
     * with a transport guarantee that requires SSL.
     */
    public int getRedirectPort() {

        return (this.redirectPort);

    }


    /**
     * Set the redirect port number.
     *
     * @param redirectPort The redirect port number (non-SSL to SSL)
     */
    public void setRedirectPort(int redirectPort) {

        this.redirectPort = redirectPort;
        setProperty("redirectPort", String.valueOf(redirectPort));

    }

    /**
     * Return the flag that specifies upload time-out behavior.
     */
    public boolean getDisableUploadTimeout() {
        return disableUploadTimeout;
    }

    /**
     * Set the flag to specify upload time-out behavior.
     *
     * @param isDisabled If <code>true</code>, then the <code>timeout</code>
     * parameter is ignored.  If <code>false</code>, then the
     * <code>timeout</code> parameter is used to control uploads.
     */
    public void setDisableUploadTimeout( boolean isDisabled ) {
        disableUploadTimeout = isDisabled;
        setProperty("disableUploadTimeout", String.valueOf(isDisabled));
    }

    /**
      * Return the maximum HTTP header size.
      */
    public int getMaxHttpHeaderSize() {
      return maxHttpHeaderSize;
    }
  
    /**
     * Set the maximum HTTP header size.
     */
    public void setMaxHttpHeaderSize(int size) {
        maxHttpHeaderSize = size;
        setProperty("maxHttpHeaderSize", String.valueOf(size));
    }

    /**
     * Return the Keep-Alive policy for the connection.
     */
    public boolean getKeepAlive() {
        return ((maxKeepAliveRequests != 0) && (maxKeepAliveRequests != 1));
    }

    /**
     * Set the keep-alive policy for this connection.
     */
    public void setKeepAlive(boolean keepAlive) {
        if (!keepAlive) {
            setMaxKeepAliveRequests(1);
        }
    }

    /**
     * Return the maximum number of Keep-Alive requests to honor 
     * per connection.
     */
    public int getMaxKeepAliveRequests() {
        return maxKeepAliveRequests;
    }

    /**
     * Set the maximum number of Keep-Alive requests to honor per connection.
     */
    public void setMaxKeepAliveRequests(int mkar) {
        maxKeepAliveRequests = mkar;
        setProperty("maxKeepAliveRequests", String.valueOf(mkar));
    }

    /**
     * Return the scheme that will be assigned to requests received
     * through this connector.  Default value is "http".
     */
    public String getScheme() {

        return (this.scheme);

    }


    /**
     * Set the scheme that will be assigned to requests received through
     * this connector.
     *
     * @param scheme The new scheme
     */
    public void setScheme(String scheme) {

        this.scheme = scheme;
        setProperty("scheme", scheme);

    }


    /**
     * Return the secure connection flag that will be assigned to requests
     * received through this connector.  Default value is "false".
     */
    public boolean getSecure() {

        return (this.secure);

    }


    /**
     * Set the secure connection flag that will be assigned to requests
     * received through this connector.
     *
     * @param secure The new secure connection flag
     */
    public void setSecure(boolean secure) {

        this.secure = secure;
        setProperty("secure", String.valueOf(secure));

    }

    // START SJSAS 6439313     
    /**
     * Return the blocking connection flag that will be assigned to requests
     * received through this connector.  Default value is "false".
     */
    public boolean getBlocking() {
        return (this.blocking);
    }


    /**
     * Set the blocking connection flag that will be assigned to requests
     * received through this connector.
     *
     * @param blocking The new blocking connection flag
     */
    public void setBlocking(boolean blocking) {

        this.blocking = blocking;
        setProperty("blocking", String.valueOf(blocking));

    }
    // END SJSAS 6439313     
    
    
    public boolean getTomcatAuthentication() {
        return tomcatAuthentication;
    }

    public void setTomcatAuthentication(boolean tomcatAuthentication) {
        this.tomcatAuthentication = tomcatAuthentication;
        setProperty("tomcatAuthentication", String.valueOf(tomcatAuthentication));
    }
    

    /**
     * Return the TCP no delay flag value.
     */
    public boolean getTcpNoDelay() {

        return (this.tcpNoDelay);

    }


    /**
     * Set the TCP no delay flag which will be set on the socket after
     * accepting a connection.
     *
     * @param tcpNoDelay The new TCP no delay flag
     */
    public void setTcpNoDelay(boolean tcpNoDelay) {

        this.tcpNoDelay = tcpNoDelay;
        setProperty("tcpNoDelay", String.valueOf(tcpNoDelay));

    }


    /**
     * Return the character encoding to be used for the URI.
     */
    public String getURIEncoding() {

        return (this.uriEncoding);

    }


    /**
     * Set the URI encoding to be used for the URI.
     *
     * @param uriEncoding The new URI character encoding.
     */
    public void setURIEncoding(String uriEncoding) {

        this.uriEncoding = uriEncoding;
        setProperty("uRIEncoding", uriEncoding);

    }


    /**
     * Indicates whether the generation of an X-Powered-By response header for
     * servlet-generated responses is enabled or disabled for this Connector.
     *
     * @return true if generation of X-Powered-By response header is enabled,
     * false otherwise
     */
    public boolean isXpoweredBy() {
        return xpoweredBy;
    }


    /**
     * Enables or disables the generation of an X-Powered-By header (with value
     * Servlet/2.4) for all servlet-generated responses returned by this
     * Connector.
     *
     * @param xpoweredBy true if generation of X-Powered-By response header is
     * to be enabled, false otherwise
     */
    public void setXpoweredBy(boolean xpoweredBy) {
        this.xpoweredBy = xpoweredBy;
        setProperty("xpoweredBy", String.valueOf(xpoweredBy));
    }


    // BEGIN S1AS 5000999
    /**
     * Sets the default host for this Connector.
     *
     * @param defaultHost The default host for this Connector
     */
    public void setDefaultHost(String defaultHost) {
        this.defaultHost = defaultHost;
    }

    /**
     * Gets the default host of this Connector.
     *
     * @return The default host of this Connector
     */
    public String getDefaultHost() {
        return this.defaultHost;
    }
    // END S1AS 5000999


    // START S1AS 6188932
    /**
     * Returns the value of this connector's authPassthroughEnabled flag.
     *
     * @return true if this connector is receiving its requests from
     * a trusted intermediate server, false otherwise
     */
    public boolean getAuthPassthroughEnabled() {
        return this.authPassthroughEnabled;
    }

    /**
     * Sets the value of this connector's authPassthroughEnabled flag.
     *
     * @param authPassthroughEnabled true if this connector is receiving its
     * requests from a trusted intermediate server, false otherwise
     */
    public void setAuthPassthroughEnabled(boolean authPassthroughEnabled) {
        this.authPassthroughEnabled = authPassthroughEnabled;
    }

    /**
     * Gets the ProxyHandler instance associated with this CoyoteConnector.
     * 
     * @return ProxyHandler instance associated with this CoyoteConnector,
     * or null
     */
    public ProxyHandler getProxyHandler() {
        return proxyHandler;
    }

    /**
     * Sets the ProxyHandler implementation for this CoyoteConnector to use.
     * 
     * @param proxyHandler ProxyHandler instance to use
     */
    public void setProxyHandler(ProxyHandler proxyHandler) {
        this.proxyHandler = proxyHandler;
    }
    // END S1AS 6188932


    // START SJSAS 6331392
    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
    // END SJSAS 6331392


    // --------------------------------------------------------- Public Methods


    /**
     * Create (or allocate) and return a Request object suitable for
     * specifying the contents of a Request to the responsible Container.
     */
    public org.apache.catalina.Request createRequest() {

        Request request = new Request();
        request.setConnector(this);
        return (request);
    }


    /**
     * Create (or allocate) and return a Response object suitable for
     * receiving the contents of a Response from the responsible Container.
     */
    public org.apache.catalina.Response createResponse() {

        Response response = new Response();
        response.setConnector(this);
        return (response);
    }


    // -------------------------------------------------- Monitoring Methods

    /**
     * Fires probe event related to the fact that the given request has
     * been entered the web container.
     *
     * @param request the request object
     * @param host the virtual server to which the request was mapped
     * @param context the Context to which the request was mapped
     */
    public void requestStartEvent(HttpServletRequest request, Host host,
            Context context) {
        // Deliberate noop
    };

    /**
     * Fires probe event related to the fact that the given request is about
     * to exit from the web container.
     *
     * @param request the request object
     * @param host the virtual server to which the request was mapped
     * @param context the Context to which the request was mapped
     * @param statusCode the response status code
     */
    public void requestEndEvent(HttpServletRequest request, Host host,
            Context context, int statusCode) {
        // Deliberate noop
    };


    // -------------------------------------------------- Private Methods


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    private void log(String message) {
        org.apache.catalina.Logger logger = container.getLogger();
        String localName = "Connector";
        if (logger != null) {
            logger.log(localName + " " + message);
        } else {
            log.info(localName + " " + message);
        }
    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     * @param t Associated exception
     */
    private void log(String message, Throwable t) {
        org.apache.catalina.Logger logger = container.getLogger();
        String localName = "Connector";
        if (logger != null) {
            logger.log(localName + " " + message, t,
                org.apache.catalina.Logger.WARNING);
        } else {
            log.log(Level.WARNING, localName + " " + message, t);
        }
    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }


    /**
     * Gets the (possibly empty) list of lifecycle listeners
     * associated with this Connector.
     */
    public List<LifecycleListener> findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to add
     */
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }


    protected ObjectName createObjectName(String domain, String type)
            throws MalformedObjectNameException {
        String encodedAddr = null;
        if (getAddress() != null) {
            encodedAddr = URLEncoder.encode(getProperty("address").toString());
        }
        String addSuffix = (getAddress() == null) ? "" : ",address="
                + encodedAddr;
        ObjectName _oname = new ObjectName(domain + ":type=" + type + ",port="
                + getPort() + addSuffix);
        return _oname;
    }

    /**
     * Initialize this connector (create ServerSocket here!)
     */
    public void initialize()
        throws LifecycleException
    {
        if (initialized) {
            log.info(sm.getString("coyoteConnector.alreadyInitialized"));
            return;
        }

        this.initialized = true;
                
        // If the Mapper is null, do not fail and creates one by default. 
        // This is the case when mod_jk is used.
        if (mapper == null){
            mapper = new Mapper();
        }
        
        mapperListener = new MapperListener(mapper);

        
        if( oname == null && (container instanceof StandardEngine)) {
            try {
                // we are loaded directly, via API - and no name was given to us
                StandardEngine cb=(StandardEngine)container;
                oname = createObjectName(domain, "Connector");
                Registry.getRegistry(null, null)
                    .registerComponent(this, oname, null);
                controller=oname;
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error registering connector ", e);
            }
            if (log.isLoggable(Level.FINE)) {
                log.fine("Creating name for connector " + oname);
            }
        }
        

        //START SJSAS 6363251
        // Initializa adapter
        //adapter = new CoyoteAdapter(this);
        //END SJSAS 6363251
        // Instantiate Adapter
        //START SJSAS 6363251
        if ( adapter == null){
            try {
                Class clazz = Class.forName(defaultClassName);
                Constructor constructor = 
                        clazz.getConstructor(new Class[]{Connector.class});
                adapter = 
                        (Adapter)constructor.newInstance(new Object[]{this});
            } catch (Exception e) {
                throw new LifecycleException
                    (sm.getString
                     ("coyoteConnector.adapterClassInstantiationFailed", e));
            } 
        }
        //END SJSAS 6363251

        // Instantiate protocol handler
        if ( protocolHandler == null ) {
            try {
                Class clazz = Class.forName(protocolHandlerClassName);

                // use no-arg constructor for JkCoyoteHandler
                if (protocolHandlerClassName.equals("org.apache.jk.server.JkCoyoteHandler")) {
                    protocolHandler = 
                            (com.sun.grizzly.tcp.ProtocolHandler) clazz.newInstance();
                    if (adapter instanceof CoyoteAdapter){
                        ((CoyoteAdapter)adapter).setCompatWithTomcat(true);
                    } else {
                        throw new IllegalStateException
                          (sm.getString
                            ("coyoteConnector.illegalAdapter",adapter));
                    }
                // START SJSAS 6439313
                } else {
                    Constructor constructor = 
                            clazz.getConstructor(new Class[]{Boolean.TYPE,
                                                             Boolean.TYPE,
                                                             String.class});

                    protocolHandler = (ProtocolHandler) 
                        constructor.newInstance(secure, blocking,
                                                selectorThreadImpl);
                // END SJSAS 6439313
                }
            } catch (Exception e) {
                throw new LifecycleException
                    (sm.getString
                     ("coyoteConnector.protocolHandlerInstantiationFailed", e));
            }
        }

        protocolHandler.setAdapter(adapter);

        IntrospectionUtils.setProperty(protocolHandler, "jkHome",
                                       System.getProperty("catalina.base"));

        // Configure secure socket factory
        // XXX For backwards compatibility only.
        if (factory instanceof CoyoteServerSocketFactory) {
            IntrospectionUtils.setProperty(protocolHandler, "secure",
                                           "" + true);
            CoyoteServerSocketFactory ssf =
                (CoyoteServerSocketFactory) factory;
            IntrospectionUtils.setProperty(protocolHandler, "algorithm",
                                           ssf.getAlgorithm());
            if (ssf.getClientAuth()) {
                IntrospectionUtils.setProperty(protocolHandler, "clientauth",
                                               "" + ssf.getClientAuth());
            }
            IntrospectionUtils.setProperty(protocolHandler, "keystore",
                                           ssf.getKeystoreFile());
            IntrospectionUtils.setProperty(protocolHandler, "randomfile",
                                           ssf.getRandomFile());
            IntrospectionUtils.setProperty(protocolHandler, "rootfile",
                                           ssf.getRootFile());

            IntrospectionUtils.setProperty(protocolHandler, "keypass",
                                           ssf.getKeystorePass());
            IntrospectionUtils.setProperty(protocolHandler, "keytype",
                                           ssf.getKeystoreType());
            IntrospectionUtils.setProperty(protocolHandler, "protocol",
                                           ssf.getProtocol());
            IntrospectionUtils.setProperty(protocolHandler, "protocols",
                                           ssf.getProtocols());
            IntrospectionUtils.setProperty(protocolHandler,
                                           "sSLImplementation",
                                           ssf.getSSLImplementation());
            IntrospectionUtils.setProperty(protocolHandler, "ciphers",
                                           ssf.getCiphers());
            IntrospectionUtils.setProperty(protocolHandler, "keyAlias",
                                           ssf.getKeyAlias());
        } else {
            IntrospectionUtils.setProperty(protocolHandler, "secure",
                                           "" + secure);
        }

        /* Set the configured properties.  This only sets the ones that were
         * explicitly configured.  Default values are the responsibility of
         * the protocolHandler.
         */
        Iterator keys = properties.keySet().iterator();
        while( keys.hasNext() ) {
            String name = (String)keys.next();
            String value = properties.get(name).toString();
	    String trnName = translateAttributeName(name);
            IntrospectionUtils.setProperty(protocolHandler, trnName, value);
        }
        

        try {
            protocolHandler.init();
        } catch (Exception e) {
            throw new LifecycleException
                (sm.getString
                 ("coyoteConnector.protocolHandlerInitializationFailed", e));
        }
    }

    /*
     * Translate the attribute name from the legacy Factory names to their
     * internal protocol names.
     */
    private String translateAttributeName(String name) {
	if ("clientAuth".equals(name)) {
	    return "clientauth";
	} else if ("keystoreFile".equals(name)) {
	    return "keystore";
	} else if ("randomFile".equals(name)) {
	    return "randomfile";
	} else if ("rootFile".equals(name)) {
	    return "rootfile";
	} else if ("keystorePass".equals(name)) {
	    return "keypass";
	} else if ("keystoreType".equals(name)) {
	    return "keytype";
	} else if ("sslProtocol".equals(name)) {
	    return "protocol";
	} else if ("sslProtocols".equals(name)) {
	    return "protocols";
	}
	return name;
    }


    /**
     * Begin processing requests via this Connector.
     *
     * @exception LifecycleException if a fatal startup error occurs
     */
    public void start() throws LifecycleException {
        if( !initialized )
            initialize();

        // Validate and update our current state
        if (started) {
            log.info(sm.getString("coyoteConnector.alreadyStarted"));
            return;
        }
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        // We can't register earlier - the JMX registration of this happens
        // in Server.start callback
        if ( this.oname != null ) {
            // We are registred - register the adapter as well.
            try {
                Registry.getRegistry(null, null).registerComponent
                    (protocolHandler, createObjectName(this.domain, "ProtocolHandler"), null);
            } catch (Exception ex) {
                log.log(Level.SEVERE,
                        sm.getString("coyoteConnector.protocolRegistrationFailed"),
                        ex);
            }
        } else {
            log.info(sm.getString
                     ("coyoteConnector.cannotRegisterProtocol"));
        }

        try {
            protocolHandler.start();
        } catch (Exception e) {
            throw new LifecycleException
                (sm.getString
                 ("coyoteConnector.protocolHandlerStartFailed", e));
        }

        if( this.domain != null ) {
            if (!"admin-listener".equals(getName())) {
                // See IT 8255
                mapper.removeContext(defaultHost, "");
                mapper.removeHost(defaultHost);
            }
            mapperListener.setDomain(domain);
            // BEGIN S1AS 5000999
            mapperListener.setPort(this.getPort());
            mapperListener.setDefaultHost(this.defaultHost);
            // END S1AS 5000999
            //mapperListener.setEngine( service.getContainer().getName() );
            mapperListener.setInstanceName(instanceName);
            mapperListener.init();
            try {
                ObjectName mapperOname = createObjectName(this.domain, "Mapper");
                Registry.getRegistry(null, null).registerComponent
                        (mapper, mapperOname, "Mapper");
            } catch (Exception ex) {
                log.log(Level.SEVERE,
                        sm.getString("coyoteConnector.protocolRegistrationFailed"),
                        ex);
            }
        }
    }


    /**
     * Terminate processing requests via this Connector.
     *
     * @exception LifecycleException if a fatal shutdown error occurs
     */
    public void stop() throws LifecycleException {

        // Validate and update our current state
        if (!started) {
            log.severe(sm.getString("coyoteConnector.notStarted"));
            return;

        }
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // START PWC 6393300
        if ( domain != null){
            try {
                Registry.getRegistry(null, null).unregisterComponent(
                    createObjectName(this.domain, "Mapper"));
                Registry.getRegistry(null, null).unregisterComponent(
                    createObjectName(this.domain, "ProtocolHandler"));
            } catch (MalformedObjectNameException e) {
                log.log(Level.INFO, "Error unregistering mapper ", e);
            }
        } 
        // END PWC 6393300

        try {
            protocolHandler.destroy();
        } catch (Exception e) {
            throw new LifecycleException
                (sm.getString
                 ("coyoteConnector.protocolHandlerDestroyFailed", e));
        }

    }

    // -------------------- Management methods --------------------

    public boolean getClientAuth() {
        boolean ret = false;

        String prop = (String) getProperty("clientauth");
        if (prop != null) {
            ret = Boolean.valueOf(prop).booleanValue();
        } else {	
            ServerSocketFactory factory = this.getFactory();
            if (factory instanceof CoyoteServerSocketFactory) {
                ret = ((CoyoteServerSocketFactory)factory).getClientAuth();
            }
        }

        return ret;
    }

    public void setClientAuth(boolean clientAuth) {
        setProperty("clientauth", String.valueOf(clientAuth));
        ServerSocketFactory factory = this.getFactory();
        if (factory instanceof CoyoteServerSocketFactory) {
            ((CoyoteServerSocketFactory)factory).setClientAuth(clientAuth);
        }
    }


    public String getKeystoreFile() {
        String ret = (String) getProperty("keystore");
        if (ret == null) {
            ServerSocketFactory factory = this.getFactory();
            if (factory instanceof CoyoteServerSocketFactory) {
                ret = ((CoyoteServerSocketFactory)factory).getKeystoreFile();
            }
        }

        return ret;
    }

    public void setKeystoreFile(String keystoreFile) {
        setProperty("keystore", keystoreFile);
        ServerSocketFactory factory = this.getFactory();
        if (factory instanceof CoyoteServerSocketFactory) {
            ((CoyoteServerSocketFactory)factory).setKeystoreFile(keystoreFile);
        }
    }

    /**
     * Return keystorePass
     */
    public String getKeystorePass() {
        String ret = (String) getProperty("keypass");
        if (ret == null) {
            ServerSocketFactory factory = getFactory();
            if (factory instanceof CoyoteServerSocketFactory ) {
                return ((CoyoteServerSocketFactory)factory).getKeystorePass();
            }
        }

        return ret;
    }

    /**
     * Set keystorePass
     */
    public void setKeystorePass(String keystorePass) {
        setProperty("keypass", keystorePass);
        ServerSocketFactory factory = getFactory();
        if( factory instanceof CoyoteServerSocketFactory ) {
            ((CoyoteServerSocketFactory)factory).setKeystorePass(keystorePass);
        }
    }

    /**
     * Gets the list of SSL cipher suites that are to be enabled
     *
     * @return Comma-separated list of SSL cipher suites, or null if all
     * cipher suites supported by the underlying SSL implementation are being
     * enabled
     */
    public String getCiphers() {
        String ret = (String) getProperty("ciphers");
        if (ret == null) {
            ServerSocketFactory factory = getFactory();
            if (factory instanceof CoyoteServerSocketFactory) {
                ret = ((CoyoteServerSocketFactory)factory).getCiphers();
            }
        }

        return ret;
    }

    /**
     * Sets the SSL cipher suites that are to be enabled.
     *
     * Only those SSL cipher suites that are actually supported by
     * the underlying SSL implementation will be enabled.
     *
     * @param ciphers Comma-separated list of SSL cipher suites
     */
    public void setCiphers(String ciphers) {
        setProperty("ciphers", ciphers);
        ServerSocketFactory factory = getFactory();
        if (factory instanceof CoyoteServerSocketFactory) {
            ((CoyoteServerSocketFactory)factory).setCiphers(ciphers);
        }
    }

    /**
     * Sets the number of seconds after which SSL sessions expire and are
     * removed from the SSL sessions cache.
     */
    public void setSSLSessionTimeout(String timeout) {
        setProperty("sslSessionTimeout", timeout);
    }

    /**
     * Sets the number of seconds after which SSL3 sessions expire and are
     * removed from the SSL sessions cache.
     */
    public void setSSL3SessionTimeout(String timeout) {
        setProperty("ssl3SessionTimeout", timeout);
    }

    /**
     * Sets the number of SSL sessions that may be cached
     */
    public void setSSLSessionCacheSize(String cacheSize) {
        setProperty("sslSessionCacheSize", cacheSize);
    }

    /**
     * Gets the alias name of the keypair and supporting certificate chain
     * used by this Connector to authenticate itself to SSL clients.
     *
     * @return The alias name of the keypair and supporting certificate chain
     */
    public String getKeyAlias() {
        String ret = (String) getProperty("keyAlias");
        if (ret == null) {
            ServerSocketFactory factory = getFactory();
            if (factory instanceof CoyoteServerSocketFactory) {
                ret = ((CoyoteServerSocketFactory)factory).getKeyAlias();
            }
        }

        return ret;
    }

    /**
     * Sets the alias name of the keypair and supporting certificate chain
     * used by this Connector to authenticate itself to SSL clients.
     *
     * @param alias The alias name of the keypair and supporting certificate
     * chain
     */
    public void setKeyAlias(String alias) {
        setProperty("keyAlias", alias);
        ServerSocketFactory factory = getFactory();
        if (factory instanceof CoyoteServerSocketFactory) {
            ((CoyoteServerSocketFactory)factory).setKeyAlias(alias);
        }
    }

    /**
     * Gets the SSL protocol variant to be used.
     *
     * @return SSL protocol variant
     */
    public String getSslProtocol() {
        String ret = (String) getProperty("sslProtocol");
        if (ret == null) {
            ServerSocketFactory factory = getFactory();
            if (factory instanceof CoyoteServerSocketFactory) {
                ret = ((CoyoteServerSocketFactory)factory).getProtocol();
            }
        }

        return ret;
    }

    /**
     * Sets the SSL protocol variant to be used.
     *
     * @param sslProtocol SSL protocol variant
     */
    public void setSslProtocol(String sslProtocol) {
        setProperty("sslProtocol", sslProtocol);
        ServerSocketFactory factory = getFactory();
        if (factory instanceof CoyoteServerSocketFactory) {
            ((CoyoteServerSocketFactory)factory).setProtocol(sslProtocol);
        }
    }

    /**
     * Gets the SSL protocol variants to be enabled.
     *
     * @return Comma-separated list of SSL protocol variants
     */
    public String getSslProtocols() {
        String ret = (String) getProperty("sslProtocols");
        if (ret == null) {
            ServerSocketFactory factory = getFactory();
            if (factory instanceof CoyoteServerSocketFactory) {
                ret = ((CoyoteServerSocketFactory)factory).getProtocols();
            }
        }

        return ret;
    }

    /**
     * Sets the SSL protocol variants to be enabled.
     *
     * @param sslProtocols Comma-separated list of SSL protocol variants
     */
    public void setSslProtocols(String sslProtocols) {
        setProperty("sslProtocols", sslProtocols);
        ServerSocketFactory factory = getFactory();
        if (factory instanceof CoyoteServerSocketFactory) {
            ((CoyoteServerSocketFactory)factory).setProtocols(sslProtocols);
        }
    }
    
    // START OF SJSAS 8.1 PE 6191830
    /**
     * Get the underlying WebContainer certificate for the request
     */
    public X509Certificate[] getCertificates(org.apache.catalina.Request request) {
        
        Request cRequest = null;
        if (request instanceof Request) {
            cRequest=(Request) request;
        } else {
            return null;
        }
        
        X509Certificate certs[] = (X509Certificate[])
        cRequest.getAttribute(Globals.CERTIFICATES_ATTR);
        if ((certs == null) || (certs.length < 1)) {
            certs = (X509Certificate[])
            cRequest.getAttribute(Globals.SSL_CERTIFICATE_ATTR);
        }
        return certs;
    }    
    // END OF SJSAS 8.1 PE 6191830


    // -------------------- JMX registration  --------------------
    protected String domain;
    protected ObjectName oname;
    protected MBeanServer mserver;
    ObjectName controller;

    public ObjectName getController() {
        return controller;
    }

    public void setController(ObjectName controller) {
        this.controller = controller;
    }

    public ObjectName getObjectName() {
        return oname;
    }

    public String getDomain() {
        return domain;
    }

    /**
     * Set the domain of this object.
     */
    public void setDomain(String domain){
        this.domain = domain;
    }
    
    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name) throws Exception {
        oname=name;
        mserver=server;
        domain=name.getDomain();
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
        try {
            if( started ) {
                stop();
            }
        } catch( Throwable t ) {
            log.log(Level.SEVERE, "Unregistering - can't stop", t);
        }
    }
    
    private void findContainer() {
        try {
            // Register to the service
            ObjectName parentName=new ObjectName( domain + ":" +
                    "type=Service");
            
            if (log.isLoggable(Level.FINE)) {
                log.fine("Adding to " + parentName );
            }
            if( mserver.isRegistered(parentName )) {
                mserver.invoke(parentName, "addConnector", new Object[] { this },
                        new String[] {"org.apache.catalina.Connector"});
                // As a side effect we'll get the container field set
                // Also initialize will be called
                //return;
            }
            // XXX Go directly to the Engine
            // initialize(); - is called by addConnector
            ObjectName engName=new ObjectName( domain + ":" + "type=Engine");
            if( mserver.isRegistered(engName )) {
                Object obj=mserver.getAttribute(engName, "managedResource");
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Found engine " + obj + " " + obj.getClass());
                }
                container=(Container)obj;
                
                // Internal initialize - we now have the Engine
                initialize();
                
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Initialized");
                }
                // As a side effect we'll get the container field set
                // Also initialize will be called
                return;
            }
        } catch( Exception ex ) {
            log.log(Level.SEVERE, "Error finding container", ex);
        }
    }

    public void init() throws Exception {

        if( this.getService() != null ) {
            if (log.isLoggable(Level.FINE)) {
                log.fine( "Already configured" );
            }
            return;
        }
        if( container==null ) {
            findContainer();
        }
    }

    public void destroy() throws Exception {
        if( oname!=null && controller==oname ) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Unregister itself " + oname );
            }
            Registry.getRegistry(null, null).unregisterComponent(oname);
        }
        if( getService() == null)
            return;
        getService().removeConnector(this);
    }

    
    // START SJSAS 6363251
    /**
     * Set the <code>Adapter</code> used by this connector.
     */
    public void setAdapter(Adapter adapter){
        this.adapter = adapter;
    }
    
    
    /**
     * Get the <code>Adapter</code> used by this connector.
     */    
    public Adapter getAdapter(){
        return adapter;
    }
 
    
    /**
     * Set the <code>ProtocolHandler</code> used by this connector.
     */
    public void setProtocolHandler(ProtocolHandler protocolHandler){
        this.protocolHandler = protocolHandler;
    }
    // END SJSAS 6363251

    
    /**
     * Get the underlying <code>SelectorThread</code> implementation, null if 
     * the default is used.
     */
    public String getSelectorThreadImpl() {
        return selectorThreadImpl;
    }

    
    /**
     * Set the underlying <code>SelectorThread</code> implementation  
     */   
    public void setSelectorThreadImpl(String selectorThreadImpl) {
        this.selectorThreadImpl = selectorThreadImpl;
    } 
}
