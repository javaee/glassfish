package org.jvnet.hk2.config;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.config.Dom.Child;

import javax.xml.stream.XMLInputFactory;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses configuration files, builds {@link Inhabitant}s,
 * and add them to {@link Habitat}.
 *
 * <p>
 * This class also maintains the model of various elements in the configuration file.
 *
 * <p>
 * This class can be sub-classed to create a {@link ConfigParser} with a custom non-standard behavior.
 *
 * @author Kohsuke Kawaguchi
 */
public class ConfigParser {
    /**
     * This is where we put parsed inhabitants into.
     */
    protected final Habitat habitat;


    public ConfigParser(Habitat habitat) {
        this.habitat = habitat;
    }


    public DomDocument parse(XMLStreamReader in) throws XMLStreamException {
        DomDocument document = new DomDocument(habitat);
        parse(in, document);
        return document;
    }

    public void parse(XMLStreamReader in, DomDocument document) throws XMLStreamException {
        in.nextTag();
        document.root = handleElement(in,document);
    }

    /**
     * Parses the given source as a config file, and adds resulting
     * {@link Dom}s into {@link Habitat} as {@link Inhabitant}s.
     */
    public DomDocument parse(URL source) {
        return parse(source, new DomDocument(habitat));
    }
                                  

    public DomDocument parse(URL source, DomDocument document) {
        try {
            parse(xif.createXMLStreamReader(new StreamSource(source.toString())), document);
            return document;
        } catch (XMLStreamException e) {
            throw new ComponentException("Failed to parse "+source,e);
        }
    }

    /**
     * Parses a whole XML tree and builds a {@link Dom} tree.
     *
     * <p>
     * This is the entry point for the root element of a configuration tree.
     *
     * @param in
     *      pre-condition:  'in' is at the start element.
     *      post-condition: 'in' is at the end element.
     * @param document
     *      The document that we are building right now.
     *      Newly created {@link Dom} will belong to this document.
     * @return
     *      Null if the XML element didn't yield anything (which can happen if the element is skipped.)
     *      Otherwise fully parsed valid {@link Dom} object.
     */
    protected Dom handleElement(XMLStreamReader in,DomDocument document) throws XMLStreamException {
        ConfigModel model = document.getModelByElementName(in.getLocalName());
        if(model==null)
            throw new XMLStreamException("Unrecognized element "+in.getLocalName(),in.getLocation());
        return handleElement(in,document,null,model);
    }

    /**
     * Parses a whole XML tree and builds a {@link Dom} tree, by using the given model
     * for the top-level element.
     *
     * <p>
     * This is the entry point for recursively parsing inside a configuration tree.
     * Since not every element is global, you don't always want to infer the model
     * just from the element name (as is the case with {@link #handleElement(XMLStreamReader, DomDocument)}.
     * 
     * @param in
     *      pre-condition:  'in' is at the start element.
     *      post-condition: 'in' is at the end element.
     * @param document
     *      The document that we are building right now.
     *      Newly created {@link Dom} will belong to this document.
     * @return
     *      Null if the XML element didn't yield anything (which can happen if the element is skipped.)
     *      Otherwise fully parsed valid {@link Dom} object.
     */
    protected Dom handleElement(XMLStreamReader in, DomDocument document, Dom parent, ConfigModel model) throws XMLStreamException {
        final Dom dom = document.make(habitat, in, parent, model);

        // read values and fill DOM
        dom.fillAttributes(in);

        List<Child> children=null;

        while(in.nextTag()==START_ELEMENT) {
            String name = in.getLocalName();
            ConfigModel.Property a = model.elements.get(name);

            if(children==null)
                children = new ArrayList<Child>();

            if(a==null) {
                // global look up
                Dom child = handleElement(in, document);
                if(child!=null)
                    children.add(new Dom.NodeChild(name, child));
            } else
            if(a.isLeaf()) {
                children.add(new Dom.LeafChild(name,in.getElementText()));
            } else {
                Dom child = handleElement(in, document, dom, ((ConfigModel.Node) a).model);
                if(child!=null)
                    children.add(new Dom.NodeChild(name, child));
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

    private static final XMLInputFactory xif = XMLInputFactory.newInstance();
}
