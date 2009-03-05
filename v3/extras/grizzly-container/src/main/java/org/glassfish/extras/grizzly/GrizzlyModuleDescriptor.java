package org.glassfish.extras.grizzly;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.xml.sax.SAXException;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

/**
 * Descriptor for a grizzly application.
 *
 * @author Jerome Dochez
 */
public class GrizzlyModuleDescriptor {


    final static String DescriptorPath = "META-INF/grizzly-glassfish.xml";
    final Map<String, String> tuples = new HashMap<String, String>();

    GrizzlyModuleDescriptor(ReadableArchive source, Logger logger) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            parse(factory.newDocumentBuilder().parse(source.getEntry(DescriptorPath)));
        } catch (SAXException e) {
            logger.log(Level.SEVERE, e.getMessage(),e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(),e);
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            logger.log(Level.SEVERE, e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

    private void parse(Document document) {
        Element element = document.getDocumentElement();
        NodeList adapters = element.getElementsByTagName("adapter");
        for (int i=0;i<adapters.getLength();i++) {
            Node adapter = adapters.item(i);
            NamedNodeMap attrs = adapter.getAttributes();
            addAdapter(attrs.getNamedItem("context-root").getNodeValue(),
                    attrs.getNamedItem("class-name").getNodeValue());
        }
        
    }

    public void addAdapter(String contextRoot, String className) {
        if (tuples.containsKey(contextRoot)) {
            throw new RuntimeException("duplicate context root in configuration :" + contextRoot);
        }
        tuples.put(contextRoot, className);
    }
        
    public Map<String, String> getAdapters() {
        return tuples;
    }
}
