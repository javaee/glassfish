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
package org.glassfish.admin.amx.mbean;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Extra;
import com.sun.appserv.management.util.jmx.JMXUtil;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import java.util.HashMap;
import java.util.Map;


/**
	Caches MBeanInfos for AMX MBeans
 */
public final class MBeanInfoCache
{
	private final	Map<Class<? extends AMX>, MBeanInfo> mInfos = new HashMap<Class<? extends AMX>, MBeanInfo>();
	private final	Map<Class<?>, MBeanInfo> mOtherInfos = new HashMap<Class<?>, MBeanInfo>();
    
    private static final MBeanInfoCache INSTANCE = new MBeanInfoCache();
    
    private final MBeanAttributeInfo[] EXTRA = getExtraAttributeInfos();
    
    private MBeanInfoCache()
    {
    }
    
        public static synchronized MBeanInfo
    getAMXMBeanInfo( final Class<? extends AMX> amxInterface )
    {
        MBeanInfo info = INSTANCE.mInfos.get( amxInterface );
        if ( info == null )
        {
            info = MBeanInfoConverter.getInstance().convert( amxInterface, INSTANCE.EXTRA );
            INSTANCE.mInfos.put( amxInterface, info );
        }
        return info;
    }
    
        public static synchronized MBeanInfo
    getOtherMBeanInfo( final Class<?> intf )
    {
        MBeanInfo info = INSTANCE.mOtherInfos.get( intf );
        if ( info == null )
        {
            info = MBeanInfoConverter.getInstance().convert( intf, null );
            INSTANCE.mOtherInfos.put( intf, info );
        }
        return info;
    }

        
    /**
		A design decision was to not include certain Attributes or pseuodo-Attributes directly
		in AMX, so these fields are separated out into 'Extra'.  However, some of these
		are real Attributes that do need to be present in the MBeanInfo supplied by each
		MBean.
	 */
		private static MBeanAttributeInfo[]
	getExtraAttributeInfos()
	{
        final String[] EXTRA_REMOVALS = new String[]
        {
            "ProxyFactory",
            "ConnectionSource",
            "MBeanInfo",
            "AllAttributes",
        };
        
        final MBeanAttributeInfo[]	extraInfos	=
            JMXUtil.interfaceToMBeanInfo( Extra.class ).getAttributes();
            
        // remove items that are client-side constructs; not real Attributes
        final Map<String,MBeanAttributeInfo>	m	= JMXUtil.attributeInfosToMap( extraInfos );
        for( int i = 0; i < EXTRA_REMOVALS.length; ++i )
        {
            m.remove( EXTRA_REMOVALS[ i ] );
        }
        
        final MBeanAttributeInfo[] result	= new MBeanAttributeInfo[ m.values().size() ];
        m.values().toArray( result );
		
		return( result );
	}

}








