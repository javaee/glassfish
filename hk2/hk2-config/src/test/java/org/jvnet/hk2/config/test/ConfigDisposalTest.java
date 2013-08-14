package org.jvnet.hk2.config.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.beans.PropertyVetoException;
import java.net.URL;
import java.util.List;
import java.util.Random;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

public class ConfigDisposalTest {
    private final static String TEST_NAME = "ConfigDisposal";
    private ServiceLocator habitat;

    @Before
    public void before() {
        habitat = ServiceLocatorFactory.getInstance().create(TEST_NAME + new Random().nextInt());
        DynamicConfigurationService dcs = habitat.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        new ConfigModule(habitat).configure(config);
        
        config.commit();
        parseDomainXml();
    }

    @After
    public void after() {
        habitat.shutdown();
    }

    public void parseDomainXml() {
        ConfigParser parser = new ConfigParser(habitat);
        URL url = ConfigDisposalTest.class.getResource("/domain.xml");
        System.out.println("URL : " + url);

        try {
            DomDocument doc = parser.parse(url);
            System.out.println("[parseDomainXml] ==> Successfully parsed");
            assert(doc != null);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert(false);
        }
    }

    // to regenerate config injectors do the following in command line:
    // mvn config-generator:generate-test-injectors
    // cp target/generated-sources/hk2-config-generator/src/test/java/org/jvnet/hk2/config/test/* src/test/java/org/jvnet/hk2/config/test/
    @Test // Removed container causes nested elements be removed
    public void testDisposedNestedAndNamed() throws TransactionFailure {
        SimpleConnector sc = habitat.getService(SimpleConnector.class);
        assertEquals("Extensions", 1, sc.getExtensions().size());
        assertEquals("Nested children", 2, sc.getExtensions().get(0).getExtensions().size());
        
        ConfigSupport.apply(new SingleConfigCode<SimpleConnector>() {
            @Override
            public Object run(SimpleConnector sc)
                    throws PropertyVetoException, TransactionFailure {
                List<GenericContainer> extensions = sc.getExtensions();
                GenericContainer child = extensions.get(extensions.size() - 1);
                extensions.remove(child);
                return child;
            }
        }, sc);

        assertEquals("Removed extensions", 0, sc.getExtensions().size());
        // NOTE, habitat.getService(GenericConfig.class) creates new instance
        //       if not all instances of GenericConfig are removed 
        assertNull("GenericContainer descriptor", habitat.getService(GenericContainer.class));
        assertNull("GenericConfig descriptor", habitat.getService(GenericConfig.class));
        // assert with visualVm there is no GenericContainer and GenericConfig instances with OQL query:
        // select x.implementation.toString() from org.jvnet.hk2.config.test.SimpleConfigBeanWrapper x
    }

    @Test 
    public void testRemoveNamed() throws TransactionFailure {
        SimpleConnector sc = habitat.getService(SimpleConnector.class);
        assertEquals("Eextensions", 1, sc.getExtensions().size());
        assertEquals("Nested children", 2, sc.getExtensions().get(0).getExtensions().size());
        
        List<GenericContainer> extensions = sc.getExtensions();
        GenericContainer child = extensions.get(extensions.size() - 1);

        ConfigSupport.apply(new SingleConfigCode<GenericContainer>() {
            @Override
            public Object run(GenericContainer child)
                    throws PropertyVetoException, TransactionFailure {
                List<GenericConfig> childExtensions = child.getExtensions();
                GenericConfig nestedChild = childExtensions.get(childExtensions.size() - 1);
                childExtensions.remove(nestedChild);
                return nestedChild;
            }
        }, child);

        assertEquals("Removed extensions", 1, sc.getExtensions().size());
        assertNull("Removed nested named child", habitat.getService(GenericConfig.class, "test2"));
        // make sure other elements are not removed
        assertNotNull("Nested named child", habitat.getService(GenericConfig.class, "test1"));
        assertNotNull("Nested named grand child", habitat.getService(GenericConfig.class, "test"));
    }

    //@Test 
    public void testRemovedOne() throws TransactionFailure {
        SimpleConnector sc = habitat.getService(SimpleConnector.class);
        assertEquals("Extensions", 1, sc.getExtensions().size());

        ConfigSupport.apply(new SingleConfigCode<SimpleConnector>() {
            @Override
            public Object run(SimpleConnector sc)
                    throws PropertyVetoException, TransactionFailure {
                List<GenericContainer> extensions = sc.getExtensions();
                GenericContainer child = sc.createChild(GenericContainer.class);
                extensions.add(child);
                return child;
            }
        }, sc);

        assertEquals("Added extensions", 2, sc.getExtensions().size());

        ConfigSupport.apply(new SingleConfigCode<SimpleConnector>() {
            @Override
            public Object run(SimpleConnector sc)
                    throws PropertyVetoException, TransactionFailure {
                List<GenericContainer> extensions = sc.getExtensions();
                GenericContainer child = extensions.get(extensions.size() - 1);
                extensions.remove(child);
                return child;
            }
        }, sc);

        assertEquals("Removed extensions", 1, sc.getExtensions().size());

        assertNotNull("Nested named child 1", habitat.getService(GenericConfig.class, "test1"));
        assertNotNull("Nested named grand child", habitat.getService(GenericConfig.class, "test"));
        assertNotNull("Nested named child 2", habitat.getService(GenericConfig.class, "test2"));
        assertNotNull("GenericContainer Service", habitat.getService(GenericContainer.class));
    }
}
