/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 * $Id: ConfigContextImplTest.java,v 1.1 2004/04/30 00:07:57 ramakant Exp $
 */

package com.sun.enterprise.config.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

//junit imports
import junit.framework.*;
import junit.textui.TestRunner;

import com.sun.logging.LogDomains;
import com.sun.logging.ee.EELogDomains;

import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigContextFactory;
import com.sun.enterprise.config.pluggable.ConfigEnvironment;
import com.sun.enterprise.config.pluggable.ConfigBeanInterceptor;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Ssl;
import com.sun.enterprise.config.serverbeans.IiopListener;
import com.sun.enterprise.config.serverbeans.AppserverConfigEnvironmentFactory;
import com.sun.enterprise.util.SystemPropertyConstants;

import com.sun.enterprise.config.impl.ConfigContextImpl;

public class ConfigContextImplTest extends TestCase
{
    /** Creates a new instance of ConfigContextImplTest */
    public ConfigContextImplTest(String name)
    {
        super(name);
    }

	public void testInitContext()
	{
		assertTrue(runtimeCtx != null);
		assertTrue(adminCtx != null);
		assertTrue(adminCtx != runtimeCtx);
	}

	public void testRuntimeCtx() throws Exception
	{
		assertTrue(getInterceptor(runtimeCtx).isResolvingPaths());
		ConfigContext clone = (ConfigContext)runtimeCtx.clone();
		assertTrue(getInterceptor(clone).isResolvingPaths());
	}

	public void testAdminCtx() throws Exception
	{
		assertFalse(getInterceptor(adminCtx).isResolvingPaths());
		ConfigContext clone = (ConfigContext)adminCtx.clone();
		assertFalse(getInterceptor(clone).isResolvingPaths());
	}

	public void testDomainInRuntimeCtx() throws Exception
	{
		Domain domain = (Domain)runtimeCtx.getRootConfigBean();
		assertTrue(domain.getInterceptor().isResolvingPaths());
		ConfigContext clone = (ConfigContext)runtimeCtx.clone();
		domain = (Domain)clone.getRootConfigBean();
		assertTrue(domain.getInterceptor().isResolvingPaths());
	}

	public void testDomainInAdminCtx() throws Exception
	{
		Domain domain = (Domain)adminCtx.getRootConfigBean();
		assertFalse(domain.getInterceptor().isResolvingPaths());
		ConfigContext clone = (ConfigContext)adminCtx.clone();
		domain = (Domain)clone.getRootConfigBean();
		assertFalse(domain.getInterceptor().isResolvingPaths());
	}

	public void testServerInRuntimeCtx() throws Exception
	{
		Domain domain = (Domain)runtimeCtx.getRootConfigBean();
		Server s = domain.getServers().getServerByName("server");
		assertTrue(s.getInterceptor().isResolvingPaths());
		ConfigContext clone = (ConfigContext)runtimeCtx.clone();
		assertTrue(s.getInterceptor().isResolvingPaths());
		domain = (Domain)clone.getRootConfigBean();
		s = domain.getServers().getServerByName("server");
		assertTrue(s.getInterceptor().isResolvingPaths());
	}

	public void testServerInAdminCtx() throws Exception
	{
		Domain domain = (Domain)adminCtx.getRootConfigBean();
		Server s = domain.getServers().getServerByName("server");
		assertFalse(s.getInterceptor().isResolvingPaths());
		ConfigContext clone = (ConfigContext)adminCtx.clone();
		assertFalse(s.getInterceptor().isResolvingPaths());
		domain = (Domain)clone.getRootConfigBean();
		s = domain.getServers().getServerByName("server");
		assertFalse(s.getInterceptor().isResolvingPaths());
	}

	public void testSSLPortInRuntimeCtx() throws Exception
	{
		assertEquals("1060", getSSLPort(runtimeCtx));
		ConfigContext clone = (ConfigContext)runtimeCtx.clone();
		assertEquals("1060", getSSLPort(clone));
	}

	public void testSSLPortInAdminCtx() throws Exception
	{
		assertEquals("${SSL-port}", getSSLPort(adminCtx));
		ConfigContext clone = (ConfigContext)adminCtx.clone();
		assertEquals("${SSL-port}", getSSLPort(clone));
	}

 	public void testAllChildrenInRuntimeCtx() throws Exception
	{
		testAllConfigBeans(getAllConfigBeans(runtimeCtx), true);
		ConfigContext clone = (ConfigContext)runtimeCtx.clone();
		testAllConfigBeans(getAllConfigBeans(clone), true);
		clone = (ConfigContext)clone.clone();
		testAllConfigBeans(getAllConfigBeans(clone), true);
	}

 	public void testAllChildrenInAdminCtx() throws Exception
	{
		testAllConfigBeans(getAllConfigBeans(adminCtx), false);
		ConfigContext clone = (ConfigContext)adminCtx.clone();
		testAllConfigBeans(getAllConfigBeans(clone), false);
		clone = (ConfigContext)clone.clone();
		testAllConfigBeans(getAllConfigBeans(clone), false);
	}

	public void testSsl() throws Exception
	{
		Domain domain = (Domain)runtimeCtx.getRootConfigBean();
		IiopListener listener = domain.getConfigs().getConfigByName("server-config").
			getIiopService().getIiopListenerById("SSL");
		assertTrue(listener.getInterceptor().isResolvingPaths());
		Ssl ssl = listener.getSsl();
		assertTrue(ssl.getConfigContext() == runtimeCtx);
		assertTrue(ssl.getInterceptor().isResolvingPaths());
	}

	List testAllConfigBeans(List l, boolean isResolve)
	{
		assertTrue((l != null) && (l.size() >=1));
		Iterator it = l.iterator();
		List failed = new ArrayList();
		while (it.hasNext())
		{
			ConfigBean cb = (ConfigBean)it.next();
			if (isResolve != cb.getInterceptor().isResolvingPaths())
			{
				failed.add(cb.getXPath());
			}
		}
		if (failed.size() > 0)
		{
			System.out.println("Failed: "+failed);
		}
		return failed;
	}

	List getAllConfigBeans(ConfigContext cc) throws Exception
	{
		ArrayList al = new ArrayList();
		ConfigBean root = cc.getRootConfigBean();
		assertTrue(root != null);
		al.add(root);
		al.addAll(getAllChildBeans(root));
		return al;
	}

	List getAllChildBeans(ConfigBean parent)
	{
		ArrayList al = new ArrayList();
		ConfigBean[] children = parent.getAllChildBeans();
		if (children != null)
		{
			for (int i = 0; i < children.length; i++)
			{
				if (children[i] != null)
				{
					al.add(children[i]);
					al.addAll(getAllChildBeans(children[i]));
				}
			}
		}
		return al;
	}

	String getSSLPort(ConfigContext cc) throws Exception
	{
		Domain domain = (Domain)cc.getRootConfigBean();
		assertTrue(domain != null);
		IiopListener sslListener = domain.getConfigs().
			getConfigByName("server-config").getIiopService().getIiopListenerById("SSL");
		assertTrue(sslListener != null);
		return sslListener.getPort();
	}

	boolean isResolve(ConfigContext cc)
	{
		return ((ConfigContextImpl)cc).getConfigBeanInterceptor().isResolvingPaths();
	}

	ConfigBeanInterceptor getInterceptor(ConfigContext cc)
	{
		return ((ConfigContextImpl)cc).getConfigBeanInterceptor();
	}

	protected void setUp()
    {
        LogDomains.getLogger(EELogDomains.EE_ADMIN_LOGGER);
    }

    protected void tearDown()
    {
    }

    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite(ConfigContextImplTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception
    {
		System.setProperty("SSL-port", "1060");

		initRuntimeContext();
		initAdminContext();
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(ConfigContextImplTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }

	static ConfigContext runtimeCtx = null;
    private static void initRuntimeContext() throws Exception
    {
		ConfigEnvironment ce = getConfigEnvironment();
		runtimeCtx = ConfigContextFactory.createConfigContext(ce);
    }

	static ConfigContext adminCtx = null;
    private static void initAdminContext() throws Exception
    {
		ConfigEnvironment ce = getConfigEnvironment();
        ce.getConfigBeanInterceptor().setResolvingPaths(false);
		adminCtx = ConfigContextFactory.createConfigContext(ce);
    }

	static ConfigEnvironment getConfigEnvironment()
	{
		ConfigEnvironment ce = new AppserverConfigEnvironmentFactory().
			getConfigEnvironment();
		ce.setUrl("F:\\tmp\\domain.xml");
        ce.setReadOnly(false);
        ce.setCachingEnabled(false);
        ce.setRootClass("com.sun.enterprise.config.serverbeans.Domain");
        ce.setHandler("com.sun.enterprise.config.serverbeans.ServerValidationHandler");
		return ce;
	}
}
