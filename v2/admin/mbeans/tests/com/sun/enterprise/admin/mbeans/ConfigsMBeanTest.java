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
 * $Id: ConfigsMBeanTest.java,v 1.3 2005/12/25 03:43:12 tcfujii Exp $
 */

package com.sun.enterprise.admin.mbeans;

//jdk imports
import java.util.Properties;
import java.io.File;

//junit imports
import junit.framework.*;
import junit.textui.TestRunner;

//JMX imports
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.MBeanServer;

//config imports
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

//config mbean imports
import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.admin.BaseAdminMBean;

public class ConfigsMBeanTest extends TestCase
{
    public void testCreateConfigsMBean()
    {
        ConfigsMBean mbean = new ConfigsMBean();
        Assert.assertTrue(mbean != null);
    }

    public void testGetConfigMBean() throws Exception
    {
        ObjectName on = configsMBean.getConfigMBean(getTarget());
        Assert.assertEquals(getConfigMBean(), on);
    }

    public void testGetHttpServiceMBean() throws Exception
    {
        ObjectName on = configsMBean.getHttpServiceMBean(getTarget());
        Assert.assertEquals(getHttpService(), on);
    }

    public void testHttpListener() throws Exception
    {
        final String target = getTargetName();
        ObjectName on = configsMBean.createHttpListener(
                                getHttpListenerAttrs(), null, target);
        Assert.assertTrue(on != null);
        Assert.assertEquals(getHttpListener(), on);
        ObjectName[] names = configsMBean.listHttpListeners(target);
        Assert.assertTrue(names != null);
        Assert.assertTrue(names.length > 0);
        Assert.assertTrue(exists(getHttpListener(), names));
        boolean isDeleted = configsMBean.deleteHttpListener("mylistener", null);
        Assert.assertTrue(isDeleted);
        names = configsMBean.listHttpListeners(target);
        Assert.assertFalse(exists(getHttpListener(), names));
    }

    private AttributeList getHttpListenerAttrs()
    {
        AttributeList attrList = new AttributeList();
        attrList.add(new Attribute("id", "mylistener"));
        attrList.add(new Attribute("address", "abcd"));
        attrList.add(new Attribute("port", "9999"));
        attrList.add(new Attribute("server-name", "server-name"));
        attrList.add(new Attribute("default-virtual-server", "server-name"));
        return attrList;
    }

    public void testIiopListener() throws Exception
    {
        final String target = getTargetName();
        ObjectName on = configsMBean.createIiopListener(
                                getIiopListenerAttrs(), null, target);
        Assert.assertTrue(on != null);
        Assert.assertEquals(getIiopListener(), on);
        ObjectName[] names = configsMBean.listIiopListeners(target);
        Assert.assertTrue(names != null);
        Assert.assertTrue(names.length > 0);
        Assert.assertTrue(exists(getIiopListener(), names));
        boolean isDeleted = configsMBean.deleteIiopListener("mylistener", null);
        Assert.assertTrue(isDeleted);
        names = configsMBean.listIiopListeners(target);
        Assert.assertFalse(exists(getIiopListener(), names));
    }

    private AttributeList getIiopListenerAttrs()
    {
        AttributeList attrList = new AttributeList();
        attrList.add(new Attribute("id", "mylistener"));
        attrList.add(new Attribute("address", "address"));
        return attrList;
    }

    public void testSSL() throws Exception
    {
        final String target = getTargetName();
        try
        {
            configsMBean.createHttpListener(getHttpListenerAttrs(), null, target);
            configsMBean.createIiopListener(getIiopListenerAttrs(), null, target);
            testSSL("http-listener");
            testSSL("iiop-listener");
            testSSL("iiop-service");
        }
        finally
        {
            configsMBean.deleteHttpListener("mylistener", target);
            configsMBean.deleteIiopListener("mylistener", target);
        }
    }

    void testSSL(String type) throws Exception
    {
        ObjectName ssl = null;
        final String target = getTargetName();
        AttributeList al = new AttributeList();
        al.add(new Attribute("cert-nickname", "nick1"));
        ssl = configsMBean.createSsl(al, "mylistener", type, target);
        Assert.assertTrue(ssl != null);
        ObjectName on = configsMBean.getSsl(type, "mylistener", target);
        Assert.assertEquals(ssl, on);
        boolean isDeleted = configsMBean.deleteSsl(
                            "mylistener", type, target);
        Assert.assertTrue(isDeleted);
    }

    public void testVirtualServer() throws Exception
    {
        final String target = getTargetName();
        ObjectName on = configsMBean.createVirtualServer(
                                getVirtualServerAttrs(), null, target);
        Assert.assertTrue(on != null);
        Assert.assertEquals(getVirtualServer(), on);
        ObjectName[] names = configsMBean.listVirtualServers(target);
        Assert.assertTrue(names != null);
        Assert.assertTrue(names.length > 0);
        Assert.assertTrue(exists(getVirtualServer(), names));
        boolean isDeleted = configsMBean.deleteVirtualServer(
            "my-virtual-server", null);
        Assert.assertTrue(isDeleted);
        names = configsMBean.listVirtualServers(target);
        Assert.assertFalse(exists(getVirtualServer(), names));
    }

    private AttributeList getVirtualServerAttrs()
    {
        AttributeList attrList = new AttributeList();
        attrList.add(new Attribute("id", "my-virtual-server"));
        attrList.add(new Attribute("hosts", "host1,host2"));
        return attrList;
    }

    public void testAuthRealm() throws Exception
    {
        final String target = getTargetName();
        final String realm = "myAuth";
        final AttributeList al = new AttributeList();
        al.add(new Attribute("name", realm));
        al.add(new Attribute("classname", "myauth.Auth"));

        ObjectName on = configsMBean.createAuthRealm(al, null, target);
        Assert.assertTrue(on != null);
        Assert.assertEquals(getAuthRealm(realm), on);
        ObjectName[] authRealms = configsMBean.listAuthRealms(target);
        Assert.assertTrue(authRealms != null);
        Assert.assertTrue(exists(on, authRealms));
        boolean deleted = configsMBean.deleteAuthRealm(realm, target);
        Assert.assertTrue(deleted);
        authRealms = configsMBean.listAuthRealms(target);
        Assert.assertTrue(authRealms != null);
        Assert.assertTrue(!exists(on, authRealms));
    }

    public void testProfiler() throws Exception
    {
        final String name = "myprofiler";
        AttributeList al = new AttributeList();
        al.add(new Attribute("name", name));

        ObjectName on = configsMBean.createProfiler(
                                al, getProperties(), null);
        Assert.assertTrue(on != null);
        Assert.assertEquals(getProfiler(), on);
        on = configsMBean.getProfiler(getTargetName());
        Assert.assertEquals(getObjectName("profiler", null), on);
        boolean isDeleted = configsMBean.deleteProfiler(null);
        Assert.assertTrue(isDeleted);
    }

    public void testJvmOptions() throws Exception
    {
        String[] exOpts = configsMBean.getJvmOptions(false, getTargetName());
        Assert.assertTrue(exOpts != null);
        String[] jvmOpts = new String[] {"-Dn1=v1", "-Dn2=v2", "-Dn3=v3"};
        configsMBean.createJvmOptions(jvmOpts, false, getTargetName());
        configsMBean.deleteJvmOptions(jvmOpts, false, getTargetName());
    }

    public void testFileUser() throws Exception
    {
        final String    user    = "ramakant3";
        final String    pwd     = "ramakant";
        final String[]  grps    = {"grp1"};
        final String    realm   = "file";

        configsMBean.addUser(user, pwd, grps, realm, getTargetName());
        configsMBean.updateUser(user, pwd, grps, realm, getTargetName());
        String[] users = configsMBean.getUserNames(realm, getTargetName());
        Assert.assertTrue(users != null);
        Assert.assertTrue(exists(user, users));

        String[] groups = configsMBean.getGroupNames(user, realm, 
                            getTargetName());
        Assert.assertTrue(groups != null);
        Assert.assertTrue(exists(grps[0], groups));

        configsMBean.removeUser(user, realm, getTargetName());
        users = configsMBean.getUserNames(realm, getTargetName());
        Assert.assertFalse(exists(user, users));
        groups = configsMBean.getGroupNames(null, realm, getTargetName());
        Assert.assertFalse(exists(grps[0], groups));
    }

    public void testAuditModule() throws Exception
    {
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute("name", "xaudit"));
        attrs.add(new Attribute("classname", "audit.xaudit"));

        ObjectName on = configsMBean.createAuditModule(
                            attrs, getProperties(), getTargetName());
        Assert.assertTrue(on != null);
        Assert.assertEquals(getAuditModule("xaudit"), on);

        ObjectName[] onames = configsMBean.listAuditModules(getTargetName());
        Assert.assertTrue(onames != null);
        Assert.assertTrue(onames.length > 0);
        Assert.assertTrue(exists(getAuditModule("xaudit"), onames));

        boolean isDeleted = configsMBean.deleteAuditModule(
                                "xaudit", getTargetName());
        Assert.assertTrue(isDeleted);
        onames = configsMBean.listAuditModules(getTargetName());
        Assert.assertTrue(!exists(getAuditModule("xaudit"), onames));
    }

    public void testJmsHost() throws Exception
    {
        final AttributeList attrs = new AttributeList();
        attrs.add(new Attribute("name", "host1"));

        ObjectName on = configsMBean.createJmsHost(
                            attrs, getProperties(), getTargetName());
        Assert.assertTrue(on != null);
        Assert.assertEquals(getJmsHost("host1"), on);
        ObjectName[] onames = configsMBean.listJmsHosts(getTargetName());
        Assert.assertTrue(onames != null);
        Assert.assertTrue(onames.length > 0);
        Assert.assertTrue(exists(getJmsHost("host1"), onames));
        boolean isDeleted = configsMBean.deleteJmsHost("host1", getTargetName());
        Assert.assertTrue(isDeleted);
        onames = configsMBean.listJmsHosts(getTargetName());
        Assert.assertTrue(!exists(getJmsHost("host1"), onames));
    }

    public void testJaccProvider() throws Exception
    {
        final AttributeList attrs = new AttributeList();
        attrs.add(new Attribute("name", "provider1"));
        attrs.add(new Attribute("policy-provider", "provider1"));
        attrs.add(new Attribute("policy-configuration-factory-provider", 
                    "providerfactory"));

        ObjectName on = configsMBean.createJaccProvider(
                            attrs, getProperties(), getTargetName());
        ObjectName[] onames = configsMBean.listJaccProviders(getTargetName());
        Assert.assertTrue(onames != null);
        Assert.assertTrue(onames.length > 0);
        Assert.assertTrue(exists(getJaccProvider("provider1"), onames));
        boolean isDeleted = configsMBean.deleteJaccProvider(
                                "provider1", getTargetName());
        Assert.assertTrue(isDeleted);
        onames = configsMBean.listJaccProviders(getTargetName());
        Assert.assertTrue(!exists(getJaccProvider("provider1"), onames));
    }

    public void testThreadPool() throws Exception
    {
        final AttributeList attrs = new AttributeList();
        attrs.add(new Attribute("thread-pool-id", "testPool"));

        ObjectName on = configsMBean.createThreadPool(
                            attrs, null, getTargetName());
        ObjectName[] onames = configsMBean.listThreadPools(getTargetName());
        Assert.assertTrue(onames != null);
        Assert.assertTrue(onames.length > 0);
        Assert.assertTrue(exists(getThreadPool("testPool"), onames));
        boolean isDeleted = configsMBean.deleteThreadPool(
                                "testPool", getTargetName());
        Assert.assertTrue(isDeleted);
        onames = configsMBean.listThreadPools(getTargetName());
        Assert.assertTrue(!exists(getThreadPool("testPool"), onames));
    }

    public void testGetChild() throws Exception
    {
        getChild("http-listener", new String[] {"http-listener-1"});
        getChild("ejb-container", null);
        getChild("ssl", new String[] {"http-listener-1"});
        getChild("ssl#", null);
        getChild("ssl##", new String[]{"SSL"});

        try
        {
            ObjectName on = configsMBean.getChild(null, null, getTargetName());
            Assert.assertTrue(false);
        }
        catch (Exception e)
        {
            //ok
        }
    }

    private void getChild(String type, String[] loc) throws Exception
    {
        try
        {
            ObjectName on = configsMBean.getChild(type, loc, getTargetName());
            Assert.assertEquals(getObjectName(type, loc), on);
        }
        catch (MBeanException mbe) {}
    }

    public void testGetters() throws Exception
    {
        ObjectName on = configsMBean.getHttpService(getTargetName());
        Assert.assertEquals(getObjectName("http-service", null), on);

        on = configsMBean.getIiopService(getTargetName());
        Assert.assertEquals(getObjectName("iiop-service", null), on);

        on = configsMBean.getTransactionService(getTargetName());
        Assert.assertEquals(getObjectName("transaction-service", null), on);

        on = configsMBean.getMonitoringService(getTargetName());
        Assert.assertEquals(getObjectName("monitoring-service", null), on);

        on = configsMBean.getLogService(getTargetName());
        Assert.assertEquals(getObjectName("log-service", null), on);

        on = configsMBean.getSecurityService(getTargetName());
        Assert.assertEquals(getObjectName("security-service", null), on);

        on = configsMBean.getJmsService(getTargetName());
        Assert.assertEquals(getObjectName("jms-service", null), on);

        on = configsMBean.getEjbContainer(getTargetName());
        Assert.assertEquals(getObjectName("ejb-container", null), on);

        on = configsMBean.getWebContainer(getTargetName());
        Assert.assertEquals(getObjectName("web-container", null), on);

        on = configsMBean.getMdbContainer(getTargetName());
        Assert.assertEquals(getObjectName("mdb-container", null), on);

        on = configsMBean.getJavaConfig(getTargetName());
        Assert.assertEquals(getObjectName("java-config", null), on);

        on = configsMBean.getHttpListener("http-listener-1", getTargetName());
        Assert.assertEquals(
            getObjectName1("http-listener", "http-listener-1"), on);

        on = configsMBean.getVirtualServer("server", getTargetName());
        Assert.assertEquals(getObjectName1("virtual-server", "server"), on);

        on = configsMBean.getOrb(getTargetName());
        Assert.assertEquals(getObjectName("orb", null), on);

        on = configsMBean.getIiopListener("orb-listener-1", getTargetName());
        Assert.assertEquals(
            getObjectName1("iiop-listener", "orb-listener-1"), on);

        on = configsMBean.getJmsHost("default_JMS_host", getTargetName());
        Assert.assertEquals(getObjectName1("jms-host", "default_JMS_host"), on);

        on = configsMBean.getAuthRealm("file", getTargetName());
        Assert.assertEquals(getObjectName1("auth-realm", "file"), on);

        on = configsMBean.getAuditModule("default", getTargetName());
        Assert.assertEquals(getObjectName1("audit-module", "default"), on);

        on = configsMBean.getJaccProvider("default", getTargetName());
        Assert.assertEquals(getObjectName1("jacc-provider", "default"), on);

        on = configsMBean.getModuleLogLevels(getTargetName());
        Assert.assertEquals(getObjectName("module-log-levels", null), on);

        on = configsMBean.getModuleMonitoringLevels(getTargetName());
        Assert.assertEquals(getObjectName("module-monitoring-levels", null), on);

        on = configsMBean.getThreadPool("thread-pool-1", getTargetName());
        Assert.assertEquals(getObjectName1("thread-pool", "thread-pool-1"), on);
    }

    public void testEjbTimerService() throws Exception
    {
        ObjectName on = configsMBean.createEjbTimerService(
            null, getProperties(), getTargetName());
        Assert.assertTrue(on != null);
        on = configsMBean.getEjbTimerService(getTargetName());
        Assert.assertEquals(getObjectName("ejb-timer-service", null), on);
        boolean isDeleted = configsMBean.deleteEjbTimerService(getTargetName());
        Assert.assertTrue(isDeleted);
        try
        {
            configsMBean.getEjbTimerService(getTargetName());
            Assert.assertTrue(false);
        }
        catch (Exception e)
        {
            //ok
        }
    }

    public void testSessionConfig() throws Exception
    {
        ObjectName  on          = null;
        boolean     isDeleted   = false;

        on = configsMBean.createSessionProperties(null, getProperties(), 
            getTargetName());
        Assert.assertEquals(getObjectName("session-properties", null), on);
        on = configsMBean.getSessionProperties(getTargetName());
        Assert.assertEquals(getObjectName("session-properties", null), on);
        isDeleted = configsMBean.deleteSessionProperties(getTargetName());
        Assert.assertTrue(isDeleted);

        on = configsMBean.createManagerProperties(null, null, getTargetName());
        Assert.assertEquals(getObjectName("manager-properties", null), on);
        on = configsMBean.getManagerProperties(getTargetName());
        Assert.assertEquals(getObjectName("manager-properties", null), on);
        isDeleted = configsMBean.deleteManagerProperties(getTargetName());
        Assert.assertTrue(isDeleted);

        on = configsMBean.createStoreProperties(null, null, getTargetName());
        Assert.assertEquals(getObjectName("store-properties", null), on);
        on = configsMBean.getStoreProperties(getTargetName());
        Assert.assertEquals(getObjectName("store-properties", null), on);
        isDeleted = configsMBean.deleteStoreProperties(getTargetName());
        Assert.assertTrue(isDeleted);

        isDeleted = configsMBean.deleteSessionConfig(getTargetName());
        Assert.assertTrue(isDeleted);
    }

    public ConfigsMBeanTest(String name) throws Exception
    {
        super(name);
    }

    private ConfigContext configContext;
    private ConfigsMBean configsMBean;

    protected void setUp()
    {
        configsMBean = new MyConfigsMBean();
        try
        {
            configContext = ConfigFactory.createConfigContext(
                                domainXml.getAbsolutePath());
            loadMBeanRegistry();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    protected void tearDown()
    {
        configsMBean = null;
        configContext = null;
        mbeanRegistry = null;
    }

    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite(ConfigsMBeanTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception
    {
        setDomainXml(new File(args[0]));
        setMBeanDescriptor(new File(args[1]));

        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(ConfigsMBeanTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }

    private static File domainXml;

    public static void setDomainXml(File xml)
    {
        domainXml = xml;
    }

    private static File mbeanDescriptor;

    public static void setMBeanDescriptor(File f)
    {
        mbeanDescriptor = f;
    }

    private static MBeanRegistry mbeanRegistry;

    private static void loadMBeanRegistry() throws Exception
    {
        if (null == mbeanRegistry)
        {
            mbeanRegistry = MBeanRegistryFactory.getMBeanRegistry(
                                mbeanDescriptor.getAbsolutePath());
        }
    }

    private String getConfigRef() throws Exception
    {
        Target target = TargetBuilder.INSTANCE.createTarget(
                            getTargetName(), configContext);
        return target.getConfigRef();
    }

    ObjectName getHttpService() throws Exception
    {
        return new ObjectName(
            getTestDomain() + 
            ":type=http-service,category=config,config=" + getConfigRef());
    }

    ObjectName getConfigMBean() throws Exception
    {
        return new ObjectName(
            getTestDomain() + 
            ":type=config,category=config,name=" + getConfigRef());
    }

    ObjectName getHttpListener() throws Exception
    {
        return new ObjectName(
            getTestDomain() + 
            ":type=http-listener,category=config,id=mylistener,config=" + 
            getConfigRef());
    }

    ObjectName getHttpListener(String listenerId) throws Exception
    {
        return new ObjectName(
            getTestDomain() + 
            ":type=http-listener,category=config,id=" + listenerId + 
            ",config=" + getConfigRef());
    }

    ObjectName getIiopListener() throws Exception
    {
        return new ObjectName(
            getTestDomain() + 
            ":type=iiop-listener,category=config,id=mylistener,config=" + 
            getConfigRef());
    }

    ObjectName getVirtualServer() throws Exception
    {
        return new ObjectName(
            getTestDomain() + 
            ":type=virtual-server,category=config,id=my-virtual-server,config="
            + getConfigRef());
    }

    ObjectName getAuthRealm(String name) throws Exception
    {
        return new ObjectName(
            getTestDomain() + 
            ":type=auth-realm,category=config,config=" + getConfigRef() + 
            ",name=" + name);
    }

    ObjectName getProfiler() throws Exception
    {
        return new ObjectName(
            getTestDomain() + 
            ":type=profiler,category=config,config=" + getConfigRef());
    }

    ObjectName getAuditModule(String name) throws Exception
    {
        return new ObjectName(
            getTestDomain() + 
            ":type=audit-module,category=config,config=" + getConfigRef() + 
            ",name=" + name);
    }

    ObjectName getJmsHost(String name) throws Exception
    {
        return new ObjectName(
            getTestDomain() + 
            ":type=jms-host,category=config,config=" + getConfigRef() + 
            ",name=" + name);
    }

    ObjectName getJaccProvider(String name) throws Exception
    {
        return new ObjectName(
            getTestDomain() + 
            ":type=jacc-provider,category=config,config=" + getConfigRef() + 
            ",name=" + name);
    }

    ObjectName getEjbContainer() throws Exception
    {
        return new ObjectName(
            getTestDomain() + 
            ":type=ejb-container,category=config,config=" + getConfigRef());
    }

    ObjectName getSsl(String type, String id) throws Exception
    {
        String on = getTestDomain() + ":type=ssl,category=config,config=" + 
                    getConfigRef();
        on += "iiop-service".equals(type) ? "" : ("," + type + '=' + id);
        return new ObjectName(on);
    }

    ObjectName getThreadPool(String threadPoolId) throws Exception
    {
        return new ObjectName(
            getTestDomain() + 
            ":type=thread-pool,category=config,config=" + getConfigRef() + 
            ",thread-pool-id=" + threadPoolId);
    }

    ObjectName getObjectName1(String type, String loc) throws Exception
    {
        return getObjectName(type, new String[] {loc});
    }

    ObjectName getObjectName2(String type, String loc1, String loc2)
        throws Exception
    {
        return getObjectName(type, new String[] {loc1, loc2});
    }

    ObjectName getObjectName(String type, String[] loc) throws Exception
    {
        if (null == loc) { loc = new String[0]; }
        String[] oTokens = new String[loc.length + 2];
        oTokens[0] = getTestDomain();
        oTokens[1] = getConfigRef();
        for (int i = 2; i < oTokens.length; i++)
        {
            oTokens[i] = loc[i-2];
        }
        return mbeanRegistry.getMbeanObjectName(type, oTokens);
    }

    boolean exists(Object on, Object[] names)
    {
        for (int i = 0; i < names.length; i++)
        {
            if (names[i].equals(on))
            {
                return true;
            }
        }
        return false;
    }

    private String getTargetName()
    {
        return null;
    }

    private Target getTarget() throws Exception
    {
        return TargetBuilder.INSTANCE.createTarget(getTargetName(), 
                    configContext);
    }

    private Properties getProperties()
    {
        Properties props = new Properties();
        props.put("prop1", "val1");
        return props;
    }

    private String getTestDomain()
    {
        return "testdomain";
    }

    private class MyConfigsMBean extends ConfigsMBean
    {
        private MBeanServer mbeanServer;

        public ConfigContext getConfigContext()
        {
            return configContext;
        }

        public MBeanServer getMBeanServer() throws MBeanException
        {
            if (mbeanServer == null)
            {
                mbeanServer = javax.management.MBeanServerFactory.
                                    createMBeanServer();
            }
            return mbeanServer;
        }

        protected void postInvoke(String opName, Object ret) 
            throws MBeanException
        {
            try
            {
                if (ret instanceof ObjectName)
                {
                    ObjectName on = (ObjectName)ret;
                    if (!getMBeanServer().isRegistered(on))
                    {
                        BaseAdminMBean mbean = 
                            mbeanRegistry.instantiateConfigMBean(
                                        on, null, getConfigContext());
                        getMBeanServer().registerMBean(mbean, on);
                    }
                }
                flush();
            }
            catch (Exception e)
            {
                throw MBeanExceptionFormatter.toMBeanException(e, null);
            }
        }

        private void flush() throws MBeanException
        {
            try
            {
                final ConfigContext cctx = getConfigContext();
                if (cctx.isChanged())
                {
                    cctx.flush();
                }
            }
            catch (Exception e)
            {
                throw MBeanExceptionFormatter.toMBeanException(e, null);
            }
        }

        public String getDomainName()
        {
            return getTestDomain();
        }
    }
}