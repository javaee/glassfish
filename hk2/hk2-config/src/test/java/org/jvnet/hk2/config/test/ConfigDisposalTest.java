package org.jvnet.hk2.config.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.beans.PropertyVetoException;
import java.net.URL;
import java.util.List;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

public class ConfigDisposalTest {
    private final static String TEST_NAME = "ConfigDisposal";
    private final static ServiceLocator habitat = ServiceLocatorFactory.getInstance().create(TEST_NAME);

    @BeforeClass
    public static void before() {
        DynamicConfigurationService dcs = habitat.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        new ConfigModule(habitat).configure(config);
        
        config.commit();
        
        parseDomainXml();
    }

    public static void parseDomainXml() {
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
    public void testDisposedNested() throws TransactionFailure {
        SimpleConnector sc = habitat.getService(SimpleConnector.class);
        assertEquals("Added extensions", 1, sc.getExtensions().size());
        assertEquals("Nested extensions", 1, sc.getExtensions().get(0).getExtensions().size());

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

        // FIXME: uncomment failing check
        //assertNull("Nested child", habitat.getService(GenericConfig.class));
    }
}
