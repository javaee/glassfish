package com.sun.enterprise.configapi.tests;

import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.NetworkListeners;
import com.sun.hk2.component.ConstructorWomb;
import org.glassfish.tests.utils.Utils;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.Transactions;

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


        ConstructorWomb<NetworkListenersContainer> womb = new ConstructorWomb<NetworkListenersContainer>(
            NetworkListenersContainer.class, habitat, null);
        NetworkListenersContainer container = womb.get(null);

        ConfigSupport.apply(new SingleConfigCode<NetworkListeners>() {

            public Object run(NetworkListeners param) throws TransactionFailure {
                NetworkListener newListener = param.createChild(NetworkListener.class);
                newListener.setName("Funky-Listener");
                newListener.setPort("8078");
                param.getNetworkListener().add(newListener);
                return null;
            }
        }, container.httpService);

        getHabitat().getComponent(Transactions.class).waitForDrain();
        assertTrue(container.received);
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(container.httpService);
        bean.removeListener(container);
    }
}
