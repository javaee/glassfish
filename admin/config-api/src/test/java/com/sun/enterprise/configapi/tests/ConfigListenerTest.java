package com.sun.enterprise.configapi.tests;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.Before;
import org.jvnet.hk2.config.*;
import org.jvnet.hk2.component.Habitat;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.hk2.component.ConstructorWomb;

import java.beans.PropertyVetoException;

import static org.junit.Assert.*;

/**
 * Simple ConfigListener tests
 */
public class ConfigListenerTest {

    Habitat habitat;

    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setup() {
        habitat = Utils.getNewHabitat(getFileName());    
    }


    @Test
    public void changedTest() throws TransactionFailure {


        ConstructorWomb<HttpListenerContainer> womb = new ConstructorWomb<HttpListenerContainer>(HttpListenerContainer.class, habitat, null);
        HttpListenerContainer container = womb.get(null);

        ConfigSupport.apply(new SingleConfigCode<HttpListener>() {

            public Object run(HttpListener param) throws PropertyVetoException, TransactionFailure {
                param.setPort("8989");
                return null;
            }
        }, container.httpListener);

        Transactions.get().waitForDrain();
        assertTrue(container.received);
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(container.httpListener);
        bean.removeListener(container);

        // put back the right values in the domain to avoid test collisions
        ConfigSupport.apply(new SingleConfigCode<HttpListener>() {

            public Object run(HttpListener param) throws PropertyVetoException, TransactionFailure {
                param.setPort("8080");
                return null;
            }
        }, container.httpListener);
    }

    @Test
    public void removeListenerTest() throws TransactionFailure {

        ConstructorWomb<HttpListenerContainer> womb = new ConstructorWomb<HttpListenerContainer>(HttpListenerContainer.class, habitat, null);
        HttpListenerContainer container = womb.get(null);

        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(container.httpListener);
        bean.removeListener(container);

        ConfigSupport.apply(new SingleConfigCode<HttpListener>() {

            public Object run(HttpListener param) throws PropertyVetoException, TransactionFailure {
                param.setPort("8989");
                return null;
            }
        }, container.httpListener);

        Transactions.get().waitForDrain();
        assertFalse(container.received);

        // put back the right values in the domain to avoid test collisions        
        ConfigSupport.apply(new SingleConfigCode<HttpListener>() {

            public Object run(HttpListener param) throws PropertyVetoException, TransactionFailure {
                param.setPort("8080");
                return null;
            }
        }, container.httpListener);
    }
}
