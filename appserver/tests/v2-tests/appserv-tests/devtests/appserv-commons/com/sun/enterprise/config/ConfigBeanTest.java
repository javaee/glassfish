package com.sun.enterprise.config;

import junit.framework.*;
import org.xml.sax.InputSource;
import java.io.FileReader;
import java.io.File;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.NodeAgents;
import com.sun.enterprise.config.serverbeans.Domain;
import org.netbeans.modules.schema2beans.BeanProp;
import org.netbeans.modules.schema2beans.BaseProperty;
import org.netbeans.modules.schema2beans.Common;

/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.2 $
 */

public class ConfigBeanTest extends TestCase {
    public void testBaseBean() throws Exception {
        JmxConnector jc = new JmxConnector();
        assertEquals("", ""+jc);
    }

        // IN this test we're trying to find out how to determine if
        // we should return an xpath with a predicate. It would appear
        // that if an element has a bean property, and taht property
        // says its indexed, then the xpath should have a predicate.
    public void testBeanProperty() throws Exception {
        ConfigContext configCtx =ConfigFactory.createConfigContext("domain.xml", new MyHandler());
        configCtx.refresh();
        NodeAgents nas = (NodeAgents) configCtx.exactLookup("/domain/node-agents");
        assertNotNull("Null Node Agents by exactLookup(\"/domain/node-agents\")", nas);
        NodeAgent na = nas.getNodeAgentByName("na1");
        assertNotNull("Null Node Agent!", na);
        assertEquals("NodeAgent", na.name());
        assertEquals("node-agent", na.dtdName());
        BaseProperty bp = na.getProperty("jmx-connector");
        assertNotNull("bean property fetch returned null", bp);
        assertEquals("unexpected bean type", Common.TYPE_0_1, bp.getInstanceType());
        BeanProp na_bp = na.beanProp();
        assertNotNull("bean property is null", na_bp);
        assertTrue("Not indexed", na_bp.isIndexed());
        JmxConnector jc = new JmxConnector();
        jc.setName("jc1");
        na.setJmxConnector(jc);
        assertNotNull(jc.beanProp());
        assertTrue(!jc.beanProp().isIndexed());
        AdminService as = (AdminService) configCtx.exactLookup("/domain/configs/config[@name='server-config']/admin-service");
        jc = new JmxConnector();
        jc.setName("jc1");
        as.addJmxConnector(jc);
        assertNotNull(jc.beanProp());
        assertTrue(jc.beanProp().isIndexed());
        
    }
    
        
        // This test demonstrates that an element that has different
        // cardinality will have different xpath expressions
    public void testXPath() throws Exception {
        ConfigContext configCtx =ConfigFactory.createConfigContext("domain.xml", new MyHandler());
        configCtx.refresh();
        NodeAgents nas = (NodeAgents) configCtx.exactLookup("/domain/node-agents");
        assertNotNull("Null Node Agents by exactLookup(\"/domain/node-agents\")", nas);
        NodeAgent na = nas.getNodeAgentByName("na1");
        assertNotNull("Null Node Agent!", na);
        JmxConnector jc = new JmxConnector();
        jc.setName("jc1");
        na.setJmxConnector(jc);
        assertEquals("/domain/node-agents/node-agent[@name='na1']/jmx-connector", na.getJmxConnector().getXPath());
        assertEquals(jc, configCtx.exactLookup(na.getJmxConnector().getXPath()));
        AdminService as = (AdminService) configCtx.exactLookup("/domain/configs/config[@name='server-config']/admin-service");
        jc = new JmxConnector();
        jc.setName("jc1");
        as.addJmxConnector(jc);
        assertEquals("/domain/configs/config[@name='server-config']/admin-service/jmx-connector[@name='jc1']", as.getJmxConnectorByName("jc1").getXPath());
        

    }

    public ConfigBeanTest(String name){
        super(name);
    }

    protected void setUp() throws Exception {
        Runtime.getRuntime().exec("rm -f domain.xml").waitFor();
        Runtime.getRuntime().exec("cp domain.orig.xml domain.xml").waitFor();
    }

    protected void tearDown() throws Exception {
        Runtime.getRuntime().exec("rm -f domain.xml").waitFor();
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            junit.textui.TestRunner.run(ConfigBeanTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new ConfigBeanTest(args[i]));
        }
        return ts;
    }

        
}
