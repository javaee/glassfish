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

import java.util.Set;
import java.util.HashSet;

import com.sun.appserv.management.util.misc.GSetUtil;

public class DottedNameAliasSupport
{
	public DottedNameAliasSupport( )
	{

	}


	public final static String	DOMAIN_SCOPE		= "domain";
	public final static String	DOMAIN_SCOPE_DOT	= DOMAIN_SCOPE + ".";
	
	
		static public boolean
	scopeIsDomain( String scope )
	{
		return( scope.equals( DOMAIN_SCOPE ) || scope.startsWith( DOMAIN_SCOPE_DOT ) );
	}
	 
	
	
	/*
		Names which are domain.xxx names which are aliased into server
	 */
	public static final Set<String>	DOMAIN_PARTS	= GSetUtil.newUnmodifiableStringSet( 
			// these are prefixes
			"applications",
			"resources"
		);
		
		public static boolean
	isAliasedDomain( final DottedName dn )
	{
		boolean	isAliased	= false;
		
		final java.util.List	parts	= dn.getParts();
		
		if ( parts.size() >= 1 )
		{
			isAliased	= DOMAIN_PARTS.contains( parts.get( 0 ) );
		}
		
		return( isAliased );
	}
	
	
	
	/*
		Names following the server name which indicate that the server name is *not* to
		be aliased into config.
	 */
	static private final Set<String>	NON_ALIASED_PARTS_SET	= GSetUtil.newUnmodifiableStringSet( 
			// these are prefixes
			"application-ref",
			"resource-ref",
			"config-ref",
			"node-agent-ref",
			"server-ref",
			
			// these are attributes
			"property",
			"name"
		);
	
		protected static boolean
	isNonAliasedServer( final DottedName dn )
	{
		boolean	isNonAliased	= false;
		
		final java.util.List	parts	= dn.getParts();
		
		if ( parts.size() >= 1 )
		{
			isNonAliased	= NON_ALIASED_PARTS_SET.contains( parts.get( 0 ) );
		}
		
		return( isNonAliased );
	}
	
	/*
		Map any aliased scope names into their true dotted names.
		
		Here 'server' is the name of a server.
		
		Rules:
			server.* 	=> config.*  (except for NON_ALIASED_PARTS_SET)
			*	 => same as input
	 */
		public static String
	resolveScope( final DottedNameServerInfo serverInfo, final DottedName dn )
		throws DottedNameServerInfo.UnavailableException
	{
		final String	scopeNameIn		= dn.getScope();
		String			actualScopeName	= scopeNameIn;
		
		if ( scopeIsDomain( scopeNameIn ) )
		{
			// no change required
		}
		else
		{
			final boolean	scopeIsServerName	= serverInfo.getServerNames().contains( actualScopeName );

			if ( scopeIsServerName )
			{
				if ( isAliasedDomain( dn ) )
				{
					actualScopeName	= DottedNameAliasSupport.DOMAIN_SCOPE;
				}
				else if ( isNonAliasedServer( dn ) )
				{
					// no change
				}
				else
				{
					// it's a server name, and it *is* aliased, but wasn't alised into domain
					// so it must be aliased into a config
					actualScopeName	= serverInfo.getConfigNameForServer( scopeNameIn );
				}
			}
		}
		
		return( actualScopeName );
	}
	
	

	
	static java.util.logging.Logger		sLogger	= null;
		static void
	dm( Object o )
	{
		if (sLogger == null )
		{
        	sLogger	= java.util.logging.Logger.getLogger( "DottedNameGetSetMBeanImplLogger" );
        	sLogger.setLevel( java.util.logging.Level.INFO );
        }
        
        sLogger.info( o.toString() );
	}
}




