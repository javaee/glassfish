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
 
/*
 * $Header: /cvs/glassfish/admin/mbeans/src/java/com/sun/enterprise/admin/dottedname/DottedNameResolverForAliases.java,v 1.3 2005/12/25 03:42:03 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:42:03 $
 */


package com.sun.enterprise.admin.dottedname;

import java.util.Set;

import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;

import com.sun.enterprise.admin.util.ArrayConversion;


/*
	This implementation understands that some names are aliased.  For example,
	"server.xxx" is aliased to "config.xxx".
 */
 
public class DottedNameResolverForAliases implements DottedNameResolver
{
	final DottedNameQuery				mQuery;
	final DottedNameServerInfo			mServerInfo;
	
		public
	DottedNameResolverForAliases(
		final DottedNameQuery		query,
		final DottedNameServerInfo	serverInfo )
	{
		mQuery			= query;
		mServerInfo		= serverInfo;
	}

	
	/*
		Change the dotted name into its "true" name.  For example "server-name.xxx" translates
		to "config-name.xxx".
	 */
		protected String
	getUnaliasedName( final String dottedNameString )
	{
		final DottedName	dn	= DottedNameFactory.getInstance().get( dottedNameString );
		DottedName			actualDN	= dn;
		
		if ( ! DottedName.isWildcardName( dottedNameString ) )
		{
			try
			{
				final String	actualScope	=
					DottedNameAliasSupport.resolveScope( mServerInfo, dn );
				
				if ( ! actualScope.equals( dn.getScope() ) )
				{
					actualDN	= DottedNameFactory.getInstance().get(
						DottedName.toString( dn.getDomain(), actualScope, dn.getParts() ) );
				}
			}
			catch( Exception e )
			{
				DottedNameLogger.logException( e );
			}
		}
		
		return( actualDN.toString() );
	}
	
	/*
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
	*/
	
		public ObjectName
	resolveDottedName( final String dottedName )
	{
		final String		unaliasedName	= getUnaliasedName( dottedName );
		
		final ObjectName	result	= mQuery.dottedNameToObjectName( unaliasedName );
		
		return( result );
	}
}




