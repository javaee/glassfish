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
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.internal.grizzly;

import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.util.http.mapper.Mapper;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.annotations.Service;

/**
 * Extended that {@link Mapper} that prevent the WebContainer to unregister
 * the current {@link Mapper} configuration.
 * 
 * @author Jeanfrancois Arcand
 */
@Service
@ContractProvided(Mapper.class)
public class V3Mapper extends ContextMapper {

    private static final String ADMIN_LISTENER = "admin-listener";
    private static final String ADMIN_VS = "__asadmin";

    private final Logger logger;

    private Adapter adapter;

    // The id of the associated network-listener
    private String id;
    

    public V3Mapper() {
        this(Logger.getAnonymousLogger());
    }   
    
    
    public V3Mapper(Logger logger) {
        this.logger = logger;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addWrapper(String hostName, String contextPath, String path,
            Object wrapper, boolean jspWildCard) {
        super.addWrapper(hostName, contextPath, path, wrapper, jspWildCard);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Wrapper-Host: " + hostName + " contextPath " + contextPath
                    + " wrapper " + wrapper + " path " + path + " jspWildcard " + jspWildCard);
        }                          
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addHost(String name, String[] aliases,
            Object host) {

        // Prevent any admin related artifacts from being registered on a
        // non-admin listener, and vice versa
        if ((ADMIN_LISTENER.equals(id) && !ADMIN_VS.equals(name)) ||
                (!ADMIN_LISTENER.equals(id) && ADMIN_VS.equals(name))) {
            return;
        }

        super.addHost(name, aliases, host);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Host-Host: " + name + " aliases " + aliases 
                    + " host " + host);
        }
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addContext(String hostName, String path, Object context,
            String[] welcomeResources, javax.naming.Context resources) {
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Context-Host: " + hostName + " path " + path + " context " + context +
                    " port " + getPort());
        }
        
        // Prevent any admin related artifacts from being registered on a
        // non-admin listener, and vice versa
        if ((ADMIN_LISTENER.equals(id) && !ADMIN_VS.equals(hostName)) ||
                (!ADMIN_LISTENER.equals(id) && ADMIN_VS.equals(hostName))) {
            return;
        }
        
        // The WebContainer is registering new Context. In that case, we must
        // clean all the previously added information, specially the 
        // MappingData.wrapper info as this information cannot apply
        // to this Container.
        if (adapter != null && adapter.getClass().getName()
                .equals("org.apache.catalina.connector.CoyoteAdapter")) {
            super.removeContext(hostName, path);
        }
        
        super.addContext(hostName, path, context, welcomeResources, resources);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void removeHost(String name) {
        // Do let the WebContainer deconfigire us.
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Faking removal of host: " + name);
        }
    }


    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }


    public Adapter getAdapter() {
        return adapter;
    }


    /**
     * Sets the id of the associated network-listener on this mapper.
     */
    public void setId(String id) {
        this.id = id;
    }
}
