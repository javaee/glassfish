package com.sun.enterprise.configapi.tests;

import org.junit.Test;
import org.junit.Ignore;
import org.jvnet.hk2.config.*;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.hk2.component.ConstructorWomb;

import java.beans.PropertyVetoException;

import static org.junit.Assert.*;

/**
 * Simple ConfigListener tests
 */
public class ConfigListenerTest extends ConfigApiTest {

    public String getFileName() {
        return "DomainTest";
    }

    @Test
    @Ignore
    public void changedTest() throws TransactionFailure {

        ConstructorWomb<HttpListenerContainer> womb = new ConstructorWomb<HttpListenerContainer>(HttpListenerContainer.class, super.getHabitat(), null);
        HttpListenerContainer container = womb.get(null);

        ConfigSupport.apply(new SingleConfigCode<HttpListener>() {

            public Object run(HttpListener param) throws PropertyVetoException, TransactionFailure {
                param.setPort("8989");
                return null;
            }
        }, container.httpListener);

        if (!container.received) {
            for (int i=0;i<200;i++) {
                if (Transactions.get().pendingTransactionEvents()) {
                // do nothing, ensure the events are processed
                    try {
                        Thread.currentThread().wait(10);
                    } catch (InterruptedException e) {

                    }
                }
            }
        }
        assertTrue(container.received);
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(container.httpListener);
        bean.removeListener(container);
    }

    @Test
    @Ignore
    public void removeListenerTest() throws TransactionFailure {

        ConstructorWomb<HttpListenerContainer> womb = new ConstructorWomb<HttpListenerContainer>(HttpListenerContainer.class, super.getHabitat(), null);
        HttpListenerContainer container = womb.get(null);

        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(container.httpListener);
        bean.removeListener(container);

        ConfigSupport.apply(new SingleConfigCode<HttpListener>() {

            public Object run(HttpListener param) throws PropertyVetoException, TransactionFailure {
                param.setPort("8989");
                return null;
            }
        }, container.httpListener);

        while (Transactions.get().pendingTransactionEvents()) {
            // do nothing, ensure the events are processed
        }
        assertFalse(container.received);
    }
}
