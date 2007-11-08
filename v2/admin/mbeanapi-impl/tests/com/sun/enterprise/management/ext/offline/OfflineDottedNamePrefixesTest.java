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
package com.sun.enterprise.management.ext.offline;

import java.util.Set;
import java.util.HashSet;

import javax.management.ObjectName;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.config.ConfigElement;

import com.sun.enterprise.management.AMXTestBase;
import com.sun.enterprise.management.offline.OfflineDottedNamePrefixes;


/**
 */
public final class OfflineDottedNamePrefixesTest extends AMXTestBase
{
        public
    OfflineDottedNamePrefixesTest()
    {
    }

        private ObjectName
    ON( final String objectNameString )
    {
        return Util.newObjectName( objectNameString );
    }
    
		public void
	testGetInstance()
	{
	    assert OfflineDottedNamePrefixes.getInstance() != null;
	}
	
	private static final String DOMAIN="amx:j2eeType=X-DomainConfig,name=na";
	private static final String SERVER_CONFIG="amx:j2eeType=X-StandaloneServerConfig,name=server-config";
	
	    private String
	getPrefix( final String objectNameString )
	{
	    return getPrefix( ON( objectNameString ) );
	}
	
	   private String
	getPrefix( final ObjectName objectName )
	{
	    final OfflineDottedNamePrefixes prefixes    =
	        OfflineDottedNamePrefixes.getInstance();
	    
	    return prefixes.getPrefix( objectName );
	}
	
		public void
	testIllegal()
	{
	    try
	    {
	        assert( getPrefix( "amx:foo=bar" ) == null );
	        assert false;
	    }
	    catch( Exception e )
	    {
	    }
	}
	
		public void
	testGetPrefix()
	{
	    assert( getPrefix( DOMAIN ).equals( "domain" ) );
	    assert( getPrefix( SERVER_CONFIG ).equals( "server-config" ) );
	}
	
	    public void
	testAllAMXConfig()
	{
	    if ( getConnection() == null )
	    {
	        warning( "testAllAMXConfig: No MBeanServerConnection, skipping test" );
	        return;
	    }
	    
	    final Set<ConfigElement> all   = getTestUtil().getAllAMX( ConfigElement.class );
	    
	    final Set<String>   missing = new HashSet<String>();
	    for( final ConfigElement config : all )
	    {
	        final ObjectName    objectName = Util.getObjectName( config );
	        final String    prefix  = getPrefix( objectName );
	        if ( prefix == null )
	        {
	            missing.add( config.getJ2EEType() );
	        }
	        else
	        {
	            //trace( prefix );
	        }
	    }
	    
	    if ( missing.size() != 0 )
	    {
	        //warning( "The following j2eeTypes do not yet have prefix support: " + NEWLINE +
	            //CollectionUtil.toString( missing, NEWLINE ) );
	    }
	}
}






















