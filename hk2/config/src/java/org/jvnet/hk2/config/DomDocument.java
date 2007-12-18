package org.jvnet.hk2.config;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a whole DOM tree.
 *
 * @author Kohsuke Kawaguchi
 */
public class DomDocument {
    /**
     * A hook to perform variable replacements on the attribute/element values
     * that are found in the configuration file.
     * The translation happens lazily when objects are actually created, not when
     * configuration is parsed, so this allows circular references &mdash;
     * {@link Translator} may refer to objects in the configuration file being read.
     */
    private volatile Translator translator = Translator.NOOP;

    protected final Map<Inhabitant<? extends ConfigInjector>,ConfigModel> models = new HashMap<Inhabitant<? extends ConfigInjector>, ConfigModel>();

    /*package*/ final Habitat habitat;

    /*package*/ Dom root;


    public DomDocument(Habitat habitat) {
        this.habitat = habitat;
    }

    public Dom getRoot() {
        return root;
    }

    public Translator getTranslator() {
        return translator;
    }

    public void setTranslator(Translator translator) {
        this.translator = translator;
    }

    /**
     * Creates {@link ConfigModel} for the given {@link ConfigInjector} if we haven't done so.
     */
    /*package*/ ConfigModel buildModel(Inhabitant<? extends ConfigInjector> i) {
        ConfigModel m = models.get(i);
        if(m==null)
            m = new ConfigModel(this,i,i.metadata());
        return m;
    }

    /**
     * Gets the {@link ConfigModel} from the "global" element name.
     */
    public ConfigModel getModel(String elementName) {
        Inhabitant<? extends ConfigInjector> i = habitat.getInhabitant(ConfigInjector.class, elementName);
        if(i==null) return null;
        return buildModel(i);
    }

    public ConfigModel getModel(Class c) {
        // not the most efficient implementation but this is probably ok as it calls only
        // when creating new instances of configured object through an explicit Allocate() call.
        final String className = c.getName();
        for (Map.Entry<Inhabitant<? extends ConfigInjector>, ConfigModel> entry : models.entrySet()) {
            final String name = entry.getValue().targetTypeName;
            if (className.equals(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Dom make(Habitat habitat, XMLStreamReader in, Dom parent, ConfigModel model) {
        return new Dom(habitat,this,parent,model,in);
    }

    /**
     * Writes back the whole DOM tree as an XML document.
     *
     * <p>
     * To support writing a subtree, this method doesn't invoke the start/endDocument
     * events. Those are the responsibility of the caller.
     *
     * @param w
     *      Receives XML infoset stream.
     */
    public void writeTo(XMLStreamWriter w) throws XMLStreamException {
        root.writeTo(null,w);
    }
}
