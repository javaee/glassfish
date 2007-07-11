package org.jvnet.hk2.config;

import com.envoisolutions.sxc.Context;
import com.envoisolutions.sxc.Reader;
import com.envoisolutions.sxc.Writer;
import com.envoisolutions.sxc.util.XoXMLStreamReader;
import com.envoisolutions.sxc.util.XoXMLStreamReaderImpl;
import com.sun.hk2.component.ScopeInstance;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.ComponentManager;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.ResourceLocator;
import org.jvnet.hk2.component.Scope;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class ConfiguredScope extends Scope implements PostConstruct {
    private final ScopeInstance instance = new ScopeInstance();

    @Inject
    public ComponentManager cm;

    public ScopeInstance current() {
        return instance;
    }

    /**
     * Determines the source of the configuration to be loaded.
     * <p>
     * {@link StreamSource} can provide system ID, which is used for
     * error reporting.
     */
    protected abstract StreamSource getConfigFile() throws IOException;

    protected String getAttributeName(String seedName, FromAttribute a) {
        String v = a.value();
        if(v.length()==0)
            v = seedName;
        return v;
    }

    /**
     * Loads the configuration file.
     */
    public void postConstruct() {
        try {
            final XMLStreamReader xsr = xif.createXMLStreamReader(getConfigFile());

            // first skip to the start element
            while(xsr.getEventType()!= XMLStreamConstants.START_ELEMENT)
                xsr.nextTag();

            Context context = new Context() {
                public Reader createReader() {throw new UnsupportedOperationException();}
                public Writer createWriter() {throw new UnsupportedOperationException();}
            };
            context.put(ComponentManager.class.getName(),cm);
            
            load(new XoXMLStreamReaderImpl(xsr), context,null);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object load(XoXMLStreamReader xsr, Context context, Map<String, Object> properties) throws XMLStreamException {
        assert xsr.getEventType()==XMLStreamConstants.START_ELEMENT;

        String name = xsr.getLocalName();
        try {
            // figure out the class to be loaded from this element
            Class component = cm.getComponentClass(new ResourceLocator(name, Configured.class));
            if(component==null)
                throw new XMLStreamException("Unrecognized element name: "+name, xsr.getLocation());

            Class<?> readerClass = component.getClassLoader().loadClass(component.getName() + "Reader");
            com.envoisolutions.sxc.Reader reader = (com.envoisolutions.sxc.Reader) readerClass.getConstructor(Context.class).newInstance(context);
            Object result = reader.read(xsr, properties);
            assert component.isInstance(result);

            // push back to the scope
            // TODO: we'd like to do this selectively
            instance.store.add(new ResourceLocator(component),result);

            return result;
        } catch (ComponentException e) {
            throw new XMLStreamException2("Failed to handle <"+name+">",xsr.getLocation());
        } catch (NoSuchMethodException e) {
            throw new XMLStreamException2("Unable to load the reader class",xsr.getLocation(), e);
        } catch (IllegalAccessException e) {
            throw new XMLStreamException2("Unable to access the reader class",xsr.getLocation(), e);
        } catch (InvocationTargetException e) {
            throw new XMLStreamException2("Unable to create the reader class",xsr.getLocation(), e);
        } catch (InstantiationException e) {
            throw new XMLStreamException2("Unable to load the reader class",xsr.getLocation(), e);
        } catch (ClassNotFoundException e) {
            throw new XMLStreamException2("Unable to find the reader class",xsr.getLocation(), e);
        } catch (Exception e) {
            throw new XMLStreamException2("Failed to handle <"+name+">",xsr.getLocation(), e);
        }
    }

    private static final XMLInputFactory xif = XMLInputFactory.newInstance();
}
