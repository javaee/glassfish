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

package com.sun.enterprise.v3.services.impl;


import java.text.MessageFormat;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;
import java.util.ResourceBundle;

import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.ConnectionPool;
import com.sun.enterprise.config.serverbeans.KeepAlive;
import com.sun.enterprise.config.serverbeans.HttpFileCache;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpProtocol;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.Property; 
import com.sun.enterprise.config.serverbeans.RequestProcessing;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.config.serverbeans.Ssl;
import com.sun.grizzly.Controller;
import com.sun.logging.LogDomains;

/**
 * Utility class that creates Grizzly's SelectorThread instance based on 
 * the HttpService of domain.xml
 * 
 * @author Jeanfrancois Arcand
 */
public class GrizzlyHttpEmbed {
    
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    protected static final Logger _logger = LogDomains.getLogger(
            LogDomains.WEB_LOGGER);
    
    /**
     * The resource bundle containing the message strings for _logger.
     */
    protected static final ResourceBundle _rb = _logger.getResourceBundle();
        
    /**
     * Allow disabling accessLog mechanism
     */
    private boolean globalAccessLoggingEnabled = true;
    
   /**
    * AccessLog buffer size for storing logs.
    */
   private String globalAccessLogBufferSize = null;
   
   /**
    * AccessLog interval before the valve flush its buffer to the disk.
    */
    private String globalAccessLogWriteInterval = null; 
       
    public static final String ACCESS_LOG_PROPERTY = "accesslog";

    public static final String ACCESS_LOG_BUFFER_SIZE_PROPERTY =
        "accessLogBufferSize";

    public static final String ACCESS_LOG_WRITE_INTERVAL_PROPERTY =
        "accessLogWriteInterval";

    public static final String ACCESS_LOGGING_ENABLED = "accessLoggingEnabled";

    public static final String SSO_ENABLED = "sso-enabled";

    // TODO: Must get the information from domain.xml Config objects.
    public static GrizzlyServiceListener createListener(HttpService httpService,
            int port, Controller controller){
        
        System.setProperty("product.name", "GlassFish/v3");      
        GrizzlyServiceListener grizzlyServiceListener 
                = new GrizzlyServiceListener();
	//TODO: Configure via domain.xml
        //grizzlyServiceListener.setController(controller);
        grizzlyServiceListener.setPort(port);   
        grizzlyServiceListener.setMaxProcessorWorkerThreads(5);
        GrizzlyServiceListener.setWebAppRootPath(
                System.getProperty("com.sun.aas.instanceRoot") + "/docroot");
        
        for (HttpListener httpListener : httpService.getHttpListener()) {
            if (!Boolean.getBoolean(httpListener.getEnabled())) {
                continue;
            }
            boolean isSecure = Boolean.getBoolean(httpListener.getSecurityEnabled());
            configureConnector(grizzlyServiceListener,httpListener,isSecure,httpService);           
        }

        return grizzlyServiceListener;
        
    }
    
    
    /*
     * Configures the given connector.
     *
     * @param connector The connector to configure
     * @param httpListener The http-listener that corresponds to the given
     * connector
     * @param isSecure true if the connector is security-enabled, false
     * otherwise
     * @param httpServiceProps The http-service properties
     */
    private static void configureConnector(GrizzlyServiceListener grizzlyServiceListener,
                                    HttpListener httpListener,
                                    boolean isSecure,
                                    HttpService httpService) {

        grizzlyServiceListener.setName(httpListener.getId());

        grizzlyServiceListener.setLogger(_logger);         
        
        configureKeepAlive(grizzlyServiceListener, httpService.getKeepAlive());
        configureHttpProtocol(grizzlyServiceListener, httpService.getHttpProtocol());     
        configureRequestProcessing(httpService.getRequestProcessing(),grizzlyServiceListener);
        configureFileCache(grizzlyServiceListener, httpService.getHttpFileCache());
        
        // TODO : not yet available missing APIs 
        // default-virtual-server
        //grizzlyServiceListener.setDefaultHost(httpListener.getDefaultVirtualServer());
        // xpoweredBy
        //grizzlyServiceListener.setXpoweredBy(Boolean.getBoolean(httpListener.getXpoweredBy()));
        
        // server-name (may contain scheme and colon-separated port number)
        /*String serverName = httpListener.getServerName();
        if (serverName != null && serverName.length() > 0) {
            // Ignore scheme, which was required for webcore issued redirects
            // in 8.x EE
            if (serverName.startsWith("http://")) {
                serverName = serverName.substring("http://".length());
            } else if (serverName.startsWith("https://")) {
                serverName = serverName.substring("https://".length());
            }
            int index = serverName.indexOf(':');
            if (index != -1) {
                grizzlyServiceListener.setProxyName(serverName.substring(0, index).trim());
                String serverPort = serverName.substring(index+1).trim();
                if (serverPort.length() > 0) {
                    try {
                        grizzlyServiceListener.setProxyPort(Integer.parseInt(serverPort));
                    } catch (NumberFormatException nfe) {
                        _logger.log(Level.SEVERE,
                            "pewebcontainer.invalid_proxy_port",
                            new Object[] { serverPort, httpListener.getId() });
		    }
                }
            } else {
                grizzlyServiceListener.setProxyName(serverName);
            }
        }

        boolean blockingEnabled = Boolean.valueOf(
                        httpListener.getBlockingEnabled());
        if (blockingEnabled){
            grizzlyServiceListener.setBlocking(blockingEnabled);
        }

        // redirect-port
        String redirectPort = httpListener.getRedirectPort();
        if (redirectPort != null && !redirectPort.equals("")) {
            try {
                grizzlyServiceListener.setRedirectPort(Integer.parseInt(redirectPort));
            } catch (NumberFormatException nfe) {
                _logger.log(Level.WARNING,
                    "pewebcontainer.invalid_redirect_port",
                    new Object[] {
                        redirectPort,
                        httpListener.getId(),
                        Integer.toString(grizzlyServiceListener.getRedirectPort()) });
            }  
        } else {
            grizzlyServiceListener.setRedirectPort(-1);
        }*/

        // acceptor-threads
        String acceptorThreads = httpListener.getAcceptorThreads();
        if (acceptorThreads != null) {
            try {
                grizzlyServiceListener.setSelectorReadThreadsCount
                    (Integer.parseInt(acceptorThreads));
            } catch (NumberFormatException nfe) {
                _logger.log(Level.WARNING,
                    "pewebcontainer.invalid_acceptor_threads",
                    new Object[] {
                        acceptorThreads,
                        httpListener.getId(),
                        Integer.toString(grizzlyServiceListener.getMaxProcessorWorkerThreads()) });
            }  
        }
        /*
        // Configure Connector with keystore password and location
        if (isSecure) {
            configureConnectorKeysAndCerts(grizzlyServiceListener);
        }
        
        configureHttpServiceProperties(httpService,grizzlyServiceListener);      

        // Override http-service property if defined.
        configureHttpListenerProperties(httpListener,grizzlyServiceListener);
         */
    }      
    
    
    // TODO : not yet available missing APIs 
    /*
     * Configures the SSL properties on the given PECoyoteConnector from the
     * SSL config of the given HTTP listener.
     *
     * @param connector PECoyoteConnector to configure
     * @param httpListener HTTP listener whose SSL config to use
     *
    private void configureSSL(GrizzlyServiceListener connector,
                              HttpListener httpListener) {

        Ssl sslConfig = httpListener.getSsl();
        if (sslConfig == null) {
            return;
        }

        // client-auth
        if (Boolean.getBoolean(sslConfig.getClientAuthEnabled())) {
            connector.setClientAuth(true);
        }

        // ssl protocol variants
        StringBuffer sslProtocolsBuf = new StringBuffer();
        boolean needComma = false;
        if (Boolean.getBoolean(sslConfig.getSsl2Enabled())) {
            sslProtocolsBuf.append("SSLv2");
            needComma = true;
        }
        if (Boolean.getBoolean(sslConfig.getSsl3Enabled())) {
            if (needComma) {
                sslProtocolsBuf.append(", ");
            } else {
                needComma = true;
            }
            sslProtocolsBuf.append("SSLv3");
        }
        if (Boolean.getBoolean(sslConfig.getTlsEnabled())) {
            if (needComma) {
                sslProtocolsBuf.append(", ");
            }
            sslProtocolsBuf.append("TLSv1");
        }
        if (Boolean.getBoolean(sslConfig.getSsl3Enabled()) || Boolean.getBoolean(sslConfig.getTlsEnabled())) {
            sslProtocolsBuf.append(", SSLv2Hello");
        }

        if (sslProtocolsBuf.length() == 0) {
            _logger.log(Level.WARNING,
                        "pewebcontainer.all_ssl_protocols_disabled",
                        httpListener.getId());
        } else {
            connector.setSslProtocols(sslProtocolsBuf.toString());
        }

        // cert-nickname
        String certNickname = sslConfig.getCertNickname();
        if (certNickname != null && certNickname.length() > 0) {
            connector.setKeyAlias(sslConfig.getCertNickname());
        }

        // ssl3-tls-ciphers
        String ciphers = sslConfig.getSsl3TlsCiphers();
        if (ciphers != null) {
            String jsseCiphers = getJSSECiphers(ciphers);
            if (jsseCiphers == null) {
                _logger.log(Level.WARNING,
                            "pewebcontainer.all_ciphers_disabled",
                            httpListener.getId());
            } else {
                connector.setCiphers(jsseCiphers);
            }
        }            
    }*/
    
    
    /*
     * Configures the keep-alive properties on the given PECoyoteConnector
     * from the given keep-alive config.
     *
     * @param connector PECoyoteConnector to configure
     * @param keepAlive Keep-alive config to use
     */
    private static void configureKeepAlive(GrizzlyServiceListener connector,
                                    KeepAlive keepAlive) {

        // timeout-in-seconds, default is 60 as per sun-domain_1_1.dtd
        int timeoutInSeconds = 60;

        // max-connections, default is 256 as per sun-domain_1_1.dtd
        int maxConnections = 256;

        // thread-count, default is 1 as per sun-domain_1_1.dtd
        int threadCount = 1;

        if (keepAlive != null) {
            // timeout-in-seconds
            try {
	        timeoutInSeconds = Integer.parseInt(
                                keepAlive.getTimeoutInSeconds());
            } catch (NumberFormatException ex) {
                String msg = _rb.getString(
                    "pewebcontainer.invalidKeepAliveTimeout");
                msg = MessageFormat.format(
                    msg,
                    new Object[] { keepAlive.getTimeoutInSeconds(),
                                   Integer.toString(timeoutInSeconds)});
                _logger.log(Level.WARNING, msg, ex);
            }

            // max-connections
            try {
	        maxConnections = Integer.parseInt(
                                keepAlive.getMaxConnections());
            } catch (NumberFormatException ex) {
                String msg = _rb.getString(
                    "pewebcontainer.invalidKeepAliveMaxConnections");
                msg = MessageFormat.format(
                    msg,
                    new Object[] { keepAlive.getMaxConnections(),
                                   Integer.toString(maxConnections)});
                _logger.log(Level.WARNING, msg, ex);
            }

            // thread-count
            try {
	        threadCount = Integer.parseInt(keepAlive.getThreadCount());
            } catch (NumberFormatException ex) {
                String msg = _rb.getString(
                    "pewebcontainer.invalidKeepAliveThreadCount");
                msg = MessageFormat.format(
                    msg,
                    new Object[] { keepAlive.getThreadCount(),
                                   Integer.toString(threadCount)});
                _logger.log(Level.WARNING, msg, ex);
            }
        }
        
        connector.setKeepAliveTimeoutInSeconds(timeoutInSeconds);
        connector.setMaxKeepAliveRequests(maxConnections);
        connector.setKeepAliveThreadCount(threadCount);
    }
    
    /*
     * Configures the given HTTP connector with the given http-protocol
     * config.
     *
     * @param connector HTTP connector to configure
     * @param httpProtocol http-protocol config to use
     */
    private static void configureHttpProtocol(GrizzlyServiceListener connector,
                                       HttpProtocol httpProtocol) {
    
        if (httpProtocol == null) {
            return;
        }

        //connector.setEnableLookups(httpProtocol.isDnsLookupEnabled());
        connector.setForcedRequestType(httpProtocol.getForcedType());
        connector.setDefaultResponseType(httpProtocol.getDefaultType());
    }
    
        
    /**
     * Configure the Grizzly FileCache mechanism
     */
    private static void configureFileCache(GrizzlyServiceListener connector,
                                    HttpFileCache httpFileCache){
        if ( httpFileCache == null ) return;
        
        /*catalinaCachingAllowed = !(httpFileCache.isGloballyEnabled() &&
                ConfigBeansUtilities.toBoolean(httpFileCache.getFileCachingEnabled()));
        
        connector.setFileCacheEnabled(httpFileCache.isGloballyEnabled());   */      
        connector.setLargeFileCacheEnabled(
            ConfigBeansUtilities.toBoolean(httpFileCache.getFileCachingEnabled()));
        
        if (httpFileCache.getMaxAgeInSeconds() != null){
            connector.setSecondsMaxAge(
                Integer.parseInt(httpFileCache.getMaxAgeInSeconds()));
        }
        
        if (httpFileCache.getMaxFilesCount() != null){
            connector.setMaxCacheEntries(
                Integer.parseInt(httpFileCache.getMaxFilesCount()));
        }
        
        if (httpFileCache.getSmallFileSizeLimitInBytes() != null){
            connector.setMinEntrySize(
                Integer.parseInt(httpFileCache.getSmallFileSizeLimitInBytes()));
        }
        
        if (httpFileCache.getMediumFileSizeLimitInBytes() != null){
            connector.setMaxEntrySize(
                Integer.parseInt(httpFileCache.getMediumFileSizeLimitInBytes()));
        }
        
        if (httpFileCache.getMediumFileSpaceInBytes() != null){
            connector.setMaxLargeCacheSize(
                Integer.parseInt(httpFileCache.getMediumFileSpaceInBytes()));
        }
        
        if (httpFileCache.getSmallFileSpaceInBytes() != null){
            connector.setMaxSmallCacheSize(
                Integer.parseInt(httpFileCache.getSmallFileSpaceInBytes())); 
        }
    }    
    
    /**
     * Configures an HTTP connector with the given request-processing
     * config.
     *
     * @param RequestProcessing http-service config to use
     * @param connector the connector used.
     */
    protected static void configureRequestProcessing(RequestProcessing rp, 
                                              GrizzlyServiceListener connector){
        if (rp == null) return;

        try{
            connector.setMaxProcessorWorkerThreads(
                    Integer.parseInt(rp.getThreadCount()));
            connector.setMinWorkerThreads(
                    Integer.parseInt(rp.getInitialThreadCount()));
            connector.setThreadsTimeout(
                    Integer.parseInt(rp.getRequestTimeoutInSeconds())); 
            connector.setThreadsIncrement(
                    Integer.parseInt(rp.getThreadIncrement()));
            connector.setMaxHttpHeaderSize(
                   Integer.parseInt(rp.getHeaderBufferLengthInBytes()));
        } catch (NumberFormatException ex){
            _logger.log(Level.WARNING, " Invalid request-processing attribute", 
                    ex);                      
        }             
    }
    
    // TODO : not yet available missing APIs 
    /**
     * Configure http-listener properties
     *
    public void configureHttpListenerProperties(HttpListener httpListener,
                                                GrizzlyServiceListener connector){
        // Configure Connector with <http-service> properties
        for (Property httpListenerProp  : httpListener.getProperty()) { 
            String propName = httpListenerProp.getName();
            String propValue = httpListenerProp.getValue();
            if (!configureHttpListenerProperty(propName,
                                               propValue,
                                               connector)){
                _logger.log(Level.WARNING,
                    "pewebcontainer.invalid_http_listener_property",
                    propName);                    
            }
        }    
    } */       
       
    
    // TODO : not yet available missing APIs 
    /**
     * Configure http-listener property.
     * return true if the property exists and has been set.
     *
    protected boolean configureHttpListenerProperty(
                                            String propName, 
                                            String propValue,
                                            GrizzlyServiceListener connector)
                                            throws NumberFormatException {
                                                        
        if ("bufferSize".equals(propName)) {
            connector.setBufferSize(Integer.parseInt(propValue)); 
            return true; 
        } else if ("recycle-objects".equals(propName)) {
            connector
                .setRecycleObjects(ConfigBeansUtilities.toBoolean(propValue));
            return true;
        } else if ("reader-threads".equals(propName)) {
            connector
                .setMaxReadWorkerThreads(Integer.parseInt(propValue));
            return true;            
        } else if ("acceptor-queue-length".equals(propName)) {
            connector
                .setMinAcceptQueueLength(Integer.parseInt(propValue));
            return true;            
        } else if ("reader-queue-length".equals(propName)) {
            connector
                .setMinReadQueueLength(Integer.parseInt(propValue));
            return true;            
        } else if ("use-nio-direct-bytebuffer".equals(propName)) {
            connector
                .setUseDirectByteBuffer(ConfigBeansUtilities.toBoolean(propValue));
            return true;   
        } else if ("maxKeepAliveRequests".equals(propName)) {
            connector
                .setMaxKeepAliveRequests(Integer.parseInt(propValue));
            return true;           
        } else if ("reader-selectors".equals(propName)) {
            connector
                .setSelectorReadThreadsCount(Integer.parseInt(propValue));
            return true;
        } else if ("authPassthroughEnabled".equals(propName)) {
            connector.setAuthPassthroughEnabled(
                                        ConfigBeansUtilities.toBoolean(propValue));
            return true;
        } else if ("maxPostSize".equals(propName)) {
            connector.setMaxPostSize(Integer.parseInt(propValue));
            return true;
        } else if ("compression".equals(propName)) {
            connector.setCompression("compression",propValue);
            return true;
        } else if ("compressableMimeType".equals(propName)) {
            connector.setCompressableMimeType("compressableMimeType",propValue);
            return true;       
        } else if ("noCompressionUserAgents".equals(propName)) {
            connector.setNoCompressionUserAgent("noCompressionUserAgents",propValue);
            return true;   
        } else if ("compressionMinSize".equals(propName)) {
            connector.setCompressionMinSize("compressionMinSize",propValue);
            return true;             
        } else if ("restrictedUserAgents".equals(propName)) {
            connector.setRestrictedUserAgents("restrictedUserAgents",propValue);
            return true;             
        } else if ("blocking".equals(propName)) {
            connector.setBlocking(ConfigBeansUtilities.toBoolean(propValue));
            return true;             
        } else if ("selectorThreadImpl".equals(propName)) {
            connector.setSelectorThreadImpl(propValue);
            return true;             
        } else if ("cometSupport".equals(propName)) {
            connector.setCometSupport(propName,ConfigBeansUtilities.toBoolean(propValue));
            return true;     
        } else if ("rcmSupport".equals(propName)) {
            connector.setRcmSupport(propName,ConfigBeansUtilities.toBoolean(propValue));
            return true;    
        } else if ("connectionUploadTimeout".equals(propName)) {
            connector.setConnectionUploadTimeout(Integer.parseInt(propValue));
            return true;            
        } else if ("disableUploadTimeout".equals(propName)) {
            connector.setDisableUploadTimeout(ConfigBeansUtilities.toBoolean(propValue));
            return true;             
        } else if ("proxiedProtocols".equals(propName)) {
            connector.setProxiedProtocols(propName,propValue);
            return true;              
        } else if ("proxyHandler".equals(propName)) {
            setProxyHandler(connector, propValue);
            return true;
        } else if ("uriEncoding".equals(propName)) {
            connector.setURIEncoding(propValue);
            return true;
        } else if ("chunkingDisabled".equals(propName)
                || "chunking-disabled".equals(propName)) {
            connector.setChunkingDisabled(ConfigBeansUtilities.toBoolean(propValue));
            return true;
        } else if ("crlFile".equals(propName)) {
            connector.setCrlFile(propValue);
            return true;
        } else if ("trustAlgorithm".equals(propName)) {
            connector.setTrustAlgorithm(propValue);
            return true;
        } else if ("trustMaxCertLength".equals(propName)) {
            connector.setTrustMaxCertLength(propValue);
            return true;
        } else {
            return false;
        }   
    }   
     */
    
    // TODO : not yet available missing APIs 
    /**
     * Configure http-service properties.
     *
    public void configureHttpServiceProperties(HttpService httpService,
                                               GrizzlyServiceListener connector){
        // Configure Connector with <http-service> properties
        List<Property> httpServiceProps = httpService.getProperty();

        // Set default ProxyHandler impl, may be overriden by
        // proxyHandler property
        connector.setProxyHandler(new ProxyHandlerImpl());

        if (httpServiceProps != null) {
            for (Property httpServiceProp : httpServiceProps) {
                String propName = httpServiceProp.getName();
                String propValue = httpServiceProp.getValue();
                               
                if (configureHttpListenerProperty(propName,
                                                  propValue, 
                                                  connector)){
                    continue;
                }
                
                if ("connectionTimeout".equals(propName)) {
                    connector.setConnectionTimeout(
                                                Integer.parseInt(propValue));
                } else if ("tcpNoDelay".equals(propName)) {
                    connector.setTcpNoDelay(ConfigBeansUtilities.toBoolean(propValue));
                } else if ("traceEnabled".equals(propName)) {
                    connector.setAllowTrace(ConfigBeansUtilities.toBoolean(propValue));
                } else if (ACCESS_LOGGING_ENABLED.equals(propName)) {
                    globalAccessLoggingEnabled = ConfigBeansUtilities.toBoolean(propValue);
                } else if (ACCESS_LOG_WRITE_INTERVAL_PROPERTY.equals(
                                propName)) {
                    globalAccessLogWriteInterval = propValue;
                } else if (ACCESS_LOG_BUFFER_SIZE_PROPERTY.equals(
                                propName)) {
                    globalAccessLogBufferSize = propValue;
                } else if ("authPassthroughEnabled".equals(propName)) {
                    connector.setAuthPassthroughEnabled(
                                    ConfigBeansUtilities.toBoolean(propValue));
                } else if ("ssl-session-timeout".equals(propName)) {
                    connector.setSSLSessionTimeout(propValue);
                } else if ("ssl3-session-timeout".equals(propName)) {
                    connector.setSSL3SessionTimeout(propValue);
                } else if ("ssl-cache-entries".equals(propName)) {
                    connector.setSSLSessionCacheSize(propValue);
                } else if ("proxyHandler".equals(propName)) {
                    setProxyHandler(connector, propValue);
                } else if (SSO_ENABLED.equals(propName)) {
                    globalSSOEnabled = ConfigBeansUtilities.toBoolean(propValue);
                } else {
                    _logger.log(Level.WARNING,
                        "pewebcontainer.invalid_http_service_property",
                        httpServiceProp.getName());
                }
            }
        }    
    }*/
    
}
