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
package org.glassfish.admin.amx.impl.loader;

import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.api.amx.AMXValues;

import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.amx.util.jmx.MBeanRegistrationListener;
import org.glassfish.admin.amx.util.SetUtil;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.Set;


/**
    Loader for the JSR 77 management MBeans.  Some are loaded automagically by tracking
    corresponding config MBeans.  The remainder (which have no config) are loaded elsewhere.
 */
public final class J2EELoader
{
    protected static void debug( final String s ) { System.out.println(s); }
    
    private final MBeanServer mMBeanServer;
    private final ConfigListener mConfigListener;
    
    public J2EELoader( final MBeanServer mbeanServer )
    {
        mMBeanServer = mbeanServer;
        ConfigListener configListener = null;
        try
        {
            configListener = new ConfigListener(mbeanServer);
        }
        catch( IOException e )
        {
            // can't really happen on a local MBeanServer
            throw new RuntimeException(e);
        }
        mConfigListener = configListener;
    }
    
    public synchronized void start()
    {
        try
        {
            mConfigListener.startListening();
        }
        catch( Exception e )
        {
            throw new RuntimeException(e);
        }
    }
    
    private static final Set<String> SYNC_TYPES = SetUtil.newUnmodifiableStringSet(
        /*
        XTypes.J2EE_APPLICATION_CONFIG,
        XTypes.WEB_MODULE_CONFIG,
        XTypes.EJB_MODULE_CONFIG,
        XTypes.APP_CLIENT_MODULE_CONFIG,
        XTypes.RAR_MODULE_CONFIG,
        
        XTypes.CLUSTERED_SERVER_CONFIG,
        XTypes.STANDALONE_SERVER_CONFIG,
        
        XTypes.RESOURCE_ADAPTER_CONFIG,
        
        XTypes.MAIL_RESOURCE_CONFIG,
        XTypes.JNDI_RESOURCE_CONFIG,
        XTypes.JDBC_RESOURCE_CONFIG
        */
        // and more
        );
    private final class ConfigListener extends MBeanRegistrationListener
    {
        public ConfigListener( final MBeanServer mbeanServer )
            throws IOException
        {
            super( "J2EELoader.ConfigListener", mbeanServer, JMXUtil.newObjectNamePattern( AMXValues.amxJMXDomain(), JMXUtil.WILD_ALL) );
        }
    
            protected void
        mbeanRegistered( final ObjectName objectName )
        {
            final String type = objectName.getKeyProperty( AMXValues.TYPE_KEY );
            
            if ( SYNC_TYPES.contains( type ) )
            {
                //debug( "ConfigListener.mbeanRegistered: should sync up with: " + objectName );
            }
            
        }
        
        protected void mbeanUnregistered( final ObjectName objectName )
        {
            final String type = objectName.getKeyProperty( AMXValues.TYPE_KEY );
            
            if ( SYNC_TYPES.contains( type ) )
            {
                //debug( "ConfigListener.mbeanUnregistered: should sync up with: " + objectName );
            }
        }
    } 
}












