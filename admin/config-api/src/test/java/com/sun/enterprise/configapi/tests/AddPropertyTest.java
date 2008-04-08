package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.KeepAlive;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Property;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.jvnet.hk2.config.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * User: Jerome Dochez
 * Date: Mar 25, 2008
 * Time: 1:32:35 PM
 */
public class AddPropertyTest extends ConfigApiTest {

    public String getFileName() {
        return "DomainTest";
    }

    List<PropertyChangeEvent> events = null;

    @Test
    public void transactionEvents() throws TransactionFailure {
        final Domain domain = getHabitat().getComponent(Domain.class);
        final TransactionListener listener = new TransactionListener() {
                public void transactionCommited(List<PropertyChangeEvent> changes) {
                    events = changes;
                }
            };

        try {
            Transactions.get().addTransactionsListener(listener);
            assertTrue(domain!=null);

            ConfigSupport.apply(new SingleConfigCode<Domain>() {

                public Object run(Domain domain) throws PropertyVetoException, TransactionFailure {
                    Property prop = ConfigSupport.createChildOf(domain, Property.class);
                    domain.getProperty().add(prop);
                    prop.setName("Jerome");
                    prop.setValue("was here");
                    return prop;
                }
            }, domain);
            Transactions.get().waitForDrain();

            assertTrue(events!=null);
            logger.fine("Number of events " + events.size());
            assertTrue(events.size()==3);
            for (PropertyChangeEvent event : events) {
                logger.fine(event.toString());
            }

            Map<String, String> configChanges = new HashMap<String, String>();
            configChanges.put("name", "julien");
            configChanges.put("value", "petit clown");
            ConfigBean domainBean = (ConfigBean) Dom.unwrap(domain);
            ConfigSupport.createAndSet(domainBean, Property.class, configChanges);

            
            Transactions.get().waitForDrain();

            assertTrue(events!=null);
            logger.fine("Number of events " + events.size());
            assertTrue(events.size()==3);
            for (PropertyChangeEvent event : events) {
                logger.fine(event.toString());
            }

            ConfigSupport.sortAndDispatch(events.toArray(new PropertyChangeEvent[0]), new Changed() {
                /**
                 * Notification of a change on a configuration object
                 *
                 * @param type            type of change : ADD mean the changedInstance was added to the parent
                 *                        REMOVE means the changedInstance was removed from the parent, CHANGE means the
                 *                        changedInstance has mutated.
                 * @param changedType     type of the configuration object
                 * @param changedInstance changed instance.
                 */
                public <T extends ConfigBeanProxy> void changed(TYPE type, Class<T> changedType, T changedInstance) {
                }
            }, logger);
        } finally {
            Transactions.get().removeTransactionsListener(listener);
        }
    }    
}
