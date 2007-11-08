/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * MBeanServerBuilder.java
 *
 * Created on August 22, 2003, 3:25 PM
 */

package com.sun.enterprise.admin.server.core.jmx;

import javax.management.MBeanServer;
import javax.management.MBeanServerDelegate;
import javax.management.MBeanServerBuilder;

import java.util.logging.Logger;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import java.util.logging.Level;

import com.sun.enterprise.interceptor.DynamicInterceptor;

import com.sun.enterprise.util.FeatureAvailability;


/**
 * AppServer MBSBuilder for PE set as the value for javax.management.initial.builder 
 * in the environment. This builder extends from javax.management.MBeanServerBuilder
 * and creates MBS with app server interceptors based on a flag. This flag can only be
 * turned in this package and is called by the AppServerMBeanServerFactory.
 * 
 * @author  sridatta
 */
public class AppServerMBeanServerBuilder extends javax.management.MBeanServerBuilder {
    private static final MBeanServerBuilder defaultBuilder = new MBeanServerBuilder();
    
    protected static final Logger _logger = Logger.getLogger(AdminConstants.kLoggerName);
    
    private static MBeanServer _defaultMBeanServer = null;
    
    /** Creates a new instance of MBeanServerBuilder */
    public AppServerMBeanServerBuilder() {
    }
      
    /**
     * This method creates a new MBeanServer implementation object. When creating a new
     * MBeanServer the MBeanServerFactory first calls newMBeanServerDelegate() in
     * order to obtain a new MBeanServerDelegate for the new MBeanServer. Then it
     * calls newMBeanServer(defaultDomain,outer,delegate) passing the delegate that
     * should be used by the MBeanServer implementation.
     * Note that the passed delegate might not be directly the MBeanServerDelegate that
     * was returned by this implementation. It could be, for instance, a new object
     * wrapping the previously returned delegate.
     *
     * The outer parameter is a pointer to the MBeanServer that should be passed
     * to the MBeanRegistration interface when registering MBeans inside the
     * MBeanServer. If outer is null, then the MBeanServer implementation must
     * use its own this reference when invoking the MBeanRegistration interface.
     *
     * This makes it possible for a MBeanServer implementation to wrap another MBeanServer
     * implementation, in order to implement, e.g, security checks, or to prevent
     * access to the actual MBeanServer implementation by returning a pointer to a wrapping object.
     *
     * Parameters:
     * defaultDomain - Default domain of the new MBeanServer.
     * outer - A pointer to the MBeanServer object that must be passed to the MBeans when invoking their MBeanRegistration interface.
     * delegate - A pointer to the MBeanServerDelegate associated with the new MBeanServer. The new MBeanServer must register this MBean in its MBean repository.
     * Returns:
     * A new private implementation of an MBeanServer.
     *
     *
     * Note that this method is sychronized on the class for the
     * case when user requests an MBS during creation on Sun's mbs.
     * This can seldom happen but needs to be a safe code.
     *
     */
     
     public MBeanServer newMBeanServer(String defaultDomain, 
                                    MBeanServer outer, 
                                    MBeanServerDelegate delegate) {
                                                  
        MBeanServer mbeanServer = null;             
        synchronized (AppServerMBeanServerBuilder.class) {
            /*
                We *must* create the first MBeanServer as our special one.  Later MBeanServers
                can be the standard JMX one.  
             */
            if ( _defaultMBeanServer == null ) {   // first time
                mbeanServer = newAppServerMBeanServer(defaultDomain, delegate);
                _defaultMBeanServer = mbeanServer;
            }
            else {
                mbeanServer = defaultBuilder.newMBeanServer(
                    defaultDomain, outer,  delegate);
            }
        }
        _logger.log(Level.FINE, "MBeanServer class = " +
            mbeanServer.getClass().getName() );

        return mbeanServer;
     }
    
     /**
      * creates a jmx MBeanServer 
      * creates a AppServerInterceptor and sets the jmx mbean server 
      * to it. It then wraps any additional interceptors (nothing for PE)
      *
      * Note that the "outer" is SunoneInterceptor and not the additional interceptors
      * added in addInterceptor. (commented out for now for testing purposes. Need
      * to comeback to this. FIXME.)
      * 
      * returns AppServerMBS
      *
      */ 
     protected MBeanServer newAppServerMBeanServer(String defaultDomain, 
                                                MBeanServerDelegate delegate) {
        // we cannot yet create the SunoneInterceptor; it will be inserted
        // later by the AdminService
        final DynamicInterceptor result = new DynamicInterceptor();
        final MBeanServer jmxMBS = defaultBuilder.newMBeanServer(
            defaultDomain, result, delegate);
        result.setDelegateMBeanServer( jmxMBS );
               
        return result;
     }
          
    /**
     * This method creates a new MBeanServerDelegate for a new MBeanServer.
     * When creating a new MBeanServer the MBeanServerFactory first calls
     * this method in order to create a new MBeanServerDelegate.
     * Then it calls newMBeanServer(defaultDomain,outer,delegate) passing the delegate
     * that should be used by the MBeanServer implementation.
     * Note that the passed delegate might not be directly the MBeanServerDelegate that
     * was returned by this method. It could be, for instance, a new object wrapping
     * the previously returned object.
     *
     * return the MBeanServerDelegate from jmx defaultBuilder
     */
    public MBeanServerDelegate newMBeanServerDelegate()  {
        return defaultBuilder.newMBeanServerDelegate();
     }
    
    /**
     * This method is used to set the state whether to create jmx mbs
     * or AppServer mbs. This method is package specific. 
    static void enableAppServerMBeanServer(boolean flag) {
        createAppServerMBeanServer = flag;
    }
     */
    
    /**
     * whether to create AppServer or just the vanilla jmx ri mbs
     * @return boolean
    static protected boolean createAppServerMBeanServer() {
        return createAppServerMBeanServer;
    }
     */
    
    /**
     * jmx ri default builder
     */
    static protected MBeanServerBuilder getDefaultBuilder() {
        return defaultBuilder;
    }
}
