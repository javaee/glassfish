/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 */

package org.glassfish.web.embed.impl;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.*;
import org.apache.catalina.Container;
import org.apache.catalina.Pipeline;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.catalina.valves.RemoteAddrValve;
import org.apache.catalina.valves.RemoteHostValve;
import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.embedded.web.ConfigException;
import org.glassfish.api.embedded.web.config.VirtualServerConfig;
import org.glassfish.web.valve.GlassFishValve;
import org.apache.catalina.authenticator.SingleSignOn;

/**
 * Representation of a virtual server.
 *
 * <p>Instances of <tt>VirtualServer</tt> may be in one of two states:
 * <i>stopped</i> or <i>started</i>. Any requests mapped to a 
 * <tt>VirtualServer</tt> that was stopped will result in a response with
 * a status code equal to
 * javax.servlet.http.HttpServletResponse#SC_NOT_FOUND.
 * 
 * @author Amy Roh
 */
public class VirtualServer extends StandardHost implements 
        org.glassfish.api.embedded.web.VirtualServer {

    
    private static Logger log = 
            Logger.getLogger(VirtualServer.class.getName());
        
    // ------------------------------------------------------------ Constructor

    /**
     * Default constructor that simply gets a handle to the web container
     * subsystem's logger.
     */
    public VirtualServer() {
        super();
        accessLogValve = new AccessLogValve();
        accessLogValve.setContainer(this);
    }
    
    // ----------------------------------------------------- Instance Variables
         
    /*
     * The accesslog valve of this VirtualServer.
     *
     * This valve is activated, that is, added to this virtual server's
     * pipeline, only when access logging has been enabled. When acess logging
     * has been disabled, this valve is removed from this virtual server's
     * pipeline.
     */
    private AccessLogValve accessLogValve; 
       
    /*
     * Indicates whether symbolic links from this virtual server's docroot
     * are followed. This setting is inherited by all web modules deployed on
     * this virtual server, unless overridden by a web modules allowLinking
     * property in sun-web.xml.
     */
    private boolean allowLinking = false;
       
    /*
     * VirtualServer instance configuration.
     */     
    private VirtualServerConfig config;
    
     /*
     * default context.xml location
     */
    private String defaultContextXmlLocation;

    /*
     * default-web.xml location
     */
    private String defaultWebXmlLocation;
    
   
    // --------------------------------------------------------- Public Methods
    
    /**
     * Gets the id of this <tt>VirtualServer</tt>.
     * 
     * @return the id of this <tt>VirtualServer</tt>
     */
    public String getId() {
        return getName();
    }
    
    /**
     * Gets the docroot of this <tt>VirtualServer</tt>.
     * 
     * @return the docroot of this <tt>VirtualServer</tt>
     */
    public File getDocRoot() {
        return new File(getAppBase());
    }

    /**
     * Gets the collection of <tt>WebListener</tt> instances from which
     * this <tt>VirtualServer</tt> receives requests.
     * 
     * @return the collection of <tt>WebListener</tt> instances from which
     * this <tt>VirtualServer</tt> receives requests.
     */
    public Collection<org.glassfish.api.embedded.web.WebListener> getWebListeners() {
        // TODO
        return null;        
    }

    /**
     * Registers the given <tt>Context</tt> with this <tt>VirtualServer</tt>
     * at the given context root.
     *
     * <p>If this <tt>VirtualServer</tt> has already been started, the
     * given <tt>context</tt> will be started as well.
     *
     * @param context the <tt>Context</tt> to register
     * @param contextRoot the context root at which to register
     *
     * @throws ConfigException if a <tt>Context</tt> already exists
     * at the given context root on this <tt>VirtualServer</tt>
     * @throws LifecycleException if the given <tt>context</tt> fails
     * to be started
     */
    public void addContext(org.glassfish.api.embedded.web.Context context, String contextRoot)
        throws ConfigException, LifecycleException {
        addChild((Container)context);
    }

    /**
     * Stops the given <tt>context</tt> and removes it from this
     * <tt>VirtualServer</tt>.
     *
     * @param context the <tt>Context</tt> to be stopped and removed
     *
     * @throws LifecycleException if an error occurs during the stopping
     * or removal of the given <tt>context</tt>
     */
    public void removeContext(org.glassfish.api.embedded.web.Context context) {
        removeChild((Container)context);
    }

    /**
     * Finds the <tt>Context</tt> registered at the given context root.
     *
     * @param contextRoot the context root whose <tt>Context</tt> to get
     *
     * @return the <tt>Context</tt> registered at the given context root,
     * or <tt>null</tt> if no <tt>Context</tt> exists at the given context
     * root
     */
    public Context findContext(String contextRoot) {
        Context context = null;
        Context[] contexts = (Context[]) findChildren();
        for (Context c : contexts) {
            if (c.getPath().equals(contextRoot)) {
                context = c;
            }
        }
        return context;
    }

    /**
     * Gets the collection of <tt>Context</tt> instances registered with
     * this <tt>VirtualServer</tt>.
     * 
     * @return the (possibly empty) collection of <tt>Context</tt>
     * instances registered with this <tt>VirtualServer</tt>
     */
    public Collection<org.glassfish.api.embedded.web.Context> getContexts() {
        org.glassfish.api.embedded.web.Context[] contexts = 
                (org.glassfish.api.embedded.web.Context[]) findChildren();
        return Arrays.asList(contexts);
    }
    
    /**
     * Gets the current configuration of this <tt>VirtualServer</tt>.
     *
     * @return the current configuration of this <tt>VirtualServer</tt>,
     * or <tt>null</tt> if no special configuration was ever applied to this
     * <tt>VirtualServer</tt>
     */
    public VirtualServerConfig getConfig() {
        return config;
    }
        
    /**
     * Enables this component.
     * 
     * @throws LifecycleException if this component fails to be enabled
     */    
    public void enable() throws LifecycleException {               
       try {
            start();
        } catch (org.apache.catalina.LifecycleException e) {
            throw new LifecycleException(e);
        }
    }

    /**
     * Disables this component.
     * 
     * @throws LifecycleException if this component fails to be disabled
     */
    public void disable() throws LifecycleException {
       try {
            stop();
        } catch (org.apache.catalina.LifecycleException e) {
            throw new LifecycleException(e);
        }        
    }
    
    /**
     * Reconfigures this <tt>VirtualServer</tt> with the given
     * configuration.
     *
     * <p>In order for the given configuration to take effect, this
     * <tt>VirtualServer</tt> may be stopped and restarted.
     *
     * @param config the configuration to be applied
     * 
     * @throws ConfigException if the configuration requires a restart,
     * and this <tt>VirtualServer</tt> fails to be restarted
     */
    public void setConfig(VirtualServerConfig config)
        throws ConfigException {
        
        this.config = config;
        configureSingleSignOn(config.isSsoEnabled());
        if (config.isAccessLoggingEnabled()) {
            enableAccessLogging();
        } else {
            disableAccessLogging();
        }
        setDefaultWebXmlLocation(config.getDefaultWebXml());
        setDefaultContextXmlLocation(config.getContextXmlDefault());
        setAllowLinking(config.isAllowLinking());
        configureRemoteAddressFilterValve(config.getAllowRemoteAddress(), config.getDenyRemoteAddress());
        configureRemoteHostFilterValve(config.getAllowRemoteHost(), config.getAllowRemoteHost());
        configureAliases(config.getHostNames());
        
    }     
        
    /**
     * Configure virtual-server alias attribute.
     */
    protected void configureAliases(String... hosts) {

        for (String host : hosts) {
            if ( !host.equalsIgnoreCase("localhost") &&
                    !host.equalsIgnoreCase("localhost.localdomain")){
                addAlias(host);
            }
        }
    }
    
    /**
     * Configures the Remote Address Filter valve of this VirtualServer.
     *
     * This valve enforces request accpetance/denial based on the string
     * representation of the remote client's IP address.
     */
    protected void configureRemoteAddressFilterValve(String allow, String deny) {

        RemoteAddrValve remoteAddrValve = null;

        if (allow != null || deny != null)  {
            remoteAddrValve = new RemoteAddrValve();
        }

        if (allow != null) {
            remoteAddrValve.setAllow(allow);
        }

        if (deny != null) {
            remoteAddrValve.setDeny(deny);
        }

        if (remoteAddrValve != null) {
            // Remove existing RemoteAddrValve (if any), in case of a reconfig
            GlassFishValve[] valves = getValves();
            for (int i=0; valves!=null && i<valves.length; i++) {
                if (valves[i] instanceof RemoteAddrValve) {
                    removeValve(valves[i]);
                    break;
                }
            }
            addValve((GlassFishValve) remoteAddrValve);
        }
    }
        
    /**
     * Configures the Remote Host Filter valve of this VirtualServer.
     *
     * This valve enforces request acceptance/denial based on the name of the
     * remote host from where the request originated.
     */
    protected void configureRemoteHostFilterValve(String allow, String deny) {

        RemoteHostValve remoteHostValve = null;

        if (allow != null || deny != null)  {
            remoteHostValve = new RemoteHostValve();
        }
        if (allow != null) {
            remoteHostValve.setAllow(allow);
        }
        if (deny != null) {
            remoteHostValve.setDeny(deny);
        }
        if (remoteHostValve != null) {
            // Remove existing RemoteHostValve (if any), in case of a reconfig
            GlassFishValve[] valves = getValves();
            for (int i=0; valves!=null && i<valves.length; i++) {
                if (valves[i] instanceof RemoteHostValve) {
                    removeValve(valves[i]);
                    break;
                }
            }
            addValve((GlassFishValve) remoteHostValve);
        }
    }   
        
    /**
     * Gets the value of the allowLinking property of this virtual server.
     *
     * @return true if symbolic links from this virtual server's docroot (as
     * well as symbolic links from archives of web modules deployed on this
     * virtual server) are followed, false otherwise
     */
    public boolean getAllowLinking() {
        return allowLinking;
    }

    /**
     * Sets the allowLinking property of this virtual server, which determines
     * whether symblic links from this virtual server's docroot are followed.
     *
     * This property is inherited by all web modules deployed on this virtual
     * server, unless overridden by the allowLinking property in a web module's
     * sun-web.xml.
     *
     * @param allowLinking Value of allowLinking property
     */
    public void setAllowLinking(boolean allowLinking) {
        this.allowLinking = allowLinking;
    }
    
    /**
     * Configures the SSO valve of this VirtualServer.
     */
    public void configureSingleSignOn(boolean ssoEnabled) {

        if (!ssoEnabled) {
            /*
             * Disable SSO
             */
            if (log.isLoggable(Level.FINE)) {
                log.fine("sso is disabled");
            }

            // Remove existing SSO valve (if any)
            GlassFishValve[] valves = getValves();
            for (int i=0; valves!=null && i<valves.length; i++) {
                if (valves[i] instanceof SingleSignOn) {
                    removeValve(valves[i]);
                    break;
                }
            }

        } else {
            /*
             * Enable SSO
             */
            try {
                SingleSignOn sso = new SingleSignOn();

                /*
                // set max idle time if given
                Property idle = vsBean.getProperty(SSO_MAX_IDLE);
                if (idle != null && idle.getValue() != null) {
                    _logger.fine("SSO entry max idle time set to: " +
                                 idle.getValue());
                    int i = Integer.parseInt(idle.getValue());
                    sso.setMaxInactive(i);
                }

                // set expirer thread sleep time if given
                Property expireTime = vsBean.getProperty(SSO_REAP_INTERVAL);
                if (expireTime !=null && expireTime.getValue() != null) {
                    _logger.fine("SSO expire thread interval set to : " +
                                 expireTime.getValue());
                    int i = Integer.parseInt(expireTime.getValue());
                    sso.setReapInterval(i);
                }*/

                // Remove existing SSO valve (if any), in case of a reconfig
                GlassFishValve[] valves = getValves();
                for (int i=0; valves!=null && i<valves.length; i++) {
                    if (valves[i] instanceof SingleSignOn) {
                        removeValve(valves[i]);
                        break;
                    }
                }

                addValve((GlassFishValve) sso);

                //configureSingleSignOnCookieSecure();

            } catch (Exception e) {
                log.severe(e.getMessage());
            }
        }
    }
        
    /**
     * @return true if the accesslog valve of this virtual server has been
     * activated, that is, added to this virtual server's pipeline; false
     * otherwise
     */
    private boolean isAccessLogValveActivated() {

        Pipeline p = getPipeline();
        if (p != null) {
            GlassFishValve[] valves = p.getValves();
            if (valves != null) {
                for (int i=0; i<valves.length; i++) {
                    if (valves[i] instanceof AccessLogValve) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    
    /**
     * Enables access logging for this virtual server, by adding its
     * accesslog valve to its pipeline, or starting its accesslog valve
     * if it is already present in the pipeline.
     */
    public void enableAccessLogging() {
        if (!isAccessLogValveActivated()) {
            addValve((GlassFishValve) accessLogValve);
        } else {
            try {
                if (accessLogValve.isStarted()) {
                    accessLogValve.stop();
                }
                accessLogValve.start();
            } catch (org.apache.catalina.LifecycleException le) {
                log.severe(le.getMessage());
            }
        }
    }


    /**
     * Disables access logging for this virtual server, by removing its
     * accesslog valve from its pipeline.
     */
    public void disableAccessLogging() {
        removeValve(accessLogValve);
    }

        /**
     * Gets the default-context.xml location of web modules deployed on this
     * virtual server.
     *
     * @return default-context.xml location of web modules deployed on this
     * virtual server
     */
    public String getDefaultContextXmlLocation() {
        return defaultContextXmlLocation;
    }

    /**
     * Sets the default-context.xml location for web modules deployed on this
     * virtual server.
     *
     * @param defaultContextXmlLocation default-context.xml location for web modules
     * deployed on this virtual server
     */
    public void setDefaultContextXmlLocation(String defaultContextXmlLocation) {
        this.defaultContextXmlLocation = defaultContextXmlLocation;
    }

    /**
     * Gets the default-web.xml location of web modules deployed on this
     * virtual server.
     *
     * @return default-web.xml location of web modules deployed on this
     * virtual server
     */
    public String getDefaultWebXmlLocation() {
        return defaultWebXmlLocation;
    }

    /**
     * Sets the default-web.xml location for web modules deployed on this
     * virtual server.
     *
     * @param defaultWebXmlLocation default-web.xml location for web modules
     * deployed on this virtual server
     */
    public void setDefaultWebXmlLocation(String defaultWebXmlLocation) {
        this.defaultWebXmlLocation = defaultWebXmlLocation;
    }

}
