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
package com.sun.appserv.management.client;

import java.util.Set;
import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.XTypes;

/**
	AMX must be "booted" before use.
 */
public final class AMXBooter
{
    private AMXBooter() {}
    
    public static final ObjectName AMX_BOOTER_OBJECT_NAME = JMXUtil.newObjectName("amx-support:name=booter");
    public static final String BOOT_AMX_OPERATION_NAME = "bootAMX";
    
    
    /**
        Ensure that AMX is loaded and ready to go.  Can be called more than once.
        @param conn connection to the MBeanServer
        @return the ObjectName of {@link DomainRoot}
     */
    public static ObjectName bootAMX( final MBeanServerConnection conn)
    {
        ObjectName domainRootObjectName = findDomainRoot(conn);
        
        if ( domainRootObjectName == null )
        { 
            try
            {
                domainRootObjectName = (ObjectName)conn.invoke( AMX_BOOTER_OBJECT_NAME, BOOT_AMX_OPERATION_NAME, null, null );
            }
            catch( final Exception e )
            {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return domainRootObjectName;
    }
    
    /**
        @return the ObjectName of DomainRoot if it exists, otherwise null
     */
        public static ObjectName
	findDomainRoot( final MBeanServerConnection conn )
	{
        ObjectName objectName = null;
		final ObjectName pattern = JMXUtil.newObjectName( AMX.JMX_DOMAIN + ":" + AMX.J2EE_TYPE_KEY + "=" + XTypes.DOMAIN_ROOT + ",*");
		
        try
        {
            final Set<ObjectName>	objectNames	= JMXUtil.queryNames( conn, pattern, null );
            if ( objectNames.size() > 1 )
            {
                throw new IllegalStateException( "Found more than one DomainRoot using " + pattern);
            }
            else if ( objectNames.size() ==  1 )
            {
                objectName	= GSetUtil.getSingleton( objectNames );
            }
        }
        catch ( final IOException e )
        {
            throw new RuntimeException(e);
        }
		
		return( objectName );
	}	
}



















