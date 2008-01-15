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

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.module.*;
import com.sun.enterprise.module.impl.Utils;
import com.sun.enterprise.v3.data.ContainerInfo;
import com.sun.enterprise.v3.data.ContainerRegistry;
import com.sun.enterprise.v3.server.ContainerStarter;
import com.sun.enterprise.v3.server.HK2Dispatcher;
import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.standalone.DynamicContentAdapter;
import com.sun.grizzly.standalone.StaticStreamAlgorithm;
import com.sun.grizzly.tcp.ActionCode;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.Response;
import com.sun.grizzly.util.buf.ByteChunk;
import com.sun.logging.LogDomains;
import org.glassfish.api.Startup;
import org.glassfish.api.container.Adapter;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.content.FileServer;
import org.glassfish.api.content.WebRequestHandler;
import org.glassfish.api.deployment.ApplicationContainer;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.*;

import java.io.*;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Grizzly Service is responsible for starting grizzly and register the 
 * top level adapter. It is also providing a runtime service where other 
 * services (like admin for instance) can register endpoints adapter to 
 * particular context root. 
 *
 * @author Jerome Dochez
 */
@Service
@Scoped(Singleton.class)
public class GrizzlyAdapter implements Startup, com.sun.grizzly.tcp.Adapter, PostConstruct, PreDestroy {
    
    SelectorThread selectorThread = null;
    int portNumber = 0;

    @Inject
    ModulesRegistry modulesRegistry;

    @Inject
    Habitat habitat;

    @Inject
    Domain domain;

    @Inject
    Logger logger;


    private static final String RFC_2616_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
    private String docRoot=null;

    private Map<String, com.sun.grizzly.tcp.Adapter> endpoints = new HashMap<String, com.sun.grizzly.tcp.Adapter>();
    private Map<com.sun.grizzly.tcp.Adapter, ApplicationContainer> apps = new HashMap();

    private List<FileServer> handlers = new ArrayList<FileServer>();

    private HK2Dispatcher dispatcher = new HK2Dispatcher();
    /**
     * Starts the grizzly service. 
     * 
     * Start the grizzly listener with the domain configured port and register the 
     * GrizzlyAdapter to the global services.
     */
    public void postConstruct() {
        
        
        String port=null;

        // get the domain configuration from our globals
        if (domain==null) {
            System.err.println("Cannot get domain.xml information");
            return;
        }
        List<Config> configs = domain.getConfigs().getConfig();
        for (Config aConfig : configs) {
            HttpService httpService = aConfig.getHttpService();
            List<HttpListener> httpListeners = httpService.getHttpListener();
            for (HttpListener httpListener : httpListeners) {
                if ("admin-listener".equals(httpListener.getId())) {
                    continue;
                }
                Boolean state = Boolean.valueOf(httpListener.getSecurityEnabled());
                if (!state.booleanValue()) {
                    port = httpListener.getPort();
                    docRoot = getDocRoot(httpListener, aConfig);
                }

            }
        }
        if (port==null) {
            logger.severe("Cannot find port information from domain.xml");
            throw new RuntimeException("Cannot find port information from domain configuration");
        }
        portNumber = 8080;
        try {
            portNumber = Integer.parseInt(port);
        } catch(java.lang.NumberFormatException e) {
            logger.severe("Cannot parse port value : " + port + ", using port 8080");
        }
        
        docRoot = com.sun.enterprise.v3.admin.Utils.decode(docRoot);
        File file = new File(docRoot);
        if (file.exists()) {
            docRoot = file.getAbsolutePath();
        } else {
            logger.severe("Invalid docroot :" + docRoot);
        }
        
        selectorThread = new SelectorThread();
        selectorThread.setPort(portNumber);
        selectorThread.setAlgorithmClassName(StaticStreamAlgorithm.class.getName());      
        selectorThread.setAdapter(this);
        selectorThread.setWebAppRootPath(docRoot);
        selectorThread.setBufferResponse(false);
        
        Thread thread = new Thread() {
            public void run() {
                try {
                    selectorThread.initEndpoint();
                    selectorThread.startEndpoint();
                } catch(InstantiationException e) {
                    logger.log(Level.SEVERE, "Cannot start grizzly selector", e);
                } catch(IOException e) {
                    logger.log(Level.SEVERE, "Cannot start grizzly selector", e);
                } catch (RuntimeException e) {
                    logger.log(Level.INFO, "Exception in grizzly thread", e);
                }
            }
        };
        thread.start();
        logger.info("Listening on port " + portNumber);

        // now register all adapters you can find out there, unless it's me !
        for (Adapter subAdapter : habitat.getAllByContract(Adapter.class)) {

            if (subAdapter.getClass().getName().equals(this.getClass().getName())) {
                continue;
            }
            logger.fine("Registering adapter " + subAdapter.getContextRoot());
            registerEndpoint(subAdapter.getContextRoot(), subAdapter, null);            
        }
    }
    
    /**
     * Stops the grizzly service.
     */
    public void preDestroy() {
        selectorThread.stopEndpoint();
    }
    
    public String toString() {
        return "Grizzly on port " + portNumber;
    }
    
    public Lifecycle getLifecycle() {
        return Lifecycle.SERVER;
    }
    
    /**
     * Crude implementation of the extraction of the docroot from 
     * what I think is the right virtual server config
     */
    private String getDocRoot(HttpListener listener, Config config) {
        
        for (VirtualServer virtualServer : config.getHttpService().getVirtualServer()) {
            if (virtualServer.getHttpListeners().indexOf(listener.getId())!=-1) { 
                String docRoot = virtualServer.getDocroot();
                if (docRoot==null) {
                    // look for the properties...
                    return ConfigBeansUtilities.getPropertyValueByName(virtualServer, "docroot");
                }
            }
        }
        return null;
    }
    /**
     * Call the service method, and notify all listeners
     *
     * @exception Exception if an error happens during handling of
     *   the request. Common errors are:
     *   <ul><li>IOException if an input/output error occurs and we are
     *   processing an included servlet (otherwise it is swallowed and
     *   handled by the top level error handler mechanism)
     *       <li>ServletException if a servlet throws an exception and
     *  we are processing an included servlet (otherwise it is swallowed
     *  and handled by the top level error handler mechanism)
     *  </ul>
     *  Tomcat should be able to handle and log any other exception ( including
     *  runtime exceptions )
     */
    public void service(Request req, Response res)
        throws Exception {

        if (Utils.getDefaultLogger().isLoggable(Level.FINER)) {
            Utils.getDefaultLogger().finer("Received something on " + req.requestURI());
        }
        
        String requestURI = req.requestURI().toString();
        try{
            // TODO: This for loop is extremely dangerous. We need to re-use
            // Grizzly HTTP Mapper here to avoid scripting attach, dos, etc.
            for(;;) {
                com.sun.grizzly.tcp.Adapter dispatchTo = endpoints.get(requestURI);
                ApplicationContainer container = apps.get(dispatchTo);
                if (dispatchTo!=null) {
                    ClassLoader cl = null;
                    if (container!=null) {
                        cl = container.getClassLoader();
                    }
                    dispatcher.dispath(dispatchTo, cl, req, res);

                    if (res.getStatus()!=200) {
                        sendError(res, "GlassFish v3 Error " + res.getStatus());
                    }
                    dispatchTo.afterService(req, res);                    
                    return;
                }
                
                if (requestURI.lastIndexOf("/")!=-1) {
                    requestURI = requestURI.substring(0, requestURI.lastIndexOf("/"));
                } else {
                    // last chance, look in our docroot...
                    File file = new File(docRoot, req.requestURI().getString());
                    if (file.exists()) {
                        serviceFile(req, res, file);
                    } else if (req.requestURI().toString().equals("/favicon.ico")) {
                        serviceFile(req, res, new File(docRoot, "/favicon.gif"));
                    } else {
                        // TODO : a better job at error reporting
                        Utils.getDefaultLogger().info("No adapter registered for : " 
                                + req.requestURI().toString());
                        sendError(res, "Glassfish v3 Error : NotFound : " 
                                + req.requestURI().getString());
                    }
                    return;
                }
            }
        } finally {        
            try{
                req.action( ActionCode.ACTION_POST_REQUEST , null);
            }catch (Throwable t) {
                t.printStackTrace();
            }

            res.finish();                
        }
    }

    private void sendError(Response res, String message) throws IOException {
        res.setStatus(404);
        res.setContentType("text/html");
        res.addHeader("Server", "GlassFish/v3");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(bos);
        printWriter.println("<html><head>Error</head><body><h1>");
        printWriter.print(message);
        printWriter.print("</h1></body></html>");
        printWriter.flush();
        res.setContentLength(bos.size());
        res.sendHeaders();
        
        ByteChunk chunk = new ByteChunk();
        chunk.setBytes(bos.toByteArray(), 0, bos.size());
        res.doWrite(chunk);
    }


    /**
     * Finish the response and recycle the request/response tokens. Base on
     * the connection header, the underlying socket transport will be closed
     */
    public void afterService(Request req, Response res) throws Exception {
        // Event if the sub Adapter might have called that method, it is
        // safer to recycle again in case they failed to do it.
        req.recycle();
        res.recycle();    
    }


    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Adapter.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param data Event data
     */
    public void fireAdapterEvent(String type, Object data) {

    }

    /**
     * Registers a new endpoint (adapter implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the adapter instance passed in.
     * @param contextRoot for the adapter
     * @param endpointAdapter servicing requests.
     */
    public void registerEndpoint(String contextRoot, com.sun.grizzly.tcp.Adapter endpointAdapter,
                                 ApplicationContainer container) {
        if (!contextRoot.startsWith("/")) {
            contextRoot = "/" + contextRoot;
        }
        endpoints.put(contextRoot, endpointAdapter);
        apps.put(endpointAdapter, container);
    }

    /**
     * Removes the contex-root from our list of endpoints.
     */
    public void unregisterEndpoint(String contextRoot) {
        endpoints.remove(contextRoot);

    }

    /**
     * Returns an Adapter instance regitered for handling request coming with
     * this context-root or null if none is registered.
     */
    public com.sun.grizzly.tcp.Adapter getEndpoint(String contextRoot) {
        return endpoints.get(contextRoot);
    }

    /**
     * Sets the doc root for this adapter.
     * In case no sub adapter is registered for a particular URI, this adapter
     * will try to fetch a file from the doc root, basically acting as a static
     * web page web server.
     * @param docRoot for this adapter to load pages from
     */
    public void setDocRoot(String docRoot) {
        this.docRoot = docRoot;
    }

    /**
     * Returns the context root for this adapter.
     * @return the context root
     */
    public String getContextRoot() {
        return "/";
    }

    /**
     * Utility method to service a local file
     */
    public void serviceFile(Request req, Response res, File file) throws IOException, FileNotFoundException {

        if (file.isDirectory()) {
            file = new File(file, "index.html");
        }
        if (modifiedSince(req, file)) {
            // Not Modified
            res.setStatus(304);

            return;
        }
        FileServer handler = getHandlerFor(req);
        if (handler!=null) {
            ClassLoader currentCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(handler.getClass().getClassLoader());
                DynamicContentAdapter adapter = DynamicContentAdapter.class.cast(handler);
                adapter.setRootFolder(docRoot);
                adapter.service(req, res);
                return;
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Exception while servicing " + file, e);
            } finally {
                Thread.currentThread().setContextClassLoader(currentCl);
            }
        }

        // set headers
        String type = URLConnection.guessContentTypeFromName(file.getName());
        res.setStatus(200);
        res.setContentType(type);
        res.setContentLength((int) file.length());
        res.addHeader("Server", "GlassFish/v3");
        res.sendHeaders();

        FileInputStream fis = new FileInputStream(file);
        byte b[] = new byte[8192];
        ByteChunk chunk = new ByteChunk();
        int rd = 0;
        while ((rd = fis.read(b)) > 0) {
            chunk.setBytes(b, 0, rd);
            res.doWrite(chunk);
        }

        try{
            req.action( ActionCode.ACTION_POST_REQUEST , null);
        }catch (Throwable t) {
            t.printStackTrace();
        }
        
        res.finish();
    }

    private static boolean modifiedSince(Request req, File file) {
        try {
            String since = req.getMimeHeaders().getHeader("If-Modified-Since");
            if (since == null) {
                return false;
            }

            Date date = new SimpleDateFormat(RFC_2616_FORMAT, Locale.US).parse(since);
            if (date.getTime() > file.lastModified()) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Returns a handler for a static file service.
     * @return the request handler
     */
    public FileServer getHandlerFor(Request req) {
        if (handlers==null) {
            return null;
        }

        String uri = req.requestURI().getString();
        for (FileServer handler : habitat.getAllByContract(FileServer.class)) {
            WebRequestHandler annotation = handler.getClass().getAnnotation(WebRequestHandler.class);
            if (annotation!=null) {
                if (annotation.urlPattern().length()>0) {
                    Pattern pattern = Pattern.compile(annotation.urlPattern());
                    if (pattern.matcher(uri).matches()) {
                        return handler;
                    }
                }
                return handler;
            }            
        }
        Logger logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        Collection<? extends Sniffer> sniffers = habitat.getAllByContract(Sniffer.class);
        for (Sniffer sniffer : sniffers) {
            if (sniffer.getURLPattern()==null)
                continue;
            Matcher m = sniffer.getURLPattern().matcher(uri);
            if (m.matches()) {
                ContainerRegistry containerRegistry;
                try {
                    containerRegistry = habitat.getComponent(ContainerRegistry.class);
                } catch(ComponentException e) {
                    logger.log(Level.SEVERE, "Cannot get container registry", e);
                    return null;
                }
                ContainerInfo containerInfo = containerRegistry.getContainer(sniffer.getModuleType());
                if (containerInfo==null) {
                    ContainerStarter starter = new ContainerStarter(modulesRegistry, habitat, logger);
                    Collection<ContainerInfo> containersInfo = starter.startContainer(sniffer);
                    containerInfo = containersInfo.iterator().next();
                }
                Iterable<Class<? extends FileServer>> services;

                if (containerInfo==null) {
                    // last ditch, maybe an implementation of the FileServer interface
                    services = modulesRegistry.getProvidersClass(FileServer.class);
                } else {
                    containerRegistry.addContainer(sniffer.getContainersNames()[0], containerInfo);
                    com.sun.enterprise.module.Module connectorModule = com.sun.enterprise.module.Module.find(containerInfo.getContainer().getClass());
                    
                    services = connectorModule.getProvidersClass(FileServer.class);
                }
                for (Class<? extends FileServer> service :services) {
                    WebRequestHandler annotation = service.getAnnotation(WebRequestHandler.class);
                    if (annotation!=null) {
                        if (annotation.urlPattern().length()>0) {
                            Pattern pattern = Pattern.compile(annotation.urlPattern());
                            if (!pattern.matcher(uri).matches()) {
                                continue;
                            }
                        }
                        // if we are here, we have a matching WebRequestHandler.
                        FileServer server = null;
                        try {
                            server = habitat.getComponent(service);
                        } catch (ComponentException e) {
                            logger.log(Level.SEVERE, "Cannot create FileServer implementation " + service, e);
                        }
                        return server;


                    }
                }
            }

        }

        return null;
    }
}
