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
 * accompanied this code.  If applicable, add the following below the Licensep
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
package org.glassfish.admin.amx.impl.mbean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.base.Pathnames;
import org.glassfish.admin.amx.base.Tools;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.jmx.JMXUtil;

public class ToolsImpl extends AMXImplBase // implements Tools
{
    private static final String NL = StringUtil.NEWLINE();
        
    public ToolsImpl(final ObjectName parent) {
        super(parent, Tools.class);
    }

    private static ObjectName newObjectName(final String s )
    {
        try {
            return new ObjectName(s);
        }
        catch (final Exception e ) {
        }
        return null;
    }
    
    static private final String WILD_SUFFIX = ",*";
    static private final String WILD_ALL = "*";
    
    public String getInfo()
    {
        return info( "*" );
    }
    
    public String infoPP(final String parentPath, final boolean recursive)
    {
        final Pathnames paths = getDomainRootProxy().getPathnames();
        
        final ObjectName[] objectNames = paths.listObjectNames(parentPath, recursive);
        final Set<ObjectName> s = SetUtil.newSet(objectNames);
        return info(s);
    }
    
    public String infoType( final String type)
    {
        return info( "*:type=" + type + WILD_SUFFIX );
    }
    
    public String infoPath( final String path)
    {
        final ObjectName objectName = getDomainRootProxy().getPathnames().resolvePath(path);
        //cdebug( "infoPath: " + path + " => " + objectName);
        
        Collection<ObjectName> c = objectName == null ?  new ArrayList<ObjectName>() : Collections.singleton(objectName);
        return info( c );
    }
    
    String info( final Collection<ObjectName> objectNames )
    {
        final StringBuffer buf = new StringBuffer();
            
        if ( objectNames.size() != 0 )
        {
            final String NL = StringUtil.NEWLINE();
            for( final ObjectName objectName : objectNames )
            {
                final MBeanInfo mbeanInfo = ProxyFactory.getInstance(getMBeanServer()).getMBeanInfo(objectName);
                
                buf.append( "MBeanInfo for " + objectName + NL);
                buf.append( JMXUtil.toString(mbeanInfo) );
                buf.append( NL + NL + NL + NL );
            }
        }
        
        buf.append( "Matched " + objectNames.size() + " mbean(s)." );
        
        return buf.toString();
    }
    
    public String info(final String searchStringIn) {
        final String domain = getObjectName().getDomain();
        
        ObjectName pattern = newObjectName(searchStringIn);
        if ( pattern == null && ( searchStringIn.length() == 0 || searchStringIn.equals(WILD_ALL)) )
        {
            pattern = newObjectName("*:*");
        }
        
        if ( pattern == null )
        {
            String temp = searchStringIn;
            
            final boolean hasProps = temp.indexOf("=") > 0;
            final boolean hasDomain = temp.indexOf(":") >= 0;
            final boolean isPattern = temp.endsWith(WILD_SUFFIX);
            
            if ( ! (hasProps || hasDomain || isPattern) )
            {
                // try it as a type
                pattern = newObjectName( "*:type=" + temp + WILD_SUFFIX );
                
                // if no luck try it as a j2eeType
                if ( pattern == null )
                {
                    pattern = newObjectName( "*:j2eeType=" + temp + WILD_SUFFIX );
                }
                
                // if no luck try it as a name
                if ( pattern == null )
                {
                    pattern = newObjectName( "*:name=" + temp + WILD_SUFFIX );
                }
            }        
                
            if ( pattern == null ) {
                return "No MBeans found for: " + searchStringIn;
            }
        }
        
        final Set<ObjectName> objectNames = getMBeanServer().queryNames( pattern, null);
        
        return info(objectNames);
    }
    
    
    private static final class Stuff 
    {
        private final Map<ObjectName, Object> mFailures = new HashMap<ObjectName,Object>();
        private final StringBuilder mBuf;
        
        public Stuff() {
            mBuf = new StringBuilder();
        }
        
        void result( final ObjectName objectName, final Object result )
        {
            if ( result != null )
            {
                mFailures.put( objectName, result );
                
                mBuf.append( objectName + ": " + result + NL );
            }
        }
        
        public String toString() {
            return mFailures.size() + " failures." + NL + mBuf.toString();
        }
    }
    
    public Object validate( final AMXProxy proxy )
    {
        final DomainRoot dr = getDomainRootProxy();
        final Pathnames paths = dr.getPathnames();
        
        try
        {
            final String path = proxy.path();
            final ObjectName actualObjectName = Util.getObjectName(proxy);
            
            final ObjectName o = paths.resolvePath(path);
            if ( o == null )
            {
                return "Path " + path + " does not resolve to any ObjectName, should resolve to: " + actualObjectName;
            }
            else  if ( ! actualObjectName.equals(o) )
            {
                return "Path " + path + " does not resolve to ObjectName: " + actualObjectName;
            }
        }
        catch( Throwable t )
        {
            return t;
        }
        
        return null;
    }
    
    public String validate()
    {
        final Stuff stuff = new Stuff();
        
        final DomainRoot dr = getDomainRootProxy();
        final Pathnames paths = dr.getPathnames();
        
        final Map<ObjectName,Object> m = new HashMap<ObjectName,Object>();
        
        Object result = validate( dr );
        stuff.result( Util.getObjectName(dr), result );
        
        final ObjectName[] all = paths.listObjectNames( dr.path(), true);
        for( final ObjectName objectName : all )
        {
            final AMXProxy amx = getProxyFactory().getProxy(objectName);
                
            result = validate( amx );
            stuff.result( objectName, result );
        }
        return all.length + " MBeans tested." + NL + stuff.toString();
    }
}




































