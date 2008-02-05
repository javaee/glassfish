package com.sun.enterprise.configapi.tests;

import org.junit.Test;
import org.glassfish.config.support.ConfigurationPersistence;
import org.glassfish.config.support.GlassFishDocument;
import org.jvnet.hk2.config.*;
import org.jvnet.hk2.component.Habitat;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.beans.PropertyVetoException;
import java.beans.PropertyChangeEvent;
import java.util.List;
import static org.junit.Assert.*;

import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.HttpListener;

/**
 * Test the persistence to a file...
 */
public class PersistenceTest extends ConfigApiTest {

    public String getFileName() {
        return "DomainTest";
    }
    
    @Test
    public void testWriting() throws TransactionFailure {


        final Habitat habitat = super.getHabitat();
        final GlassFishDocument document = super.getHabitat().getByType(GlassFishDocument.class);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.reset();

        final ConfigurationPersistence testPersistence =  new ConfigurationPersistence() {
            public void save(DomDocument doc) throws IOException, XMLStreamException {
                XMLOutputFactory factory = XMLOutputFactory.newInstance();
                XMLStreamWriter writer = factory.createXMLStreamWriter(baos);
                doc.writeTo(writer);
                writer.close();
            }
        };
        
        TransactionListener testListener = new TransactionListener() {
            public void transactionCommited(List<PropertyChangeEvent> changes) {
                try {
                    testPersistence.save(document);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (XMLStreamException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        };
        try {
            Transactions.get().addTransactionsListener(testListener);

            HttpService service = super.getHabitat().getComponent(HttpService.class);
            // now do a transaction

            ConfigSupport.apply(new SingleConfigCode<HttpListener>() {
                /**
                 * Runs the following command passing the configration object. The code will be run
                 * within a transaction, returning true will commit the transaction, false will abort
                 * it.
                 *
                 * @param param is the configuration object protected by the transaction
                 * @return any object that should be returned from within the transaction code
                 * @throws java.beans.PropertyVetoException
                 *          if the changes cannot be applied
                 *          to the configuration
                 */
                public Object run(HttpListener param) throws PropertyVetoException, TransactionFailure {
                    param.setAcceptorThreads("9782");
                    return null;
                }
            }, service.getHttpListener().get(0));
        } finally {
            Transactions.get().waitForDrain();
            Transactions.get().removeTransactionsListener(testListener);
        }

        // now check if we persisted correctly...

        final String resultingXml = baos.toString();
        assertTrue(resultingXml.indexOf("acceptor-threads=\"9782\"")!=-1);
    }
}
