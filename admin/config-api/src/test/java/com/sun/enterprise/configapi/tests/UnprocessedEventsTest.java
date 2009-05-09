package com.sun.enterprise.configapi.tests;

import java.beans.PropertyChangeEvent;
import java.util.List;

import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import org.glassfish.tests.utils.Utils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.TransactionListener;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvent;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 * @author Jerome Dochez
 */
public class UnprocessedEventsTest  extends ConfigApiTest
        implements ConfigListener, TransactionListener {

    Habitat habitat = Utils.getNewHabitat(this);
    UnprocessedChangeEvents unprocessed = null;

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
     public void unprocessedEventsTest() throws TransactionFailure {

        // let's find our target
        NetworkConfig service = habitat.getComponent(NetworkConfig.class);
        NetworkListener listener = service.getNetworkListener("http-listener-1");
        assertNotNull(listener);

        // Let's register a listener
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(listener);
        bean.addListener(this);
        Transactions transactions = getHabitat().getComponent(Transactions.class);

        try {
            transactions.addTransactionsListener(this);

            ConfigSupport.apply(new SingleConfigCode<NetworkListener>() {
                public Object run(NetworkListener param) {
                    param.setPort("8908");
                    return null;
                }
            }, listener);

            // check the result.
            String port = listener.getPort();
            assertEquals(port, "8908");

            // ensure events are delivered.
            transactions.waitForDrain();
            assertNotNull(unprocessed);

            // finally
            bean.removeListener(this);
        } finally {

            // check we recevied the event
            transactions.removeTransactionsListener(this);
        }

    }

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] propertyChangeEvents) {
        assertEquals("Array size", propertyChangeEvents.length, 1 );
        
        final UnprocessedChangeEvent unp = new UnprocessedChangeEvent(
            propertyChangeEvents[0], "Java NIO port listener cannot reconfigure its port dynamically" );
        unprocessed = new UnprocessedChangeEvents( unp );
        return unprocessed;
    }

    public void transactionCommited(List<PropertyChangeEvent> changes) {
        // don't care...
    }

    public void unprocessedTransactedEvents(List<UnprocessedChangeEvents> changes) {
        assertTrue(changes.size()==1);
    }
}