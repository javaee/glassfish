package org.jvnet.hk2.config;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.config.ConfigModel.ElementAction;
import org.jvnet.hk2.config.Dom.Child;

import javax.xml.stream.XMLInputFactory;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Parses configuration files, builds {@link Inhabitant}s,
 * and add them to {@link Habitat}.
 *
 * <p>
 * This class also maintains the model of various elements in the configuration file.
 *
 * @author Kohsuke Kawaguchi
 */
public class ConfigParser {
    /**
     * This is where we put parsed inhabitants into.
     */
    protected final Habitat habitat;

    protected final Map<Inhabitant<? extends ConfigInjector>,ConfigModel> models = new HashMap<Inhabitant<? extends ConfigInjector>, ConfigModel>();

    public ConfigParser(Habitat habitat) {
        this.habitat = habitat;
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

    public DomDocument parse(XMLStreamReader in) throws XMLStreamException {
        in.nextTag();
        DomDocument document = new DomDocument();
        document.root = handleElement(in,document);
        return document;
    }

    /**
     * Parses the given source as a config file, and adds resulting
     * {@link Dom}s into {@link Habitat} as {@link Inhabitant}s.
     */
    public DomDocument parse(URL source) {
        try {
            return parse(xif.createXMLStreamReader(new StreamSource(source.toString())));
        } catch (XMLStreamException e) {
            throw new ComponentException("Failed to parse "+source,e);
        }
    }

    /**
     * Processes a tree.
     *
     * @param in
     *      pre-condition:  'in' is at the start element.
     *      post-condition: 'in' is at the end element.
     * @param document
     */
    private Dom handleElement(XMLStreamReader in,DomDocument document) throws XMLStreamException {
        ConfigModel model = getModel(in.getLocalName());
        if(model==null)
            throw new XMLStreamException("Unrecognized element "+in.getLocalName(),in.getLocation());
        return handleElement(in,document,null,model);
    }

    private Dom handleElement(XMLStreamReader in, DomDocument document, Dom parent, ConfigModel model) throws XMLStreamException {
        final Dom dom = createDom(in, document, parent, model);

        // read values and fill DOM
        dom.fillAttributes(in);

        List<Child> children=null;

        while(in.nextTag()==START_ELEMENT) {
            String name = in.getLocalName();
            ElementAction a = model.elements.get(name);

            if(children==null)
                children = new ArrayList<Child>();

            if(a==null) {
                // global look up
                children.add(new Dom.NodeChild(name,handleElement(in,document)));
            } else
            if(a.isLeaf()) {
                children.add(new Dom.LeafChild(name,in.getElementText()));
            } else {
                children.add(new Dom.NodeChild(name,handleElement(in,document,dom,((ConfigModel.Node)a).model)));
            }
        }

        if(children!=null)
            dom.setChildren(children);

        habitat.add(dom);

        // register 'dom' under indices
        String key = dom.getKey();
        for (String contract : model.contracts)
            habitat.addIndex(dom,contract,key);
        if(key!=null)
            // if this is named component, register under its own FQCN too
            // to support look up.
            habitat.addIndex(dom,model.targetTypeName,key);

        return dom;
    }

    /**
     * Derived classes can create a sub-type of {@link Dom} to enhance
     * the ability of the DOM tree.
     */
    protected Dom createDom(XMLStreamReader in, DomDocument document, Dom parent, ConfigModel model) {
        return new Dom(habitat,document,parent,model,in);
    }

    private static final XMLInputFactory xif = XMLInputFactory.newInstance();
}
