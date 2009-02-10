/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.amx.internal;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.*;

import com.sun.appserv.management.client.AMXBooter;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.JDBCConnectionPoolConfig;
import com.sun.appserv.management.config.NamedConfigElement;
import com.sun.appserv.management.monitor.Monitoring;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.ListUtil;
import com.sun.appserv.management.util.misc.TimingDelta;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.lang.reflect.Method;


/**
 * Why are  tests here?
 * <ul>
 * <li>They provide sample code for the users of the API</li>
 * <li>The unit tests need a server-side target for certain tests</li>
 * <li>The test code can be run either in process with the server, or
 *  on the client side.  Both scenarios are used by clients, and testing
 * both ways is needed.</li>
 * <li>Efficiency: the ability to run the tests in the server itself means
 * that more involved tests can run much faster, making it feasible to do
 * extensive testing.</li>
 * <li>Code factoring: this code can be run in Quicklook, standalone or
 * in the server to validate things in debug mode.</li>
 * </ul>
 * <em>Code here is subject to change at any time. It is not to be used
 * by any clients as an API</em>
 * <p>
 * This class can be registered as an MBean, or used standalone.
 * @author lloyd
 */
public final class SanityChecks implements SanityChecksMBean {

    private final MBeanServerConnection mMBeanServer;
    private final DomainRoot mDomainRoot;
    private final QueryMgr mQueryMgr;

    public static final String SUCCESS = "OK";
    
    private static void debug(final String s) {
        System.out.println(s);
    }
    private SanityChecks(final MBeanServerConnection server)
            throws IOException {
        try {
            assert false;
            throw new IllegalStateException("Assertions must be enabled");
        } catch (AssertionError e) {
            // good, we want assertions on
        }
        mMBeanServer = server;
        //AMXBooter.bootAMX(server);

        mDomainRoot = ProxyFactory.getInstance(server).getDomainRoot();
        mQueryMgr = mDomainRoot.getQueryMgr();
    }

    /**
       In-process or out of process (remote); can be an MBeanServer for in-process or
       an MBeanServerConnection for out-of-process.
     */
    public static SanityChecks newInstance(final MBeanServerConnection server) {
        try {
            server.getDomains(); // verify that it works
            
            return new SanityChecks(server);
        } catch (final IOException e) {
            // should  never happen with an MBeanServer,  but could with an MBeanServerConnection
            throw new RuntimeException(e);
        }
    }

    /**
       In-process (same JVM).
     */
    public static SanityChecks newInstance(final MBeanServer server) {
        return newInstance((MBeanServerConnection) server);
    }

    public DomainRoot getDomainRoot() {
        return mDomainRoot;
    }

    public QueryMgr getQueryMgr() {
        return mQueryMgr;
    }
    
    private final class TestRunner extends Thread
    {
        private final SanityChecks mTarget;
        private final Method mTest;

        volatile Object     mResult = null;
        
        TestRunner( final SanityChecks target, final Method test )
        {
            mTarget = target;
            mTest = test;
        }
        
        public String getTestName() { return mTest.getName(); }
        
        public void run()
        {
            try
            {
                final String result = (String)mTest.invoke(mTarget);
                mResult = result;
            }
            catch( Throwable t )
            {
                mResult = t;
            }
        }
    }
    
    /** map is keyed by test name, value is any output (or assertion/Throwable) */
        public Map<String,Object>
    runAllTests()
    {
        final Method[] candidates = SanityChecksMBean.class.getMethods();
        final Map<String,Object> results = new HashMap<String,Object>();
        
        // figure out which are tests
        final List<Method> tests = new ArrayList<Method>();
        for( final Method method : candidates )
        {
            final String methodName = method.getName();
            if ( methodName.startsWith("test") )
            {
                tests.add(method);
            }
        }
        
        // run the tests, each in its own thread
        final List<TestRunner> threads = new ArrayList<TestRunner>();
        for( final Method test : tests )
        {
            final TestRunner runner = new TestRunner(this, test);
            threads.add(runner);
            runner.start();
        }
        
        for( final TestRunner runner : threads )
        {
            try {
                runner.join();
                results.put( runner.getTestName(), runner.mResult);
            }
            catch( final InterruptedException e )
            {
                results.put( runner.getTestName(), e);
            }
        }
        threads.clear();
        
        return results;
    }

    /** get all AMX MBeans that were found when the test started
    Caller should use the QueryMgr if a fresh set is needed */
    public Set<AMX> getAllAMX() {
        return mQueryMgr.queryAllSet();
    }

    public <T> Set<T> getAllAMX(final Class<T> intf) {
        return getAllAMX(getAllAMX(), intf);
    }

    public <T> Set<T> getAllAMX(final Set<AMX> all, final Class<T> intf) {
        final Set<T> result = new HashSet<T>();
        for (final AMX amx : all) {
            if (intf.isAssignableFrom(amx.getClass())) {
                result.add(intf.cast(amx));
            }
        }
        return result;
    }
    
    private <T extends AMX> String successFor(final Set<T> amx)
    {
        return SUCCESS + ": " + amx.size() + " MBeans";
    }

    public String testBasics() {
        final List<String> problems = new ArrayList<String>();

        final Set<AMX> all = getAllAMX();
        assert all.size() > 20;

        for (final AMX amx : all) {
            _checkTypeAndInterfaces(amx);

            // verify that all the attributes can be accessed
            final Extra extra = Util.getExtra(amx);
            final ObjectName objectName = extra.getObjectName();
            final String[] attrNames = extra.getAttributeNames();
            final Map<String, Object> attrsMap = extra.getAllAttributes();

            final Set<String> missing = GSetUtil.newStringSet(attrNames);
            missing.removeAll(attrsMap.keySet());
            if (missing.size() != 0) {
                final String missingStr = CollectionUtil.toString(missing, ", ");
                final String msg = "WARNING: could not retrieve attributes: {" + missingStr +
                        "} for MBean " + JMXUtil.toString(objectName);
                debug(msg);
                problems.add(msg);
            } else {
                final String msg = attrsMap.keySet().size() + " attrs fetched ok: " +
                        JMXUtil.toString(Util.getExtra(amx).getObjectName());
                //debug(msg);
            }
        }
        assert problems.size() == 0 : CollectionUtil.toString(problems, "\n");
        
        return successFor(all);
    }

    private void _checkTypeAndInterfaces(final AMX amx) {
        final ObjectName objectName = Util.getExtra(amx).getObjectName();

        if (amx instanceof AMXConfig) {
            assert amx.getJ2EEType().startsWith("X-") :
                    "j2eeType prefix 'X-' required for " + objectName;

            assert amx.getJ2EEType().endsWith("Config") :
                    "j2eeType suffix 'Config' required for " + objectName;
        }

        if ( amx instanceof NamedConfigElement ){
            assert !(amx instanceof Singleton) : "NamedConfigElement must not be Singleton: " + amx.getJ2EEType();
            assert !(amx instanceof Utility) : "NamedConfigElement must not be Utility: " + amx.getJ2EEType();
            assert !(amx instanceof Monitoring) : "NamedConfigElement must not be Monitoring: " + amx.getJ2EEType();
        }
    }

    /**
     * Iterate all MBeans and verify that each parent's children have the right
     * parent.
     */
    public String testParentChild() {
        final Set<AMX> all = getAllAMX();
        for (final AMX amx : all) {
            _checkParentChild(amx);
        }
        return successFor(all);
    }

    private void _checkParentChild(final AMX amx) {
        if (!(amx instanceof DomainRoot)) {
            final Container c = amx.getContainer();
            final Set<AMX> s = c.getContaineeSet(amx.getJ2EEType());
            assert s.contains(amx);

            final Map<String, AMX> m = c.getContaineeMap(amx.getJ2EEType());
            assert m.containsKey(amx.getName());
        }
    }

    public String testContainer() {
        final Set<Container> all = getAllAMX(Container.class);
        for (final Container amx : all) {
            _checkContainer(amx);
        }
        return successFor(all);
    }

    private void _checkContainer(final Container c) {
        final Set<String> j2eeTypes = c.getContaineeJ2EETypes();
        final Map<String, Map<String, AMX>> containeeMap = c.getMultiContaineeMap(j2eeTypes);
        assert containeeMap.keySet().size() <= j2eeTypes.size();
        
        // check equivalency of null and complete set of j2eeTypes
        final Map<String, Map<String, AMX>> m1 = c.getMultiContaineeMap(j2eeTypes);
        final Map<String, Map<String, AMX>> m2 = c.getMultiContaineeMap(null);
        assert m1.keySet().equals(m2.keySet());

        // just verify it can be calle
        final Set<AMX> s1 = c.getContaineeSet(j2eeTypes);

        // verified that every containee can be fetched by type and name
        for (final String j2eeType : j2eeTypes) {
            final Set<AMX> byType = c.getContaineeSet(j2eeType);
            for (final AMX amx : byType) {
                final AMX a = c.getContainee(j2eeType, amx.getName());
                assert a == amx;
            }
        }
    }

    public String testDefaultValues() {
        final Set<AMXConfig> all = getAllAMX(AMXConfig.class);
        for (final AMXConfig amx : all) {
            _checkDefaultValues(amx);
        }
        return successFor(all);
    }

    private void _checkDefaultValues(final AMXConfig amxConfig) {
        final String objectName = JMXUtil.toString(Util.getExtra(amxConfig).getObjectName());

        // test the Map keyed by XML attribute name
        final Map<String, String> defaultValuesXML = amxConfig.getDefaultValues(false);
        for (final String attrName : defaultValuesXML.keySet()) {
            // no default value should ever be null
            assert defaultValuesXML.get(attrName) != null :
                    "null value for attribute " + attrName + " in " + objectName;

            final String value = amxConfig.getDefaultValue(attrName);
            assert value != null :
                    "null value for XML attribute fetched singly: " + attrName + " in " + objectName;
        }

        // test the Map keyed by AMX attribute name
        final Map<String, String> defaultValuesAMX = amxConfig.getDefaultValues(true);
        assert defaultValuesXML.size() == defaultValuesAMX.size();
        for (final String attrName : defaultValuesAMX.keySet()) {
            // no default value should ever be null
            assert defaultValuesAMX.get(attrName) != null :
                    "null value for attribute " + attrName + " in " + objectName;

            final String value = amxConfig.getDefaultValue(attrName);
            assert value != null :
                    "null value for AMX attribute fetched singly: " + attrName + " in " + objectName;
        }
    }

    public String testAttributeResolver() {
        final Set<AMXConfig> all = getAllAMX(AMXConfig.class);
        for (final AMXConfig amx : all) {
            _checkAttributeResolver(amx);
        }
        return successFor(all);
    }

    private void _checkAttributeResolver(final AMXConfig amxConfig) {
        /*
        if ( getEffortLevel() != EffortLevel.EXTENSIVE ) { return; }
         */

        final String[] attrNames = Util.getExtra(amxConfig).getAttributeNames();
        for (final String attrName : attrNames) {
            final String resolvedValue = amxConfig.resolveAttribute(attrName);
            if (resolvedValue != null) {
                // crude check
                assert resolvedValue.indexOf("${") < 0 :
                        "Attribute " + attrName + " did not resolve: " + resolvedValue;
            }
        }

        final AttributeList attrsList = amxConfig.resolveAttributes(attrNames);
        for (final Object o : attrsList) {
            final Attribute a = (Attribute) o;
            final String resolvedValue = "" + a.getValue();
            if (resolvedValue != null) {
                // crude check
                assert resolvedValue.indexOf("${") < 0 :
                        "Attribute " + a.getName() + " did not resolve: " + resolvedValue;
            }
        }
    }

    public String testSystemStatus() {
        final SystemStatus ss = getDomainRoot().getSystemStatus();

        // just invoke it for now
        final List<Object[]>  restarts = ss.getRestartRequiredChanges();

        final Set<JDBCConnectionPoolConfig> pools = getQueryMgr().queryJ2EETypeSet(JDBCConnectionPoolConfig.J2EE_TYPE);

        for (final JDBCConnectionPoolConfig pool : pools) {
            final Map<String, Object> result = ss.pingJDBCConnectionPool(pool.getName());
        }
        return successFor(pools);
    }
}
