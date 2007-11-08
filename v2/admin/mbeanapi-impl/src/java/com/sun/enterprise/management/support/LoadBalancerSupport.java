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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.enterprise.management.support;

import com.sun.appserv.management.util.jmx.NotificationListenerBase;
import com.sun.enterprise.management.ext.lb.LoadBalancerImpl;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.io.IOException;
import javax.management.AttributeChangeNotification;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.ObjectName;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.AMXDebug;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.AMXRootLogger;
import static com.sun.appserv.management.base.XTypes.LOAD_BALANCER_CONFIG;
import static com.sun.appserv.management.base.XTypes.LOAD_BALANCER;
import static com.sun.appserv.management.base.AMX.JMX_DOMAIN;

import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.config.LoadBalancerConfig;
import com.sun.appserv.management.ext.lb.LoadBalancer;
import com.sun.appserv.management.util.jmx.MBeanRegistrationListener;

/**
    This class is complicated by out-of-order registration of MBeans.
    Because LoadBalancerImpl depends on LBConfig, and they might be registered
    out of order, special care must be taken at startup.
 */
public class LoadBalancerSupport  {
    //private final DomainRoot mDomainRoot;
    private final MBeanServer   mMBeanServer;
    private final DomainRoot    mDomainRoot;
    private final Map<ObjectName,ObjectName> mConfigToLoadBalancers;
    
    private final LoadBalancerConfigListener mConfigListener;
    
    private LoadBalancerSupport(
        final DomainRoot domainRoot,
        final MBeanServer mbeanServer)
        throws InstanceNotFoundException, IOException {
       // mDomainRoot  = domainRoot;
        mMBeanServer = mbeanServer;
        mDomainRoot = domainRoot;
        mConfigToLoadBalancers =
            Collections.synchronizedMap(new HashMap<ObjectName,ObjectName>());
        
        mConfigListener = new LoadBalancerConfigListener( mMBeanServer );
    }
    
    private static volatile LoadBalancerSupport INSTANCE = null;
    
    /**
        Call once and only once.
     */
        public static synchronized LoadBalancerSupport
    start(
        final ObjectName    domainRootObjectName,
        final MBeanServer   server ) throws InstanceNotFoundException, IOException
    {
        if ( INSTANCE != null )
        {
            throw new IllegalStateException();
        }
        
        final DomainRoot domainRoot =
            ProxyFactory.getInstance( server ).getProxy( domainRootObjectName, DomainRoot.class );
        
        INSTANCE = new LoadBalancerSupport( domainRoot, server );
        INSTANCE.mConfigListener.startListening();
        
        //final InitialRegistrationListener initial = new InitialRegistrationListener();
        //initial.startListening();
        
        return INSTANCE;
    }
    
    /**
        Listens for registration and unregistration of LOAD_BALANCER_CONFIG
        MBeans, dynamically creating or removing corresponding LOAD_BALANCER
        MBeans.
     */
    private final class LoadBalancerConfigListener
        extends MBeanRegistrationListener
    {
        private final MBeanServer   mServer;
        
        LoadBalancerConfigListener( final MBeanServer mbeanServer )
            throws InstanceNotFoundException, IOException
        {
            super( "LoadBalancerSupport.LoadBalancerConfigListener",
                mbeanServer, null );
            mServer = mbeanServer;
        }
        
        private boolean isLoadBalancerConfig( final ObjectName o )
        {
            return LOAD_BALANCER_CONFIG.equals( Util.getJ2EEType(o) );
        }
    
        protected void mbeanRegistered( final ObjectName registeredObjectName )
        {
            if ( isLoadBalancerConfig( registeredObjectName ) )
            {
                registerNewLoadBalancer( registeredObjectName );
            }
        }
        
        protected void mbeanUnregistered( final ObjectName unregisteredObjectName )
        {
            if ( isLoadBalancerConfig( unregisteredObjectName ) )
            {
                unregisterLoadBalancer( unregisteredObjectName );
            }
        }
    };
    
    /**
        Register a  {@link LoadBalancer} MBean corresponding to
        a newly-registered  LoadBalancerConfig MBean.  See {@link #unregisterLoadBalancer}.
     */
       synchronized LoadBalancer
    registerNewLoadBalancer(final ObjectName loadBalancerConfigObjectName)
    {
        //create load balancer runtime mbean
        final ObjectName domainRootObjName = Util.getObjectName( mDomainRoot );
        
        final ObjectNames objectNames = ObjectNames.getInstance(JMX_DOMAIN);
        final String name = loadBalancerConfigObjectName.getKeyProperty( AMX.NAME_KEY );
        final ObjectName loadBalancerObjName = 
            objectNames.buildContaineeObjectName( domainRootObjName, 
                mDomainRoot.getFullType(), LOAD_BALANCER, name);

        final LoadBalancerConfig loadBalancerConfig =
            getProxy( loadBalancerConfigObjectName, LoadBalancerConfig.class);
            
        final LoadBalancerImpl loadBalancerImpl =
            new LoadBalancerImpl(mMBeanServer, loadBalancerConfig);
        LoadBalancer loadBalancerProxy = null;
        try
        {
            final ObjectName actualObjectName = mMBeanServer.registerMBean(
                loadBalancerImpl, loadBalancerObjName).getObjectName();
                
            loadBalancerProxy = getProxy( actualObjectName, LoadBalancer.class);
            mConfigToLoadBalancers.put( loadBalancerConfigObjectName, actualObjectName );
        }
        catch( JMException ex )
        {
            logWarning(
                "LoadBalancerRegistrationListener registerLoadBalancer " +
                "failed. Exception = ", ex);	
        }
        return loadBalancerProxy;
    }
    
    /**
        'synchronized' to avoid any register/unregister conditions with rapid
        registration/unregistration using the same name.
     */
        synchronized void
    unregisterLoadBalancer(final ObjectName unregisteredLoadBalancerConfig)
    {
        final ObjectName correspondingLoadBalancerObjectName =
            mConfigToLoadBalancers.remove( unregisteredLoadBalancerConfig );
        
        if ( correspondingLoadBalancerObjectName != null )
        {
            try
            {
            mMBeanServer.unregisterMBean( correspondingLoadBalancerObjectName );
            }
            catch( JMException e )
            {
                // shouldn't happen, but not a problem
            }
        }
    }
    
        protected ProxyFactory
    getProxyFactory()
    {
        return ProxyFactory.getInstance( mMBeanServer );
    }
    
        protected <T extends AMX> T
    getProxy( final ObjectName objectName, final Class<T> theClass)
    {
        return getProxyFactory().getProxy( objectName, theClass );
    }
    
    protected void logWarning(String prefix, Exception ex) {
        AMXRootLogger.getInstance().warning(prefix + " : " + ex.getMessage());
    }


    /**
        private synchronized void
    amxNowReady()
    {
        // start listening for new registrations *before*
        INSTANCE.startListening();
        mAMXReady   = true;
        
        //get ALL AMX <load-balancer> mbeans in the domain
        final Map<String, LoadBalancerConfig> loadBalancerConfigMap =
            mDomainRoot.getDomainConfig().getLoadBalancerConfigMap();
        for (final LoadBalancerConfig lbc : loadBalancerConfigMap.values())
        {
            registerNewLoadBalancer( Util.getObjectName( lbc ) );
        }
    }
     */
    
    /**
        Listen for AMX to be ready, and when ready, 
    private final InitialRegistrationListener extends NotificationListenerBase
    {
        InitialRegistrationListener()
        {
            super( "LoadBalancerSupport.InitialRegistrationListener",
                mServer, Util.getObjectName( mDomainRoot ) );
        }
        
        public void handleNotification(
            final Notification notifIn,
            final Object handback)
        {
            if ( DomainRoot.AMX_READY_NOTIFICATION_TYPE.equals(notifIn.getType()) )
            {
                amxNowReady();
                cleanup();  // stops listening
            }
            } catch (Exception ex) {
                AMXRootLogger.getInstance().warning( 
                    "LBBootstrapUtil:handleNotification" + " : " + ex.getMessage());           
            }
        }
    };
     */
}





