package com.sun.enterprise.v3.admin;

import com.sun.enterprise.v3.common.HTMLActionReporter;
import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.NetworkListeners;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.*;
import org.glassfish.tests.utils.ConfigApiTest;
import org.glassfish.tests.utils.Utils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import java.beans.PropertyChangeEvent;
import java.util.Properties;

/**
 * test the set command
 * @author Jerome Dochez
 */
// Ignored temporarily because it fails to inject CommandRunnerImpl as ModulesRegistry is not available
@Ignore 
public class ConfigAttributeSetTest  extends ConfigApiTest implements ConfigListener {

    Habitat habitat = Utils.getNewHabitat(this);
    PropertyChangeEvent event = null;

    public DomDocument getDocument(Habitat habitat) {
        return new TestDocument(habitat);
    }

    /**
     * Returns the DomainTest file name without the .xml extension to load the test configuration
     * from.
     *
     * @return the configuration file name
     */
    public String getFileName() {
        return "DomainTest";
    }     

    @Test
     public void simpleAttributeSetTest() {

        CommandRunnerImpl runner = habitat.getComponent(CommandRunnerImpl.class);
        assertNotNull(runner);

        // let's find our target
        NetworkListener listener = null;
        NetworkListeners service = habitat.getComponent(NetworkListeners.class);
        for (NetworkListener l : service.getNetworkListener()) {
            if ("http-listener-1".equals(l.getName())) {
                listener = l;
                break;
            }
        }
        assertNotNull(listener);        

        // Let's register a listener
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(listener);
        bean.addListener(this);

        // parameters to the command
        Properties parameters = new Properties();
        parameters.put("value", "8090");
        parameters.put("DEFAULT", "configs.config.server-config.http-service.http-listener.http-listener-1.port");

        // execute the set command.
        runner.getCommandInvocation("set", new HTMLActionReporter()).parameters(parameters).execute();
                                                                                                                                                                                                                           
        // check the result.
        String port = listener.getPort();
        assertEquals(port, "8090");

        // ensure events are delivered.
        habitat.getComponent(Transactions.class).waitForDrain();
        
        // finally
        bean.removeListener(this);

        // check we recevied the event
        assertNotNull(event);
        assertEquals("8080", event.getOldValue());
        assertEquals("8090", event.getNewValue());
        assertEquals("port", event.getPropertyName());
        
    }

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] propertyChangeEvents) {
        assertEquals("Array size", propertyChangeEvents.length, 1 );
        event = propertyChangeEvents[0];
        return null;
    }
}
