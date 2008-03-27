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

package com.sun.enterprise.v3.admin.adapter;

import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.Property;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.v3.server.ServerEnvironment;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.Response;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.container.Adapter;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

/**
 * An HK-2 Service that provides the functionality so that admin console access is handled properly.
 * The general contract of this adapter is as follows:
 * <ol>
 * <li>This adapter is *always* installed as a Grizzly adapter for a particular
 *     URL designated as admin URL in domain.xml. This translates to context-root
 *     of admin console application. </li>
 * <li>When the control comes to the adapter for the first time, user is asked
 *     to confirm if downloading the application is OK. In that case, the admin console
 *     application is downloaded and expanded. While the download and installation
 *     is happening, all the clients or browser refreshes get a status message.
 *     No push from the server side is attempted (yet).
 *     After the application is "installed", ApplicationLoaderService is contacted,
 *     so that the application is loaded by the containers. This application is
 *     available as a <code> system-application </code> and is persisted as
 *     such in the domain.xml. </li>
 * <li>Even after this application is available, we don't load it on server
 *     startup by default. It is always loaded <code> on demand </code>.
 *     Hence, this adapter will always be available to find
 *     out if application is loaded and load it in the container(s) if it is not.
 *     If the application is already loaded, it simply exits.
 * </li>
 * </ol>
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish V3
 */
//@Service
public final class AdminConsoleAdapter implements Adapter, PostConstruct {

    @Inject 
    ServerEnvironment env;
    
    @Inject 
    AdminService as; //need to take care of injecting the right AdminService
    
    private String contextRoot;
    private URL[]  urls;
    private File diskLocation;
    
    private final ReentrantLock lock = new ReentrantLock();
    
    private Logger logger;
    
    public AdminConsoleAdapter() {
    }
    
    private void init() {
        List<Property> props = as.getProperty();
        for (Property prop : props) {
            setContextRoot(prop);
            setDownloadLocations(prop);
            setLocationOnDisk(prop);
        }
    }

    public String getContextRoot() {
       return contextRoot; //default is /admin
    }

    public void afterService(Request req, Response res) throws Exception {
        
    }

    public void fireAdapterEvent(String type, Object data) {
    }

    public void service(Request req, Response res) throws Exception {
        logRequest(req);
    }
    
    public void postConstruct() {
        lock.lock();
        //set up the environment properly
        init();        
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    
    public void ready() {
        lock.unlock();
        if (logger != null) 
            logger.info("AdminConsoleAdapter is ready ...");
    }
    
    private void logRequest(Request req) {
        if (logger.isLoggable(Level.INFO)) { //Change all INFO to FINE
            logger.log(Level.INFO, "Req is: " + req.toString());
            logger.log(Level.INFO, "Remote address sending the request: " + req.remoteAddr());
        }
    }
    private void setContextRoot(Property prop) {
        if(ServerTags.ADMIN_CONSOLE_CONTEXT_ROOT.equals(prop.getName())) {
            if (prop.getValue() != null && prop.getValue().startsWith("/")) {
                contextRoot = prop.getValue();
                logger.info("Admin Console Adapter: context root: " + contextRoot);
            } else {
                logger.info("Invalid context root for the admin console application, using default:" + ServerEnvironment.DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT);
                contextRoot = ServerEnvironment.DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT;
            }
        }        
    }
    private void setDownloadLocations(Property prop) {
        if (ServerTags.ADMIN_CONSOLE_DOWNLOAD_LOCATION.equals(prop.getName())) {
            String value = prop.getValue();
            StringTokenizer st = new StringTokenizer(value, "|");
            int i = 0;
            while (st.hasMoreTokens()) {
                try {
                    urls[i] = new URL(st.nextToken());
                    logger.info("Admin Console Download location: " + urls[i]);
                    i++;
                }catch(MalformedURLException me) {
                    i--;
                    logger.info("Skipping malformed Admin URL");
                }
            }
        }        
    }
    private void setLocationOnDisk(Property prop) {
        if (ServerTags.ADMIN_CONSOLE_LOCATION_ON_DISK.equals(prop.getName())) {
            if (prop.getValue() != null) {
                diskLocation = new File(prop.getValue());
                logger.info("Admin Console will be downloaded to: " + diskLocation.getAbsolutePath());
                if (!diskLocation.canWrite()) {
                    logger.warning(diskLocation.getAbsolutePath() + " can't be written to, download will fail");
                }
            }
        }
    }
}
