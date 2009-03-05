package com.sun.enterprise.configapi.tests;

import org.glassfish.tests.utils.*;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.config.*;
import org.jvnet.hk2.component.Habitat;
import org.junit.Test;
import static org.junit.Assert.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Properties;
import java.util.List;

import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpService;

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
        HttpListener listener = null;
        HttpService service = habitat.getComponent(HttpService.class);
        for (HttpListener l : service.getHttpListener()) {
            if ("http-listener-1".equals(l.getId())) {
                listener = l;
                break;
            }
        }
        assertNotNull(listener);

        // Let's register a listener
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(listener);
        bean.addListener(this);
        Transactions transactions = getHabitat().getComponent(Transactions.class);

        try {
            transactions.addTransactionsListener(this);

            ConfigSupport.apply(new SingleConfigCode<HttpListener>() {
                public Object run(HttpListener param) throws PropertyVetoException, TransactionFailure {
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