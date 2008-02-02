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
package com.sun.enterprise.management.config;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Collections;

import javax.management.ObjectName;
import javax.management.AttributeList;
import javax.management.Attribute;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.TypeCast;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.helper.RefHelper;

import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.config.ClusteredServerConfig;

import com.sun.enterprise.management.support.Delegate;
import com.sun.enterprise.management.config.AMXConfigImplBase;

//import com.sun.enterprise.management.support.oldconfig.OldDomainMBean;
//import com.sun.enterprise.management.support.OldConfigTypes;


import com.sun.enterprise.util.Issues;

/**
*/
public final class DomainConfigImpl extends AMXConfigImplBase
	implements ConfigFactoryCallback
	// implements DomainConfig
{
		public
	DomainConfigImpl( Delegate	delegate )
	{
		super( delegate );
	}


	private static final Set<String> NOT_SUPERFLUOUS =
	    GSetUtil.newUnmodifiableStringSet(
    	    "getServerConfigObjectNameMap"
        );
            
	    protected final Set<String>
	getNotSuperfluousMethods()
	{
	    return GSetUtil.newSet( super.getNotSuperfluousMethods(), NOT_SUPERFLUOUS );
	}
	
		public Map<String,ObjectName>
	getServerConfigObjectNameMap()
	{
		final Map<String,StandaloneServerConfig> m1	=
		    ((DomainConfig)getSelf()).getStandaloneServerConfigMap();
		final Map<String,ClusteredServerConfig> m2	=
		    ((DomainConfig)getSelf()).getClusteredServerConfigMap();
		
		final Map<String,ObjectName> allObjectNames   = Util.toObjectNames( m1 );
		allObjectNames.putAll( Util.toObjectNames( m2 ) );

        return allObjectNames;
	}
    
    private static final Map<String,String> SPECIAL_CASE_DEFAULT_ATTRIBUTES =
        Collections.unmodifiableMap( MapUtil.newMap( new String[]
        {
            // screwball special case, should have been 'health-checker'
            XTypes.HEALTH_CHECKER_CONFIG, "lb-server-ref-health-checker", // or lb-cluster-ref-health-checker
        }
        ));
    
        public Map<String,String>
    getDefaultAttributeValues( final String j2eeType )
    {
        final String msg = "DomainConfigImpl().getDefaultAttributeValues";
        Issues.getAMXIssues().notDone( msg );
        throw new RuntimeException( msg );
        
        /*
        String com_sun_appservType = SPECIAL_CASE_DEFAULT_ATTRIBUTES.get( j2eeType );
        if ( com_sun_appservType == null )
        {
            // not a special case, use the usual mappings
            com_sun_appservType = OldConfigTypes.getInstance().j2eeTypeToOldType( j2eeType );
            debug( "j2eeType = " + j2eeType + ", com_sun_appservType = " + com_sun_appservType );
        }
        
        final OldDomainMBean  d = getOldConfigProxies().getOldDomainMBean();
        final List<Attribute> oldAttrs = TypeCast.asList( 
            d.getDefaultAttributeValues( com_sun_appservType, (String[])null ) );
        
        Map<String,String>    results = Collections.emptyMap();
        if ( oldAttrs != null && oldAttrs.size() != 0)
        {
            results = new HashMap<String,String>();
            
            for( final Attribute attr : oldAttrs )
            {
                final String name    = attr.getName();
                final String value   = (String)attr.getValue();
                results.put( name, value );
            }
        }
        return results;
        */
    }
}










