package com.sun.enterprise.configapi.tests;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.*;
import org.jvnet.hk2.config.types.Property;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.glassfish.tests.utils.Utils;
import com.sun.enterprise.config.serverbeans.*;


import java.beans.PropertyVetoException;
import java.beans.PropertyChangeEvent;

/**
 * This test listen to a property change event only injecting the parent containing the property.
 *
 * @author Jerome Dochez
 */
public class PropertyChangeListenerTest  extends ConfigApiTest implements ConfigListener {

    Habitat habitat;
    boolean result = false;

    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setup() {
        habitat = Utils.getNewHabitat(this);
    }

    @Test
    public void propertyChangeEventReceptionTest() throws TransactionFailure {

        HttpService  httpService = habitat.getComponent(HttpService.class);
        assertNotNull(httpService);

       // let's find a acceptable target.
        VirtualServer target =null;
        for (VirtualServer vs : httpService.getVirtualServer()) {
            if (!vs.getProperty().isEmpty()) {
                target = vs;
                break;
            }
        }

        assertNotNull(target);

        ((ObservableBean) ConfigSupport.getImpl(target)).addListener(this);
        final Property prop  = target.getProperty().get(0);

        ConfigSupport.apply(new SingleConfigCode<Property>() {

            public Object run(Property param) throws PropertyVetoException, TransactionFailure {
                // first one is fine...
                param.setValue(prop.getValue().toUpperCase());
                return null;
            }
        }, prop);

        getHabitat().getComponent(Transactions.class).waitForDrain();
        assertTrue(result);
        ((ObservableBean) ConfigSupport.getImpl(target)).removeListener(this);
    }

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        result = true;
        return null;
    }
}
