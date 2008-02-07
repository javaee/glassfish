package org.jvnet.hk2.config;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.ComponentException;

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
     * Obtains a {@link ConfigModel} for the given class (Which should have {@link Configured} annotation on it.)
     */
    public ConfigModel buildModel(Class<?> clazz) {
        return buildModel(clazz.getName());
    }

    /**
     * Obtains a {@link ConfigModel} for the given class (Which should have {@link Configured} annotation on it.)
     */
    public ConfigModel buildModel(String fullyQualifiedClassName) {
        Inhabitant i = habitat.getInhabitantByAnnotation(InjectionTarget.class, fullyQualifiedClassName);
        if(i==null)
            throw new ComponentException("ConfigInjector for %s is not found",fullyQualifiedClassName);
        return buildModel(i);
    }

    /**
     * Obtains the {@link ConfigModel} from the "global" element name.
     *
     * <p>
     * This method uses {@link #buildModel} to lazily build models if necessary.
     * 
     * @return
     *      Null if no configurable component is registered under the given global element name.
     */
    public ConfigModel getModelByElementName(String elementName) {
        Inhabitant<? extends ConfigInjector> i = habitat.getInhabitant(ConfigInjector.class, elementName);
        if(i==null) return null;
        return buildModel(i);
    }

    // TODO: to be removed once we make sure that no one is using it anymore
    @Deprecated
    public ConfigModel getModel(Class c) {
        return buildModel(c);
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
