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
package com.sun.enterprise.admin.server.core.jmx;

import java.util.Set;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.ObjectInputStream;

//JMX imports 
import javax.management.*;  // we'll need just about all of them, so avoid clutter
import javax.management.loading.ClassLoaderRepository;


import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.server.core.AdminNotificationHelper;
import com.sun.enterprise.admin.event.EventContext;
import com.sun.enterprise.admin.event.EventStack;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigChange;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.logging.LogDomains;

import com.sun.appserv.management.helper.AMXDebugHelper;
import com.sun.appserv.management.util.misc.TypeCast;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.enterprise.interceptor.DynamicInterceptorHook;

import com.sun.enterprise.admin.server.core.ConfigInterceptor;


/**
    Checks for certain invocations that require flusing the config, and does so if needed.
 */
public class FlushConfigHook implements DynamicInterceptorHook
{
    private static final Logger sLogger = Logger.getLogger(AdminConstants.kLoggerName);
            
    private MBeanServer  mSuppliedMBeanServer;
    private MBeanServer  mDelegateMBeanServer;
    private AdminContext mAdminContext;
    private AdminNotificationHelper     mAdminNotificationHelper;
    private AMXDebugHelper      mDebug;
    //private Logger              mLogger;
    
    static final boolean    USE_OLD_CONFIG_INTERCEPTOR = true;
    static final boolean    EVENT_STACK_ISNT_POINTLESS = false;
    
        public
    FlushConfigHook( final AdminContext adminContext )
    {
        mDebug  = new AMXDebugHelper( "__FlushConfigHook__" );
        mSuppliedMBeanServer    = null;
        mDelegateMBeanServer    = null;
        
        mAdminContext   = adminContext;
        mAdminNotificationHelper = new AdminNotificationHelper( mAdminContext );
        //mLogger = LogDomains.getLogger(AdminConstants.kLoggerName);
        
        mDebug.setEchoToStdOut( false );
        debug( "FlushConfigHook created OK" );
    }

        private void
    debug( final Object...  args)
    {
        if ( mDebug.getDebug() )
        {
            mDebug.println( args );
        }
    }
    
        private AdminNotificationHelper
    getAdminNotificationHelper()
    {
        return mAdminNotificationHelper;
    }
    
        public void
    setDelegateMBeanServer( final MBeanServer mbs)
    {
        if ( mbs == null )
        {
            throw new IllegalArgumentException();
        }
        if ( mSuppliedMBeanServer != null )
        {
            throw new IllegalArgumentException( "already have an MBeanServer" );
        }
        
        mSuppliedMBeanServer    = mbs;
        
        if ( USE_OLD_CONFIG_INTERCEPTOR )
        {
            debug( "FlushConfigHook.setDelegateMBeanServer(): instantiating ConfigInterceptor" );
            final ConfigInterceptor configInterceptor = new ConfigInterceptor( mAdminContext );
            mDelegateMBeanServer = (MBeanServer)com.sun.enterprise.admin.util.proxy.ProxyFactory.createProxy(
                        MBeanServer.class, mSuppliedMBeanServer,
                        configInterceptor );
            debug( "FlushConfigHook.setDelegateMBeanServer(): instantiated ConfigInterceptor" );
        }
        else
        {
            mDelegateMBeanServer    = mbs;
        }
    }
    
       public AdminContext
    getAdminContext()
    {
        return mAdminContext;
    }
        private static final boolean    ENABLED = false;
    
        private final boolean
    isRelevantInvoke( final ObjectName objectName, final String operationName )
    {
        boolean   relevant    = false;
        
        final String domain = objectName.getDomain();
        if ( ENABLED && domain.equals( "com.sun.appserv" ) )
        {
            if ( "config".equals( objectName.getKeyProperty( "category" ) ) ||
                "server-instance".equals( objectName.getKeyProperty( "type" ) ) )
            {
                relevant    = true;
                
                // common cases, covers probably 90%
                if (    operationName.startsWith( "get " ) ||
                        operationName.startsWith( "is " ) ||
                        operationName.startsWith( "list" ) )
                {
                    relevant    = false;
                }
            }
        }
        return relevant;
    }
  
    private static InheritableThreadLocal sDepth = new InheritableThreadLocal() {
        protected synchronized Object initialValue() {
            return INTEGER_0;
        }
    };
    private static Integer  getDepth()  { return (Integer)sDepth.get(); }
    private static void     setDepth( final Integer i)  { sDepth.set(i); }
    private static final Integer INTEGER_0  = new Integer(0);
    private static final Integer INTEGER_1  = new Integer(1);
    
    private static boolean  LOG_CONFIG_CHANGES   = true;
    
        private void
    dumpEventStack( final EventStack eventStack )
    {
        if ( LOG_CONFIG_CHANGES )
        {
            final List<ConfigChange> changes = TypeCast.asList( eventStack.getConfigChangeList() );
            String msg  = "CONFIG CHANGES: " + changes.size() + ": \n{";
            
            for ( final com.sun.enterprise.config.ConfigChange configChange : changes )
            {
                msg += configChange.getConfigChangeType() + "=" + configChange.getName() + ": " +
                        configChange.getXPath();
            }
            msg = msg + "}";
            debug( msg );
        }
    }
    
        private EventStack
    setupEventStack( final Integer startDepthInteger )
    {
        EventStack eventStack    = null;
        if ( EVENT_STACK_ISNT_POINTLESS )
        {
            if ( startDepthInteger.intValue() == 0 )
            {
                debug( "FlushConfigHook.setupEventStack(): startDepth = 0");
                eventStack = new EventStack();
                eventStack.setConfigContext( getAdminContext().getAdminConfigContext() );
                EventContext.setEventStackToThreadLocal(eventStack);
            }
            else
            {
                debug( "FlushConfigHook.setupEventStack(): startDepth = ", startDepthInteger);
            }
        }
        return eventStack;
    }
    
        private void
    checkDepth(
        final Integer       startDepthInteger,
        final EventStack    eventStack,
        final boolean       success )
        throws ReflectionException
    {
        setDepth( startDepthInteger );
        if ( startDepthInteger.intValue() == 0 && success )
        {
            final ConfigContext configContext = getAdminContext().getAdminConfigContext();
            final boolean   needFlush   = configContext.isChanged();
            if ( needFlush )
            {
                debug( "FlushConfigHook.checkDepth(): CHANGED, need flush"); 
                dumpEventStack( eventStack );
                
                debug( "FlushConfigHook.checkDepth: Config has changed, flushing..." );
                try
                {
                    configContext.flush();
                }
                catch( ConfigException e )
                {
                    throw new ReflectionException( e );
                }
                debug( "FlushConfigHook.checkDepth: done flushing changed config, sending Notification" );
                
                // NOTE: this uses the ThreadLocal EventStack from the EventContext set set earlie
                if ( EVENT_STACK_ISNT_POINTLESS )
                {
                    getAdminNotificationHelper().sendNotification();
                    debug( "FlushConfigHook.checkDepth: sending Notification of changed config" );
                }
            }
            else
            {
                debug( "FlushConfigHook.checkDepth(): unchanged, no flush needed"); 
            }
        }
    }
    
        public Object
    invoke(
        final ObjectName objectName,
        final String operationName, 
        final Object[] params,
        final String[] signature) 
        throws ReflectionException, InstanceNotFoundException, MBeanException
    {
        Object  result  = null;
        
        debug( "FlushConfigHook.invoke(): ", objectName, ".", operationName, "{", params, "}", "{", signature, "}" );
        
        if ( USE_OLD_CONFIG_INTERCEPTOR )
        {
            result = mDelegateMBeanServer.invoke( objectName, operationName, params, signature );
        }
        else
        {
            if ( isRelevantInvoke( objectName,  operationName ) )
            {
                debug( "FlushConfigHook.invoke: ", operationName, "() START" );
                            
                final Integer startDepthInteger   = getDepth();
                final int startDepth = startDepthInteger.intValue();
                setDepth( startDepth == 0 ? INTEGER_1 : new Integer( startDepth + 1 ) );
                final EventStack eventStack    = setupEventStack( startDepthInteger );
                
                boolean success = false;
                try
                {
                    debug( "FlushConfigHook.invoke: ", operationName, "(): NEW EventStack");
                    result = mDelegateMBeanServer.invoke( objectName, operationName, params, signature );
                    success = true;
                }
                finally
                {
                    checkDepth( startDepthInteger, eventStack, success );
                }
            }
            else
            {
                result = mSuppliedMBeanServer.invoke( objectName, operationName, params, signature );
            }
        }
        
        debug( "FlushConfigHook.invoke: ", objectName, operationName, "() DONE" );

        return result;
    }
        
        public void
    setAttribute( final ObjectName objectName, final Attribute attribute)
            throws  InstanceNotFoundException, AttributeNotFoundException, 
                    MBeanException, ReflectionException, InvalidAttributeValueException
    {
        debug( "FlushConfigHook.setAttribute(): ", objectName, ", ", attribute.getName(), "=", attribute.getValue() );
        
        if ( USE_OLD_CONFIG_INTERCEPTOR )
        {
            mDelegateMBeanServer.setAttribute( objectName, attribute );
        }
        else
        {
            final Integer startDepthInteger   = getDepth();
            final int startDepth = startDepthInteger.intValue();
            setDepth( startDepth == 0 ? INTEGER_1 : new Integer( startDepth + 1 ) );
            final EventStack eventStack    = setupEventStack( startDepthInteger );
            boolean success = false;
            try
            {
                mDelegateMBeanServer.setAttribute( objectName, attribute );
                success = true;
            }
            finally
            {
                checkDepth( startDepthInteger, eventStack, success );
            }
        }
    }

        public AttributeList
    setAttributes (final ObjectName objectName, final AttributeList attributeList) 
        throws InstanceNotFoundException, ReflectionException
    {
        debug( "FlushConfigHook.setAttributes(): ", objectName, ", ", attributeList);
        
         AttributeList result    = null;
            
        if ( USE_OLD_CONFIG_INTERCEPTOR )
        {
            result  = mDelegateMBeanServer.setAttributes( objectName, attributeList );
        }
        else
        {
            final Integer startDepthInteger   = getDepth();
            final int startDepth = startDepthInteger.intValue();
            setDepth( startDepth == 0 ? INTEGER_1 : new Integer( startDepth + 1 ) );
            final EventStack eventStack    = setupEventStack( startDepthInteger );
            boolean success = false;
            try
            {
                result  = mDelegateMBeanServer.setAttributes( objectName, attributeList );
                success = true;
            }
            finally
            {
                checkDepth( startDepthInteger, eventStack, success );
            }
        }
        return result;
    }
    
    
    public Object  getAttribute( final ObjectName objectName, final String attrName) 
        throws InstanceNotFoundException, ReflectionException, MBeanException,
        AttributeNotFoundException
    {
        final Object result = mDelegateMBeanServer.getAttribute( objectName, attrName );
        
        return result; 
    }


    public AttributeList  getAttributes( final ObjectName objectName, final String[] attrNames) 
        throws InstanceNotFoundException, ReflectionException
    {
        final AttributeList result = mDelegateMBeanServer.getAttributes( objectName, attrNames );
        
        return result;
    }    
 
 
        
    public ClassLoader getClassLoader( final ObjectName objectName)
        throws InstanceNotFoundException
    {
        return mDelegateMBeanServer.getClassLoader( objectName );
    }
    
    
    public ClassLoader getClassLoaderFor( final ObjectName objectName)
        throws InstanceNotFoundException
    {
        return mDelegateMBeanServer.getClassLoaderFor( objectName );
    }
    
    public MBeanInfo getMBeanInfo( final ObjectName objectName)
        throws InstanceNotFoundException, IntrospectionException, ReflectionException
    {
        return mDelegateMBeanServer.getMBeanInfo( objectName );
    }

}






