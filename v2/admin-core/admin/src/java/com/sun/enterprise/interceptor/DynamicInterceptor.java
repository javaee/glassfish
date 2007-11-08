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
package com.sun.enterprise.interceptor;

import java.util.Set;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.io.ObjectInputStream;

import javax.management.*;  // we'll need just about all of them, so avoid clutter
import javax.management.loading.ClassLoaderRepository;


import com.sun.appserv.management.helper.AMXDebugHelper;
import com.sun.appserv.management.util.jmx.JMXUtil;

/**
    This Interceptor wraps the real MBeanServer so that additional interceptor code can be
    "turned on" at a later point.  However, it must be possible to start the MBeanServer even before
    the JVM calls main().  Therefore,
    <b>This class must not depend on anything that can't initialize before the JVM calls main()</b>.
    <i>This includes things like logging which is not happy being invoked
    that early.</i>
    <p>
    When instantiated at startup, the instance of this class that wraps the real MBeanServer
    is termed the "Primary Interceptor".  There can only be one such Interceptor for each
    *real* MBeanServer.  MBeanServer #0 is the Platform MBeanServer, and this class <b>must</b> be
    used for GlassFish.  Additional MBeanServers can be created if desired.
    <p>
    This class can also be used to implement an Interceptor which can be set for use by the Primary
    Interceptor.  Such interceptors are used only for get/setAttribute(s) and invoke(), though
    the use of them could be expanded for other methods.
    <p>
    Note that many methods are declared 'final' for efficiency.  If a subclass needs
    to override a method, remove 'final'. Until that time, we might as well remain efficient,
    since most methods won't be overridden.
 */
public class DynamicInterceptor implements MBeanServer
{
    //private  final Logger sLogger         = Logger.getLogger(AdminConstants.kLoggerName);
    //private  StringManager localStrings   = StringManager.getManager( SunoneInterceptor.class );
    
    private volatile MBeanServer mDelegateMBeanServer;
    
    protected final AMXDebugHelper    mDebug;
    
    protected final Map<String,DynamicInterceptorHook> mHooks;
    
        public
    DynamicInterceptor()
    {
        mDelegateMBeanServer    = null;
        
        mDebug  = new AMXDebugHelper( "__DynamicInterceptor__" );
        mDebug.setEchoToStdOut( false );    // must not print to stdout, or a infinite recursion might result
        debug( "DynamicInterceptor.DynamicInterceptor" );
        
        mHooks = Collections.synchronizedMap( new HashMap<String,DynamicInterceptorHook>() );
    }
    
    /*
        public synchronized void
    addDynamicInterceptorRegistrationHook( final DynamicInterceptorRegistrationHook hook )
    {
        mDynamicRegistrationHooks.add( hook );
    }
    */
    
    /**
        Add a per-domain hook which will be called for all invocations on MBeans. 
        <p>Not supported: interceptors on calls that are not specific to an MBean eg countMBeans().
        <p>Works only for methods in {@link DynamicInterceptorHook).  
     */
        public synchronized void
    addHook( final String jmxDomain, final DynamicInterceptorHook hook )
    {
        if ( mHooks.containsKey( jmxDomain ) )
        {
            throw new IllegalStateException();
        }
        
        debug( "Added hook for JMX domain ", jmxDomain,
            " using hook of class ", hook.getClass().getName() );
        
        mHooks.put( jmxDomain, hook );
    }
    
        protected final void
    debug( final Object... args)
    {
        mDebug.println( args );
    }
    
    /**
        Get the MBeanServer to which the request can be delegated.
     */
       public MBeanServer
    getDelegateMBeanServer()
    {
        return mDelegateMBeanServer;
    }
    
       protected DynamicInterceptorHook
    getHook( final ObjectName objectName )
    {
        return mHooks.get( objectName.getDomain() );
    }
    
    /** May/must be called once exactly once. */ 
        public void
    setDelegateMBeanServer( final MBeanServer server )
    {
        debug( "DynamicInterceptor.setDelegateMBeanServer: " + server.getClass().getName() );
        if ( mDelegateMBeanServer != null )
        {
            throw new IllegalStateException();
        }
        mDelegateMBeanServer    = server;
        
        //System.out.println( "DynamicInterceptor: set mDelegateMBeanServer to " +
        //    "MBeanServer of class " + server.getClass().getName() );
    }
    


        public Object
    invoke(
        final ObjectName objectName,
        final String operationName, 
        final Object[] params,
        final String[] signature) 
        throws ReflectionException, InstanceNotFoundException, MBeanException
    {
        debug( "DynamicInterceptor.invoke(): ", objectName, ".", operationName, "{", params, "}", "{", signature, "}" );

        Object result = null;
        final DynamicInterceptorHook    hook    = getHook(objectName);
        if ( hook != null )
        {
            result  = hook.invoke( objectName, operationName, params, signature );
        }
        else
        {
            result  = getDelegateMBeanServer().invoke( objectName, operationName, params, signature );
        }
        return result;
    }
    
        public final Object
    getAttribute(final ObjectName objectName, final String attributeName) 
        throws InstanceNotFoundException, AttributeNotFoundException, 
               MBeanException, ReflectionException
    {
        Object result = null;
        final DynamicInterceptorHook    hook    = getHook(objectName);
        if ( hook != null )
        {
            result  = hook.getAttribute( objectName, attributeName );
        }
        else
        {
            result  = getDelegateMBeanServer().getAttribute( objectName, attributeName );
        }
        debug( "DynamicInterceptor.getAttribute: ", objectName, attributeName, result );
        return result;
    }
    
        public void
    setAttribute( final ObjectName objectName, final Attribute attribute)
            throws  InstanceNotFoundException, AttributeNotFoundException, 
                    MBeanException, ReflectionException, InvalidAttributeValueException
    {
        debug( "DynamicInterceptor.setAttribute: ", objectName, attribute );

        final DynamicInterceptorHook    hook    = getHook(objectName);
        if ( hook != null )
        {
            hook.setAttribute( objectName, attribute );
        }
        else
        {
            getDelegateMBeanServer().setAttribute( objectName, attribute );
        }
    }

        public final AttributeList 
    getAttributes( final ObjectName objectName, final String[] attrNames) 
        throws InstanceNotFoundException, ReflectionException
    {
        AttributeList result = null;
        final DynamicInterceptorHook    hook    = getHook(objectName);
        if ( hook != null )
        {
            result  = hook.getAttributes( objectName, attrNames );
        }
        else
        {
            result  = getDelegateMBeanServer().getAttributes( objectName, attrNames );
        }
        debug( "DynamicInterceptor.getAttributes: ", objectName, attrNames, result );
        return result;
    }

        public AttributeList
    setAttributes (final ObjectName objectName, final AttributeList attributeList) 
        throws InstanceNotFoundException, ReflectionException
    {
        debug( "DynamicInterceptor.setAttributes: ", objectName, attributeList );
        
        AttributeList result = null;
        final DynamicInterceptorHook    hook    = getHook(objectName);
        if ( hook != null )
        {
            result  = hook.setAttributes( objectName, attributeList );
        }
        else
        {
            result  = getDelegateMBeanServer().setAttributes( objectName, attributeList );
        }
        return result;

    }
    
    private static final boolean    SPECIAL_CHECKS  = true;
        private void
    checkForIllegalMBean( final ObjectName objectName )
    {
        if ( SPECIAL_CHECKS )
        {
            final String jmxDomain = objectName.getDomain();
            if ( jmxDomain.equals( "ias" ) )
            {
                final String msg = "JMX domain 'ias' may not be used: " + JMXUtil.toString(objectName);
                final IllegalArgumentException e    = new IllegalArgumentException( msg );
                debug( msg, "\n", e );
                throw e;
            }
            
            /*
            if ( "server-instance".equals( objectName.getKeyProperty( "type" ) ) )
            {
                final String msg = "MBean " + JMXUtil.toString(objectName) + " is used, why?";
                final IllegalArgumentException e    = new IllegalArgumentException( msg );
                debug( msg, "\n", e );
                throw e;
            }
            */
        }
    }
    
    public final ObjectInstance registerMBean(final Object obj, final ObjectName objectName) 
        throws NotCompliantMBeanException, MBeanRegistrationException, InstanceAlreadyExistsException
    {
        checkForIllegalMBean( objectName );
        
        debug( "DynamicInterceptor.registerMBean: ", objectName, obj.getClass().getName() );
        return getDelegateMBeanServer().registerMBean( obj, objectName );
    }
    
    public final void unregisterMBean(final ObjectName objectName) 
        throws InstanceNotFoundException, MBeanRegistrationException
    {
        debug( "DynamicInterceptor.unregisterMBean: ", objectName );
        getDelegateMBeanServer().unregisterMBean( objectName );
    }
	
    public final Integer getMBeanCount()
    {
        debug( "DynamicInterceptor.getMBeanCount: " );
        return getDelegateMBeanServer().getMBeanCount( );
    }

        public final Set
    queryMBeans( final ObjectName objectName, final QueryExp expr )
    {
        debug( "DynamicInterceptor.queryMBeans: ", objectName, expr );
        return getDelegateMBeanServer().queryMBeans( objectName, expr );
    }

    public final MBeanInfo getMBeanInfo( final ObjectName objectName) throws
        InstanceNotFoundException, IntrospectionException, ReflectionException
    {
        debug( "DynamicInterceptor.getMBeanInfo: ", objectName );
        MBeanInfo result = null;
        
        final DynamicInterceptorHook    hook    = getHook(objectName);
        if ( hook != null )
        {
            result  = hook.getMBeanInfo( objectName );
        }
        else
        {
            result  = getDelegateMBeanServer().getMBeanInfo( objectName );
        }
        return result;
    }

    public final boolean isRegistered( final ObjectName objectName)
    {
        boolean isRegistered    = getDelegateMBeanServer().isRegistered( objectName );
        
        /*
        final DynamicInterceptorHook hook = getHook(objectName);
        if ( (! isRegistered) && objectName != null && hook != null )
        {
            if ( hook.registrationHook( objectName ) )
            {
                isRegistered    = getDelegateMBeanServer().isRegistered( objectName );
            }
        }
        */

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
        debug( "DynamicInterceptor.addNotificationListener: ", objectName, notificationListener.getClass().getName() );
        getDelegateMBeanServer().addNotificationListener(objectName, 
        notificationListener, notificationFilter, obj);
    }

    public final void addNotificationListener(
        final ObjectName objectName, 
        final ObjectName objectName1,
        final NotificationFilter notificationFilter, 
        final Object obj)
        throws InstanceNotFoundException
    {
        debug( "DynamicInterceptor.addNotificationListener: ", objectName, objectName1 );
        getDelegateMBeanServer().addNotificationListener(objectName, objectName1, notificationFilter, obj);
    }

        public final ObjectInstance
    createMBean( final String str, final ObjectName objectName) 
        throws ReflectionException, InstanceAlreadyExistsException, 
                MBeanRegistrationException, MBeanException, NotCompliantMBeanException
    {
        debug( "DynamicInterceptor.createMBean: ", str, objectName );
        return getDelegateMBeanServer().createMBean (str, objectName);
    }

        public final ObjectInstance
    createMBean(
        final String str,
        final ObjectName objectName, 
        final ObjectName objectName2)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, 
            MBeanException, NotCompliantMBeanException, InstanceNotFoundException
    {
        debug( "DynamicInterceptor.createMBean: ", str, objectName, objectName2 );
        return getDelegateMBeanServer().createMBean (str, objectName, objectName2);
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
        debug( "DynamicInterceptor.createMBean: ", str, objectName, obj, str3);
        return getDelegateMBeanServer().createMBean (str, objectName, obj, str3);
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
       debug( "DynamicInterceptor.createMBean: ", str, objectName, objectName2, obj, str4);
       return getDelegateMBeanServer().createMBean (str, objectName, objectName2, obj, str4);
    }

    /* deprecated API @since 1.1 - use with caution */
        public final ObjectInputStream
    deserialize (String str, byte[] values) 
        throws OperationsException, ReflectionException
    {
       debug( "DynamicInterceptor.deserialize: ", str, values);
        return getDelegateMBeanServer().deserialize (str, values);
    }

    /* deprecated API @since 1.1 - use with caution */
        public final ObjectInputStream
    deserialize( final ObjectName objectName, final byte[] values) 
        throws InstanceNotFoundException, OperationsException
    {
        debug( "DynamicInterceptor.deserialize: ", objectName, values);
        return getDelegateMBeanServer().deserialize (objectName, values);
    }

        public final ObjectInputStream
    deserialize( final String str, final ObjectName objectName, 
        byte[] values) throws InstanceNotFoundException, OperationsException, 
        ReflectionException
    {
        debug( "DynamicInterceptor.deserialize: ", str, objectName, values);
        return getDelegateMBeanServer().deserialize (str, objectName, values);
    }

        public final String
    getDefaultDomain()
    {
        debug( "DynamicInterceptor.getDefaultDomain: " );
        return getDelegateMBeanServer().getDefaultDomain();
    }
    
        public final ObjectInstance
    getObjectInstance(ObjectName objectName)
        throws InstanceNotFoundException
    {
        debug( "DynamicInterceptor.getDefaultDomain: getObjectInstance", objectName );
        return getDelegateMBeanServer().getObjectInstance(objectName);
    }
    
        public final Object
    instantiate( final String str)
        throws ReflectionException, MBeanException
    {
        debug( "DynamicInterceptor.instantiate: ", str );
        return getDelegateMBeanServer().instantiate(str);
    }
    
        public final Object
    instantiate( final String str, final ObjectName objectName)
        throws ReflectionException, MBeanException, InstanceNotFoundException
    {
        debug( "DynamicInterceptor.instantiate: ", str, objectName );
        return getDelegateMBeanServer().instantiate(str, objectName);
    }
    
        public final Object
    instantiate(
        final String str, 
        final Object[] obj, 
        final String[] str2)
        throws ReflectionException, MBeanException
    {
        debug( "DynamicInterceptor.instantiate: ", str, obj, str2 );
        return getDelegateMBeanServer().instantiate(str, obj, str2);
    }
    
        public final Object
    instantiate(
        final String str,
        final ObjectName objectName,
        final Object[] obj,
        final String[] str3)
        throws ReflectionException, MBeanException, InstanceNotFoundException
    {
        debug( "DynamicInterceptor.instantiate: ", str, objectName, obj, str3 );
        return getDelegateMBeanServer().instantiate(str, objectName, obj, str3);
    }

        public final boolean
    isInstanceOf ( final ObjectName objectName,  final String str) 
        throws InstanceNotFoundException
    {
        debug( "DynamicInterceptor.isInstanceOf: ", objectName, str );
        return getDelegateMBeanServer().isInstanceOf(objectName, str);
    }

        public final Set
    queryNames( final ObjectName objectName, final QueryExp queryExp)
    {
        debug( "DynamicInterceptor.queryNames: ", objectName, queryExp );
        return getDelegateMBeanServer().queryNames( objectName, queryExp);
    }

        public final void
    removeNotificationListener(final ObjectName objectName,  final ObjectName objectName1) 
        throws InstanceNotFoundException, ListenerNotFoundException
    {
        debug( "DynamicInterceptor.removeNotificationListener: ", objectName, objectName1 );
        getDelegateMBeanServer().removeNotificationListener( objectName, objectName1);
    }

        public final void
    removeNotificationListener(
        final ObjectName objectName, 
        final NotificationListener notificationListener)
        throws InstanceNotFoundException, ListenerNotFoundException
    {
        debug( "DynamicInterceptor.removeNotificationListener: ", objectName, notificationListener );
        getDelegateMBeanServer().removeNotificationListener( objectName, notificationListener);
    }
      
        public final void
    removeNotificationListener(
        final ObjectName            objectName, 
        final NotificationListener  notificationListener,
        final NotificationFilter    notificationFilter,
        final Object                obj)
            throws InstanceNotFoundException, ListenerNotFoundException
    {
        debug( "DynamicInterceptor.removeNotificationListener: ",
            objectName, notificationListener, notificationFilter, obj);
        getDelegateMBeanServer().removeNotificationListener(objectName, notificationListener, notificationFilter, obj);
    }
    
        public final void
    removeNotificationListener(
        final ObjectName            objectName, 
        final ObjectName            objectName1,
        final NotificationFilter    notificationFilter, 
        final Object                obj) 
        throws InstanceNotFoundException, ListenerNotFoundException
    {
        debug( "DynamicInterceptor.removeNotificationListener: ",
            objectName, objectName1, notificationFilter, obj);
        getDelegateMBeanServer().removeNotificationListener( objectName, objectName1, notificationFilter, obj);
    }

        public final ClassLoader
    getClassLoader( final ObjectName objectName) 
        throws InstanceNotFoundException
    {
        debug( "DynamicInterceptor.getClassLoader: ", objectName);
            
        ClassLoader result = null;
        
        final DynamicInterceptorHook    hook    = getHook(objectName);
        if ( hook != null )
        {
        debug( "calling hook" );
            result  = hook.getClassLoader( objectName );
        }
        else
        {
            result  = getDelegateMBeanServer().getClassLoader( objectName );
        }
        return result;
    }    
    
        public final ClassLoader
    getClassLoaderFor( final ObjectName objectName) 
        throws InstanceNotFoundException
    {
        debug( "DynamicInterceptor.getClassLoaderFor: ", objectName);
        ClassLoader result = null;
        
        final DynamicInterceptorHook    hook    = getHook(objectName);
        if ( hook != null )
        {
        debug( "calling hook" );
            result  = hook.getClassLoaderFor( objectName );
        }
        else
        {
            result  = getDelegateMBeanServer().getClassLoaderFor( objectName );
        }
        return result;
    }
    
        public final ClassLoaderRepository
    getClassLoaderRepository()
    {
        debug( "DynamicInterceptor.getClassLoaderRepository" );
		return getDelegateMBeanServer().getClassLoaderRepository();
    }
    
        public final String[]
    getDomains()
    {
        debug( "DynamicInterceptor.getDomains" );
        return getDelegateMBeanServer().getDomains();
    }
}






