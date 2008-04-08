package com.sun.enterprise.configapi.tests;

import org.glassfish.config.support.GlassFishDocument;
import org.glassfish.config.support.ConfigurationPersistence;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.TransactionListener;
import org.jvnet.hk2.config.IndentingXMLStreamWriter;
import org.jvnet.hk2.config.Transactions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.beans.PropertyChangeEvent;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * User: Jerome Dochez
 * Date: Mar 25, 2008
 * Time: 11:36:46 AM
 */
public abstract class ConfigPersistence extends ConfigApiTest {


    @Test
    public void test() throws TransactionFailure {

        final DomDocument document = getDocument(getHabitat());
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.reset();

        final ConfigurationPersistence testPersistence =  new ConfigurationPersistence() {
            public void save(DomDocument doc) throws IOException, XMLStreamException {
                XMLOutputFactory factory = XMLOutputFactory.newInstance();
                XMLStreamWriter writer = factory.createXMLStreamWriter(baos);
                doc.writeTo(new IndentingXMLStreamWriter(writer));
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

            doTest();
        } catch(TransactionFailure f) {
            f.printStackTrace();
            throw f;

        } finally {
            Transactions.get().waitForDrain();
            Transactions.get().removeTransactionsListener(testListener);
        }

        // now check if we persisted correctly...

        final String resultingXml = baos.toString();
        //System.out.println(resultingXml);
        logger.fine(resultingXml);
        assertTrue(assertResult(resultingXml));
    }

    public abstract void doTest() throws TransactionFailure;

    public abstract boolean assertResult(String resultingXml);    
}
