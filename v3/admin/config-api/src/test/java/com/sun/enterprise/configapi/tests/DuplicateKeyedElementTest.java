package com.sun.enterprise.configapi.tests;

import java.beans.PropertyVetoException;

import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import org.jvnet.hk2.config.types.Property;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Test for invalid duplicate keyed entries
 *
 * @author Jerome Dochez
 */
public class DuplicateKeyedElementTest extends ConfigApiTest {
    boolean result = false;

    public String getFileName() {
        return "DomainTest";
    }

    @Test(expected = TransactionFailure.class)
    public void duplicateKeyTest() throws TransactionFailure {
        HttpService httpService = getHabitat().getComponent(HttpService.class);
        assertNotNull(httpService);
        // let's find a acceptable target.
        VirtualServer target = null;
        for (VirtualServer vs : httpService.getVirtualServer()) {
            if (!vs.getProperty().isEmpty()) {
                target = vs;
                break;
            }
        }
        assertNotNull(target);
        final Property prop = target.getProperty().get(0);
        Property newProp = (Property) ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {
            public Object run(VirtualServer param) throws PropertyVetoException, TransactionFailure {
                // first one is fine...
                Property dupProp = param.createChild(Property.class);
                dupProp.setName(prop.getName());
                dupProp.setValue(prop.getValue().toUpperCase());
                // this should fail...
                param.getProperty().add(dupProp);
                return dupProp;
            }
        }, target);
        // if we arrive here, this is an error, we succeeded adding a property with
        // the same key name.
        assertTrue(false);
    }

    @Test(expected = TransactionFailure.class)
    public void identicalKeyTest() throws TransactionFailure {
        HttpService httpService = getHabitat().getComponent(HttpService.class);
        assertNotNull(httpService);
        // let's find a acceptable target.
        VirtualServer target = null;
        for (VirtualServer vs : httpService.getVirtualServer()) {
            if (!vs.getProperty().isEmpty()) {
                target = vs;
                break;
            }
        }
        assertNotNull(target);
        Property newProp = (Property) ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {
            public Object run(VirtualServer param) throws PropertyVetoException, TransactionFailure {
                // first one is fine...
                Property firstProp = param.createChild(Property.class);
                firstProp.setName("foo");
                firstProp.setValue("bar");
                param.getProperty().add(firstProp);
                // this should fail...
                Property secondProp = param.createChild(Property.class);
                secondProp.setName("foo");
                secondProp.setValue("bar");
                param.getProperty().add(secondProp);
                return secondProp;
            }
        }, target);
        // if we arrive here, this is an error, we succeeded adding a property with
        // the same key name.
        assertTrue(false);
    }
}