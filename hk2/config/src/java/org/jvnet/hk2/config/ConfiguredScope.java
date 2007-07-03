package org.jvnet.hk2.config;

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

    /**
     * Loads the configuration file.
     */
    public void postConstruct() {
        try {
            XMLStreamReader xsr = xif.createXMLStreamReader(getConfigFile());
            while(xsr.next()!=XMLStreamConstants.END_DOCUMENT) {
                if(xsr.getEventType()==XMLStreamConstants.START_ELEMENT) {
                    String name = xsr.getLocalName();
                    Class component = cm.getComponentClass(
                        new ResourceLocator(name,Configured.class));
                    if(component==null)
                        throw new ComponentException("Unrecognized element name: "+name);
                    
                    /*
                        A large portion of this code somewhat overlaps with ComponentManager,
                        and therefore it doesn't feel right. Hmm.
                     */
                    // create new instance
                    Object o = component.newInstance();
                    // get values injected
                    cm.inject(o,Inject.class);
                    // push back to the scope

                    instance.store.add(new ResourceLocator(component),o);
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ComponentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static final XMLInputFactory xif = XMLInputFactory.newInstance();
}
