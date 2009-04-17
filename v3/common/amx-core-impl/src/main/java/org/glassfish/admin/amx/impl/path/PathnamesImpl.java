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
package org.glassfish.admin.amx.impl.path;

import org.glassfish.admin.amx.base.*;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.Util;

import org.glassfish.admin.amx.util.jmx.JMXUtil;

import javax.management.ObjectName;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;

import javax.management.MBeanServer;
import org.glassfish.admin.amx.core.AMXConstants;
import org.glassfish.admin.amx.impl.mbean.AMXImplBase;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.util.ListUtil;

/**
  GlassFish V3 dotted names implementation (MBean).
 */
 
public final class PathnamesImpl  extends AMXImplBase
	// implements Pathnames  (can't directly implement the interface)
{
		public
	PathnamesImpl( final ObjectName parentObjectName )
	{
        super( parentObjectName, Pathnames.class );
	}

        public ObjectName
    resolvePath( final String path )
    {
        // fixed query based on the path, which will find all MBeans with that parent path
        final String props = Util.makeProp( AMXConstants.PARENT_PATH_KEY, path );
        final ObjectName pattern = JMXUtil.newObjectNamePattern( getObjectName().getDomain(), props );
        final Set<ObjectName> s = getMBeanServer().queryNames( pattern, null);
        
        if ( s.size() == 0 )
        {
            return null;
        }
        
        // get the ObjectName of the parent of any of the children
        final ObjectName first = s.iterator().next();
        final ObjectName parent = (ObjectName)JMXUtil.getAttribute( getMBeanServer(), first, AMXConstants.ATTR_PARENT );
        //throw new IllegalStateException("Path " + path + " is used by more than one MBean: " + CollectionUtil.toString(s,"\n") );
        
        return parent;
    }
    
        public ObjectName[]
    resolvePaths( final String[] paths )
    {
        final ObjectName[] objectNames = new ObjectName[paths.length];
        
        int i = 0;
        for( final String path : paths )
        {
            try
            {
                objectNames[i] = resolvePath(path);
            }
            catch( final Exception e )
            {
                objectNames[i] = null;
            }
            ++i;
        }
        return objectNames;
    }

        
        public ObjectName[]
    ancestors( final String path )
    {
        final ObjectName objectName = resolvePath(path);
        if ( objectName == null )
        {
            return null;
        }
        return ancestors(objectName);
    }
        
        public ObjectName[]
    ancestors( final ObjectName mbean )
    {
        final List<ObjectName> objectNames = new ArrayList<ObjectName>();
        final MBeanServer server = getMBeanServer();

        ObjectName cur = mbean;
        while ( cur != null )
        {
            objectNames.add(cur);
            cur = (ObjectName)JMXUtil.getAttribute(server, cur, AMXConstants.ATTR_PARENT);
        }
        
        final List<ObjectName> reversed = ListUtil.reverse(objectNames);
        final ObjectName[] ancestors = new ObjectName[reversed.size()];
        reversed.toArray(ancestors);
        return ancestors;
    }
    
    private void listChildren( final AMXProxy top, final List<AMXProxy> list, boolean recursive)
    {
        final Set<AMXProxy> children = top.childrenSet();
        if  ( children == null ) return;
        
        for( final AMXProxy child : children )
        {
            list.add( child );
            if ( recursive )
            {
                listChildren( child, list, true );
            }
        }
    }

    
    public String[] getAllPathnames()
    {
        final String[] allButRoot = listPaths( DomainRoot.PATH, true );
        
        final String[] all = new String[ allButRoot.length + 1 ];
        all[0] = DomainRoot.PATH;
		System.arraycopy( allButRoot, 0, all, 1, allButRoot.length );
        
        return all;
    }
    
    public String[] listPaths( final String path, final boolean recursive )
    {
        final ObjectName top = resolvePath(path);
        if ( top == null ) return null;
        
        final AMXProxy topProxy = getProxyFactory().getProxy(top, AMXProxy.class);
        
        final List<AMXProxy> all  = new ArrayList<AMXProxy>();
        listChildren(topProxy, all, recursive);
        
        final List<String> paths = new ArrayList<String>();
        for ( final AMXProxy amx: all )
        {
            paths.add( amx.path() );
        }
        
        final String[] a = new String[paths.size()];
        paths.toArray(a);
        return a;
    }
   
    public String dump( final String path )
    {
        final ObjectName top = resolvePath(path);
        
        final AMXProxy topProxy = getProxyFactory().getProxy(top, AMXProxy.class);
        final List<AMXProxy> list  = new ArrayList<AMXProxy>();
        list.add( topProxy);
        listChildren(topProxy, list, true);
        
        final String NL = "\n";
        final StringBuffer buf = new StringBuffer();
        for( final AMXProxy amx : list )
        {
            final String p = amx.path();
            buf.append(p);
            buf.append(NL);
            
            final Map<String,Object> attributesMap = amx.attributesMap();
            for( final String name : attributesMap.keySet() )
            {
                buf.append("\t");
                buf.append(name);
                buf.append(" = ");
                buf.append( "" + attributesMap.get(name));
                buf.append(NL);
            }
            buf.append(NL);
        }
        return buf.toString();
    }
}

















