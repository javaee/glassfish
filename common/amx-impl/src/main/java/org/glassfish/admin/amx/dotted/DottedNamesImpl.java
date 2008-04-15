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
package org.glassfish.admin.amx.dotted;

import com.sun.appserv.management.base.*;
import com.sun.appserv.management.config.ConfigDottedNames;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.StringUtil;

import javax.management.ObjectName;
import java.util.*;


public final class DottedNamesImpl  extends DottedNamesBase
	// implements DottedNames
{
		public
	DottedNamesImpl( final ObjectName parentObjectName )
	{
        super( ConfigDottedNames.J2EE_TYPE, ConfigDottedNames.J2EE_TYPE, parentObjectName, DottedNames.class );
	}
    
    private String getDomainRootPart()
    {
        return getDomainRoot().getDottedNamePart();
    }
    
		public String
	getGroup()
	{
		return( GROUP_UTILITY );
	}
    
    private DomainConfig getDomainConfig() { return getDomainRoot().getDomainConfig(); }
	
		public Object[]
	dottedNameGet( String[] names )
	{        
        final Object[] results = new Object[names.length];
        
        for( int i = 0; i < names.length; ++i )
        {
            results[i] = dottedNameGet( names[i] );
        }
        
        return results;
	}
    
        private AMX
    findMatchingChild(
        final AMX    amx,
        final String namePart )
    {
        AMX child = null;
        if ( amx instanceof Container )
        {
            final Set<AMX> containees = ((Container)amx).getContaineeSet();
            for( final AMX containee : containees )
            {
                final String p = containee.getDottedNamePart();
                if ( namePart.equals(p) )
                {
                    child = containee;
                    break;
                }
            }
        }
        return child;
    }
    
    /**
        Forgive certain transgressions, such as ommission of the 'root' prefix.
     */
        private String
    laxName( final String dottedName )
    {
        // be friendly: allow the root name to be omitted
        String name = dottedName;
        if ( ! dottedName.startsWith( getDomainRootPart() ) )
        {
            name = getDomainRootPart() + "." + name;
        }
        
        final int idx = name.indexOf( ".property." );
        if ( idx > 0 )
        {
            name    = name.replace( ".property.", ".property:" );
        }
        
        if ( ! dottedName.equals(name) )
        {
            cdebug( "laxName: translated " + dottedName + " => " + name );
        }
        
        return name;
    }
    
        private AMX
    resolveToAnAMX( final String nameIn )
    {        
        AMX amx = getDomainRoot();
        final DottedName dottedName = new DottedName( laxName(nameIn) );
        final List<String> unescapedParts = dottedName.getParts();
        //cdebug( "DottedName: domain = " + dottedName.getDomain() + ", scope = " + dottedName.getScope() + ", parts = " +  StringUtil.toString( unescapedParts ) );

        if ( ! dottedName.getScope().equals( getDomainRoot().getDottedNamePart() ) )
        {
            throw new IllegalArgumentException( "bad root: " + nameIn );
        }

        if ( unescapedParts.size() != 0 )
        {
            final String[] escapedParts = new String[ unescapedParts.size() ];
            for( int i = 0; i < unescapedParts.size(); ++i )
            {
                escapedParts[i] = DottedName.escapePart( unescapedParts.get(i) );
            }
                
            //cdebug( "resolveToAnAMX: matched DomainRoot part of " + nameIn );
            for( int i = 0; i < escapedParts.length; ++i)
            {
                //cdebug( "resolveToAnAMX: matching: " + escapedParts[i] );

                final AMX child = findMatchingChild( amx, escapedParts[i] );
                if ( child == null )
                {
                    //cdebug( "resolveToAnAMX: no match for: " + escapedParts[i] );
                    throw new IllegalArgumentException( "Match failed for: " + nameIn );
                }
                amx = child;
                //cdebug( "resolveToAnAMX: matched: " + escapedParts[i] + " to " + JMXUtil.toString( Util.getObjectName(amx)) );
            }
        }
        
        return amx;
    }
    
    
        public ObjectName
    getDottedNameTargetObjectName( final String dottedName )
    {
        final AMX amx = resolveToAnAMX( dottedName );
        
        return amx == null ? null : Util.getObjectName( amx );
    }
    
        public Map<String,ObjectName>
    getDottedNameTargetObjectNameMap( final String[] dottedNames )
    {
        final Map<String,ObjectName> m = new HashMap<String,ObjectName>();
        
        for( final String dottedName : dottedNames )
        {
            m.put( dottedName, getDottedNameTargetObjectName(dottedName) );
        }
        
        return m;
    }
    
        public String[]
    getAllDottedNames()
    {
        // make sure it's the right kind of Set
        final Set<String> all = getAllDottedNameTargetsObjectNameMap().keySet();
        
        final String[] allStrings = new String[all.size()];
        all.toArray( allStrings );
        return allStrings;
    }

    public String testResolve()
    {
        final Map<String,ObjectName> all = getAllDottedNameTargetsObjectNameMap();
        
        String dump = "";
        int numProblems = 0;
        for( final String dottedName : all.keySet() )
        {
            try
            {
                final ObjectName on = getDottedNameTargetObjectName( dottedName );
                //cdebug( "RESOLVED " + dottedName + " to " + on );
                dump = dump + dottedName + " => " + JMXUtil.toString(on) + StringUtil.NEWLINE();
            }
            catch( Exception e )
            {
                //cdebug( "RESOLVE FAILURE: " + dottedName + " : " + ExceptionUtil.toString(e) );
                dump = dump + dottedName + " => " + ExceptionUtil.toString(e) + StringUtil.NEWLINE();
                
                ++numProblems;
            }
        }
        
        dump = dump + "PROBLEMS FOUND: " + numProblems;
        
        return dump;
    }
    
		public Object
	dottedNameGet( final String nameIn )
	{
    cdebug( "dottedNameGet: " + nameIn );
        final AMX amx = resolveToAnAMX( nameIn );
        
        if ( amx != null )
        {
    cdebug( "dottedNameGet: resolved: " + nameIn + " to " + JMXUtil.toString( Util.getObjectName(amx)) );
        }
        
        return amx == null ? null : Util.getObjectName( amx );
	}
    
        public Object[]
	dottedNameList( final String name )
	{
        Object[] results = null;
        
        try
        {
            final AMX amx = resolveToAnAMX( name );
            if ( amx instanceof Container )
            {
                final Set<AMX> containees = ((Container)amx).getContaineeSet();
                results = new String[ containees.size() ];
                int i = 0;
                for( final AMX containee : containees )
                {
                    results[i++] = containee.getDottedName();
                }
            }
            else
            {
                results = new String[0];
            }
        }
        catch( Exception e )
        {
            results = new Exception[] { e };
        }
        return results;
	}
    
		public Object[]
	dottedNameList( String[] names )
	{
        final Object[] results = new Object[names.length];
        for( int i = 0; i < names.length; ++i )
        {
            results[i] = dottedNameList(names[i]);
        }
        return results;
	}
	
		public Object[]
	dottedNameSet( String[] nameValuePairs )
	{
        return null;
	}
	
		protected boolean
	isWriteableDottedName( String name )
	{
		return( true );
	}
        
        public Map<String,ObjectName>
    getAllDottedNameTargetsObjectNameMap()
    {
        final QueryMgr queryMgr = getDomainRoot().getQueryMgr();
        
        final Set<AMX> allAMX = queryMgr.queryAllSet();
        
        final Map<String,ObjectName> result = new HashMap<String,ObjectName>();
        for ( final AMX amx : allAMX )
        {
            result.put( amx.getDottedName(), Util.getObjectName(amx) );
        }
        
        return result;
    }
    
        
    private static final class Pieces
    {
        final String mPath;
        final String mValueName;
        Pieces( final String path, final String valueName )
        {
            mPath      = path;
            mValueName = valueName;
        }
        public String getPath()      { return mPath; }
        public String getValueName() { return mValueName; }
        
        public String toString() { return mPath + "@" + mValueName; }
    }
    
        private Pieces
    split( final String dottedName )
    {
        final String[] parts = dottedName.split( ATTR_NAME_SEP );
        
        if ( parts.length > 2 )
        {
            throw new IllegalArgumentException( "Malformed dotted name: " + dottedName );
        }
        
        return new Pieces( parts[0], parts.length == 2 ? parts[1] : null );
    }
    
    /**
        Make this code more sophisticated in the future so that it groups all dotted names
        that operate on the same MBean so it can make a single invocation on that MBean.
     */
        public Map<String,String>
    getDottedNameValuesMap( final Set<String> dottedNames )
    {
        final Map<String,String> results = new HashMap<String,String>();
        
        for( final String dottedName : dottedNames )
        {
            final Pieces pieces = split( dottedName );
            
            final AMX amx = resolveToAnAMX( pieces.getPath() );
            if ( amx != null )
            {
                cdebug( "Resolved " + pieces.getPath() + " to " + Util.getObjectName(amx) );
                try
                {
                    final String value = "" + amx.getDottedValue(pieces.getValueName());
                    results.put( dottedName, value );
                }
                catch( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }
       return results;
    }
    
         public String
    getDottedNameValue( final String dottedName )
    {
        // slow way, treat it as multiple-item case
        final Set<String> temp = Collections.singleton( dottedName );
        final Map<String,String> results = getDottedNameValuesMap( temp );
        
        final String result = results.get( dottedName );
        
        return result;
    }
}

















