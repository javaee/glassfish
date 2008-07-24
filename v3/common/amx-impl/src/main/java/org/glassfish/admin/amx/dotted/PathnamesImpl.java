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
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.StringUtil;

import javax.management.ObjectName;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import org.glassfish.admin.amx.mbean.AMXNonConfigImplBase;

/**
  GlassFish V3 dotted names implementation (MBean).
 */
 
public final class PathnamesImpl  extends AMXNonConfigImplBase
	// implements Pathnames  (can't directly implement the interface)
{
		public
	PathnamesImpl( final ObjectName parentObjectName )
	{
        super( Pathnames.J2EE_TYPE, Pathnames.J2EE_TYPE, parentObjectName, Pathnames.class, null );
        cdebug( "PathnamesImpl: " + this.getClass().getName() );
        
        //mLax = new LaxNameHandler( getDomainRoot().getPathname() );
	}
    
		public String
	getGroup()
	{
		return( GROUP_UTILITY );
	}
    
    private DomainConfig getDomainConfig() { return getDomainRoot().getDomainConfig(); }
	
		public Object[]
	pathnameGet( String[] names )
	{        
        final Object[] results = new Object[names.length];
        
        for( int i = 0; i < names.length; ++i )
        {
            results[i] = pathnameGet( names[i] );
        }
        
        return results;
	}
    
        private AMX
    findMatchingChild(
        final AMX       amx,
        final PathPart part )
    {
        AMX child = null;
        if ( amx instanceof Container )
        {
            final Set<AMX> containees = ((Container)amx).getContaineeSet();
            for( final AMX c : containees )
            {
                final String type = part.getType();
                final String name = part.getName();
                
                if ( type.equalsIgnoreCase(c.getPathnameType()) )
                {
                    if ( name == null || name.equalsIgnoreCase(c.getName()))
                    {
                        child = c;
                        break;
                    }
                }
                else if ( type.length() == 0 )
                {
                    // a type of length 0 means look at the name (only)
                    if ( name != null && name.equalsIgnoreCase(c.getName()) )
                    {
                        child = c;
                        break;
                    }
                }
            }
        }
        return child;
    }
        
        private AMX
    resolveToAnAMX( final V3Pathname  v3path )
    {        
        AMX amx = getDomainRoot();
        final List<PathPart> parts = v3path.getParts();
        cdebug( "Pathname: v3path = " + v3path + ",  parts = " +  StringUtil.toString( v3path.getParts() ) + ", attr = " + v3path.getParsed().getAttrPart() );

        // first part should be /root
        if ( ! parts.get(0).getType().equals( getDomainRoot().getPathnameType() ) )
        {
            throw new IllegalArgumentException( "bad root: " +  parts.get(0).getType() + " != " + getDomainRoot().getPathnameType() + 
                    " path = " + v3path.toString() );
        }

        if ( parts.size() > 1 )
        {
                
            //cdebug( "resolveToAnAMX: matched DomainRoot part of " + nameIn );
            for( int i = 1; i < parts.size(); ++i)
            {
                final PathPart part = parts.get(i);
                //cdebug( "resolveToAnAMX: matching: " + escapedParts[i] );

                final AMX child = findMatchingChild( amx, part );
                if ( child == null )
                {
                    //cdebug( "resolveToAnAMX: no match for: " + escapedParts[i] );
                    throw new IllegalArgumentException( "Match failed for: " + v3path + " at " + part );
                }
                amx = child;
                //cdebug( "resolveToAnAMX: matched: " + escapedParts[i] + " to " + JMXUtil.toString( Util.getObjectName(amx)) );
            }
        }
        else
        {
            amx = getDomainRoot();
        }
        
        return amx;
    }
    
    
        public ObjectName
    getPathnameTargetObjectName( final String pathname )
    {
        final AMX amx = resolveToAnAMX( new V3Pathname(pathname) );
        
        return amx == null ? null : Util.getObjectName( amx );
    }
    
        public Map<String,ObjectName>
    getPathnameTargetObjectNameMap( final String[] pathnames )
    {
        final Map<String,ObjectName> m = new HashMap<String,ObjectName>();
        
        for( final String pathname : pathnames )
        {
            m.put( pathname, getPathnameTargetObjectName(pathname) );
        }
        
        return m;
    }
    
        public String[]
    getAllPathnames()
    {
        // make sure it's the right kind of Set
        final Set<String> all = getAllPathnameTargetsObjectNameMap().keySet();
        
        final String[] allStrings = new String[all.size()];
        all.toArray( allStrings );
        return allStrings;
    }

    public String testResolve()
    {
        final Map<String,ObjectName> all = getAllPathnameTargetsObjectNameMap();
        
        String dump = "";
        int numProblems = 0;
        for( final String pathname : all.keySet() )
        {
            try
            {
                final ObjectName on = getPathnameTargetObjectName( pathname );
                //cdebug( "RESOLVED " + pathname + " to " + on );
                dump = dump + pathname + " => " + JMXUtil.toString(on) + StringUtil.NEWLINE();
            }
            catch( Exception e )
            {
                //cdebug( "RESOLVE FAILURE: " + pathname + " : " + ExceptionUtil.toString(e) );
                dump = dump + pathname + " => " + ExceptionUtil.toString(e) + StringUtil.NEWLINE();
                
                ++numProblems;
            }
        }
        
        dump = dump + "PROBLEMS FOUND: " + numProblems;
        
        return dump;
    }
    
		public Object
	pathnameGet( final String nameIn )
	{
    cdebug( "pathnameGet: " + nameIn );
        final AMX amx = resolveToAnAMX( new V3Pathname(nameIn) );
        
        if ( amx != null )
        {
    cdebug( "pathnameGet: resolved: " + nameIn + " to " + JMXUtil.toString( Util.getObjectName(amx)) );
        }
        
        return amx == null ? null : Util.getObjectName( amx );
	}
    
        public Object[]
	pathnameList( final String name )
	{
        Object[] results = null;
        
        try
        {
            final AMX amx = resolveToAnAMX( new V3Pathname(name) );
            if ( amx instanceof Container )
            {
                final Set<AMX> containees = ((Container)amx).getContaineeSet();
                results = new String[ containees.size() ];
                int i = 0;
                for( final AMX containee : containees )
                {
                    results[i++] = containee.getPathname();
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
	pathnameList( String[] names )
	{
        final Object[] results = new Object[names.length];
        for( int i = 0; i < names.length; ++i )
        {
            results[i] = pathnameList(names[i]);
        }
        return results;
	}
	
		public Object[]
	pathnameSet( String[] nameValuePairs )
	{
        return null;
	}
	
		protected boolean
	isWriteablePathname( String name )
	{
		return( true );
	}
        
        public Map<String,ObjectName>
    getAllPathnameTargetsObjectNameMap()
    {
        final QueryMgr queryMgr = getDomainRoot().getQueryMgr();
        
        final Set<AMX> allAMX = queryMgr.queryAllSet();
        
        final Map<String,ObjectName> result = new HashMap<String,ObjectName>();
        for ( final AMX amx : allAMX )
        {
            result.put( amx.getPathname(), Util.getObjectName(amx) );
        }
        
        return result;
    }
           
    /**
        Make this code more sophisticated in the future so that it groups all dotted names
        that operate on the same MBean so it can make a single invocation on that MBean.
     */
        public Map<String,String>
    getPathnameValuesMap( final Set<String> pathnames )
    {
        final Map<String,String> results = new HashMap<String,String>();
        
        for( final String pathname : pathnames )
        {
            final V3Pathname p = new V3Pathname( pathname );
            
            final AMX amx = resolveToAnAMX( p );
            if ( amx != null )
            {
                cdebug( "Resolved " + p + " to " + Util.getObjectName(amx) );
                try
                {
                    final String valueName =  p.getParsed().getAttrPart().getName();
                    if ( valueName != null )
                    {
                        final String value = "" + amx.getPathnameValue( valueName );
                        results.put( pathname, value );
                    }
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
    pathnameGetSingleValue( final String pathname )
    {
        // slow way, treat it as multiple-item case
        final Set<String> temp = Collections.singleton( pathname );
        final Map<String,String> results = getPathnameValuesMap( temp );
        
        final String result = results.get( pathname );
        
        return result;
    }
    
/* 
		public AttributeList
	getAttributes( final String[]	names )
	{
    	mCoverage.attributesWereRead( names );
	    
		final Set<String>	dotted	= new HashSet<String>();
		final Set<String>	parent	= new HashSet<String>();
		filterNames( names, dotted, parent);

		final Object[]	dottedResults	=
			pathnameGet( (String[])dotted.toArray( new String[ dotted.size() ] ) );
		
		final String[]		namesForParent	= new String[ parent.size() ];
		final AttributeList	parentResults	=
			super.getAttributes( (String[])parent.toArray( namesForParent ) );
		
		final AttributeList	successList	= new AttributeList();
		successList.addAll( parentResults );
		
		// add all the dotted name results
		for( int i = 0; i < dottedResults.length; ++i )
		{
			if ( dottedResults[ i ] instanceof Attribute )
			{
				successList.add( (Attribute)dottedResults[ i ] );
			}
			else
			{
				assert( dottedResults[ i ] instanceof Exception );
			}
		}
		
		return( successList );
	}


		public AttributeList
	setAttributes( AttributeList attributes )
	{
		*
			Convert each attribute to a name/value pair.
			Omit any attributes that don't have a legal attribute name
		 *
		final int	numAttrsIn	= attributes.size();
		final List<String>	legalPairs	= new ArrayList<String>();
		for( int i = 0; i < numAttrsIn; ++i )
		{
			final Attribute	attr	= (Attribute)attributes.get( i );
			
			final String    name    = attr.getName();
			mCoverage.attributeWasWritten( name );
			
			if ( isLegalAttributeName( name ) )
			{
				legalPairs.add( attributeToNamePair( attr ) );
			}
		}
		
		final String[]	pairs	= (String[])legalPairs.toArray( new String[ legalPairs.size() ] );

		final Object[] results	= pathnameSet( pairs );
		
		final AttributeList	attributeList	= new AttributeList();
		for( int i = 0; i < results.length; ++i )
		{
			if ( results[ i ] instanceof Attribute )
			{
				attributeList.add( (Attribute)results[ i ] );
			}
			else
			{
				assert( results[ i ] instanceof Exception );
				// it's an exception
			}
		}
		
		return( attributeList );
	}

*/
}

















