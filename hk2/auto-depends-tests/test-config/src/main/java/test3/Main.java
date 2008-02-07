package test3;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import junit.framework.Assert;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.IndentingXMLStreamWriter;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DOMReader;
import org.w3c.dom.Document;
import test3.substitution.SecurityMap;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.Collection;

/**
 * @author Kohsuke Kawaguchi
 */
@Service
public class Main extends Assert implements ModuleStartup {
    @Inject
    FooBean foo;

    @Inject
    Habitat habitat;

    private static final XMLOutputFactory xof = XMLOutputFactory.newInstance();

    public void setStartupContext(StartupContext context) {
    }

    public void run() {
        assertNotNull(foo);
        // foo.e.printStackTrace(); // could be useful to find out where foo is created.
        assertEquals(80,foo.httpPort);
        assertEquals(foo.bar,"qwerty");

        // test the proxies
        JmsHost jms = find(foo.all, JmsHost.class);
        System.out.println(jms.toString());
        List<Property> props = jms.getProperties();
        assertEquals(2, props.size());
        assertEquals("foo",props.get(0).name);
        assertEquals("abc",props.get(0).value);

        assertEquals(3,foo.properties.size());
        assertNotNull(foo.properties.get("xyz"));
        assertNotNull(foo.properties.get("qqq"));
        assertNotNull(foo.properties.get("adminPort"));

        for (Property p : foo.properties.values())
            assertTrue(p.constructed);

        assertEquals(2,foo.jvmOptions.size());
        assertEquals(foo.jvmOptions.get(0),"-Xmx256m");
        assertEquals(foo.jvmOptions.get(1),"-verbose:abcwww");

        assertEquals(2,foo.httpListeners.size());

        HttpListener listener = habitat.getComponent(HttpListener.class, "a");
        assertEquals("a",listener.id);

        assertEquals(1,foo.virtualServers.size());
        VirtualServer vserver = foo.virtualServers.get(0);
        assertEquals(2,vserver.httpListeners.size());
        assertTrue(vserver.httpListeners.contains(habitat.getComponent(HttpListener.class, "a")));
        assertTrue(vserver.httpListeners.contains(habitat.getComponent(HttpListener.class, "b")));

        // test substitutability
        System.out.println(foo.find(SecurityMap.class).toString());

        // testing dynamic reconfiguration
        assertEquals(5,listener.acceptorThreads);
        Dom i = (Dom) habitat.getInhabitant(HttpListener.class, "a");
        i.attribute("acceptor-threads","56");
        assertEquals(56,listener.acceptorThreads);

        {// test update
            Dom dom = Dom.unwrap(jms);
            DomDocument doc = dom.document;
            Dom pointConfig = new Dom(habitat, doc, dom, doc.buildModel(PointConfig.class));
            pointConfig.attribute("x","100");
            pointConfig.attribute("y","-100");
            jms.getPoints().add((PointConfig)pointConfig.createProxy());

            try {
                // dump for visual inspection
                doc.writeTo(new IndentingXMLStreamWriter(xof.createXMLStreamWriter(System.out)));

                // make sure it's there
                DOMResult dr = new DOMResult();
                dr.setNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
                doc.writeTo(xof.createXMLStreamWriter(dr));
                assertNotNull(new DOMReader().read((Document) dr.getNode()).selectSingleNode("//jms-host/point[@x='100'][@y='-100']"));
            } catch (XMLStreamException e) {
                throw new AssertionError(e);
            } catch (ParserConfigurationException e) {
                throw new AssertionError(e);
            }
        }
    }

    private <T> T find(Collection<?> all, Class<T> type) {
        for (Object t : all) {
            if(type.isInstance(t))
                return type.cast(t);
        }
        return null;
    }
}
