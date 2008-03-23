package com.sun.enterprise.configapi.tests;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.jvnet.hk2.config.*;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.config.support.ConfigurationPersistence;
import org.glassfish.config.support.GlassFishDocument;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.Config;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * User: Jerome Dochez
 * Date: Mar 12, 2008
 * Time: 8:50:42 PM
 */
public class DirectAccessTest extends ConfigApiTest {

    Habitat habitat;
    
    /**
     * Returns the file name without the .xml extension to load the test configuration
     * from. By default, it's the name of the TestClass.
     *
     * @return the configuration file name
     */
    public String getFileName() {
        return "DomainTest";
    }


    @Before
    public void setup() {
        habitat = Utils.getNewHabitat(getFileName());
    }


    @Test
    public void changedTest() throws TransactionFailure {

        HttpService service = getHabitat().getComponent(HttpService.class);
        final GlassFishDocument document = super.getHabitat().getByType(GlassFishDocument.class);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.reset();


        assertTrue(service!=null);
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
            ConfigBean config = (ConfigBean) ConfigBean.unwrap(service.getHttpFileCache());
            ConfigBean config2 = (ConfigBean) ConfigBean.unwrap(service.getHttpProtocol());
            Map<ConfigBean, Map<String, String>> changes = new HashMap<ConfigBean, Map<String, String>>();
            Map<String, String> configChanges = new HashMap<String, String>();
            configChanges.put("max-age-in-seconds", "12543");
            configChanges.put("medium-file-size-limit-in-bytes", "1200");
            Map<String, String> config2Changes = new HashMap<String, String>();
            config2Changes.put("version", "12351");
            changes.put(config, configChanges);
            changes.put(config2, config2Changes);

            ConfigSupport.apply(changes);

        } finally {
            Transactions.get().waitForDrain();
            Transactions.get().removeTransactionsListener(testListener);
        }

        // now check if we persisted correctly...

        final String resultingXml = baos.toString();
        logger.fine(resultingXml);
        assertTrue(resultingXml.indexOf("max-age-in-seconds=\"12543\"")!=-1);
        assertTrue(resultingXml.indexOf("version=\"12351\"")!=-1);
    }

    
}
