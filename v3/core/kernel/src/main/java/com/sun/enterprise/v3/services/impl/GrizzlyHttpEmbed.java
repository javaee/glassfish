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
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ResourceBundle;

import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.KeepAlive;
import com.sun.enterprise.config.serverbeans.HttpFileCache;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpProtocol;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.Property; 
import com.sun.enterprise.config.serverbeans.RequestProcessing;
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
    protected static final Logger logger = LogDomains.getLogger(
            LogDomains.WEB_LOGGER);
    
    /**
     * The resource bundle containing the message strings for logger.
     */
    protected static final ResourceBundle _rb = logger.getResourceBundle();
    

    // TODO: Must get the information from domain.xml Config objects.
    // TODO: Pending Grizzly issue 54
    public static GrizzlyServiceListener createListener(HttpService httpService,
            int port, Controller controller){
        
        System.setProperty("product.name", "GlassFish/v3");      
        GrizzlyServiceListener grizzlyListener 
                = new GrizzlyServiceListener();
	//TODO: Configure via domain.xml
        //grizzlyListener.setController(controller);
        grizzlyListener.setPort(port);   
        GrizzlyServiceListener.setWebAppRootPath(
                System.getProperty("com.sun.aas.instanceRoot") + "/docroot");
        
        for (HttpListener httpListener : httpService.getHttpListener()) {
            //TODO: Uncomment when issue 4300 is fixed.
        /*  if (!Boolean.getBoolean(httpListener.getEnabled())) {
                continue;
            } */
            boolean isSecure = Boolean.getBoolean(httpListener.getSecurityEnabled());
            configureGrizzlyListener(grizzlyListener,
                    httpListener,isSecure,httpService);           
        }

        return grizzlyListener;
        
    }
    
    
    /*
     * Configures the given grizzlyListener.
     *
     * @param grizzlyListener The grizzlyListener to configure
     * @param httpListener The http-listener that corresponds to the given
     * grizzlyListener
     * @param isSecure true if the grizzlyListener is security-enabled, false
     * otherwise
     * @param httpServiceProps The http-service properties
     */
    private static void configureGrizzlyListener(
            GrizzlyServiceListener grizzlyListener,
            HttpListener httpListener,
            boolean isSecure,
            HttpService httpService) {

        //TODO: Use the grizzly-config name.
        grizzlyListener.setName("grizzly-v3-" + httpListener.getPort());
        GrizzlyServiceListener.setLogger(logger);                 
        configureKeepAlive(grizzlyListener, httpService.getKeepAlive());
        configureHttpProtocol(grizzlyListener, httpService.getHttpProtocol());     
        configureRequestProcessing(httpService.getRequestProcessing(),grizzlyListener);
        configureFileCache(grizzlyListener, httpService.getHttpFileCache());


        // acceptor-threads
        String acceptorThreads = httpListener.getAcceptorThreads();
        if (acceptorThreads != null) {
            try {
                // Acceptor-Thread needs to be > 1 to be used by Grizzly
                int readController = Integer.parseInt(acceptorThreads) -1;
                if (readController > 0){
                    grizzlyListener.setSelectorReadThreadsCount
                        (readController);
                }
            } catch (NumberFormatException nfe) {
                logger.log(Level.WARNING,
                    "pewebcontainer.invalid_acceptor_threads",
                    new Object[] {
                        acceptorThreads,
                        httpListener.getId(),
                        Integer.toString(grizzlyListener.getMaxProcessorWorkerThreads()) });
            }  
        }
        /* TODO: Enable for SSL
        // Configure Connector with keystore password and location
        if (isSecure) {
            configureConnectorKeysAndCerts(grizzlyListener);
        }*/
        
        configureHttpServiceProperties(httpService,grizzlyListener);      

        // Override http-service property if defined.
        configureHttpListenerProperties(httpListener,grizzlyListener);
    }      
    
    
    // TODO : not yet available missing APIs 
    /*
     * Configures the SSL properties on the given PECoyoteConnector from the
     * SSL config of the given HTTP listener.
     *
     * @param grizzlyListener PECoyoteConnector to configure
     * @param httpListener HTTP listener whose SSL config to use
     *
    private void configureSSL(GrizzlyServiceListener grizzlyListener,
                              HttpListener httpListener) {

        Ssl sslConfig = httpListener.getSsl();
        if (sslConfig == null) {
            return;
        }

        // client-auth
        if (Boolean.getBoolean(sslConfig.getClientAuthEnabled())) {
            grizzlyListener.setClientAuth(true);
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
            logger.log(Level.WARNING,
                        "pewebcontainer.all_ssl_protocols_disabled",
                        httpListener.getId());
        } else {
            grizzlyListener.setSslProtocols(sslProtocolsBuf.toString());
        }

        // cert-nickname
        String certNickname = sslConfig.getCertNickname();
        if (certNickname != null && certNickname.length() > 0) {
            grizzlyListener.setKeyAlias(sslConfig.getCertNickname());
        }

        // ssl3-tls-ciphers
        String ciphers = sslConfig.getSsl3TlsCiphers();
        if (ciphers != null) {
            String jsseCiphers = getJSSECiphers(ciphers);
            if (jsseCiphers == null) {
                logger.log(Level.WARNING,
                            "pewebcontainer.all_ciphers_disabled",
                            httpListener.getId());
            } else {
                grizzlyListener.setCiphers(jsseCiphers);
            }
        }            
    }*/
    
    
    /*
     * Configures the keep-alive properties on the given PECoyoteConnector
     * from the given keep-alive config.
     *
     * @param grizzlyListener PECoyoteConnector to configure
     * @param keepAlive Keep-alive config to use
     */
    private static void configureKeepAlive(GrizzlyServiceListener grizzlyListener,
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
        
        grizzlyListener.setKeepAliveTimeoutInSeconds(timeoutInSeconds);
        grizzlyListener.setMaxKeepAliveRequests(maxConnections);
        grizzlyListener.setKeepAliveThreadCount(threadCount);
    }
    
    /*
     * Configures the given HTTP grizzlyListener with the given http-protocol
     * config.
     *
     * @param grizzlyListener HTTP grizzlyListener to configure
     * @param httpProtocol http-protocol config to use
     */
    private static void configureHttpProtocol(GrizzlyServiceListener grizzlyListener,
                                       HttpProtocol httpProtocol) {
    
        if (httpProtocol == null) {
            return;
        }

        //grizzlyListener.setEnableLookups(httpProtocol.isDnsLookupEnabled());
        grizzlyListener.setForcedRequestType(httpProtocol.getForcedType());
        grizzlyListener.setDefaultResponseType(httpProtocol.getDefaultType());
    }
    
        
    /**
     * Configure the Grizzly FileCache mechanism
     */
    private static void configureFileCache(GrizzlyServiceListener grizzlyListener,
                                    HttpFileCache httpFileCache){
        if ( httpFileCache == null ) return;
        
        /*catalinaCachingAllowed = !(httpFileCache.isGloballyEnabled() &&
                ConfigBeansUtilities.toBoolean(httpFileCache.getFileCachingEnabled()));
        
        grizzlyListener.setFileCacheEnabled(httpFileCache.isGloballyEnabled());   */      
        grizzlyListener.setLargeFileCacheEnabled(
            ConfigBeansUtilities.toBoolean(httpFileCache.getFileCachingEnabled()));
        
        if (httpFileCache.getMaxAgeInSeconds() != null){
            grizzlyListener.setSecondsMaxAge(
                Integer.parseInt(httpFileCache.getMaxAgeInSeconds()));
        }
        
        if (httpFileCache.getMaxFilesCount() != null){
            grizzlyListener.setMaxCacheEntries(
                Integer.parseInt(httpFileCache.getMaxFilesCount()));
        }
        
        if (httpFileCache.getSmallFileSizeLimitInBytes() != null){
            grizzlyListener.setMinEntrySize(
                Integer.parseInt(httpFileCache.getSmallFileSizeLimitInBytes()));
        }
        
        if (httpFileCache.getMediumFileSizeLimitInBytes() != null){
            grizzlyListener.setMaxEntrySize(
                Integer.parseInt(httpFileCache.getMediumFileSizeLimitInBytes()));
        }
        
        if (httpFileCache.getMediumFileSpaceInBytes() != null){
            grizzlyListener.setMaxLargeCacheSize(
                Integer.parseInt(httpFileCache.getMediumFileSpaceInBytes()));
        }
        
        if (httpFileCache.getSmallFileSpaceInBytes() != null){
            grizzlyListener.setMaxSmallCacheSize(
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
    protected static void configureRequestProcessing(RequestProcessing rp, 
                                              GrizzlyServiceListener grizzlyListener){
        if (rp == null) return;

        try{
            grizzlyListener.setMaxProcessorWorkerThreads(
                    Integer.parseInt(rp.getThreadCount()));
            grizzlyListener.setMinWorkerThreads(
                    Integer.parseInt(rp.getInitialThreadCount()));
            grizzlyListener.setThreadsTimeout(
                    Integer.parseInt(rp.getRequestTimeoutInSeconds())); 
            grizzlyListener.setThreadsIncrement(
                    Integer.parseInt(rp.getThreadIncrement()));
            grizzlyListener.setMaxHttpHeaderSize(
                   Integer.parseInt(rp.getHeaderBufferLengthInBytes()));
        } catch (NumberFormatException ex){
            logger.log(Level.WARNING, " Invalid request-processing attribute", 
                    ex);                      
        }             
    }
    
    /**
     * Configure http-listener properties
     */
    private static void configureHttpListenerProperties(HttpListener httpListener,
                                                GrizzlyServiceListener grizzlyListener){
        // Configure Connector with <http-service> properties
        for (Property httpListenerProp  : httpListener.getProperty()) { 
            String propName = httpListenerProp.getName();
            String propValue = httpListenerProp.getValue();
            if (!configureHttpListenerProperty(propName,
                                               propValue,
                                               grizzlyListener)){
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
                                            String propName, 
                                            String propValue,
                                            GrizzlyServiceListener grizzlyListener)
                                            throws NumberFormatException {
        
        if ("bufferSize".equals(propName)) {
            grizzlyListener.setBufferSize(Integer.parseInt(propValue)); 
            return true; 
        } else if ("recycle-objects".equals(propName)) {
            grizzlyListener.setRecycleTasks(
                    ConfigBeansUtilities.toBoolean(propValue));
            return true;
        } else if ("use-nio-direct-bytebuffer".equals(propName)) {
            grizzlyListener.setUseByteBufferView(
                    ConfigBeansUtilities.toBoolean(propValue));
            return true;   
        } else if ("maxKeepAliveRequests".equals(propName)) {
            grizzlyListener.setMaxKeepAliveRequests(Integer.parseInt(propValue));
            return true;           
        } else if ("authPassthroughEnabled".equals(propName)) {
            grizzlyListener.setProperty(propName,
                    ConfigBeansUtilities.toBoolean(propValue));
            return true;
        } else if ("maxPostSize".equals(propName)) {
            grizzlyListener.setMaxPostSize(Integer.parseInt(propValue));
            return true;
        } else if ("compression".equals(propName)) {
            grizzlyListener.setCompression(propValue);
            return true;
        } else if ("compressableMimeType".equals(propName)) {
            grizzlyListener.setCompressableMimeTypes(propValue);
            return true;       
        } else if ("noCompressionUserAgents".equals(propName)) {
            grizzlyListener.setNoCompressionUserAgents(propValue);
            return true;   
        } else if ("compressionMinSize".equals(propName)) {
            grizzlyListener.setCompressionMinSize(Integer.parseInt(propValue));
            return true;             
        } else if ("restrictedUserAgents".equals(propName)) {
            grizzlyListener.setRestrictedUserAgents(propValue);
            return true;             
        } else if ("rcmSupport".equals(propName)) {
            grizzlyListener.setProperty(
                    propName,ConfigBeansUtilities.toBoolean(propValue));
            return true;    
        } else if ("connectionUploadTimeout".equals(propName)) {
            grizzlyListener.setUploadTimeout(Integer.parseInt(propValue));
            return true;            
        } else if ("disableUploadTimeout".equals(propName)) {
            grizzlyListener.setDisableUploadTimeout(
                    ConfigBeansUtilities.toBoolean(propValue));
            return true;             
        } else if ("proxiedProtocols".equals(propName)) {
            grizzlyListener.setProperty(propName,propValue);
            return true;    
        // TODO: Add support
        } else if ("chunkingDisabled".equals(propName)
                || "chunking-disabled".equals(propName)) {
            grizzlyListener.setProperty(propName,
                    ConfigBeansUtilities.toBoolean(propValue));
            return true;
        } else if ("crlFile".equals(propName)) {
            grizzlyListener.setProperty(propName,propValue);
            return true;
        } else if ("trustAlgorithm".equals(propName)) {
            grizzlyListener.setProperty(propName,propValue);
            return true;
        } else if ("trustMaxCertLength".equals(propName)) {
            grizzlyListener.setProperty(propName,propValue);
            return true;
        } else {
            return false;
        }   
    }   
     
    
    /**
     * Configure http-service properties.
     */
    private static void configureHttpServiceProperties(HttpService httpService,
                                               GrizzlyServiceListener grizzlyListener){
        // Configure Connector with <http-service> properties
        List<Property> httpServiceProps = httpService.getProperty();
        if (httpServiceProps != null) {
            for (Property httpServiceProp : httpServiceProps) {
                String propName = httpServiceProp.getName();
                String propValue = httpServiceProp.getValue();
                               
                if (configureHttpListenerProperty(propName,
                                                  propValue, 
                                                  grizzlyListener)){
                    continue;
                }
                

                if ("connectionTimeout".equals(propName)) {
                    grizzlyListener.setSoTimeout(Integer.parseInt(propValue));
                } else if ("tcpNoDelay".equals(propName)) {
                    grizzlyListener.setTcpNoDelay(
                            ConfigBeansUtilities.toBoolean(propValue));
                } else if ("traceEnabled".equals(propName)) {
                    grizzlyListener.setProperty(
                            propName,ConfigBeansUtilities.toBoolean(propValue));
                    
                // TODO: ENABLE FOR SSL
                } /*else if ("ssl-session-timeout".equals(propName)) {
                    grizzlyListener.setSSLSessionTimeout(propValue);
                } else if ("ssl3-session-timeout".equals(propName)) {
                    grizzlyListener.setSSL3SessionTimeout(propValue);
                } else if ("ssl-cache-entries".equals(propName)) {
                    grizzlyListener.setSSLSessionCacheSize(propValue);
                }*/ else {
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
    
}
