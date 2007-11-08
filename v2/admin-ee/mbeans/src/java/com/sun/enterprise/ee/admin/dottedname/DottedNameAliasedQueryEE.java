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

package com.sun.enterprise.ee.admin.dottedname;

import com.sun.enterprise.admin.dottedname.*;

import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;

/**
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Jun 7, 2004
 * @version $Revision: 1.1.1.1 $
 */
public class DottedNameAliasedQueryEE extends DottedNameAliasedQuery{
    DottedNameClusterInfo mClusterInfo;
    DottedNameResolverForAliasesEE mAliasResolver;
    DottedNameServerInfo mServerInfo;
    public DottedNameAliasedQueryEE(DottedNameQuery srcQuery, DottedNameServerInfo serverInfo,
                                  DottedNameClusterInfo clusterInfo) {
        super(srcQuery, serverInfo);
        mServerInfo = serverInfo;
        mClusterInfo=clusterInfo;
		mAliasResolver	= new DottedNameResolverForAliasesEE( srcQuery, serverInfo, clusterInfo);
    }

	/*
		Return a set of all dotted names *as visible to the user*.

		This means that we convert any dotted names beginning with a config name
		to dotted names beginning with its corresponding server name.

		Certain domain names are also aliased into the server name.
	 */
    protected java.util.Set
	allDottedNameStringsThrow(  )
		throws DottedNameServerInfo.UnavailableException {

        final Set		srcSet	= mSrcQuery.allDottedNameStrings();
		final Iterator	iter	= srcSet.iterator();
		final HashSet	destSet	= new HashSet();
		final Set	configNames	= mServerInfo.getConfigNames();
        try{
            configNames.addAll(mClusterInfo.getConfigNames());
        }catch(Exception e){
            throw new DottedNameServerInfo.UnavailableException(e);
        }

        while ( iter.hasNext() ) {
			final String		dottedName	= (String)iter.next();
			final DottedName	dn	= DottedNameFactory.getInstance().get( dottedName );

			final String	scope	= dn.getScope();

			if ( DottedNameAliasSupport.scopeIsDomain( scope ) ) {
				if ( DottedNameAliasSupport.isAliasedDomain( dn ) )	{
					destSet.add (dottedName);
                    addAllNamesForDomain( dn, destSet );
				}
                else {
                    destSet.add(dottedName); // This adds the keyword "domain"
                }
			}
			else {
                if ( configNames.contains( scope ) ) {
					addAllNamesForConfig( dn, destSet );
                    destSet.add(dottedName);
				}
				else {
					// not a config name.
					destSet.add( dottedName );
				}
			}
		}

		return( destSet );
	}
    /*
        Given a config dotted name, generate and add all corresponding dotted
        names that start with the cluster name (none in PE).  (In SE/EE there
        could be more than one clusters using the same config).
     */
    protected void
    addAllNamesForDomain( final DottedName domainDN, final Set outSet )
        throws DottedNameServerInfo.UnavailableException
    {
        final Iterator iter;
        try {
            iter = mClusterInfo.getClusterNames().iterator();
        } catch (Exception e) {
            throw new DottedNameServerInfo.UnavailableException(e);
        }

        // there may be none if no servers refer to the config--that's OK
        while ( iter.hasNext() )
        {
            final String	clusterName	= (String)iter.next();

            final String		dottedNameString	=
                    DottedName.toString( domainDN.getDomain(), clusterName, domainDN.getParts() );

            final DottedName	newName	= DottedNameFactory.getInstance().get( dottedNameString );
            outSet.add( newName.toString() );
        }
        super.addAllNamesForDomain(domainDN, outSet);
    }

    /*
        Given a config dotted name, generate and add all corresponding dotted
        names that start with the cluster name (none in PE).  (In SE/EE there
        could be more than one cluster using the same config).
     */
    protected void
    addAllNamesForConfig( final DottedName configDN, final Set outSet )
        throws DottedNameServerInfo.UnavailableException
    {
        final String []	clusterNames;
        try {
            clusterNames = mClusterInfo.getClusterNamesForConfig(configDN.getScope() );
        } catch (Exception e) {
            throw new DottedNameServerInfo.UnavailableException(e);
        }

        if(clusterNames != null){
            // there may be none if no servers refer to the config--that's OK
            for( int i = 0; i < clusterNames.length; ++i )
            {
                final String		newName	=
                        DottedName.toString( configDN.getDomain(), clusterNames[ i ], configDN.getParts() );

                outSet.add( newName );
            }
        }
        super.addAllNamesForConfig(configDN, outSet);
    }
}
