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

import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ResourceBundle;

import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.ConnectionPool;
import com.sun.enterprise.config.serverbeans.KeepAlive;
import com.sun.enterprise.config.serverbeans.HttpFileCache;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpProtocol;
import com.sun.enterprise.config.serverbeans.HttpService;
import org.glassfish.api.admin.config.Property; 
import com.sun.enterprise.config.serverbeans.RequestProcessing;
import com.sun.enterprise.config.serverbeans.Ssl;
import com.sun.grizzly.Controller;
import com.sun.grizzly.arp.DefaultAsyncHandler;
import com.sun.grizzly.arp.AsyncHandler;
import com.sun.grizzly.arp.AsyncFilter;
import com.sun.logging.LogDomains;
import java.util.LinkedList;

import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.component.Habitat;

/**
 * Utility class that creates Grizzly's SelectorThread instance based on 
 * the HttpService of domain.xml
 * 
 * @author Jeanfrancois Arcand
 * @author Alexey Stashok
 */
public class GrizzlyListenerConfigurator {
    
    
    /**
     * The logger to use for logging messages.
     */
    protected static final Logger logger = LogDomains.getLogger(
            GrizzlyListenerConfigurator.class,LogDomains.CORE_LOGGER);
    
    /**
     * The resource bundle containing the message strings for logger.
     */
    protected static final ResourceBundle _rb = logger.getResourceBundle();
    
    /*
     * Configures the given grizzlyListener.
     *
     * @param grizzlyListener The grizzlyListener to configure
     * @param httpListener The http-listener that corresponds to the given
     * grizzlyListener
     * @param isSecure true if the grizzlyListener is security-enabled, false
     * otherwise
     * @param httpServiceProps The http-service properties
     * @param isWebProfile if true - just HTTP protocol is supported on port,
     *        false - port unification will be activated
     */
    // TODO: Must get the information from domain.xml Config objects.
    // TODO: Pending Grizzly issue 54
    public static void configure(
            GrizzlyServiceListener grizzlyListener, HttpService httpService,
            HttpListener httpListener, int port, InetAddress address,
            Controller controller,
            boolean isWebProfile){
        
        System.setProperty("product.name", "GlassFish/v3");      

        //TODO: Configure via domain.xml
        //grizzlyListener.setController(controller);
        grizzlyListener.setPort(port); 
        grizzlyListener.setAddress(address); 
        
        // TODO: This is not the right way to do.
        GrizzlyEmbeddedHttp.setWebAppRootPath(
                System.getProperty("com.sun.aas.instanceRoot") + "/docroot");
        
        boolean isSecure = Boolean.parseBoolean(httpListener.getSecurityEnabled());
    
    
        //TODO: Use the grizzly-config name.
        grizzlyListener.initializeEmbeddedHttp(isSecure);
        grizzlyListener.setName("v3-" + port);
        
        GrizzlyEmbeddedHttp grizzlyEmbeddedHttp = grizzlyListener.getEmbeddedHttp();
        
        if (isSecure) {
            configureSSL(grizzlyEmbeddedHttp, httpService, httpListener);
        }

        GrizzlyEmbeddedHttp.setLogger(logger);                 

        configureHttpServiceProperties(grizzlyEmbeddedHttp, httpService);      

        // Override http-service property if defined.
        configureHttpListenerProperties(grizzlyEmbeddedHttp, httpListener);
        
        configureKeepAlive(grizzlyEmbeddedHttp, httpService.getKeepAlive());
        configureHttpProtocol(grizzlyEmbeddedHttp, httpService.getHttpProtocol());     
        configureRequestProcessing(grizzlyEmbeddedHttp, httpService.getRequestProcessing());
        configureFileCache(grizzlyEmbeddedHttp, httpService.getHttpFileCache());
        configureConnectionPool(grizzlyEmbeddedHttp, httpService.getConnectionPool());

        // acceptor-threads
        String acceptorThreads = httpListener.getAcceptorThreads();
        if (acceptorThreads != null) {
            try {
                // Acceptor-Thread needs to be > 1 to be used by Grizzly
                int readController = Integer.parseInt(acceptorThreads) -1;
                if (readController > 0){
                    grizzlyEmbeddedHttp.setSelectorReadThreadsCount
                        (readController);
                }
            } catch (NumberFormatException nfe) {
                logger.log(Level.WARNING,
                    "pewebcontainer.invalid_acceptor_threads",
                    new Object[] {
                        acceptorThreads,
                        httpListener.getId(),
                        Integer.toString(grizzlyEmbeddedHttp.getMaxThreads()) });
            }  
        }

        if((Boolean.valueOf(System.getProperty("v3.grizzly.cometSupport","false")))
                && !httpListener.getId().equalsIgnoreCase("admin-listener")){       
            configureComet(grizzlyEmbeddedHttp);       
        }

        if (!isWebProfile) {
            grizzlyListener.configurePortUnification();
        }
    }      
    
    
    // TODO : not yet available missing APIs 
    /*
     * Configures the SSL properties on the given PECoyoteConnector from the
     * SSL config of the given HTTP listener.
     *
     * @param grizzlyListener PECoyoteConnector to configure
     * @param httpListener HTTP listener whose SSL config to use
     */
    private static boolean configureSSL(GrizzlyEmbeddedHttp grizzlyEmbeddedHttp,
                              HttpService httpService, HttpListener httpListener) {
        Ssl sslConfig = httpListener.getSsl();

        GrizzlyEmbeddedHttps grizzlyEmbeddedHttps = (GrizzlyEmbeddedHttps) grizzlyEmbeddedHttp;
        
        List<String> tmpSSLArtifactsList = new LinkedList<String>();

        if (sslConfig != null) {
            // client-auth
            if (Boolean.parseBoolean(sslConfig.getClientAuthEnabled())) {
                grizzlyEmbeddedHttps.setNeedClientAuth(true);
            }

            // ssl protocol variants
            if (Boolean.parseBoolean(sslConfig.getSsl2Enabled())) {
                tmpSSLArtifactsList.add("SSLv2");
            }

            if (Boolean.parseBoolean(sslConfig.getSsl3Enabled())) {
                tmpSSLArtifactsList.add("SSLv3");
            }
            if (Boolean.parseBoolean(sslConfig.getTlsEnabled())) {
                tmpSSLArtifactsList.add("TLSv1");
            }
            if (Boolean.parseBoolean(sslConfig.getSsl3Enabled()) || 
                    Boolean.parseBoolean(sslConfig.getTlsEnabled())) {
                tmpSSLArtifactsList.add("SSLv2Hello");
            }
        }
        
        if (tmpSSLArtifactsList.isEmpty()) {
            logger.log(Level.WARNING,
                        "pewebcontainer.all_ssl_protocols_disabled",
                        httpListener.getId());
        } else {
            String[] enabledProtocols = new String[tmpSSLArtifactsList.size()];
            tmpSSLArtifactsList.toArray(enabledProtocols);
            grizzlyEmbeddedHttps.setEnabledProtocols(enabledProtocols);
        }

        tmpSSLArtifactsList.clear();

        if (sslConfig != null) {
            // cert-nickname
            String certNickname = sslConfig.getCertNickname();
            if (certNickname != null && certNickname.length() > 0) {
                grizzlyEmbeddedHttps.setCertNickname(certNickname);
            }

            // ssl3-tls-ciphers
            String ssl3Ciphers = sslConfig.getSsl3TlsCiphers();

            if (ssl3Ciphers != null && ssl3Ciphers.length() > 0) {
                String[] ssl3CiphersArray = ssl3Ciphers.split(",");
                for (String cipher : ssl3CiphersArray) {
                    tmpSSLArtifactsList.add(cipher.trim());
                }
            }

            // ssl2-tls-ciphers
            String ssl2Ciphers = sslConfig.getSsl2Ciphers();

            if (ssl2Ciphers != null && ssl2Ciphers.length() > 0) {
                String[] ssl2CiphersArray = ssl2Ciphers.split(",");
                for (String cipher : ssl2CiphersArray) {
                    tmpSSLArtifactsList.add(cipher.trim());
                }
            }
        }
        
        if (tmpSSLArtifactsList.isEmpty()) {
            logger.log(Level.WARNING,
                        "pewebcontainer.all_ssl_ciphers_disabled",
                        httpListener.getId());
        } else {
            String[] enabledCiphers = new String[tmpSSLArtifactsList.size()];
            tmpSSLArtifactsList.toArray(enabledCiphers);
            grizzlyEmbeddedHttps.setEnabledCipherSuites(enabledCiphers);
        }

        try {
            grizzlyEmbeddedHttps.initializeSSL();
            return true;
        } catch(Exception e) {
            logger.log(Level.WARNING, "SSL support could not be configured!", e);
        }
        
        return false;
    }
    
    
    /*
     * Configures the keep-alive properties on the given PECoyoteConnector
     * from the given keep-alive config.
     *
     * @param grizzlyListener PECoyoteConnector to configure
     * @param keepAlive Keep-alive config to use
     */
    private static void configureKeepAlive(GrizzlyEmbeddedHttp grizzlyEmbeddedHttp,
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
                logger.log(Level.WARNING, msg, ex);
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
                logger.log(Level.WARNING, msg, ex);
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
                logger.log(Level.WARNING, msg, ex);
            }
        }
        
        grizzlyEmbeddedHttp.setKeepAliveTimeoutInSeconds(timeoutInSeconds);
        grizzlyEmbeddedHttp.setMaxKeepAliveRequests(maxConnections);
        grizzlyEmbeddedHttp.setKeepAliveThreadCount(threadCount);
    }
    
    /*
     * Configures the given HTTP grizzlyListener with the given http-protocol
     * config.
     *
     * @param grizzlyListener HTTP grizzlyListener to configure
     * @param httpProtocol http-protocol config to use
     */
    private static void configureHttpProtocol(
            GrizzlyEmbeddedHttp grizzlyEmbeddedHttp,
            HttpProtocol httpProtocol) {
    
        if (httpProtocol == null) {
            return;
        }

        // http-protocol's "dns-lookup-enabled" attribute configured at
        // Connector level
        grizzlyEmbeddedHttp.setForcedRequestType(httpProtocol.getForcedType());
        grizzlyEmbeddedHttp.setDefaultResponseType(httpProtocol.getDefaultType());
    }
    
        
    /**
     * Configure the Grizzly FileCache mechanism
     */
    private static void configureFileCache(GrizzlyEmbeddedHttp grizzlyEmbeddedHttp,
                                    HttpFileCache httpFileCache){
        if ( httpFileCache == null ) return;
               
   /*     grizzlyListener.setFileCacheIsEnabled(
                ConfigBeansUtilities.toBoolean(httpFileCache.getGloballyEnabled()));         
        grizzlyListener.setLargeFileCacheEnabled(
            ConfigBeansUtilities.toBoolean(httpFileCache.getFileCachingEnabled()));*/
        grizzlyEmbeddedHttp.setFileCacheIsEnabled(true);         
        grizzlyEmbeddedHttp.setLargeFileCacheEnabled(true);
        
        if (httpFileCache.getMaxAgeInSeconds() != null){
            grizzlyEmbeddedHttp.setSecondsMaxAge(
                Integer.parseInt(httpFileCache.getMaxAgeInSeconds()));
        }
        
        if (httpFileCache.getMaxFilesCount() != null){
            grizzlyEmbeddedHttp.setMaxCacheEntries(
                Integer.parseInt(httpFileCache.getMaxFilesCount()));
        }
        
        if (httpFileCache.getSmallFileSizeLimitInBytes() != null){
            grizzlyEmbeddedHttp.setMinEntrySize(
                Integer.parseInt(httpFileCache.getSmallFileSizeLimitInBytes()));
        }
        
        if (httpFileCache.getMediumFileSizeLimitInBytes() != null){
            grizzlyEmbeddedHttp.setMaxEntrySize(
                Integer.parseInt(httpFileCache.getMediumFileSizeLimitInBytes()));
        }
        
        if (httpFileCache.getMediumFileSpaceInBytes() != null){
            grizzlyEmbeddedHttp.setMaxLargeCacheSize(
                Integer.parseInt(httpFileCache.getMediumFileSpaceInBytes()));
        }
        
        if (httpFileCache.getSmallFileSpaceInBytes() != null){
            grizzlyEmbeddedHttp.setMaxSmallCacheSize(
                Integer.parseInt(httpFileCache.getSmallFileSpaceInBytes())); 
        }
    }    
    
    /**
     * Configures an HTTP grizzlyListener with the given request-processing
     * config.
     *
     * @param RequestProcessing http-service config to use
     * @param grizzlyListener the grizzlyListener used.
     */
    protected static void configureRequestProcessing(GrizzlyEmbeddedHttp grizzlyEmbeddedHttp,
            RequestProcessing rp) {
        if (rp == null) return;

        try{
            grizzlyEmbeddedHttp.setMaxThreads(
                    Integer.parseInt(rp.getThreadCount()));
            grizzlyEmbeddedHttp.setMinWorkerThreads(
                    Integer.parseInt(rp.getInitialThreadCount()));
            grizzlyEmbeddedHttp.setThreadsIncrement(
                    Integer.parseInt(rp.getThreadIncrement()));
            grizzlyEmbeddedHttp.setMaxHttpHeaderSize(
                   Integer.parseInt(rp.getHeaderBufferLengthInBytes()));
        } catch (NumberFormatException ex){
            logger.log(Level.WARNING, " Invalid request-processing attribute", 
                    ex);                      
        }             
    }
    
    /**
     * Configure http-listener properties
     */
    private static void configureHttpListenerProperties(GrizzlyEmbeddedHttp grizzlyEmbeddedHttp,
            HttpListener httpListener) {
        // Configure Connector with <http-service> properties
        for (Property httpListenerProp  : httpListener.getProperty()) {
            String propName = httpListenerProp.getName();
            String propValue = httpListenerProp.getValue();
            if (!configureHttpListenerProperty(grizzlyEmbeddedHttp, 
                    propName, propValue)) {
                logger.log(Level.WARNING,
                    "pewebcontainer.invalid_http_listener_property",
                    propName);                    
            }
        }    
    }        
       
    
    /**
     * Configure http-listener property.
     * return true if the property exists and has been set.
     */
    private static boolean configureHttpListenerProperty(
            GrizzlyEmbeddedHttp grizzlyEmbeddedHttp, String propName, String propValue)
            throws NumberFormatException {
        
        if ("bufferSize".equals(propName)) {
            grizzlyEmbeddedHttp.setBufferSize(Integer.parseInt(propValue)); 
            return true; 
        } else if ("use-nio-direct-bytebuffer".equals(propName)) {
            grizzlyEmbeddedHttp.setUseByteBufferView(
                    ConfigBeansUtilities.toBoolean(propValue));
            return true;   
        } else if ("maxKeepAliveRequests".equals(propName)) {
            grizzlyEmbeddedHttp.setMaxKeepAliveRequests(Integer.parseInt(propValue));
            return true;           
        } else if ("authPassthroughEnabled".equals(propName)) {
            grizzlyEmbeddedHttp.setProperty(propName,
                    ConfigBeansUtilities.toBoolean(propValue));
            return true;
        } else if ("maxPostSize".equals(propName)) {
            grizzlyEmbeddedHttp.setMaxPostSize(Integer.parseInt(propValue));
            return true;
        } else if ("compression".equals(propName)) {
            grizzlyEmbeddedHttp.setCompression(propValue);
            return true;
        } else if ("compressableMimeType".equals(propName)) {
            grizzlyEmbeddedHttp.setCompressableMimeTypes(propValue);
            return true;       
        } else if ("noCompressionUserAgents".equals(propName)) {
            grizzlyEmbeddedHttp.setNoCompressionUserAgents(propValue);
            return true;   
        } else if ("compressionMinSize".equals(propName)) {
            grizzlyEmbeddedHttp.setCompressionMinSize(Integer.parseInt(propValue));
            return true;             
        } else if ("restrictedUserAgents".equals(propName)) {
            grizzlyEmbeddedHttp.setRestrictedUserAgents(propValue);
            return true;             
        } else if ("rcmSupport".equals(propName)) {
            grizzlyEmbeddedHttp.enableRcmSupport(ConfigBeansUtilities.toBoolean(propValue));
            return true;   
        } else if ("cometSupport".equals(propName)) {
            configureComet(grizzlyEmbeddedHttp);
            return true;               
        } else if ("connectionUploadTimeout".equals(propName)) {
            grizzlyEmbeddedHttp.setUploadTimeout(Integer.parseInt(propValue));
            return true;            
        } else if ("disableUploadTimeout".equals(propName)) {
            grizzlyEmbeddedHttp.setDisableUploadTimeout(
                    ConfigBeansUtilities.toBoolean(propValue));
            return true;             
        } else if ("proxiedProtocols".equals(propName)) {
            grizzlyEmbeddedHttp.setProperty(propName,propValue);
            return true;    
        // TODO: Add support
        } else if ("chunkingDisabled".equals(propName)
                || "chunking-disabled".equals(propName)) {
            grizzlyEmbeddedHttp.setProperty(propName,
                    ConfigBeansUtilities.toBoolean(propValue));
            return true;
        } else if ("crlFile".equals(propName)) {
            grizzlyEmbeddedHttp.setProperty(propName,propValue);
            return true;
        } else if ("trustAlgorithm".equals(propName)) {
            grizzlyEmbeddedHttp.setProperty(propName,propValue);
            return true;
        } else if ("trustMaxCertLength".equals(propName)) {
            grizzlyEmbeddedHttp.setProperty(propName,propValue);
            return true;
        } else if ("uriEncoding".equals(propName)) {
            grizzlyEmbeddedHttp.setProperty(propName,propValue);
            return true;
        } else if ("jkEnabled".equals(propName)) {
            grizzlyEmbeddedHttp.setProperty(propName,propValue);
            return true;
        } else {
            return false;
        }   
    }   
     
    
    /**
     * Configure http-service properties.
     */
    private static void configureHttpServiceProperties(GrizzlyEmbeddedHttp grizzlyEmbeddedHttp, 
            HttpService httpService) {
        // Configure Connector with <http-service> properties
        List<Property> httpServiceProps = httpService.getProperty();
        if (httpServiceProps != null) {
            for (Property httpServiceProp : httpServiceProps) {
                String propName = httpServiceProp.getName();
                String propValue = httpServiceProp.getValue();
                
                if (configureHttpListenerProperty(grizzlyEmbeddedHttp, propName,
                        propValue)) {
                    continue;
                }
                
                if ("tcpNoDelay".equals(propName)) {
                    grizzlyEmbeddedHttp.setTcpNoDelay(
                            ConfigBeansUtilities.toBoolean(propValue));
                } else if ("traceEnabled".equals(propName)) {
                    grizzlyEmbeddedHttp.setProperty(
                            propName,ConfigBeansUtilities.toBoolean(propValue));
                    
                // TODO: ENABLE FOR SSL
                // (oleksiys): there is no such properties in domain.xml dtd
/*                } else if ("ssl-session-timeout".equals(propName)) {
                    grizzlyListener.setSSLSessionTimeout(propValue);
                } else if ("ssl3-session-timeout".equals(propName)) {
                    grizzlyListener.setSSL3SessionTimeout(propValue);
                } else if ("ssl-cache-entries".equals(propName)) {
                    grizzlyListener.setSSLSessionCacheSize(propValue); */
                } else {
                    //TODO: Turn it to WARNING
                    if (logger.isLoggable(Level.FINE)){
                        logger.log(Level.FINE,
                            "pewebcontainer.invalid_http_service_property",
                            httpServiceProp.getName());
                    }
                }
            }
        }    
    }
    

    /**
     * Configure connection-pool.
     */
    private final static void configureConnectionPool(
            GrizzlyEmbeddedHttp grizzlyEmbeddedHttp, ConnectionPool connPool) {
        if (connPool != null && connPool.getQueueSizeInBytes() != null) {
            int queueSizeInBytes = Integer.parseInt(connPool.getQueueSizeInBytes());
            grizzlyEmbeddedHttp.setMaxQueueSizeInBytes(queueSizeInBytes);
        }
    }
    
    
    /**
     * Enable Comet/Poll request support.
     */
    private final static void configureComet(GrizzlyEmbeddedHttp grizzlyEmbeddedHttp) {
        Habitat habitat = Globals.getDefaultHabitat();
        AsyncFilter cometFilter = habitat.getComponent(AsyncFilter.class, "comet");
        if (cometFilter!=null) {
            grizzlyEmbeddedHttp.setEnableAsyncExecution(true);
            AsyncHandler asyncHandler = new DefaultAsyncHandler();
            asyncHandler.addAsyncFilter(cometFilter);
            grizzlyEmbeddedHttp.setAsyncHandler(asyncHandler);
        }
    }
        
}
