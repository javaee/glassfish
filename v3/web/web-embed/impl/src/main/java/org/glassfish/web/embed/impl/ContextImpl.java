/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.web.embed.impl;

import javax.servlet.*;

import org.glassfish.embeddable.web.Context;
import org.glassfish.embeddable.web.config.SecurityConfig;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.Constants;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.servlets.DefaultServlet;
import org.glassfish.embeddable.GlassFishException;


/**
 * Representation of a web application.
 *
 * @author Amy Roh
 */
// TODO: Add support for configuring environment entries
public class ContextImpl extends StandardContext implements Context {


    // ----------------------------------------------------- Instance Variables
    
    private SecurityConfig config;

    /**
     * Should we generate directory listings?
     */
    protected boolean directoryListing = false;

    
    // --------------------------------------------------------- Public Methods

    /**
     * Enables or disables directory listings on this <tt>Context</tt>.
     *
     * @param directoryListing true if directory listings are to be
     * enabled on this <tt>Context</tt>, false otherwise
     */
    public void setDirectoryListing(boolean directoryListing) {
        this.directoryListing = directoryListing;
        Wrapper wrapper = (Wrapper) findChild(Constants.DEFAULT_SERVLET_NAME);
        if (wrapper !=null) {
            Servlet servlet = ((StandardWrapper)wrapper).getServlet();
            if (servlet instanceof DefaultServlet) {
                ((DefaultServlet)servlet).setListings(directoryListing);
            }
        }
    }

    /**
     * Checks whether directory listings are enabled or disabled on this
     * <tt>Context</tt>.
     *
     * @return true if directory listings are enabled on this 
     * <tt>Context</tt>, false otherwise
     */
    public boolean isDirectoryListing() {      
        return directoryListing;
    }

    /**
     * Set the security related configuration for this context
     *
     * @see org.glassfish.embeddable.web.config.SecurityConfig
     *
     * @param config the security configuration for this context
     */
    public void setSecurityConfig(SecurityConfig config) {
        this.config = config;
        // TODO 
    }

    /**
     * Gets the security related configuration for this context
     *
     * @see org.glassfish.embeddable.web.config.SecurityConfig
     *
     * @return the security configuration for this context
     */
    public SecurityConfig getSecurityConfig() {
        return config;
    }
    
    // ------------------------------------------------- Lifecycle Methods
            
    /**
     * Enables this component.
     * 
     * @throws GlassFishException if this component fails to be enabled
     */    
    public void enable() throws GlassFishException {               
       try {
            start();
        } catch (org.apache.catalina.LifecycleException e) {
            throw new GlassFishException(e);
        }
    }

    /**
     * Disables this component.
     * 
     * @throws GlassFishException if this component fails to be disabled
     */
    public void disable() throws GlassFishException {
       try {
            stop();
        } catch (org.apache.catalina.LifecycleException e) {
            throw new GlassFishException(e);
        }        
    }
    
    
}
