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
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

import javax.management.ObjectName;
import javax.management.JMException;


import com.sun.enterprise.management.AMXTestBase;
import com.sun.enterprise.management.Capabilities;

import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.LifecycleModuleConfig;

import com.sun.appserv.management.helper.RefHelper;

import com.sun.enterprise.management.support.AMXLifecycleModule;

/**
 */
public final class LifecycleModuleConfigTest extends AMXTestBase
{
    // built-into server already
    private static final String IMPL_CLASSNAME = AMXLifecycleModule.class.getName();
        
    private static final String TEST_NAME_BASE = "custom";
    private static final String TEST_TYPE      = "CustomMBeanConfigTest";
    
		public
	LifecycleModuleConfigTest ()
	{
	    if ( checkNotOffline( "ensureDefaultInstance" ) )
	    {
	        ensureDefaultInstance( getDomainConfig() );
	    }
	}
	
	   public static String
    getDefaultInstanceName()
    {
        return getDefaultInstanceName( "LifecycleModuleConfig" );
    }
    
        public static LifecycleModuleConfig
	ensureDefaultInstance( final DomainConfig domainConfig )
	{
	    LifecycleModuleConfig   result  =
	        domainConfig.getLifecycleModuleConfigMap().get( getDefaultInstanceName() );
	    
	    if ( result == null )
	    {
	        result  = createInstance( 
	            domainConfig,
	            getDefaultInstanceName(),
	            IMPL_CLASSNAME,
	            1 );
	    }
	    
	    return result;
	}
	
		public static LifecycleModuleConfig
	createInstance(
	    final DomainConfig          domainConfig,
	    final String                name,
	    final String                classname,
	    final int                   loadOrder )
	{
	    final boolean  isFailureFatal = false;
	    final boolean  enabled = true;
	    final String   classpath = "/test";
	    final String   description = null;
	    
	    final LifecycleModuleConfig life    =
	    	domainConfig.createLifecycleModuleConfig(  name,
	    	                    description,
	                            classname,
	                            classpath, 
	                            "" + loadOrder,
	                            isFailureFatal,
	                            enabled,
	                            null );
	    
	    return life;
	}
	
	
		public Map<String,LifecycleModuleConfig>
	getLifecycleModuleConfigs()
	{
	    return getDomainConfig().getLifecycleModuleConfigMap();
	}
	
		private void
	_testGetAll()
	{
	    
	    final Map<String,LifecycleModuleConfig> all = getLifecycleModuleConfigs();
		assert( all != null );
	}
	
	    private void
	sanityCheck( final LifecycleModuleConfig config )
	{
        config.setClassname( config.getClassname() );
        
        final String    classpath = config.getClasspath();
        config.setClasspath( classpath == null ? "" : classpath );
        
        config.setLoadOrder( config.getLoadOrder() );
        
        config.setIsFailureFatal( config.getIsFailureFatal() );
        
        config.setEnabled( config.getEnabled() );
	}

		public synchronized void
	testAttrs()
	{
	    ensureDefaultInstance( getDomainConfig() );
	    
	    final Map<String,LifecycleModuleConfig> all = getLifecycleModuleConfigs();
	    
	    if ( all.size() != 0 )
	    {
    	    for( final LifecycleModuleConfig config : all.values() )
    	    {
    	        sanityCheck( config );
    	    }
	    }
	    else
	    {
	        warning( "LifecycleModuleConfigTest: No LifecycleModuleConfig to test" );
	    }
	}
	
		public synchronized void
	testCreateRemove()
	{
	    final DomainConfig  domainConfig    = getDomainConfig();
	    
	}
}






































