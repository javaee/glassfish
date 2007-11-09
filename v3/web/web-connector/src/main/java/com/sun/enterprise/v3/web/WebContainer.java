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

package com.sun.enterprise.v3.web;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.util.StringUtils;
import com.sun.grizzly.tcp.Adapter;
import com.sun.logging.LogDomains;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.startup.DigesterFactory;
import org.apache.catalina.startup.Embedded;
import org.apache.coyote.tomcat5.CoyoteAdapter;
import org.glassfish.api.container.Container;
import org.glassfish.api.container.ContainerProvider;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Web container service
 *
 * @author jluehe
 */
@Service(name="web")
@Container(type="web", deployerImpl=WebDeployer.class)
public class WebContainer implements ContainerProvider, PostConstruct, PreDestroy {

    @Inject
    Domain domain;

    @Inject
    ServerContext sc;

    HashMap<String, Integer> portMap = new HashMap<String, Integer>();
    HashMap<Integer, Adapter> adapterMap = new HashMap<Integer, Adapter>();
    
    
    final static Logger logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    
    Embedded embedded;
    Engine engine;
    String instanceName;
    String defaultWebXml;

    public void postConstruct() {
        
        defaultWebXml = System.getProperty("AS_DEF_DOMAINS_PATH");
        if (defaultWebXml != null) {
            defaultWebXml += File.separator + "domain1"
                + File.separator + "config" + File.separator
                + "default-web.xml";
            logger.info("Using default-web.xml " + defaultWebXml);
        }

        String root = System.getProperty("com.sun.aas.installRoot");
        File libRoot = new File(root, "lib");
        File schemas = new File(libRoot, "schemas");
        File dtds = new File(libRoot, "dtds");

        try {
            DigesterFactory.setSchemaResourcePrefix(schemas.toURL().toString());
            DigesterFactory.setDtdResourcePrefix(dtds.toURL().toString());
        } catch(MalformedURLException e) {
            logger.log(Level.SEVERE, "Exception setting the schemas/dtds location", e);
        }

        instanceName = sc.getInstanceName();

        embedded = new Embedded();
        Module module = Module.find(Embedded.class);
        System.out.println(module.getModuleDefinition());
        engine = embedded.createEngine();
        engine.setParentClassLoader(module.getClassLoader());
        embedded.addEngine(engine);
        ((StandardEngine) engine).setDomain("com.sun.appserv");

        List<Config> configs = domain.getConfigs().getConfig();
        for (Config aConfig : configs) {

            HttpService httpService = aConfig.getHttpService();

            // Configure HTTP listeners
            List<HttpListener> httpListeners = httpService.getHttpListener();
            for (HttpListener httpListener : httpListeners) {
                if ("admin-listener".equals(httpListener.getId())) {
                    // XXX TBD
                    continue;
                } else {
                    createHttpListener(httpListener);
                    logger.info("Created HTTP listener "
                                        + httpListener.getId());
                }
            }

            // Configure virtual servers
            List<VirtualServer> virtualServers = httpService.getVirtualServer();
            for (VirtualServer vs : virtualServers) {
                createVirtualServer(vs);
                logger.info("Created virtual server " + vs.getId());
            }
        }

        try {
            embedded.start();
        } catch (LifecycleException le) {
            logger.log(Level.SEVERE,
                               "Unable to start web container", le);
            return;
        }        
    }    
    
    public void preDestroy() {
        
    }


    private void createHttpListener(HttpListener hListener) {

        portMap.put(hListener.getId(),
                    Integer.valueOf(hListener.getPort()));

        WebConnector webConnector = new WebConnector();
        webConnector.setPort(Integer.parseInt(hListener.getPort()));
        webConnector.setDefaultHost(hListener.getDefaultVirtualServer());
        embedded.addConnector(webConnector);

        CoyoteAdapter coyoteAdapter = new CoyoteAdapter(webConnector);
        adapterMap.put(Integer.valueOf(hListener.getPort()), coyoteAdapter);
    }

    private void createVirtualServer(
                com.sun.enterprise.config.serverbeans.VirtualServer vsBean) {
        
        String docroot =
            ConfigBeansUtilities.getPropertyValueByName(vsBean, "docroot");
        Host vs = embedded.createHost(vsBean.getId(), docroot);

        // Configure the virtual server with the port numbers of its
        // associated HTTP listeners
        List listeners =
            StringUtils.parseStringList(vsBean.getHttpListeners(), ",");
        if (listeners != null) {
            int[] ports = new int[listeners.size()];
            int i = 0;
            ListIterator<String> iter = listeners.listIterator();
            while (iter.hasNext()) {
                Integer port = portMap.get(iter.next());
                if (port != null) {
                    ports[i++] = port.intValue();
                }
	    }
            vs.setPorts(ports);
        }

        // Set Host alias names
        List<String> aliasNames =
            StringUtils.parseStringList(vsBean.getHosts(), ",");
        for (String alias: aliasNames){
            // XXX remove once ${com.sun.aas.hostName} has been properly
            // resolved thru parametric replacement
            if ("${com.sun.aas.hostName}".equals(alias)) {
                try {
                    alias = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    logger.log(Level.SEVERE,
                                       "Unable to get local host name",
                                       e);
                }
            }
            vs.addAlias(alias);
        }

        engine.addChild(vs);
    }
    
}
