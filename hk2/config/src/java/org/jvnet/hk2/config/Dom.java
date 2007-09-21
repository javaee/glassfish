package org.jvnet.hk2.config;

import com.sun.hk2.component.LazyInhabitant;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.Womb;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * {@link Inhabitant} that loads configuration from XML.
 *
 * <p>
 * This object also captures all the configuration values in a typeless way,
 * so that the loading of the actual classes can be deferred as much as possible.
 *
 * <p>
 * This is the {@link Inhabitant} that gets registered into {@link Habitat},
 * so one can access this object by {@link Habitat#getInhabitant(Class, String)} family
 * of methods.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Dom extends LazyInhabitant {
    /**
     * Model drives the interpretation of this DOM.
     */
    public final ConfigModel model;

    private Dom parent;

    static class Child {
        final String name;

        Child(String name) {
            this.name = name;
        }
    }

    static final class NodeChild extends Child {
        final Dom dom;

        NodeChild(String name, Dom dom) {
            super(name);
            this.dom = dom;
        }
    }

    static final class LeafChild extends Child {
        final String value;

        LeafChild(String name, String value) {
            super(name);
            this.value = value;
        }
    }

    private Map<String,String> attributes;
    /**
     * List of all child elements, both leaves and nodes.
     *
     * <p>
     * The list is read-only and copy-on-write to support concurrent access.
     */
    private volatile List<Child> children = Collections.emptyList();
    private final Location location;

    /**
     * Owner of the DOM tree.
     */
    private final DomDocument document;

    public Dom(Habitat habitat, DomDocument document, Dom parent, ConfigModel model, XMLStreamReader in) {
        super(habitat,model.classLoaderHolder,model.targetTypeName,MultiMap.<String,String>emptyMap());
        this.location =  new LocationImpl(in.getLocation());
        this.model = model;
        this.document = document;
        this.parent = parent;
        assert parent==null || parent.document==document; // all the nodes in the tree must belong to the same document
    }

    /**
     * Obtains the actual key value from this {@link Dom}.
     */
    public String getKey() {
        String k = model.key;
        if(k==null) return null;

        switch(k.charAt(0)) {
        case '@':
            return attribute(k.substring(1));
        case '<':
            return leafElement(k.substring(1,k.length()-1));
        default:
            throw new IllegalStateException("Invalid key value:"+k);
        }
    }

    /**
     * If this DOM is a child of another DOM, the parent pointer.
     * Otherwise null.
     */
    public Dom parent() {
        return parent;
    }

    /*package*/ void fillAttributes(XMLStreamReader in) {
        for( int i=in.getAttributeCount()-1; i>=0; i-- ) {
            String n = in.getAttributeLocalName(i);
            if(model.attributes.containsKey(n)) {
                if(attributes==null)
                    attributes = new HashMap<String, String>();
                attributes.put(n,in.getAttributeValue(i));
            }
        }
        if(attributes==null)
            attributes = Collections.emptyMap();
    }

    /**
     * Where was this {@link Dom} loaded from?
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Performs translation with null pass-through.
     */
    private String t(String s) {
        if(s==null) return null;
        return document.getTranslator().translate(s);
    }

    /**
     * Obtains the attribute value, after variable expansion.
     *
     * @return
     *      null if the attribute is not found.
     */
    public String attribute(String name) {
        return t(attributes.get(name));
    }

    /**
     * Obtians the attribute value without variable expansion.
     *
     * @return
     *      null if the attribute is not found.
     */
    public String rawAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Obtains the plural attribute value. Values are separate by ',' and surrounding whitespaces are ignored.
     *
     * @return
     *      null if the attribute doesn't exist. This is a distinct state from the empty list,
     *      which indicates that the attribute was there but no values were found.
     */
    public List<String> attributes(String name) {
        String v = attribute(name);
        if(v==null)     return null;
        List<String> r = new ArrayList<String>();
        StringTokenizer tokens = new StringTokenizer(v,",");
        while(tokens.hasMoreTokens())
            r.add(tokens.nextToken().trim());
        return r;
    }

    /**
     * Updates the attribute value.
     *
     * This would trigger the re-injection of the value.
     */
    public void attribute(String name, String value) {
        attributes.put(name,value);
        // TODO:
        // this re-injection has two problems. First, it forces an instantiation
        // even if that hasn't happened yet. Second, if the component is scoped,
        // this won't work correctly (but then, there's no way to make that work,
        // since we can't enumerate all scope instances.)
        getInjector().injectAttribute(this,name,get());
    }

    /**
     * Picks up one leaf-element value.
     */
    public String leafElement(String name) {
        return t(rawLeafElement(name));
    }

    /**
     * Picks up one leaf-element value without variable expansion.
     */
    public String rawLeafElement(String name) {
        List<Child> children = this.children; // fix the snapshot that we'll work with

        int len = children.size();
        for( int i=0; i<len; i++ ) {
            Child child = children.get(i);
            if(child.name.equals(name)) {
                // error check on model guarantees that this works.
                return ((LeafChild)child).value;
            }
        }
        return null;
    }

    /**
     * Updates leaf-element values.
     */
    public void leafElement(String name, String... values) {
        // TODO: implement this method later
        throw new UnsupportedOperationException();
        //leafElements.set(name, Arrays.asList(values));
        //// see attribute(String,String) for the issue with this
        //getInjector().injectElement(this,name,get());
    }

    /**
     * Picks up all leaf-element values of the given name.
     */
    public List<String> leafElements(String name) {
        List<Child> children = this.children; // fix the snapshot that we'll work with

        final List<String> r = new ArrayList<String>();
        int len = children.size();
        for( int i=0; i<len; i++ ) {
            Child child = children.get(i);
            if(child.name.equals(name)) {
                // error check on model guarantees that this works.
                r.add(t(((LeafChild)child).value));
            }
        }
        return r;
    }

    /**
     * Picks up all leaf-element values of the given name, without variable expansion.
     *
     * @return
     *      can be empty, but never null (even if such element name is not defined in the model.)
     */
    public List<String> rawLeafElements(String name) {
        List<Child> children = this.children; // fix the snapshot that we'll work with

        final List<String> r = new ArrayList<String>();
        int len = children.size();
        for( int i=0; i<len; i++ ) {
            Child child = children.get(i);
            if(child.name.equals(name)) {
                // error check on model guarantees that this works.
                r.add(((LeafChild)child).value);
            }
        }
        return r;
    }

    /**
     * Picks up one node-element value.
     */
    public Dom nodeElement(String name) {
        List<Child> children = this.children; // fix the snapshot that we'll work with

        int len = children.size();
        for( int i=0; i<len; i++ ) {
            Child child = children.get(i);
            if(child.name.equals(name)) {
                // error check on model guarantees that this works.
                return ((NodeChild)child).dom;
            }
        }
        return null;
    }

    /**
     * Picks up all node-elements that have the given element name.
     */
    public List<Dom> nodeElements(String elementName) {
        List<Child> children = this.children; // fix the snapshot that we'll work with

        final List<Dom> r = new ArrayList<Dom>();
        int len = children.size();
        for( int i=0; i<len; i++ ) {
            Child child = children.get(i);
            if(child.name.equals(elementName)) {
                // error check on model guarantees that this works.
                r.add(((NodeChild)child).dom);
            }
        }
        return r;
    }

    /**
     * Picks up all node elements that are assignable to the given type,
     * except those who are matched by other named elements in the model.
     *
     * Used to implement {@code FromElement("*")}.
     */
    public <T> List<T> nodeByTypeElements(Class<T> baseType) {
        List<T> r = new ArrayList<T>();

        int len = children.size();
        for( int i=0; i<len; i++ ) {
            Child child = children.get(i);
            if (child instanceof NodeChild) {
                NodeChild nc = (NodeChild) child;
                if(model.elements.containsKey(nc.name))
                    continue;   // match with named
                if(baseType.isAssignableFrom(nc.dom.type()))
                    r.add(baseType.cast(nc.dom.get()));
            }
        }
        return r;
    }

    public <T> T nodeByTypeElement(Class<T> baseType) {
        int len = children.size();
        for( int i=0; i<len; i++ ) {
            Child child = children.get(i);
            if (child instanceof NodeChild) {
                NodeChild nc = (NodeChild) child;
                if(model.elements.containsKey(nc.name))
                    continue;   // match with named
                if(baseType.isAssignableFrom(nc.dom.type()))
                    return baseType.cast(nc.dom.get());
            }
        }
        return null;
    }

    /**
     * Performs injection to the given object.
     */
    public void inject(Object target) {
        model.inject(this,target);
    }

    /**
     * Gets the {@link ConfigInjector} instance that can be used to inject
     * this DOM to a bean. 
     */
    public ConfigInjector getInjector() {
        return model.injector.get();
    }

    /**
     * Locates the DOM that serves as the symbol space root.
     *
     * @return always non-null.
     */
    public Dom getSymbolSpaceRoot(String typeName) {
        Dom dom = this;
        while(!dom.model.symoblSpaces.contains(typeName)) {
            Dom p = dom.parent();
            if(p==null) return dom; // root
            dom = p;
        }
        return dom;
    }

    /**
     * Recursively decends the DOM tree and finds a DOM that has the given key
     * and the type name.
     *
     * <p>
     * TODO: the current algorithm does a full tree scan. Expand the model
     * so that we can detect deadends that are statically known not to contain
     * the kind we are looking for, and use that to cut the search space.
     */
    public Dom resolveReference(String key, String typeName) {
        String keyedAs = model.keyedAs;
        if(keyedAs!=null && keyedAs.equals(typeName) && getKey().equals(key))
            return this; // found it

        for (Child child : children) {
            if (child instanceof NodeChild) {
                NodeChild n = (NodeChild) child;
                Dom found = n.dom.resolveReference(key,typeName);
                if(found!=null) return found;
            }
        }

        return null;
    }

    /**
     * This is how we inject the configuration into the created object
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Womb createWomb(Class c) {
        return new ConfiguredWomb(super.createWomb(c),this);
    }

    /**
     * Used by the parser to set a list of children.
     */
    /*package*/ void setChildren(List<Child> children) {
       this.children = children;
    }
}
