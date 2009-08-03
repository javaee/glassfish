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
package org.glassfish.api.amx;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import static org.glassfish.external.amx.AMX.*;
import org.glassfish.external.amx.AMXGlassfish;

/**
    Small utilities for AMXBooter and related.
 */
@org.glassfish.external.arc.Taxonomy(stability = org.glassfish.external.arc.Stability.UNCOMMITTED)
public final class AMXUtil
{
    private AMXUtil()
    {
    }
    
    /**
        Invoke the bootAMX() method on {@link BootAMXMBean}.  Upon return,
        AMX continues to load.
        A cilent should call {@link invokeWaitAMXReady} prior to use.
     */
    public static void invokeBootAMX(final MBeanServerConnection conn)
    {
        // start AMX and wait for it to be ready
        try
        {
            conn.invoke(BootAMXMBean.OBJECT_NAME, BootAMXMBean.BOOT_AMX_OPERATION_NAME, null, null);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    /**
        Invoke the waitAMXReady() method on the DomainRoot MBean, which must already be loaded.
     */
    public static ObjectName invokeWaitAMXReady(final MBeanServerConnection conn)
    {
        final ObjectName domainRoot = AMXGlassfish.DEFAULT.domainRoot();
        try
        {
            conn.invoke( domainRoot, "waitAMXReady", null, null );
        }
        catch( final Exception e )
        {
            throw new RuntimeException(e);
        }
        return domainRoot;
    }

    /**
    @return the ObjectName of DomainRoot if it exists, otherwise null
     */
    public static ObjectName findDomainRoot(final MBeanServerConnection conn)
    {
        final ObjectName objectName = AMXGlassfish.DEFAULT.domainRoot();
        try
        {
            if (!conn.isRegistered(objectName))
            {
                return null;
            }
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }

        return objectName;
    }


    public static ObjectName newObjectName(final String s)
    {
        try
        {
            return new ObjectName( s );
        }
        catch( final Exception e )
        {
            throw new RuntimeException("bad ObjectName", e);
        }
    }


    /** Make a new AMX ObjectName with unchecked exception */
    public static ObjectName newObjectName(
            final String pp,
            final String type,
            final String name)
    {
        String props = prop(PARENT_PATH_KEY, pp) + "," + prop(TYPE_KEY, type);
        if (name != null)
        {
            props = props + "," + prop(NAME_KEY, name);
        }

        return newObjectName( AMXGlassfish.DEFAULT.amxJMXDomain(), props);
    }

    /** Make a new ObjectName with unchecked exception */
    public static ObjectName newObjectName(
            final String domain,
            final String props)
    {
        return newObjectName(domain + ":" + props);
    }

    public static ObjectName getMBeanServerDelegateObjectName()
    {
        return newObjectName("JMImplementation", "type=MBeanServerDelegate");
    }

    public static String prop(final String key, final String value)
    {
        return key + "=" + value;
    }
}



















