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


import com.sun.appserv.management.config.AvailabilityServiceConfig;

import com.sun.enterprise.management.AMXTestBase;
import com.sun.enterprise.management.ClusterSupportRequired;

import javax.management.InstanceNotFoundException;


public class AvailabilityServiceConfigTest extends AMXTestBase
    implements ClusterSupportRequired
{
	public AvailabilityServiceConfigTest()
        throws InstanceNotFoundException
	{
	}
	
	    private AvailabilityServiceConfig
	getIt()
	{
		return getConfigConfig().getAvailabilityServiceConfig();
	}
        
        public void
    testWarnAvail()
    {
        if ( getIt() == null )
        {
            assert false : "AvailabilityServiceConfigTest:  no AvailabilityServiceConfig to test";
        }
    }
			
	/**
	 * Test of [g/s]etAvailabilityEnabled method, of class com.sun.appserv.management.config.AvailabilityServiceConfig.
	 */
	public void testAvailabilityEnabled()
	{
        if ( getIt() != null )
        {
            getIt().setAvailabilityEnabled(false);
            assertFalse("getAvailabilityEnabled() was supposed to return false.", getIt().getAvailabilityEnabled());
            getIt().setAvailabilityEnabled(true);
            assertTrue("getAvailabilityEnabled() was supposed to return true.", getIt().getAvailabilityEnabled());
        }
	}

	/**
	 * Test of [g/s]etAutoManageHAStore method, of class com.sun.appserv.management.config.AvailabilityServiceConfig.
	 */
	public void testAutoManageHAStore()
	{
        if ( getIt() != null )
        {
            final boolean save = getIt().getAutoManageHAStore();
            getIt().setAutoManageHAStore(true);
            assertTrue("getAutoManageHAStore() was supposed to return true.", getIt().getAutoManageHAStore());
            getIt().setAutoManageHAStore(false);
            assertFalse("getAutoManageHAStore() was supposed to return false.", getIt().getAutoManageHAStore());
            getIt().setAutoManageHAStore(save);
        }
	}

	/**
	 * Test of [g/s]etHAAgentHosts methods, of class com.sun.appserv.management.config.AvailabilityServiceConfig.
	 */
	public void testHAAgentHosts()
	{
        if ( getIt() != null )
        {
            final String hosts = "hp,hp,hp,hp";
            final String save = getIt().getHAAgentHosts();
            getIt().setHAAgentHosts(hosts);
            String s = getIt().getHAAgentHosts();
            assertEquals(hosts, s);
            getIt().setHAAgentHosts( (save == null) ? "" : save);
        }
	}

	/**
	 * Test of [g/s]etHAAgentPort methods, of class com.sun.appserv.management.config.AvailabilityServiceConfig.
	 */
	public void testHAAgentPort()
	{
        if ( getIt() != null )
        {
            final String port = "3456";
            final String save = getIt().getHAAgentPort();
            getIt().setHAAgentPort(port);
            final String s = getIt().getHAAgentPort();
            assertEquals(port, s);
            getIt().setHAAgentPort( (save == null) ? "" : save);
        }
	}

	/**
	 * Test of [g/s]etHAStoreHealthcheckIntervalSeconds methods, of class com.sun.appserv.management.config.AvailabilityServiceConfig.
	 */
	public void testHAStoreHealthcheckIntervalSeconds()
	{
        if ( getIt() != null )
        {
            final String time = "90";
            final String save = getIt().getHAStoreHealthcheckIntervalSeconds();
            getIt().setHAStoreHealthcheckIntervalSeconds(time);
            String s = getIt().getHAStoreHealthcheckIntervalSeconds();
            assertEquals(time, s);
            getIt().setHAStoreHealthcheckIntervalSeconds( (save == null) ? "" : save);
        }
	}

	/**
	 * Test of [g/s]etHAStoreName methods, of class com.sun.appserv.management.config.AvailabilityServiceConfig.
	 */
	public void testHAStoreName()
	{
        if ( getIt() != null )
        {
            final String storeName = "cluster1";
            final String save = getIt().getHAStoreName();
            getIt().setHAStoreName(storeName);
            final String s = getIt().getHAStoreName();
            assertEquals(storeName, s);
            getIt().setHAStoreName( (save == null) ? "" : save);
        }
	}
	
	/**
	 * Test of [g/s]etStorePoolName methods, of class com.sun.appserv.management.config.AvailabilityServiceConfig.
	 */
	public void testStorePoolName()
	{
        if ( getIt() != null )
        {
            final String storeName = "xxxx";
            final String save = getIt().getStorePoolName();
            getIt().setStorePoolName(storeName);
            final String s = getIt().getStorePoolName();
            assertEquals(storeName, s);
            getIt().setStorePoolName( (save == null) ? "" : save);
        }
	}

	/**
	 * Test of [g/s]etHAStoreHealthcheckEnabled methods, of class com.sun.appserv.management.config.AvailabilityServiceConfig.
	 */
	public void testHAStoreHealthcheckEnabled()
	{
        if ( getIt() != null )
        {
            final boolean save = getIt().getHAStoreHealthcheckEnabled();
            
            getIt().setHAStoreHealthcheckEnabled(false);
            assertFalse("getHAStoreHealthcheckEnabled() was supposed to return false.", getIt().getHAStoreHealthcheckEnabled());
            getIt().setHAStoreHealthcheckEnabled(true);
            assertTrue("getHAStoreHealthcheckEnabled() was supposed to return true.", getIt().getHAStoreHealthcheckEnabled());
            getIt().setHAStoreHealthcheckEnabled( save );
        }
	}
}


