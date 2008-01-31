package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.KeepAlive;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.jvnet.hk2.config.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 23, 2008
 * Time: 10:48:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionListenerTest extends ConfigApiTest {


    public String getFileName() {
        return "DomainTest";
    }

    HttpService httpService = null;
    List<PropertyChangeEvent> events = null;

    @Test
    public void transactionEvents() throws TransactionFailure {
        httpService = getHabitat().getComponent(HttpService.class);
        final TransactionListener listener = new TransactionListener() {
                public void transactionCommited(List<PropertyChangeEvent> changes) {
                    events = changes;
                }
            };

        try {
            Transactions.get().addTransactionsListener(listener);
            assertTrue(httpService!=null);

            logger.fine("Max connections = " + httpService.getKeepAlive().getMaxConnections());
            ConfigSupport.apply(new SingleConfigCode<KeepAlive>() {

                public Object run(KeepAlive param) throws PropertyVetoException, TransactionFailure {
                    param.setMaxConnections("500");
                    return null;
                }
            }, httpService.getKeepAlive());
            assertTrue(httpService.getKeepAlive().getMaxConnections().equals("500"));

            while (Transactions.get().pendingTransactionEvents()) {
                // wait until all events are delivered
            }
            assertTrue(events!=null);
            logger.fine("Number of events " + events.size());
            assertTrue(events.size()==1);
            PropertyChangeEvent event = events.iterator().next();
            assertTrue("max-connections".equals(event.getPropertyName()));
            assertTrue("500".equals(event.getNewValue().toString()));
            assertTrue("250".equals(event.getOldValue().toString()));
        } finally {
            Transactions.get().removeTransactionsListener(listener);
        }

    }
}
