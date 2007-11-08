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
package com.sun.enterprise.management.offline;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import javax.management.ObjectName;

import static com.sun.appserv.management.base.XTypes.*;
import static com.sun.appserv.management.base.AMX.*;


/**
 */
public final class OfflineDottedNamePrefixes
{
    private final Map<String,String[]> mTemplates;
    
		private
	OfflineDottedNamePrefixes( )
	{
	    mTemplates  = initTemplates();
	}
	
	private static OfflineDottedNamePrefixes    INSTANCE    = null;
	
	    public static synchronized OfflineDottedNamePrefixes
	getInstance()
	{
	    if ( INSTANCE == null )
	    {
	        INSTANCE    = new OfflineDottedNamePrefixes();
	    }
	    return INSTANCE; 
	}
	
	static private final String VAR = "$";
	static private final String PAR = "^";
	static private final String PART_DELIM = ".";
	
	    private static String
	PAR( final String s )
	{
	    return PAR + s;
	}
	
	    private static String
	VAR( final String s )
	{
	    return VAR + s;
	}
	
	    private static String
	varName( final String s )
	{
	    if ( ! s.startsWith( VAR ) )
	    {
	        throw new IllegalArgumentException( s );
	    }
	    
	    return s.substring( VAR.length(), s.length() );
	}
	
	private static final String NAME_VAR    = VAR( NAME_KEY );

	static private final Object[] TEMPLATES   = new Object[]
	{
	    DOMAIN_CONFIG, new String[] { "domain" },
	    CONFIG_CONFIG, new String[] { NAME_VAR },
	    STANDALONE_SERVER_CONFIG, new String[] { NAME_VAR },
	    HTTP_SERVICE_CONFIG, new String[] { VAR( CONFIG_CONFIG ), "http-service" },
	    IIOP_SERVICE_CONFIG, new String[] { VAR( CONFIG_CONFIG ), "iiop-service" },
	    ADMIN_SERVICE_CONFIG, new String[] { VAR( CONFIG_CONFIG ), "admin-service" },
	    JAVA_CONFIG, new String[] { VAR( CONFIG_CONFIG ), "java" },
	    AVAILABILITY_SERVICE_CONFIG, new String[] { VAR( CONFIG_CONFIG ), "availability" },
	    EJB_CONTAINER_CONFIG, new String[] { VAR( CONFIG_CONFIG ), "ejb-container" },
	    
	    HTTP_LISTENER_CONFIG, new String[] { PAR( HTTP_SERVICE_CONFIG ), "listeners", NAME_VAR },
        ACCESS_LOG_CONFIG, new String[] { PAR( HTTP_SERVICE_CONFIG ), "access-log"},
        KEEP_ALIVE_CONFIG, new String[] { PAR( HTTP_SERVICE_CONFIG ), "keep-alive"},
        REQUEST_PROCESSING_CONFIG, new String[] { PAR( HTTP_SERVICE_CONFIG ), "request-processing" },
        CONNECTION_POOL_CONFIG, new String[] { PAR( HTTP_SERVICE_CONFIG ), "connection-pool" },
        HTTP_PROTOCOL_CONFIG, new String[] { PAR( HTTP_SERVICE_CONFIG ), "http-protocol" },
        HTTP_FILE_CACHE_CONFIG, new String[] { PAR( HTTP_SERVICE_CONFIG ), "file-cache" },
        VIRTUAL_SERVER_CONFIG, new String[] { PAR( HTTP_SERVICE_CONFIG ), "virtual-servers", NAME_VAR }, 
	};
	
	
	    private Map<String,String[]> 
	initTemplates()
	{
	    final Map<String,String[]>  templates   = new HashMap<String,String[]>();
	    
	    for( int i = 0; i < TEMPLATES.length; i += 2 )
	    {
	        final String    j2eeType    = (String)TEMPLATES[ i ];
	        final String[]  template    = (String[])TEMPLATES[ i + 1 ];
	        
	        templates.put( j2eeType, template );
	    }
	    
	    return templates;
	}
	    
	    private String
    concat(
        final String prefix,
        final String part )
    {
        String  result  = null;
        
        if ( prefix == null || prefix.length() == 0 )
        {
            result  = part;
        }
        else
        {
            result  = prefix + PART_DELIM + part;
        }
        return( result );
    }
    
        private String[]
    getTemplate( final String j2eeType )
    {
        return mTemplates.get( j2eeType );
    }
	
	    public String
	getPrefix( final ObjectName objectName )
	{
	    final String    j2eeType    = objectName.getKeyProperty( J2EE_TYPE_KEY );
	    if ( j2eeType == null )
	    {
	        throw new IllegalArgumentException( "" + objectName );
	    }
	    
	    String  prefix  = null;

	    final String[]  template    = getTemplate( j2eeType );
	    if ( template != null )
	    {
	        for( final String part : template )
	        {
	            String value    = null;
	            
	            if ( part.startsWith( VAR ) )
	            {
	                final String prop   = varName( part );
	                value   = objectName.getKeyProperty( prop );
	                if ( value == null )
	                {
	                    throw new IllegalArgumentException(
	                        "No property " + prop + " in " + objectName );
	                }
	            }
	            else
	            {
	                value   = part;
	            }
	            
	            prefix  = concat( prefix, value );
	        }
	    }
	    
	    return prefix;
	}
}
































