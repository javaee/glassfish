/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.v3.services.impl;

import com.sun.appserv.server.util.Version;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.logging.Level;

import com.sun.enterprise.v3.server.HK2Dispatcher;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.config.GrizzlyEmbeddedHttp;
import com.sun.grizzly.config.ContextRootInfo;
import com.sun.grizzly.config.FileCacheAware;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.Response;
import com.sun.grizzly.tcp.StaticResourcesAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.util.buf.ByteChunk;
import com.sun.grizzly.util.buf.CharChunk;
import com.sun.grizzly.util.buf.MessageBytes;
import com.sun.grizzly.util.buf.UDecoder;
import com.sun.grizzly.util.http.HttpRequestURIDecoder;
import com.sun.grizzly.util.http.mapper.Mapper;
import com.sun.grizzly.util.http.mapper.MappingData;
import com.sun.grizzly.util.http.MimeType;
import com.sun.grizzly.util.http.mapper.AlternateDocBase;
import java.io.IOException;
import java.util.List;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.internal.grizzly.V3Mapper;
import org.jvnet.hk2.component.Habitat;

/**
 * Contaier's mapper which maps {@link ByteBuffer} bytes representation to an  {@link Adapter}, {@link
 * ApplicationContainer} and {@link ProtocolFilter} chain. The mapping result is stored inside {@link MappingData} which
 * is eventually shared with the {@link CoyoteAdapter}, which is the entry point with the Catalina Servlet Container.
 *
 * @author Jeanfrancois Arcand
 * @author Alexey Stashok
 */
@SuppressWarnings({"NonPrivateFieldAccessedInSynchronizedContext"})
public class ContainerMapper extends StaticResourcesAdapter  implements FileCacheAware {
    private final static String ROOT = "";
    private Mapper mapper;
    private GrizzlyEmbeddedHttp grizzlyEmbeddedHttp;
    private String defaultHostName = "server";
    private final UDecoder urlDecoder = new UDecoder();
    private final Habitat habitat;
    private final GrizzlyService grizzlyService;
    protected final static int MAPPING_DATA = 12;
    protected final static int MAPPED_ADAPTER = 13;
            
    private final HK2Dispatcher hk2Dispatcher = new HK2Dispatcher();

    private String version;

    /**
     * Are we running multiple {@ Adapter} or {@link GrizzlyAdapter}
     */
    private boolean mapMultipleAdapter = false;

    public ContainerMapper(GrizzlyService grizzlyService, GrizzlyEmbeddedHttp grizzlyEmbeddedHttp) {
        this.grizzlyEmbeddedHttp = grizzlyEmbeddedHttp;
        this.grizzlyService = grizzlyService;
        this.habitat = grizzlyService.habitat;
        logger = GrizzlyEmbeddedHttp.logger();

        version = System.getProperty("product.name");
        if (version == null) {
            version = Version.getVersion();
        }
    }

    /**
     * Set the default host that will be used when we map.
     *
     * @param defaultHost
     */
    protected void setDefaultHost(String defaultHost) {
        defaultHostName = defaultHost;
    }

    /**
     * Set the {@link V3Mapper} instance used for mapping the container and its associated {@link Adapter}.
     *
     * @param mapper
     */
    protected void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Configure the {@link V3Mapper}.
     */
    protected synchronized void configureMapper() {
        mapper.setDefaultHostName(defaultHostName);
        mapper.addHost(defaultHostName,new String[]{},null);
        mapper.addContext(defaultHostName,ROOT,
                new ContextRootInfo(this,null),
                new String[]{"index.html","index.htm"},null);
        // Container deployed have the right to override the default setting.
        Mapper.setAllowReplacement(true);
    }

    /**
     * Map the request to its associated {@link Adapter}.
     *
     * @param req
     * @param res
     *
     * @throws IOException
     */
    @Override
    public void service(Request req, Response res) throws Exception{
        MappingData mappingData = null;
        try{
             
            // If we have only one Adapter deployed, invoke that Adapter
            // directly.
            // TODO: Not sure that will works with JRuby.
            if (!mapMultipleAdapter && mapper instanceof V3Mapper){
                // Remove the MappingData as we might delegate the request
                // to be serviced directly by the WebContainer
                req.setNote(MAPPING_DATA, null);
                Adapter a = ((V3Mapper)mapper).getAdapter();
                if (a != null){
                    req.setNote(MAPPED_ADAPTER, a);
                    a.service(req, res);
                    return;
                }
            }

            MessageBytes decodedURI = req.decodedURI();
            decodedURI.duplicate(req.requestURI());
            mappingData = (MappingData) req.getNote(MAPPING_DATA);
            if (mappingData == null) {
                mappingData = new MappingData();
                req.setNote(MAPPING_DATA, mappingData);
            } 
            Adapter adapter = null;
            
            // Map the request without any trailling.
            ByteChunk uriBB = decodedURI.getByteChunk();
            CharChunk uriCC = decodedURI.getCharChunk();
            int start = uriBB.getStart();
            int end = uriBB.getEnd();
            int semicolon = uriBB.indexOf(';', 0);
            byte[] trailer = null;
            
            if (semicolon > 0) {
                trailer = new byte[end - semicolon];
                System.arraycopy(uriBB.getBuffer(), semicolon, trailer, 0, trailer.length);
                decodedURI.setBytes(uriBB.getBuffer(), uriBB.getStart(), semicolon);
            }

            String uriEncoding = (String) grizzlyEmbeddedHttp.getProperty("uriEncoding");
            HttpRequestURIDecoder.decode(decodedURI, urlDecoder, uriEncoding, null);
            adapter = map(req, decodedURI, mappingData);
            if (adapter == null || adapter instanceof ContainerMapper) {
                String ext = decodedURI.toString();
                String type = "";
                if (ext.indexOf(".") > 0) {
                    ext = "*" + ext.substring(ext.lastIndexOf("."));
                    type = ext.substring(ext.lastIndexOf(".") + 1);
                }

                if (!MimeType.contains(type) && !ext.equals("/")){
                    initializeFileURLPattern(ext);
                    mappingData.recycle();
                    adapter = map(req, decodedURI, mappingData);
                } else {
                    super.service(req, res);
                    return;
                }
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Request: " + decodedURI.toString()
                    + " was mapped to Adapter: " + adapter);
            }

            // The Adapter used for servicing static pages doesn't decode the
            // request by default, hence do not pass the undecoded request.
            if (adapter == null || adapter instanceof ContainerMapper) {
                super.service(req, res);
            } else {
                // Re-set back the position.
                if (semicolon > 0 && end != 0) {
                    decodedURI.setBytes(uriBB.getBuffer(), start, end);
                    for(byte b: trailer){
                        uriCC.append((char)b);
                    }
                }
                req.setNote(MAPPED_ADAPTER, adapter);

                ContextRootInfo contextRootInfo = null;
                if (mappingData.context != null && mappingData.context instanceof ContextRootInfo) {
                    contextRootInfo = (ContextRootInfo) mappingData.context;
                }

                if (contextRootInfo == null){
                    adapter.service(req, res);
                } else {
                    ClassLoader cl = null;
                    if (contextRootInfo.getContainer() instanceof ApplicationContainer){
                        cl = ((ApplicationContainer)contextRootInfo.getContainer()).getClassLoader();
                    }
                    hk2Dispatcher.dispath(adapter, cl, req, res);
                }
            }
        } catch (Exception ex) {
            try {
                res.setStatus(500);
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "Internal Server error: " + req.decodedURI(), ex);
                }
                customizedErrorPage(req, res);
            } catch (Exception ex2) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "Unable to error page", ex2);
                }
            }
        } 
    }

    public synchronized void initializeFileURLPattern(String ext) {
        for (Sniffer sniffer : grizzlyService.habitat.getAllByContract(Sniffer.class)) {
            boolean match = false;
            if (sniffer.getURLPatterns() != null) {

                for (String pattern : sniffer.getURLPatterns()) {
                    if (pattern.equalsIgnoreCase(ext)) {
                        match = true;
                        break;
                    }
                }
                
                Adapter adapter = this;
                if (match) {
                    adapter = grizzlyService.habitat.getComponent(SnifferAdapter.class);
                    ((SnifferAdapter)adapter).initialize(sniffer, this);
                    ContextRootInfo c= new ContextRootInfo(adapter, null);
   
                    for (String pattern : sniffer.getURLPatterns()) {
                        for (String host: grizzlyService.hosts ){
                            mapper.addWrapper(host,ROOT, pattern,c,
                                    ("*.jsp".equals(pattern) || "*.jspx".equals(pattern)) ? true:false);
                        }
                    }
                    return;
                }
            }
        }
    }

    Adapter map(Request req, MessageBytes decodedURI, MappingData mappingData) throws Exception {
        if (mappingData == null) {
            mappingData = (MappingData) req.getNote(MAPPING_DATA);
        }
        // Map the request to its Adapter/Container and also it's Servlet if
        // the request is targetted to the CoyoteAdapter.
        mapper.map(req.serverName(), decodedURI, mappingData);
        ContextRootInfo contextRootInfo = null;
        if (mappingData.context != null && (mappingData.context instanceof ContextRootInfo 
                || mappingData.wrapper instanceof ContextRootInfo )) {
            if (mappingData.wrapper != null) {
                contextRootInfo = (ContextRootInfo) mappingData.wrapper;
            } else {
                contextRootInfo = (ContextRootInfo) mappingData.context;
            }
            return contextRootInfo.getAdapter();
        } else if (mappingData.context != null && mappingData.context.getClass()
            .getName().equals("com.sun.enterprise.web.WebModule")) {
            return ((V3Mapper) mapper).getAdapter();
        }
        return null;
    }

    /**
     * Recycle the mapped {@link Adapter} and this instance.
     *
     * @param req
     * @param res
     *
     * @throws Exception
     */
    @Override
    public void afterService(Request req, Response res) throws Exception {
        MappingData mappingData = (MappingData) req.getNote(MAPPING_DATA);
        try {
            Adapter adapter = (Adapter) req.getNote(MAPPED_ADAPTER);
            if (adapter != null) {
                adapter.afterService(req, res);
            }
            super.afterService(req, res);
        } finally {
            req.setNote(MAPPED_ADAPTER, null);
            if (mappingData != null){
                mappingData.recycle();
            }
        }
    }

    /**
     * Return an error page customized for GlassFish v3.
     *
     * @param req
     * @param res
     *
     * @throws Exception
     */
    @Override
    protected void customizedErrorPage(Request req, Response res) throws Exception {
        byte[] errorBody = null;
        if (res.getStatus() == 404){
            errorBody = HttpUtils.getErrorPage(Version.getVersion(),
                    "The requested resource () is not available.", "404");
        } else {
             errorBody = HttpUtils.getErrorPage(Version.getVersion(),
                     "Internal Error", "500");
        }

        ByteChunk chunk = new ByteChunk();
        chunk.setBytes(errorBody, 0, errorBody.length);
        res.setContentLength(errorBody.length);
        res.setContentType("text/html");
        if (!version.isEmpty()){
            res.addHeader("Server", version);
        }
        res.sendHeaders();
        res.doWrite(chunk);
    }

    public void register(String contextRoot, Collection<String> vs, Adapter adapter
            ,ApplicationContainer container) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("MAPPER(" + this + ") REGISTER contextRoot: " + contextRoot +
                " adapter: " + adapter + " container: " + container +
                " port: " + grizzlyEmbeddedHttp.getPort());
        }
        /*
        * In the case of CoyoteAdapter, return, because the context will
        * have already been registered with the mapper by the connector's
        * MapperListener, in response to a JMX event
        */
        if (adapter.getClass().getName().equals("org.apache.catalina.connector.CoyoteAdapter")) {
            return;
        }

        mapMultipleAdapter = true;
        String ctx = getContextPath(contextRoot);
        String wrapper = getWrapperPath(ctx, contextRoot);
        ContextRootInfo c = new ContextRootInfo(adapter, container);
        for (String host : vs) {
            mapper.addContext(host, contextRoot,
                c, new String[0], null);
            if (adapter instanceof StaticResourcesAdapter){
                mapper.addWrapper(host,ctx,wrapper,c);
            }
        }
    }

    private String getWrapperPath(String ctx, String mapping) {
        if (mapping.indexOf("*.") > 0) {
            return mapping.substring(mapping.lastIndexOf("/") + 1);
        } else if (!ctx.equals("")) {
            return mapping.substring(ctx.length());
        } else {
            return mapping;
        }
    }

    private String getContextPath(String mapping) {
        String ctx = "";
        int slash = mapping.indexOf("/", 1);
        if (slash != -1) {
            ctx = mapping.substring(0, slash);
        } else {
            ctx = mapping;
        }

        if (ctx.startsWith("/*.") ||ctx.startsWith("*.") ) {
            if (ctx.indexOf("/") == ctx.lastIndexOf("/")){
                ctx = "";
            } else {
                ctx = ctx.substring(1);
            }
        }


        if (ctx.startsWith("/*") || ctx.startsWith("*")) {
            ctx = "";
        }

        // Special case for the root context
        if (ctx.equals("/")) {
            ctx = "";
        }

        return ctx;
    }

    public void unregister(String contextRoot) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("MAPPER (" + this + ") UNREGISTER contextRoot: " + contextRoot);
        }
        for (String host : grizzlyService.hosts) {
            mapper.removeContext(host, contextRoot);
        }
    }

}
