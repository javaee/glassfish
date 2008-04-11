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

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import javax.management.ObjectName;

/*
	Implements DottedNameQuery over config dotted names by presenting a view
	of user-visible dotted names even though the underlying dotted names are
	in fact not user-visible.  For PE, this means that dotted names start with
	the server name "server", but in actuality they are registered starting
	with the config name.  This class hides that detail.
	
	Additionally, domain.* is also aliased into server
 */
public class DottedNameAliasedQuery implements DottedNameQuery
{
	protected final DottedNameQuery				mSrcQuery;
	protected final DottedNameServerInfo			mServerInfo;
	final DottedNameResolverForAliases	mAliasResolver;
	
		public
	DottedNameAliasedQuery( final DottedNameQuery srcQuery, final DottedNameServerInfo serverInfo )
	{
		mSrcQuery	= srcQuery;
		mServerInfo	= serverInfo;
		mAliasResolver	= new DottedNameResolverForAliases( srcQuery, serverInfo );
	}
	
		public ObjectName
	dottedNameToObjectName( final String	dottedName )
	{
		return( mAliasResolver.resolveDottedName( dottedName ) );
	}
	
		public Set<String>
	allDottedNameStrings(  )
	{
		Set<String>	result	= Collections.EMPTY_SET;
		
		try
		{
			result	= allDottedNameStringsThrow();
		}
		catch( DottedNameServerInfo.UnavailableException e )
		{
			DottedNameLogger.logException( e );
		}
		return( result );
	}
	
	/*
		Return a set of all dotted names *as visible to the user*.
		
		This means that we convert any dotted names beginning with a config name
		to dotted names beginning with its corresponding server name.
		
		Certain domain names are also aliased into the server name.
	 */
		protected Set<String>
	allDottedNameStringsThrow(  )
		throws DottedNameServerInfo.UnavailableException
	{
		final Set<String>   srcSet	= mSrcQuery.allDottedNameStrings();
		final Set<String>   destSet	= new HashSet<String>();
		
		final Set<String>	configNames	= mServerInfo.getConfigNames();
		
        for( final String dottedName : srcSet )
		{
			final DottedName	dn	= DottedNameFactory.getInstance().get( dottedName );
			
			final String	scope	= dn.getScope();
			
			if ( DottedNameAliasSupport.scopeIsDomain( scope ) )
			{
				if ( DottedNameAliasSupport.isAliasedDomain( dn ) )
				{
					destSet.add (dottedName);
                    addAllNamesForDomain( dn, destSet );
				}
			}
			else
			{
				if ( configNames.contains( scope ) )
				{
					addAllNamesForConfig( dn, destSet );
				}
				else
				{
					// not a config name.
					destSet.add( dottedName );
				}
			}
			
		}
		
		return( destSet );
	}
	
	/*
		Given a config dotted name, generate and add all corresponding dotted
		names that start with the server name (just one in PE).  (In SE/EE there
		could be more than one server using the same config).
	 */
        protected void
	addAllNamesForDomain( final DottedName domainDN, final Set<String> outSet )
		throws DottedNameServerInfo.UnavailableException
	{
		// there may be none if no servers refer to the config--that's OK
        for( final String serverName : mServerInfo.getServerNames() )
		{
			final String		dottedNameString	=
					DottedName.toString( domainDN.getDomain(), serverName, domainDN.getParts() );
					
			final DottedName	newName	= DottedNameFactory.getInstance().get( dottedNameString );
			outSet.add( newName.toString() );
		}
	}
	
	/*
		Given a config dotted name, generate and add all corresponding dotted
		names that start with the server name (just one in PE).  (In SE/EE there
		could be more than one server using the same config).
	 */
    protected void
	addAllNamesForConfig( final DottedName configDN, final Set<String> outSet )
		throws DottedNameServerInfo.UnavailableException
	{
		final String []	serverNames	= mServerInfo.getServerNamesForConfig( configDN.getScope() );
		
		// there may be none if no servers refer to the config--that's OK
		for( int i = 0; i < serverNames.length; ++i )
		{
			final String		newName	=
					DottedName.toString( configDN.getDomain(), serverNames[ i ], configDN.getParts() );

			outSet.add( newName );
		}
	}
}


