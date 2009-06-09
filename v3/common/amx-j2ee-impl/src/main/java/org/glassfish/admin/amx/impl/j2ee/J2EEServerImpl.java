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
package org.glassfish.admin.amx.impl.j2ee;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import org.glassfish.admin.amx.j2ee.J2EEServer;
import org.glassfish.admin.amx.impl.util.Issues;

import javax.management.ObjectName;
import org.glassfish.admin.amx.config.AMXConfigConstants;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.impl.util.ImplUtil;
import org.glassfish.admin.amx.impl.util.ObjectNameBuilder;
import org.glassfish.admin.amx.intf.config.Resources;
import org.glassfish.admin.amx.j2ee.J2EEManagedObject;
import org.glassfish.admin.amx.j2ee.J2EETypes;

import org.glassfish.admin.amx.util.ClassUtil;
import org.glassfish.admin.amx.util.FeatureAvailability;
import org.glassfish.admin.amx.util.MapUtil;
import org.glassfish.admin.amx.util.jmx.JMXUtil;

/**
JSR 77 extension representing an Appserver standalone server (non-clustered).

Note that this class has a subclass:  DASJ2EEServerImpl.
 */
public class J2EEServerImpl extends J2EELogicalServerImplBase {
    public static final Class<J2EEServer> INTF = J2EEServer.class;
    
    /** Maps the ObjectName of a config MBean to that of a JSR 77 MBean */
    private static final Map<ObjectName,ObjectName> mConfigTo77 = new HashMap<ObjectName,ObjectName>();
    
    public J2EEServerImpl(final ObjectName parentObjectName) {
        super(parentObjectName, INTF);
    }
    /* The vendor information for this server. */
    private static final String serverVendor = "Sun Microsystems, Inc.";

    private volatile ResourceListener mResourceListener;

    public String[] getjavaVMs() {
        final ObjectName child = child( J2EETypes.JVM );

        return child == null ? new String[0] : new String[] { child.toString() };
    }

    /** Maps configuration MBean type to J2EE type */
    private static final Map<String,Class> RESOURCE_TYPES	=
    MapUtil.toMap( new Object[] {
        "jdbc-resource", JDBCResourceImpl.class,
        "java-mail-resource", JavaMailResourceImpl.class,
        "jca-resource", JCAResourceImpl.class,
        "jms-resource", JMSResourceImpl.class,
        "jndi-resource", JNDIResourceImpl.class,
        "jta-resource", JTAResourceImpl.class,
        "rmi-iiop-resource", RMI_IIOPResourceImpl.class,
        "url-resource", URLResourceImpl.class},
        String.class, Class.class
    );

    public String[] getresources() {
        return getChildrenAsStrings( RESOURCE_TYPES.keySet() );
    }

    public String getserverVersion() {
        Issues.getAMXIssues().notDone("How to get the server version");
        return "Glassfish V3" ;
    }

    public String getserverVendor() {
        return serverVendor;
    }

    public String getjvm()
    {
        return "" + getAncestorByType( J2EETypes.JVM );
    }

    @Override
        protected void
    registerChildren()
    {
        super.registerChildren();

        // wait until configuration MBeans have been loaded
        FeatureAvailability.getInstance().waitForFeature(
                AMXConfigConstants.AMX_CONFIG_READY_FEATURE, "J2EEServerImpl.registerChildren()" );
        
        final Resources resourcesConfig = getDomainConfig().getResources();
        mResourceListener = new ResourceListener( getMBeanServer(), getObjectName(), resourcesConfig.objectName() );
        mResourceListener.startListening();
        
        // scan for existing resources, listener will pick up any changes
        final Set<AMXProxy> candidates = resourcesConfig.childrenSet();
        //cdebug( "J2EEServerImpl.registerChildren: " + candidates.size() );
        for( final AMXProxy amx : candidates )
        {
            mResourceListener.register(amx);
        }
    }

    @Override
        protected void
    unregisterChildren()
    {
        mResourceListener.stopListening();
        mResourceListener = null;
        super.unregisterChildren();
    }
    
       protected <I extends J2EEManagedObject, C extends J2EEManagedObjectImplBase> ObjectName
    registerJ2EEChild(
        final ObjectName parent,
        final Class<I> intf,
        final Class<C>  clazz,
        final String    name)
    {
        ObjectName on = null;
        try
        {
            final Constructor<C> c = clazz.getConstructor(ObjectName.class);
            final J2EEManagedObjectImplBase impl = c.newInstance(parent);
            final String j2eeType = Util.deduceType(intf);
            on = new ObjectNameBuilder( getMBeanServer(), parent).buildChildObjectName( j2eeType, name);
            on = registerChild( impl, on);
        }
        catch( final Exception e )
        {
            e.printStackTrace();
        }

        return on;
    }


    /**
     *  Listen for config resource MBeans that are being registered and unregistered,
     *  and associate them with JSR 77 MBeans for this J2EEServer.
     */
    final class ResourceListener implements NotificationListener
    {
        private final MBeanServer mServer;
        private final ObjectName  mJ2EEServer;
        /** the parent MBeans for config resources MBeans */
        private final ObjectName  mResourcesConfig;
        
        public ResourceListener( final MBeanServer server, final ObjectName j2eeServer, final ObjectName resourcesConfig)
        {
            mServer = server;
            mJ2EEServer = j2eeServer;
            mResourcesConfig = resourcesConfig;
        }
        
        /** consider the MBean to see if it is a resource that should be manifested */
        public void register(final AMXProxy amxConfig)
        {
            if ( ! mResourcesConfig.equals( amxConfig.parent().objectName() ) )
            {
                return;
            }
            
            final String configType = amxConfig.type();
            final Class<J2EEManagedObjectImplBase> implClass = RESOURCE_TYPES.get(configType);
            if ( implClass == null )
            {
                ImplUtil.getLogger().info( "Unrecognized resource type for JSR 77 purposes: " + amxConfig.objectName() );
                return;
            }
            final Class<J2EEManagedObject>  intf = (Class)ClassUtil.getFieldValue(implClass, "INTF");
            final String j2eeType = Util.deduceType(intf);
            
            try
            {
                final ObjectName mbean77 = registerJ2EEChild( mJ2EEServer, intf, implClass, amxConfig.getName() );
                synchronized(mConfigTo77) // prevent race condition where MBean is unregistered before we can put it into our Map
                {
                    mConfigTo77.put( amxConfig.objectName(), mbean77 );
                }
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
            
            //cdebug( "Registered " + child + " for  config resource " + amx.objectName() );
        }

        public void handleNotification( final Notification notifIn, final Object handback)
        {
            if ( ! (notifIn instanceof MBeanServerNotification) ) return;

            final MBeanServerNotification notif = (MBeanServerNotification)notifIn;
            final ObjectName objectName = notif.getMBeanName();
            
            if ( notif.getType().equals( MBeanServerNotification.REGISTRATION_NOTIFICATION ) )
            {
                final AMXProxy amx = getProxyFactory().getProxy(objectName);
                register( amx );
            }
            else if ( notif.getType().equals( MBeanServerNotification.UNREGISTRATION_NOTIFICATION ) )
            {
                // determine if it's a config for which a JSR 77  MBean is registered
                synchronized(mConfigTo77)
                {
                    final ObjectName mbean77 = mConfigTo77.remove(objectName);
                    if ( mbean77 != null )
                    {
                        try
                        {
                            getMBeanServer().unregisterMBean( mbean77 );
                        }
                        catch( final Exception e )
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        
        public void startListening()
        {
            try
            {
                mServer.addNotificationListener( JMXUtil.getMBeanServerDelegateObjectName(), this, null, null);
            }
            catch( final JMException e) {
                throw new RuntimeException(e);
            }
        }
        public void stopListening()
        {
            try
            {
                mServer.removeNotificationListener( JMXUtil.getMBeanServerDelegateObjectName(), this);
            }
            catch( final JMException e) {
                throw new RuntimeException(e);
            }
        }
    }
}





















