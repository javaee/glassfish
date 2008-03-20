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
package org.glassfish.admin.mbeanserver;

import java.util.Set;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.io.ObjectInputStream;

import javax.management.*;

import javax.management.loading.ClassLoaderRepository;


/**
    Wraps the MBeanServe so as to allow lazy-loading of AMX MBeans
 */
public final class AppserverMBeanServer implements MBeanServer
{
    private final String         mDefaultDomain;
    private volatile MBeanServer mTargetMBeanServer;
    
    private static final AppserverMBeanServer INSTANCE = new AppserverMBeanServer( "" );
    
    private AppserverMBeanServer( final String defaultDomain )
    {
        mDefaultDomain = defaultDomain;
        
        // can't initialize right here--we can't be sure an MBeanServer doesn't have other threads
        // defer to setup()
        mTargetMBeanServer = null;
    }
    
    private void setup() 
    {
        final MBeanServerBuilder builder = new MBeanServerBuilder();
        mTargetMBeanServer = builder.newMBeanServer( "", this, builder.newMBeanServerDelegate() );
    }
    
        public static synchronized MBeanServer
    getInstance()
    {
        if ( INSTANCE.mTargetMBeanServer == null )
        {
            INSTANCE.setup();
        }
        
        return INSTANCE;
    }
    
    private static final boolean DEBUG = false;
    private static void debug( final Object... items )
    {
        if ( DEBUG && items != null )
        {
            String msg = "";
            
            for( int i = 0; i < items.length; ++i )
            {
                msg = msg + items[i];
            }
            
            System.out.println( msg );
        }
    }

//--------------------------------------------------------------------------------------------

// UGLY: hard-coding "amx" and it's startup ObjectName (we can't depend on amx-api or amx-impl).
// Move these elsewhere?
    private static final String AMX_DOMAIN = "amx";
    private static final ObjectName AMX_STARTUP_OBJECT_NAME = getAMXStartupObjectName();
    
        private static ObjectName
    getAMXStartupObjectName()
    {
        try { return new ObjectName( "amx-support:name=startup" ); } catch( JMException e ){ throw new Error("impossible");}
    }
    
    private static volatile boolean AMX_STARTED = false;
    
    /**
       Ensure that AMX MBeans are loaded; a request has come in for one.
     */
        private void
    ensureAMXLoaded()
    {
        if ( (! AMX_STARTED) )
        {
            if ( isRegistered( AMX_STARTUP_OBJECT_NAME ) )
            {
                // thread safety is not an issue here; it's safe to call startAMX() more than
                // once.  Set this flag to indicate we've tried to initalize AMX; never try
                // more than once since it should always work or there's a bug.
                AMX_STARTED = true;
                
                System.out.println( "AppserverMBeanServer: loading AMX MBeans" );
                try
                {
                    invoke( AMX_STARTUP_OBJECT_NAME, "startAMX", null, null);
                }
                catch( Exception e )
                {
                    e.printStackTrace();
                }
            }
            else
            {
                System.out.println( "WARNING: request for AMX but AMXStartupService not available" );
            }
        }
    }
    
    /**
       Check if the ObjectName uses the AMX domain name.  Special checks are done for wildcards
       in the relevant queryNames() and queryMBeans() methods.
     */
        private void
    ensureAMXLoaded( final ObjectName objectName )
    {
        if ( ! AMX_STARTED && objectName.getDomain().equals(AMX_DOMAIN) )
        {
            ensureAMXLoaded();
        }
    }
    
//--------------------------------------------------------------------------------------------
        
    /**
        Get the MBeanServer to which the request can be delegated.
     */
       public MBeanServer
    getTargetMBeanServer()
    {
        return mTargetMBeanServer;
    }

        public Object
    invoke(
        final ObjectName objectName,
        final String operationName, 
        final Object[] params,
        final String[] signature) 
        throws ReflectionException, InstanceNotFoundException, MBeanException
    {
        ensureAMXLoaded(objectName);
        
        debug( "AppserverMBeanServer.invoke(): ", objectName, ".", operationName, "{", params, "}", "{", signature, "}" );

        Object result =  getTargetMBeanServer().invoke( objectName, operationName, params, signature );
        return result;
    }
    
        public final Object
    getAttribute(final ObjectName objectName, final String attributeName) 
        throws InstanceNotFoundException, AttributeNotFoundException, 
               MBeanException, ReflectionException
    {
        ensureAMXLoaded(objectName);
        
        Object result = getTargetMBeanServer().getAttribute( objectName, attributeName );
        debug( "AppserverMBeanServer.getAttribute: ", objectName, attributeName, result );
        return result;
    }
    
        public void
    setAttribute( final ObjectName objectName, final Attribute attribute)
            throws  InstanceNotFoundException, AttributeNotFoundException, 
                    MBeanException, ReflectionException, InvalidAttributeValueException
    {
        ensureAMXLoaded(objectName);
        debug( "AppserverMBeanServer.setAttribute: ", objectName, attribute );

        getTargetMBeanServer().setAttribute( objectName, attribute );
    }

        public final AttributeList 
    getAttributes( final ObjectName objectName, final String[] attrNames) 
        throws InstanceNotFoundException, ReflectionException
    {
        ensureAMXLoaded(objectName);
        AttributeList result = getTargetMBeanServer().getAttributes( objectName, attrNames );
        debug( "AppserverMBeanServer.getAttributes: ", objectName, attrNames, result );
        return result;
    }

        public AttributeList
    setAttributes (final ObjectName objectName, final AttributeList attributeList) 
        throws InstanceNotFoundException, ReflectionException
    {
        ensureAMXLoaded(objectName);
        debug( "AppserverMBeanServer.setAttributes: ", objectName, attributeList );
        
        AttributeList result = getTargetMBeanServer().setAttributes( objectName, attributeList );
        return result;

    }
    
            
    public final ObjectInstance registerMBean(final Object obj, final ObjectName objectName) 
        throws NotCompliantMBeanException, MBeanRegistrationException, InstanceAlreadyExistsException
    {
        debug( "AppserverMBeanServer.registerMBean: ", objectName, obj.getClass().getName() );
        return getTargetMBeanServer().registerMBean( obj, objectName );
    }
    
    public final void unregisterMBean(final ObjectName objectName) 
        throws InstanceNotFoundException, MBeanRegistrationException
    {
        debug( "AppserverMBeanServer.unregisterMBean: ", objectName );
        getTargetMBeanServer().unregisterMBean( objectName );
    }
	
    public final Integer getMBeanCount()
    {
        debug( "AppserverMBeanServer.getMBeanCount: " );
        return getTargetMBeanServer().getMBeanCount( );
    }

   

    public final MBeanInfo getMBeanInfo( final ObjectName objectName) throws
        InstanceNotFoundException, IntrospectionException, ReflectionException
    {
        ensureAMXLoaded(objectName);
        debug( "AppserverMBeanServer.getMBeanInfo: ", objectName );
        MBeanInfo result = getTargetMBeanServer().getMBeanInfo( objectName );
        return result;
    }

    public final boolean isRegistered( final ObjectName objectName)
    {
        ensureAMXLoaded(objectName);
        boolean isRegistered    = getTargetMBeanServer().isRegistered( objectName );
        return isRegistered;
    }

        public final void
    addNotificationListener(
        final ObjectName objectName, 
        final NotificationListener notificationListener, 
        final NotificationFilter notificationFilter,
        final Object obj) 
        throws InstanceNotFoundException
    {
        debug( "AppserverMBeanServer.addNotificationListener: ", objectName, notificationListener.getClass().getName() );
        getTargetMBeanServer().addNotificationListener(objectName,  notificationListener, notificationFilter, obj);
    }

    public final void addNotificationListener(
        final ObjectName objectName, 
        final ObjectName objectName1,
        final NotificationFilter notificationFilter, 
        final Object obj)
        throws InstanceNotFoundException
    {
        debug( "AppserverMBeanServer.addNotificationListener: ", objectName, objectName1 );
        getTargetMBeanServer().addNotificationListener(objectName, objectName1, notificationFilter, obj);
    }

        public final ObjectInstance
    createMBean( final String str, final ObjectName objectName) 
        throws ReflectionException, InstanceAlreadyExistsException, 
                MBeanRegistrationException, MBeanException, NotCompliantMBeanException
    {
        debug( "AppserverMBeanServer.createMBean: ", str, objectName );
        return getTargetMBeanServer().createMBean (str, objectName);
    }

        public final ObjectInstance
    createMBean(
        final String str,
        final ObjectName objectName, 
        final ObjectName objectName2)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, 
            MBeanException, NotCompliantMBeanException, InstanceNotFoundException
    {
        debug( "AppserverMBeanServer.createMBean: ", str, objectName, objectName2 );
        return getTargetMBeanServer().createMBean (str, objectName, objectName2);
    }

        public final ObjectInstance
    createMBean(
        final String        str,
        final ObjectName    objectName, 
        final Object[]      obj,
        final String[]      str3) 
            throws ReflectionException, InstanceAlreadyExistsException, 
            MBeanRegistrationException, MBeanException, NotCompliantMBeanException
    {
        debug( "AppserverMBeanServer.createMBean: ", str, objectName, obj, str3);
        return getTargetMBeanServer().createMBean (str, objectName, obj, str3);
    }

        public final ObjectInstance
    createMBean (
        final String        str,
        final ObjectName    objectName, 
        final ObjectName    objectName2,
        final Object[]      obj,
        final String[]      str4) 
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
            MBeanException, NotCompliantMBeanException, InstanceNotFoundException
    {
       debug( "AppserverMBeanServer.createMBean: ", str, objectName, objectName2, obj, str4);
       return getTargetMBeanServer().createMBean (str, objectName, objectName2, obj, str4);
    }

    /* deprecated API @since 1.1 - use with caution */
        public final ObjectInputStream
    deserialize (String str, byte[] values) 
        throws OperationsException, ReflectionException
    {
       debug( "AppserverMBeanServer.deserialize: ", str, values);
        return getTargetMBeanServer().deserialize (str, values);
    }

    /* deprecated API @since 1.1 - use with caution */
        public final ObjectInputStream
    deserialize( final ObjectName objectName, final byte[] values) 
        throws InstanceNotFoundException, OperationsException
    {
        debug( "AppserverMBeanServer.deserialize: ", objectName, values);
        return getTargetMBeanServer().deserialize (objectName, values);
    }

        public final ObjectInputStream
    deserialize( final String str, final ObjectName objectName, 
        byte[] values) throws InstanceNotFoundException, OperationsException, 
        ReflectionException
    {
        debug( "AppserverMBeanServer.deserialize: ", str, objectName, values);
        return getTargetMBeanServer().deserialize (str, objectName, values);
    }

        public final String
    getDefaultDomain()
    {
        debug( "AppserverMBeanServer.getDefaultDomain: " );
        return getTargetMBeanServer().getDefaultDomain();
    }
    
        public final ObjectInstance
    getObjectInstance(ObjectName objectName)
        throws InstanceNotFoundException
    {
        debug( "AppserverMBeanServer.getDefaultDomain: getObjectInstance", objectName );
        return getTargetMBeanServer().getObjectInstance(objectName);
    }
    
        public final Object
    instantiate( final String str)
        throws ReflectionException, MBeanException
    {
        debug( "AppserverMBeanServer.instantiate: ", str );
        return getTargetMBeanServer().instantiate(str);
    }
    
        public final Object
    instantiate( final String str, final ObjectName objectName)
        throws ReflectionException, MBeanException, InstanceNotFoundException
    {
        debug( "AppserverMBeanServer.instantiate: ", str, objectName );
        return getTargetMBeanServer().instantiate(str, objectName);
    }
    
        public final Object
    instantiate(
        final String str, 
        final Object[] obj, 
        final String[] str2)
        throws ReflectionException, MBeanException
    {
        debug( "AppserverMBeanServer.instantiate: ", str, obj, str2 );
        return getTargetMBeanServer().instantiate(str, obj, str2);
    }
    
        public final Object
    instantiate(
        final String str,
        final ObjectName objectName,
        final Object[] obj,
        final String[] str3)
        throws ReflectionException, MBeanException, InstanceNotFoundException
    {
        debug( "AppserverMBeanServer.instantiate: ", str, objectName, obj, str3 );
        return getTargetMBeanServer().instantiate(str, objectName, obj, str3);
    }

        public final boolean
    isInstanceOf ( final ObjectName objectName,  final String str) 
        throws InstanceNotFoundException
    {
        debug( "AppserverMBeanServer.isInstanceOf: ", objectName, str );
        return getTargetMBeanServer().isInstanceOf(objectName, str);
    }

        private static boolean
    mightBeAMX( final ObjectName objectName )
    {
        // see JMX Javadoc: null means "*"
        if ( objectName == null ) return true;
        
        final String domain = objectName.getDomain();
        return domain.equals( AMX_DOMAIN ) || domain.equals( "*" );
    }
    
        public final Set
    queryNames( final ObjectName objectName, final QueryExp queryExp)
    {
        if ( (! AMX_STARTED) && mightBeAMX(objectName) )
        {
            ensureAMXLoaded();
        }
        
        debug( "AppserverMBeanServer.queryNames: ", objectName, queryExp );
        return getTargetMBeanServer().queryNames( objectName, queryExp);
    }
    
         public final Set
    queryMBeans( final ObjectName objectName, final QueryExp expr )
    {
        if ( (! AMX_STARTED) && mightBeAMX(objectName) )
        {
            ensureAMXLoaded();
        }
        
        debug( "AppserverMBeanServer.queryMBeans: ", objectName, expr );
        return getTargetMBeanServer().queryMBeans( objectName, expr );
    }

        public final void
    removeNotificationListener(final ObjectName objectName,  final ObjectName objectName1) 
        throws InstanceNotFoundException, ListenerNotFoundException
    {
        debug( "AppserverMBeanServer.removeNotificationListener: ", objectName, objectName1 );
        getTargetMBeanServer().removeNotificationListener( objectName, objectName1);
    }

        public final void
    removeNotificationListener(
        final ObjectName objectName, 
        final NotificationListener notificationListener)
        throws InstanceNotFoundException, ListenerNotFoundException
    {
        debug( "AppserverMBeanServer.removeNotificationListener: ", objectName, notificationListener );
        getTargetMBeanServer().removeNotificationListener( objectName, notificationListener);
    }
      
        public final void
    removeNotificationListener(
        final ObjectName            objectName, 
        final NotificationListener  notificationListener,
        final NotificationFilter    notificationFilter,
        final Object                obj)
            throws InstanceNotFoundException, ListenerNotFoundException
    {
        debug( "AppserverMBeanServer.removeNotificationListener: ",
            objectName, notificationListener, notificationFilter, obj);
        getTargetMBeanServer().removeNotificationListener(objectName, notificationListener, notificationFilter, obj);
    }
    
        public final void
    removeNotificationListener(
        final ObjectName            objectName, 
        final ObjectName            objectName1,
        final NotificationFilter    notificationFilter, 
        final Object                obj) 
        throws InstanceNotFoundException, ListenerNotFoundException
    {
        debug( "AppserverMBeanServer.removeNotificationListener: ",
            objectName, objectName1, notificationFilter, obj);
        getTargetMBeanServer().removeNotificationListener( objectName, objectName1, notificationFilter, obj);
    }

        public final ClassLoader
    getClassLoader( final ObjectName objectName) 
        throws InstanceNotFoundException
    {
        ensureAMXLoaded(objectName);
        debug( "AppserverMBeanServer.getClassLoader: ", objectName);
            
        ClassLoader result = getTargetMBeanServer().getClassLoader( objectName );
        return result;
    }    
    
        public final ClassLoader
    getClassLoaderFor( final ObjectName objectName) 
        throws InstanceNotFoundException
    {
        ensureAMXLoaded(objectName);
        
        debug( "AppserverMBeanServer.getClassLoaderFor: ", objectName);
        ClassLoader result = getTargetMBeanServer().getClassLoaderFor( objectName );
        return result;
    }
    
        public final ClassLoaderRepository
    getClassLoaderRepository()
    {
        debug( "AppserverMBeanServer.getClassLoaderRepository" );
		return getTargetMBeanServer().getClassLoaderRepository();
    }
    
        public final String[]
    getDomains()
    { 
        ensureAMXLoaded();

        debug( "AppserverMBeanServer.getDomains" );
        return getTargetMBeanServer().getDomains();
    }
    
}







