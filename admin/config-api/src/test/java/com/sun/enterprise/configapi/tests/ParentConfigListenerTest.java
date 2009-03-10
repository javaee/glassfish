package com.sun.enterprise.configapi.tests;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.glassfish.tests.utils.Utils;
import com.sun.hk2.component.ConstructorWomb;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpService;

import java.beans.PropertyVetoException;

/**
 * This test will ensure that when a class is injected with a parent bean and a child
 * is added to the parent, anyone injected will that parent will be notified
 * correctly.
 * 
 * User: Jerome Dochez
 */
public class ParentConfigListenerTest extends ConfigApiTest {

    Habitat habitat;

    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setup() {
        habitat = Utils.getNewHabitat(this);
    }


    @Test
    public void addHttpListenerTest() throws TransactionFailure {


        ConstructorWomb<HttpServiceContainer> womb = new ConstructorWomb<HttpServiceContainer>(HttpServiceContainer.class, habitat, null);
        HttpServiceContainer container = womb.get(null);

        ConfigSupport.apply(new SingleConfigCode<HttpService>() {

            public Object run(HttpService param) throws PropertyVetoException, TransactionFailure {
                HttpListener newListener = param.createChild(HttpListener.class);
                newListener.setId("Funky-Listener");
                newListener.setPort("8078");
                param.getHttpListener().add(newListener);
                return null;
            }
        }, container.httpService);

        getHabitat().getComponent(Transactions.class).waitForDrain();
        assertTrue(container.received);
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(container.httpService);
        bean.removeListener(container);
    }
}
