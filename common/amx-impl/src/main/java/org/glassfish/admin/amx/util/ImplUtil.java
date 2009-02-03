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
package org.glassfish.admin.amx.util;

import java.util.Set;
import java.util.logging.Logger;

import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.JMException;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.client.ProxyFactory;

import com.sun.logging.LogDomains;
import org.glassfish.server.ServerEnvironmentImpl;

public final class ImplUtil 
{
    private static void debug( final String s ) { System.out.println(s); }
    
    public static Logger getLogger() { return LogDomains.getLogger(ServerEnvironmentImpl.class, LogDomains.ADMIN_LOGGER); }
    
    /**
        Unload this AMX MBean and all its children.
        MBean should be unloaded at the leafs first, working back to DomainRoot so as to
        not violate the rule that a Container must always be present for a Containee.
     */
        public static void
    unregisterAMXMBeans( final AMX top )
    {
        if ( top == null) throw new IllegalArgumentException();
        
        final MBeanServer mbeanServer = (MBeanServer)
            Util.getExtra(top).getConnectionSource().getExistingMBeanServerConnection();
        
        if ( top instanceof Container )
        {
            // unregister all Containees first
            final Set<AMX>  all = ((Container)top).getContaineeSet();
            for( final AMX amx : all )
            {
                unregisterAMXMBeans( amx );
            }
        }
        
        unregisterOneMBean( mbeanServer, Util.getObjectName(top) );
    }
    
    /** see javadoc for unregisterAMXMBeans(AMX) */
        public static void
    unregisterAMXMBeans( final MBeanServer mbs, final ObjectName objectName )
    {
        ImplUtil.getLogger().fine( "Unregister MBean hierarchy for: " + objectName );
        unregisterAMXMBeans( ProxyFactory.getInstance(mbs).getProxy(objectName) );
    }
    
    /**
        Unregister a single MBean, returning true if it was unregistered, false otherwise.
     */
        public static boolean
    unregisterOneMBean( final MBeanServer mbeanServer, final ObjectName objectName )
    {
        boolean success = false;
        try
        {
            getLogger().fine( "UNREGISTER MBEAN: " + objectName );
            if ( mbeanServer.isRegistered(objectName) )
            {
                mbeanServer.unregisterMBean( objectName );
            }
        }
        catch( JMException e )
        {
            getLogger().warning( "unregisterOneMBean: " + objectName + " FAILED: " + ExceptionUtil.toString(e));
        }
        return success;
    }
}































