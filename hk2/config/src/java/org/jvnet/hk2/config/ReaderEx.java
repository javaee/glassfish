package org.jvnet.hk2.config;

import com.envoisolutions.sxc.Context;
import com.envoisolutions.sxc.Reader;
import com.envoisolutions.sxc.util.XoXMLStreamReader;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.ComponentManager;
import org.jvnet.hk2.component.PostConstruct;

import javax.xml.stream.XMLStreamException;
import java.util.Map;

/**
 * Base class for all {@link Configured} bean reader.
 * 
 * @author Kohsuke Kawaguchi
 */
public abstract class ReaderEx extends Reader {
    protected final ComponentManager cm;

    protected ReaderEx(Context context) {
        super(context);
        cm = (ComponentManager) context.get(ComponentManager.class.getName());
        assert cm!=null;
    }

    /**
     * Performs injection to the object.
     */
    protected final void inject(XoXMLStreamReader xsr, Object o) throws XMLStreamException {
        try {
            cm.inject(o, Inject.class);
        } catch (ComponentException e) {
            throw new XMLStreamException2("Unable to inject to "+o,xsr.getLocation(),e);
        }
    }

    /**
     * Adds an object to a map.
     */
    protected final <T extends Named> void addToMap(Map<String,T> m, T o) {
        m.put(o.getName(),o);
    }

    /**
     * Perform post-construction.
     */
    protected final <T> T wrapUp(T t) {
        if(t instanceof PostConstruct)
            ((PostConstruct)t).postConstruct();
        return t;
    }
}
