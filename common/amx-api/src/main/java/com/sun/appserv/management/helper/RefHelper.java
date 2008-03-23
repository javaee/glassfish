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
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/helper/RefHelper.java,v 1.4 2007/07/16 18:09:05 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2007/07/16 18:09:05 $
 */
package com.sun.appserv.management.helper;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import javax.management.ObjectName;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.AMXDebug;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.Extra;

import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.Output;

import com.sun.appserv.management.config.RefConfigReferent;
import com.sun.appserv.management.config.DeployedItemRefConfigReferent;
import com.sun.appserv.management.config.ResourceRefConfigReferent;
import com.sun.appserv.management.config.ServerRefConfigReferent;
import com.sun.appserv.management.config.ClusterRefConfigReferent;
import com.sun.appserv.management.config.ClusterRefConfig;
import com.sun.appserv.management.config.ServerRefConfig;
import com.sun.appserv.management.config.ConfigRemover;

import com.sun.appserv.management.config.RefConfig;
import com.sun.appserv.management.config.ServerRefConfig;
import com.sun.appserv.management.config.ClusterRefConfig;
import com.sun.appserv.management.config.ResourceConfig;
import com.sun.appserv.management.config.ResourceRefConfig;
import com.sun.appserv.management.config.ResourceRefConfigCR;
import com.sun.appserv.management.config.ModuleConfig;
import com.sun.appserv.management.config.DeployedItemRefConfig;
import com.sun.appserv.management.config.DeployedItemRefConfigCR;
import com.sun.appserv.management.config.LifecycleModuleConfig;
import com.sun.appserv.management.config.CustomMBeanConfig;

/**
    Helper routines for references.  This class should only be used
    in EE builds.
    
	@since AppServer 9.0
 */
public class RefHelper
{
    private	RefHelper()	{}


        private static Output
    getDebug()
    {
        return AMXDebug.getInstance().getOutput( "com.sun.appserv.management.helper.RefHelper" );
    }
    
    /**
        Find all ref configs of any j2EEType
        @return Set of all {@link RefConfig} found.
     */
        public static <T extends RefConfig> Set<T>
    findAllRefConfigs( final QueryMgr  queryMgr )
    {
        final Set<T>  refs    = 
            queryMgr.queryInterfaceSet( RefConfig.class.getName(), null);
        
        return refs;
    }
    
    /**
        Find all ref configs of the specified j2EEType
        @return Set of all {@link RefConfig} found.
     */
        public static <T extends RefConfig> Set<T>
    findAllRefConfigsByJ2EEType(
        final QueryMgr  queryMgr,
        final String    refJ2EEType )
    {
        final Set<T>  refs    =
            queryMgr.queryJ2EETypeSet( refJ2EEType );
        
        return refs;
    }
    
    /**
        Find all {@link ResourceRefConfig}.
        @return Set of all {@link ResourceRefConfig} found.
     */
        public static Set<ResourceRefConfig>
    findAllResourceRefConfigs( final QueryMgr  queryMgr )
    {
        return
            findAllRefConfigsByJ2EEType( queryMgr, XTypes.RESOURCE_REF_CONFIG );
    }
    
    /**
        Find all {@link DeployedItemRefConfig}.
        @return Set of all {@link DeployedItemRefConfig} found.
     */
        public static Set<DeployedItemRefConfig>
    findAllDeployedItemRefConfigs( final QueryMgr  queryMgr )
    {
        return
            findAllRefConfigsByJ2EEType( queryMgr, XTypes.DEPLOYED_ITEM_REF_CONFIG );
    }
    
    /**
        Find all {@link ServerRefConfig}.
        @return Set of all {@link ServerRefConfig} found.
     */
        public static Set<ServerRefConfig>
    findAllServerRefConfigs( final QueryMgr  queryMgr )
    {
        return
            findAllRefConfigsByJ2EEType( queryMgr, XTypes.SERVER_REF_CONFIG );
    }
    
    
    /**
        Find all {@link ClusterRefConfig}.
        @return Set of all {@link ClusterRefConfig} found.
     */
        public static Set<ClusterRefConfig>
    findAllClusterRefConfigs( final QueryMgr  queryMgr )
    {
        return
            findAllRefConfigsByJ2EEType( queryMgr, XTypes.CLUSTER_REF_CONFIG );
    }
    
    /**
        Find all {@link RefConfig} of the specified j2EEType having
        the specified name.
        @return Set of all {@link RefConfig} found with the specified name.
     */
        public static <T extends RefConfig> Set<T>
    findAllRefConfigsWithName(
        final QueryMgr  queryMgr,
        final String    refJ2EEType,
        final String    name )
    {
        final String props = Util.makeRequiredProps( refJ2EEType, name );
        
        final Set<T> refs  = queryMgr.queryPatternSet( null, props );
        
        return refs;
    }
    
    /**
        @return the j2eeType of the config element which can <i>refer</i> to this item
     */
        public static String
    getReferentRefJ2EEType( final RefConfigReferent referent )
    {
        String  j2eeType    = null;
        
        if ( referent instanceof ResourceRefConfigReferent )
        {
            j2eeType    = XTypes.RESOURCE_REF_CONFIG;
        }
        else if ( referent instanceof DeployedItemRefConfigReferent )
        {
            j2eeType    = XTypes.DEPLOYED_ITEM_REF_CONFIG;
        }
        else if ( referent instanceof ServerRefConfigReferent )
        {
            j2eeType    = XTypes.SERVER_REF_CONFIG;
        }
        else if ( referent instanceof ClusterRefConfigReferent )
        {
            j2eeType    = XTypes.CLUSTER_REF_CONFIG;
        }
        else
        {
            throw new IllegalArgumentException(
                "Unknown referent class: " + Util.asAMX( referent ).getJ2EEType() );
        }
        
        return j2eeType;
    }
    
   private static final Set<String>  REFERENT_J2EE_TYPES =
    Collections.unmodifiableSet( GSetUtil.newSet( new String[]
        {
            XTypes.RESOURCE_REF_CONFIG,
            XTypes.DEPLOYED_ITEM_REF_CONFIG,
            XTypes.SERVER_REF_CONFIG,
            XTypes.CLUSTER_REF_CONFIG,
        }));
    
        public static Set<String>
    getReferentJ2EETypes()
    {
        return REFERENT_J2EE_TYPES;
    }
   
    
     /**
        Find all {@link RefConfig} that reference the specified j2eeType/name
        combo.
     */
        public static <T extends RefConfig> Set<T>
    findAllRefConfigs( final RefConfigReferent    referent )
    {
        final AMX   amx = Util.asAMX( referent );
        
        return findAllRefConfigsWithName( getQueryMgr( amx ),
                    getReferentRefJ2EEType( referent ),
                    amx.getName() );
    }
    
    /**
        Find all {@link RefConfig} that reference the specified j2eeType/name
        combo.
     */
        public static <T extends RefConfig> Set<T>
    findAllRefConfigs( final RefConfigReferent referent, final String refJ2EEType )
    {
        final AMX   amx = Util.asAMX( referent );
        
        return findAllRefConfigsWithName( getQueryMgr( amx ), refJ2EEType, amx.getName() );
    }
    
    /**
        Find all {@link DeployedItemRefConfig} that reference
        the specified item.
     */
        public static Set<DeployedItemRefConfig>
    findAllRefConfigs( final DeployedItemRefConfigReferent referent )
    {
        final AMX   amx = Util.asAMX( referent );
        
        return
            findAllRefConfigsWithName( getQueryMgr( amx ), XTypes.DEPLOYED_ITEM_REF_CONFIG, amx.getName() );
    }
    
    /**
        Find all {@link ResourceRefConfig} that reference
        the specified item.
     */
        public static Set<ResourceRefConfig>
    findAllRefConfigs( final ResourceRefConfigReferent referent )
    {
        final AMX   amx = Util.asAMX( referent );
        
        return
            findAllRefConfigsWithName( getQueryMgr( amx ), XTypes.RESOURCE_REF_CONFIG, amx.getName() );
    }
    
    /**
        Find all {@link ServerRefConfig} that reference
        the specified item.
     */
        public static Set<ServerRefConfig>
    findAllRefConfigs( final ServerRefConfigReferent referent )
    {
        final AMX   amx = Util.asAMX( referent );
        
        return
            findAllRefConfigsWithName( getQueryMgr( amx ), XTypes.SERVER_REF_CONFIG, amx.getName() );
    }
    
        private static QueryMgr
    getQueryMgr( final AMX amx )
    {
        return amx.getDomainRoot().getQueryMgr();
    }
    
    /**
        Find all {@link ClusterRefConfig} that reference
        the specified item.
     */
        public static Set<ClusterRefConfig>
    findAllRefConfigs( final ClusterRefConfigReferent referent )
    {
        final AMX   amx = Util.asAMX( referent );
        
        return
            findAllRefConfigsWithName( getQueryMgr( amx ), XTypes.CLUSTER_REF_CONFIG, amx.getName() );
    }
    
        public static <T extends RefConfig> Set<T>
    removeAllRefsTo(
        final RefConfigReferent item,
        final boolean allowSingleFailure )
    {
        final Set<T> refs   = RefHelper.findAllRefConfigs( item );

	    final Set<T>    failures   = RefHelper.removeRefConfigs( refs );
	    if ( failures.size() != 0 )
        {
            if ( failures.size() > 1 || ! allowSingleFailure )
            {
                final Set<ObjectName>  objectNames = Util.toObjectNames( failures );
                
                throw new IllegalArgumentException( "failure removing refererences:\n{" +
                    CollectionUtil.toString( objectNames, "\n" ) + "\n}" );
            }
        }
        
        return failures;
    }
    
    /**
        Remove all specified references (that are possible).
        @return any references that could not be removed
     */
        public static <T extends RefConfig> Set<T>
    removeRefConfigs( final Set<T> refs )
    {
        final Set<T> failures    = new HashSet<T>();
        
        for( final T ref : refs )
        {
            /* while deleting references, certain ones, such as those contained
               within a cluster, implicitly delete other (redundant) references
               eg references by clustered servers.  Verify that a reference still
               exists before attempting to delete it.
             */
            final Extra extra = Util.getExtra( ref );
            if ( extra.checkValid() )
            {
                final ConfigRemover rrc   = ConfigRemover.class.cast( ref.getContainer() );
                
                try
                {
                    // may fail as illegal in PE or EE with one ref
                    rrc.removeConfig( ref.getJ2EEType(), ref.getName() );
                }
                catch( Exception e )
                {
                    failures.add( ref );
                }
            }
        }
        
        return failures;
    }
    
}












