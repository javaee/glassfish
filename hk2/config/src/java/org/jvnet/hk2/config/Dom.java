package org.jvnet.hk2.config;

import com.sun.hk2.component.LazyInhabitant;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.Womb;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

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
public class Dom extends LazyInhabitant implements InvocationHandler {
    /**
     * Model drives the interpretation of this DOM.
     */
    public final ConfigModel model;

    private Dom parent;

    static abstract class Child {
        final String name;

        Child(String name) {
            this.name = name;
        }

        /**
         * Writes this node to XML.
         */
        protected abstract void writeTo(XMLStreamWriter w) throws XMLStreamException;
    }

    static final class NodeChild extends Child {
        final Dom dom;

        NodeChild(String name, Dom dom) {
            super(name);
            this.dom = dom;
        }

        protected void writeTo(XMLStreamWriter w) throws XMLStreamException {
            dom.writeTo(name,w);
        }
    }

    static final class LeafChild extends Child {
        /**
         * Raw element text value before {@link Translator} processing.
         */
        final String value;

        LeafChild(String name, String value) {
            super(name);
            this.value = value;
        }

        protected void writeTo(XMLStreamWriter w) throws XMLStreamException {
            w.writeStartElement(name);
            w.writeCharacters(value);
            w.writeEndElement();
        }
    }

    /**
     * All attributes and their raw values before {@link Translator} processing.
     */
    private Map<String,String> attributes = new HashMap<String, String>();
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
    protected final DomDocument document;

    /**
     * @param in
     *      If provided, this is used to record the source location where this DOM object is loaded from.
     *      Otherwise this can be null.
     */
    public Dom(Habitat habitat, DomDocument document, Dom parent, ConfigModel model, XMLStreamReader in) {
        super(habitat,model.classLoaderHolder,model.targetTypeName,MultiMap.<String,String>emptyMap());
        if (in!=null) {
            this.location =  new LocationImpl(in.getLocation());
        } else {
            this.location=null;
        }
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
     * Given a master list and the new sub list, replace the items in the master list with the matching items
     * from the new sub list. This process works even if the length of the new sublist is different.
     *
     * <p>
     * For example, givn:
     *
     * <pre>
     * replace A by A':
     *   M=[A,B,C], S=[A'] => [A',B,C]
     *   M=[A,B,A,B,C], S=[A',A'] => [A',B,A',B,C]
     *
     * when list length is different:
     *   M=[A,A,B,C], S=[] => [B,C]
     *   M=[A,B,C], S=[A',A'] => [A',A',B,C]
     *   M=[B,C], S=[A',A'] => [B,C,A',A']
     * </pre>
     */
    private static void stitchList(List<Child> list, String name, List<? extends Child> newSubList) {
        // to preserve order, try to put new itesm where old items are found.
        // if the new list is longer than the current list, we put all the extra
        // after the last item in the sequence. That is,
        // given [A,A,B,C] and [A',A',A'], we'll update the list to [A',A',A',B,C]
        // The 'last' variable remembers the insertion position.
        int last = list.size();

        ListIterator<Child> itr = list.listIterator();
        ListIterator<? extends Child> jtr = newSubList.listIterator();
        while(itr.hasNext()) {
            Child child = itr.next();
            if(child.name.equals(name)) {
                if(jtr.hasNext()) {
                    itr.set(jtr.next());    // replace
                    last = itr.nextIndex();
                } else
                    itr.remove();   // remove
            }
        }

        // new list is longer than the current one
        if(jtr.hasNext())
            list.addAll(last,newSubList.subList(jtr.nextIndex(),newSubList.size()));
    }

    /**
     * Updates leaf-element values.
     * <p>
     * Synchronized so that concurrenct modifications will work correctly.
     */
    public synchronized void setLeafElements(final String name, String... values) {
        List<Child> newChildren = new ArrayList<Child>(children);

        LeafChild[] leaves = new LeafChild[values.length];
        for (int i = 0; i < values.length; i++)
            leaves[i] = new LeafChild(name,values[i]);

        stitchList(newChildren,name,Arrays.asList(leaves));
        children = newChildren;

        // see attribute(String,String) for the issue with this
        getInjector().injectElement(this,name,get());
    }

    /**
     * Picks up all leaf-element values of the given name.
     * @return
     *      Can be empty but never null.
     */
    public List<String> leafElements(String name) {
        List<Child> children = this.children; // fix the snapshot that we'll work with

        final List<String> r = new ArrayList<String>();
        for (Child child : children) {
            if (child.name.equals(name)) {
                // error check on model guarantees that this cast works.
                r.add(t(((LeafChild) child).value));
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
        for (Child child : children) {
            if (child.name.equals(name)) {
                // error check on model guarantees that this cast works.
                r.add(((LeafChild) child).value);
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
     * Updates node-element values.
     * <p>
     * Synchronized so that concurrenct modifications will work correctly.
     */
    public synchronized void setNodeElements(final String name, Dom... values) {
        List<Child> newChildren = new ArrayList<Child>(children);

        NodeChild[] leaves = new NodeChild[values.length];
        for (int i = 0; i < values.length; i++)
            leaves[i] = new NodeChild(name,values[i]);

        stitchList(newChildren,name,Arrays.asList(leaves));
        children = newChildren;

        // see attribute(String,String) for the issue with this
        getInjector().injectElement(this,name,get());
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
     * Creates a strongly-typed proxy to access values in this {@link Dom} object.
     */
    public <T extends ConfigBeanProxy> T createProxy(Class<T> proxyType) {
        return proxyType.cast(Proxy.newProxyInstance(proxyType.getClassLoader(),new Class[]{proxyType},this));
    }

    /**
     * {@link InvocationHandler} implementation that allows strongly-typed access
     * to the configuration.
     *
     * <p>
     * TODO: it might be a great performance improvement to have APT generate
     * code that does this during the development time by looking at the interface.
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // serve java.lang.Object methods by ourselves
        if(method.getDeclaringClass()==Object.class) {
            try {
                return method.invoke(this,args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        ConfigModel.Property p = toProperty(method);
        if(p==null)
            throw new IllegalArgumentException("No corresponding property found for method: "+method);

        if(args==null || args.length==0) {
            // getter
            return getter(p, method.getGenericReturnType());
        } else {
            // setter
            setter(p, args[0]);
            return null;
        }
    }

    protected Object getter(ConfigModel.Property target, Type t) {
        return target.get(this, t);
    }

    protected void setter(ConfigModel.Property target, Object value) {
        target.set(this, value);
    }

    /**
     * Obtain XML names (like "abc-def") from strings like "getAbcDef" and "hasAbcDef".
     * <p>
     * The conversion rule uses the {@link #model} to find a good match.
     */
    protected ConfigModel.Property toProperty(Method method) {
        String name = method.getName();

        // check annotations first
        Element e = method.getAnnotation(Element.class);
        if(e!=null) {
            String en = e.value();
            if(en.length()>0)
                return model.elements.get(en);
        }
        Attribute a = method.getAnnotation(Attribute.class);
        if(a!=null) {
            String an = a.value();
            if(an.length()>0)
                return model.attributes.get(an);
        }
        // TODO: check annotations on the getter/setter

        // first, trim off the prefix
        for (String p : PROPERTY_PREFIX) {
            if(name.startsWith(p)) {
                name = name.substring(p.length());
                break;
            }
        }

        // tokenize by finding 'x|X' and 'X|Xx' then insert '-'.
        StringBuilder buf = new StringBuilder(name.length()+5);
        for(String t : TOKENIZER.split(name)) {
            if(buf.length()>0)  buf.append('-');
            buf.append(t.toLowerCase());
        }
        name = buf.toString();

        // at this point name should match XML names in the model, modulo case.
        return model.findIgnoreCase(name);
    }


    /**
     * Used to tokenize the property name into XML name.
     */
    private static final Pattern TOKENIZER;
    private static String split(String lookback,String lookahead) {
        return "(?<="+lookback+")(?="+lookahead+')';
    }
    static {
        String pattern = String.format("(%1s)|(%2s)", split("x","X"), split("X","Xx"));
        pattern = pattern.replace("x","\\p{Lower}").replace("X","\\p{Upper}");
        TOKENIZER = Pattern.compile(pattern);
    }

    private static final String[] PROPERTY_PREFIX = new String[]{"get","set","is","has"};

    /**
     * This is how we inject the configuration into the created object.
     * <p>
     * There are two kinds &mdash; one where @{@link Configured} is put on
     * a bean and that is placedinto Habitat, and the other is
     * where @{@link Configured} is on {@link ConfigBeanProxy} subtype,
     * in which case the proxy to {@link Dom} will be placed into the habitat.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Womb createWomb(Class c) {
        if(ConfigBeanProxy.class.isAssignableFrom(c))
            return new DomProxyWomb(c,metadata(),this);
        else
            return new ConfiguredWomb(super.createWomb(c),this);
    }

    /**
     * Used by the parser to set a list of children.
     */
    /*package*/ void setChildren(List<Child> children) {
       this.children = children;
    }

    /**
     * Writes back this element.
     *
     * @param tagName
     *      The tag name of this element to be written. If null, this DOM node
     *      must be a global element and its tag name will be used.
     * @param w
     *      Receives XML infoset stream.
     */
    public void writeTo(String tagName, XMLStreamWriter w) throws XMLStreamException {
        if(tagName==null)
            tagName = model.tagName;
        if(tagName==null)
            throw new IllegalArgumentException("Trying t write a local element "+this+" w/o a tag name");
        w.writeStartElement(tagName);

        for (Map.Entry<String, String> a : attributes.entrySet())
            w.writeAttribute(a.getKey(),a.getValue());

        for (Child c : children)
            c.writeTo(w);
        
        w.writeEndElement();
    }
}
