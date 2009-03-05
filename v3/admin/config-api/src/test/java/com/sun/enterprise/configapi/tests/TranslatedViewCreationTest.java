package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.KeepAlive;
import com.sun.enterprise.config.serverbeans.VirtualServer;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.jvnet.hk2.config.*;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.tests.utils.Utils;
import org.glassfish.config.support.GlassFishConfigBean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.List;

/**
 * User: Jerome Dochez
 * Date: Jun 24, 2008
 * Time: 8:27:29 PM
 */
public class TranslatedViewCreationTest extends ConfigApiTest {

    final static String propName = "com.sun.my.chosen.docroot";
    
    public String getFileName() {
        return "DomainTest";
    }

    HttpService httpService = null;
    List<PropertyChangeEvent> events;
    Habitat habitat;

    @Before
    public void setup() {
        System.setProperty(propName, "/foo/bar/docroot");
        habitat = Utils.getNewHabitat(this);

    }

    @Override
    public Habitat getHabitat() {
        return habitat;
    }

    @Test
    public void createVirtualServerTest() throws TransactionFailure {
        httpService = getHabitat().getComponent(HttpService.class);
        final TransactionListener listener = new TransactionListener() {
                public void transactionCommited(List<PropertyChangeEvent> changes) {
                    events = changes;
                }

            public void unprocessedTransactedEvents(List<UnprocessedChangeEvents> changes) {
            }
        };

        Transactions transactions = getHabitat().getComponent(Transactions.class);
        
        try {
            transactions.addTransactionsListener(listener);
            assertTrue(httpService!=null);

            logger.fine("Max connections = " + httpService.getKeepAlive().getMaxConnections());
            ConfigSupport.apply(new SingleConfigCode<HttpService>() {

                public Object run(HttpService param) throws PropertyVetoException, TransactionFailure {
                    VirtualServer newVirtualServer = param.createChild(VirtualServer.class);
                    newVirtualServer.setDocroot("${"+propName+"}");
                    newVirtualServer.setId("translated-view-creation");
                    param.getVirtualServer().add(newVirtualServer);
                    return null;
                }
            }, httpService);

            // first let check that our new virtual server has the right translated value
            VirtualServer vs = httpService.getVirtualServerByName("translated-view-creation");
            assertTrue(vs!=null);
            String docRoot = vs.getDocroot();
            assertTrue(docRoot.equals("/foo/bar/docroot"));

            transactions.waitForDrain();

            assertTrue(events!=null);
            logger.fine("Number of events " + events.size());
            assertTrue(events.size()==3);
            for (PropertyChangeEvent event : events) {
                if (event.getPropertyName().equals("virtual-server")) {
                    VirtualServer newVS = (VirtualServer) event.getNewValue();
                    assertTrue(event.getOldValue()==null);
                    docRoot = newVS.getDocroot();
                    assertTrue(docRoot.equals("/foo/bar/docroot"));

                    VirtualServer rawView = GlassFishConfigBean.getRawView(newVS);
                    assertTrue(rawView!=null);
                    assertTrue(rawView.getDocroot().equalsIgnoreCase("${" + propName + "}"));
                    return;
                }
            }
            assertTrue(false);

        } finally {
            transactions.removeTransactionsListener(listener);
        }

    }

    @After
    public void tearDown() {
        System.setProperty(propName, "");
    }
}
