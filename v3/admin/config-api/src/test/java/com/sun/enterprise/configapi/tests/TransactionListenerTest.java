package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.KeepAlive;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
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
    @Ignore
    public void transactionEvents() throws TransactionFailure {
        httpService = getHabitat().getComponent(HttpService.class);
        Transactions.get().listenToAllTransactions(new TransactionListener() {
            public void transactionCommited(List<PropertyChangeEvent> changes) {
                events = changes;
            }
        });
        assertTrue(httpService!=null);

        logger.fine("Max connections = " + httpService.getKeepAlive().getMaxConnections());
        TransactionHelper.apply(new SingleConfigCode<KeepAlive>() {

            public boolean run(KeepAlive param) throws PropertyVetoException, TransactionFailure {
                param.setMaxConnections("500");
                return true;
            }
        }, httpService.getKeepAlive());
        assertTrue(httpService.getKeepAlive().getMaxConnections().equals("500"));

        for (int i=0;i<10;i++) {
            if (Transactions.get().pendingTransactionEvents()) {
                try {
                    Thread.currentThread().wait(10);
                } catch (InterruptedException e) {
                }
            } else {
                break;
            }
        }
        assertTrue(events!=null);
        assertTrue(events.size()==1);
        PropertyChangeEvent event = events.iterator().next();
        assertTrue("max-connections".equals(event.getPropertyName()));
        assertTrue("500".equals(event.getNewValue().toString()));
        assertTrue("250".equals(event.getOldValue().toString()));
    }
}
