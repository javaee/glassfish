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
package org.glassfish.admin.amx.impl.config;

import org.glassfish.admin.amx.core.Extra;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.Util;

import org.glassfish.admin.amx.base.QueryMgr;

import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.admin.amx.util.Output;
import org.glassfish.admin.amx.util.AMXDebug;

import org.glassfish.admin.amx.intf.config.*;

import javax.management.ObjectName;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.glassfish.admin.amx.config.AMXConfigProxy;

/**
    Helper routines for references.  This class should only be used
    in EE builds.
    
	@since AppServer 9.0
 */
public class RefHelper
{
    private	RefHelper()	{}



    public static final String DEPLOYED_ITEM_REF_CONFIG_TYPE = Util.deduceType(DeployedItemRefConfig.class);
    public static final String RESOURCE_REF_CONFIG_TYPE = Util.deduceType(ResourceRefConfig.class);
    public static final String SERVER_REF_CONFIG_TYPE = Util.deduceType(ServerRefConfig.class);
    public static final String CLUSTER_REF_CONFIG_TYPE = Util.deduceType(ClusterRefConfig.class);

    private static final Set<String>  REFERENT_J2EE_TYPES =
    Collections.unmodifiableSet( SetUtil.newSet( new String[]
        {
            DEPLOYED_ITEM_REF_CONFIG_TYPE,
            RESOURCE_REF_CONFIG_TYPE,
            SERVER_REF_CONFIG_TYPE,
            CLUSTER_REF_CONFIG_TYPE
        }));


        private static Output
    getDebug()
    {
        return AMXDebug.getInstance().getOutput( "com.sun.appserv.management.helper.RefHelper" );
    }

    /**
        Find all ref configs of any j2EEType
        @return Set of all {@link RefConfig} found.
     */
        public static Set<RefConfig>
    findAllRefConfigs( final QueryMgr  queryMgr )
    {
        final Set<RefConfig>  refs    =
            queryMgr.queryInterfaceSet( RefConfig.class.getName(), null);

        return refs;
    }

    /**
        Find all ref configs of the specified j2EEType
        @return Set of all {@link RefConfig} found.
     */

            public static <T extends RefConfig> Set<T>
    findAllRefConfigs(
        final QueryMgr  queryMgr,
        final Class<T>  intf)
    {
        final Set<RefConfig>  refs    = queryMgr.queryTypeSet( Util.deduceType(intf) );

        return as( refs, intf);
    }

    /**
        Find all {@link ResourceRefConfig}.
        @return Set of all {@link ResourceRefConfig} found.
     */
        public static Set<ResourceRefConfig>
    findAllResourceRefConfigs( final QueryMgr  queryMgr )
    {
        return findAllRefConfigs( queryMgr, ResourceRefConfig.class);
    }

    /**
        Find all {@link DeployedItemRefConfig}.
        @return Set of all {@link DeployedItemRefConfig} found.
     */
        public static Set<DeployedItemRefConfig>
    findAllDeployedItemRefConfigs( final QueryMgr  queryMgr )
    {
        return findAllRefConfigs( queryMgr, DeployedItemRefConfig.class);
    }

    /**
        Find all {@link ServerRefConfig}.
        @return Set of all {@link ServerRefConfig} found.
     */
        public static Set<ServerRefConfig>
    findAllServerRefConfigs( final QueryMgr  queryMgr )
    {
        return findAllRefConfigs( queryMgr, ServerRefConfig.class);
    }


    /**
        Find all {@link ClusterRefConfig}.
        @return Set of all {@link ClusterRefConfig} found.
     */
        public static Set<ClusterRefConfig>
    findAllClusterRefConfigs( final QueryMgr  queryMgr )
    {
        return findAllRefConfigs( queryMgr, ClusterRefConfig.class);
    }

    /**
        Find all {@link RefConfig} of the specified j2EEType having
        the specified name.
        @return Set of all {@link RefConfig} found with the specified name.
     */
        public static Set<RefConfig>
    findAllRefConfigsWithName(
        final QueryMgr  queryMgr,
        final String    refJ2EEType,
        final String    name )
    {
        final String props = Util.makeRequiredProps( refJ2EEType, name );

        final Set<RefConfig> refs  = queryMgr.queryPatternSet( null, props );

        return refs;
    }

    /**
        @return the type of the config element which can <i>refer</i> to this item
     */
        public static String
    getReferentRefType( final RefConfigReferent referent )
    {
        String  type    = null;

        if ( referent instanceof ResourceRefConfigReferent )
        {
            type    = Util.deduceType(ResourceRefConfig.class);
        }
        else if ( referent instanceof DeployedItemRefConfigReferent )
        {
            type    = Util.deduceType(DeployedItemRefConfig.class);
        }
        else if ( referent instanceof ServerRefConfigReferent )
        {
            type    = Util.deduceType(ServerRefConfig.class);
        }
        else if ( referent instanceof ClusterRefConfigReferent )
        {
            type    = Util.deduceType(ClusterRefConfig.class);
        }
        else
        {
            throw new IllegalArgumentException(
                "Unknown referent class: " + Util.getTypeProp(Util.asAMX(referent)) );
        }

        return type;
    }
        public static Set<String>
    getReferentJ2EETypes()
    {
        return REFERENT_J2EE_TYPES;
    }


     /**
        Find all {@link RefConfig} that reference the specified type/name
        combo.
     */
        public static Set<RefConfig>
    findAllRefConfigs( final RefConfigReferent    referent )
    {
        final AMXProxy   amx = Util.asAMX( referent );

        return findAllRefConfigsWithName( getQueryMgr( amx ),
                    getReferentRefType( referent ),
                    amx.getName() );
    }

    /**
        Find all {@link RefConfig} that reference the specified type/name
        combo.
     */
        public static Set<RefConfig>
    findAllRefConfigs( final RefConfigReferent referent, final String refJ2EEType )
    {
        final AMXProxy   amx = Util.asAMX( referent );

        return findAllRefConfigsWithName( getQueryMgr( amx ), refJ2EEType, amx.getName() );
    }

    /**
        Find all {@link DeployedItemRefConfig} that reference
        the specified item.
     */
        public static Set<DeployedItemRefConfig>
    findAllRefConfigs( final DeployedItemRefConfigReferent referent )
    {
        final AMXProxy   amx = Util.asAMX( referent );

        final Set<RefConfig> refs = 
            findAllRefConfigsWithName( getQueryMgr( amx ), DEPLOYED_ITEM_REF_CONFIG_TYPE, amx.getName() );

        return as( refs, DeployedItemRefConfig.class);
    }

    /**
        Find all {@link ResourceRefConfig} that reference
        the specified item.
     */
        public static Set<ResourceRefConfig>
    findAllRefConfigs( final ResourceRefConfigReferent referent )
    {
        final AMXProxy   amx = Util.asAMX( referent );

       final Set<RefConfig> refs =
            findAllRefConfigsWithName( getQueryMgr( amx ), RESOURCE_REF_CONFIG_TYPE, amx.getName() );
        return as( refs, ResourceRefConfig.class);
    }

        public static <T  extends RefConfig> Set<T>
    as( Set<? extends RefConfig> refs, Class<T> intf )
    {
        final Set<T> s = new HashSet<T>();
        for( final RefConfig r : refs )
        {
            s.add( r.as(intf) );
        }
        return s;
    }

    /**
        Find all {@link ServerRefConfig} that reference
        the specified item.
     */
        public static Set<ServerRefConfig>
    findAllRefConfigs( final ServerRefConfigReferent referent )
    {
        final AMXProxy   amx = Util.asAMX( referent );

        return as( 
            findAllRefConfigsWithName( getQueryMgr( amx ), SERVER_REF_CONFIG_TYPE, amx.nameProp() ), ServerRefConfig.class);
    }

        private static QueryMgr
    getQueryMgr( final AMXProxy amx )
    {
        return amx.extra().proxyFactory().getDomainRootProxy().getQueryMgr();
    }

    /**
        Find all {@link ClusterRefConfig} that reference
        the specified item.
     */
        public static Set<ClusterRefConfig>
    findAllRefConfigs( final ClusterRefConfigReferent referent )
    {
        final AMXProxy   amx = Util.asAMX( referent );

        final Set<RefConfig> refs =
            findAllRefConfigsWithName( getQueryMgr( amx ), CLUSTER_REF_CONFIG_TYPE, amx.nameProp() );
        return as(refs, ClusterRefConfig.class);
    }

        public static Set<RefConfig>
    removeAllRefsTo(
        final RefConfigReferent item,
        final boolean allowSingleFailure )
    {
        final Set<RefConfig> refs   = RefHelper.findAllRefConfigs( item );

        final Set<RefConfig>    failures   = RefHelper.removeRefConfigs( refs );
        if ( failures.size() != 0 )
        {
            if ( failures.size() > 1 || ! allowSingleFailure )
            {
                final List<ObjectName>  objectNames = Util.toObjectNames( failures );

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
        public static Set<RefConfig>
    removeRefConfigs( final Set<RefConfig> refs )
    {
        final Set<RefConfig> failures    = new HashSet<RefConfig>();

        for( final RefConfig ref : refs )
        {
            /* while deleting references, certain ones, such as those contained
               within a cluster, implicitly delete other (redundant) references
               eg references by clustered servers.  Verify that a reference still
               exists before attempting to delete it.
             */
            final Extra extra = ref.extra();
            if ( extra.valid() )
            {
                final AMXConfigProxy rrc   = AMXConfigProxy.class.cast( ref.getParent() );

                try
                {
                    // may fail as illegal in PE or EE with one ref
                    rrc.removeChild( Util.getTypeProp(ref), ref.getName() );
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












