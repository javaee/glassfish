/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.jvnet.hk2.config;

import com.sun.hk2.component.LazyInhabitant;
import org.jvnet.hk2.component.*;

import javax.validation.constraints.NotNull;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.beans.PropertyVetoException;
import java.lang.reflect.*;
import java.lang.annotation.Annotation;
import java.util.*;
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
public class Dom extends LazyInhabitant implements InvocationHandler, ObservableBean {
    /**
     * Model drives the interpretation of this DOM.
     */
    public final ConfigModel model;

    private final Dom parent;

    static abstract class Child {
        final String name;

        Child(String name) {
            this.name = name;
        }

        /**
         * Writes this node to XML.
         */
        protected abstract void writeTo(XMLStreamWriter w) throws XMLStreamException;

        /**
         * Returns a deep copy of itself.
         * @return a deep copy of itself.
         */
        protected abstract Child deepCopy(Dom parent);

        /**
         * Returns true if it is an empty child, meaning
         * all its attributes are either null or the default value
         * as well as for all the descendants.
         */
        protected abstract boolean isEmpty();
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

        @Override
        protected Child deepCopy(Dom parent) {

            return new NodeChild(name, dom.copy(parent));
        }

        @Override
        protected boolean isEmpty() {
            return dom.isEmpty();
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

        @Override
        protected Child deepCopy(Dom parent) {
            return new LeafChild(name, value);
        }

        @Override
        protected boolean isEmpty() {
            return false;
        }
    }

    public void initializationCompleted() {
    }

    /* package */ void register() {
        habitat.add(this);
        String key = getKey();
        for (String contract : model.contracts) {
            habitat.addIndex(this, contract, key);
        }
        if (key!=null) {
            habitat.addIndex(this, model.targetTypeName, key);
        }

    }

    /**
     * When a new Dom object is created, ensures that all @NotNull annotated
     * elements have a value.
     * 
     */
    public void addDefaultChildren() {
        List<Dom.Child> children = new ArrayList<Dom.Child>();
        ensureConstraints(children);
        if (!children.isEmpty()) {
            setChildren(children);
        }
    }

    /* package */ void ensureConstraints(List<Child> children) {
        Set<String> nullElements = new HashSet<String>(model.getElementNames());
        for (Child child : children) {
            nullElements.remove(child.name);
        }

        for (String s : nullElements) {
            ConfigModel.Property p = model.getElement(s);
            for (String annotation : p.getAnnotations()) {
                if (annotation.equals(NotNull.class.getName())) {
                    if (p instanceof ConfigModel.Node) {
                        ConfigModel childModel = ((ConfigModel.Node) p).model;
                        Dom child = document.make(habitat, null, this, childModel);
                        child.register();

                        children.add(new Dom.NodeChild(s, child));

                        // recursive call to ensure the children constraints are also respected
                        List<Child> grandChildren = new ArrayList<Child>();
                        child.ensureConstraints(grandChildren);
                        if (!grandChildren.isEmpty()) {
                            child.setChildren(grandChildren);
                        }

                        child.initializationCompleted();
                    }
                }
            }
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
    public final DomDocument document;

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

    public Dom(Habitat habitat, DomDocument document, Dom parent, ConfigModel model) {
        this(habitat, document, parent, model, null);
    }

    /**
     * Copy constructor, used to get a deep copy of the passed instance
     * @param source the instance to copy
     */
    public Dom(Dom source, Dom parent) {
        this(source.habitat, source.document, parent, source.model);
        List<Child> newChildren = new ArrayList<Child>();
        for (Child child : source.children) {
            newChildren.add(child.deepCopy(this));
        }
        setChildren(newChildren);
        attributes.putAll(source.attributes);
    }

    /**
     * Returns a copy of itself providing the parent for the new copy.
     *
     * @param parent the parent instance for the cloned copy
     * @return the cloned copy
     */
    protected <T extends Dom> T copy(T parent) {
        return (T) new Dom(this, parent);
    }
    
    /**
     * Unwraps the proxy and returns the underlying {@link Dom} object.
     *
     * @return
     *      null if the given instance is not actually a proxy to a DOM.
     */
    public static Dom unwrap(ConfigBeanProxy proxy) {
        InvocationHandler ih = Proxy.getInvocationHandler(proxy);
        if (ih instanceof Dom)
            return (Dom) ih;
        if (ih instanceof ConfigView) {
            return (Dom) ((ConfigView)ih).getMasterView();
        }
        return null;
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
     * Returns the list of attributes with a value on this config instance.
     * This is by definition a subset of the attributes names as known
     * to the model {@see ConfigModel.getAttributeNames}.
     *
     * @return list of attributes names which have values on this config instance
     */
    public Set<String> getAttributeNames() {
        return Collections.unmodifiableSet( attributes.keySet() );
    }

    /**
     * Returns the children name associated with this config instance.
     * This is by definition a subset of the element names as known to the
     * model {#see ConfigModel.getElementNames().
     *
     * @Return list of elements names associated with this config instance
     */
    public Set<String> getElementNames() {
        Set<String> names = new HashSet<String>();
        for (Child child : children) {
            names.add(child.name);
        }
        return names;
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
        return t(rawAttribute(name));
    }

    /**
     * Obtians the attribute value without variable expansion.
     *
     * @return
     *      null if the attribute is not found.
     */
    public String rawAttribute(String name) {
        String value = attributes.get(name);
        if (value==null && model.attributes.containsKey(name)) {
                value = model.attributes.get(name).getDefaultValue();
        }
        return value;
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
        if (value==null) {
            attributes.remove(name);
        } else {
            attributes.put(name,value);
            // TODO:
            // this re-injection has two problems. First, it forces an instantiation
            // even if that hasn't happened yet. Second, if the component is scoped,
            // this won't work correctly (but then, there's no way to make that work,
            // since we can't enumerate all scope instances.)
            getInjector().injectAttribute(this,name,get());
        }
    }

    /**
     * Returns the child element by name
     * @param name of the element
     * @return child element
     */
    public Dom element(String name) {
        
        List<Child> children = this.children; // fix the snapshot that we'll work with

        for (Child child : children) {
            if (child.name.equals(name)) {
                return ((NodeChild) child).dom;
            }
        }
        return null;
    }

    /**
     * Picks up one leaf-element value.
     */
    public String leafElement(String name) {
        return t(rawLeafElement(name));
    }

    /**
     * Inserts a new {@link Dom} node right after the given DOM element.
     *
     * @param reference
     *      If null, the new element will be inserted at the very beginning.
     * @param name
     *      The element name of the newly inserted item. "*" to indicate that the element
     *      name be determined by the model of the new node.
     */
    public synchronized void insertAfter(Dom reference, String name, Dom newNode) {
        // TODO: reparent newNode
        if(name.equals("*"))    name=newNode.model.tagName;
        NodeChild newChild = new NodeChild(name, newNode);

        if (children.size()==0) {
            children = new ArrayList<Child>();
        }
        if(reference==null) {
            children.add(0, newChild);
            habitat.addIndex(newNode, newNode.getProxyType().getName(), newNode.getKey());
            return;
        }

        ListIterator<Child> itr = children.listIterator();
        while(itr.hasNext()) {
            Child child = itr.next();
            if (child instanceof NodeChild) {
                NodeChild nc = (NodeChild) child;
                if(nc.dom==reference) {
                    itr.add(newChild);
                    habitat.addIndex(newNode, newNode.getProxyType().getName(), newNode.getKey());
                    //habitat.add(newNode);
                    return;
                }
            }
        }
        throw new IllegalArgumentException(reference+" is not a valid child of "+this+". Children="+children);
    }

    /**
     * Replaces an existing {@link NodeChild} with another one.
     *
     * @see #insertAfter(Dom, String, Dom)
     */
    public synchronized void replaceChild(Dom reference, String name, Dom newNode) {
        ListIterator<Child> itr = children.listIterator();
        while(itr.hasNext()) {
            Child child = itr.next();   
            if (child instanceof NodeChild) {
                NodeChild nc = (NodeChild) child;
                if(nc.dom==reference) {
                    habitat.removeIndex(reference.getProxyType().getName(), reference.getKey());
                    habitat.addIndex(newNode, newNode.getProxyType().getName(), newNode.getKey());                    
                    itr.set(new NodeChild(name,newNode));
                    return;
                }
            }
        }
        throw new IllegalArgumentException(reference+" is not a valid child of "+this+". Children="+children);
    }

    /**
     * Removes an existing {@link NodeChild}
     *
     */
    public synchronized void removeChild(Dom reference) {
        ListIterator<Child> itr = children.listIterator();
        while(itr.hasNext()) {
            Child child = itr.next();
            if (child instanceof NodeChild) {
                NodeChild nc = (NodeChild) child;
                if(nc.dom==reference) {
                    itr.remove();
                    habitat.removeIndex(reference.getProxyType().getName(), reference.getKey());
                    reference.release();
                    return;
                }
            }
        }
        throw new IllegalArgumentException(reference+" is not a valid child of "+this+". Children="+children);

    }

    public synchronized boolean addLeafElement(String xmlName, String value) {
        if (children.size()==0) {
            children = new ArrayList<Child>();
        }
        return children.add(new LeafChild(xmlName, value));
        
    }

    public synchronized boolean removeLeafElement(String xmlName, String element) {
        List<Child> children = this.children; // fix the snapshot that we'll work with

        for (Child child : children) {
            if(child.name.equals(xmlName) && ((LeafChild) child).value.equals(element)) {
                return children.remove(child);
            }
        }
        return false;

    }

    public synchronized boolean changeLeafElement(String xmlName, String oldValue, String newValue) {
        List<Child> children = this.children; // fix the snapshot that we'll work with

        int len = children.size();
        for( int i=0; i<len; i++ ) {
            Child child = children.get(i);
            if(child.name.equals(xmlName) && ((LeafChild) child).value.equals(oldValue)) {
                return (children.set(i, new LeafChild(xmlName, newValue))!=null);
            }
        }
        return false;
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
    public  List<Dom> domNodeByTypeElements(Class baseType) {
        List<Dom> r = new ArrayList<Dom>();

        int len = children.size();
        for( int i=0; i<len; i++ ) {
            Child child = children.get(i);
            if (child instanceof NodeChild) {
                NodeChild nc = (NodeChild) child;
                if(model.elements.containsKey(nc.name))
                    continue;   // match with named
                if(baseType.isAssignableFrom(nc.dom.type()))
                    r.add(nc.dom);
            }
        }
        return r;
    }

    public <T> List<T> nodeByTypeElements(final Class<T> baseType) {
        final List<Dom> elements = domNodeByTypeElements(baseType);
        return new AbstractList<T>() {
            public T get(int index) {
                return baseType.cast(elements.get(index).get());
            }
            public int size() {
                return elements.size();
            }
        };
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
        while(!dom.model.symbolSpaces.contains(typeName)) {
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
     * Creates a strongly-typed proxy to access values in this {@link Dom} object,
     * by using the specified interface type as the proxy type.
     */
    public <T extends ConfigBeanProxy> T createProxy(Class<T> proxyType) {
        return proxyType.cast(Proxy.newProxyInstance(proxyType.getClassLoader(),new Class[]{proxyType},this));
    }

    /**
     * Creates a strongly-typed proxy to access values in this {@link Dom} object,
     */
    public <T extends ConfigBeanProxy> T createProxy() {
        return createProxy(this.<T>getProxyType());
    }

    /**
     * Returns the proxy type for this configuration object
     * @param <T> the proxy type
     * @return the class object for the proxy type
     */
    public <T extends ConfigBeanProxy> Class<T> getProxyType() {
        return model.getProxyType();
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
        Class<?> clazz = method.getDeclaringClass();
        if(clazz ==Object.class) {
            try {
                return method.invoke(this,args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
        if (clazz==Injectable.class) {
            injectInto(this, args[0]);
            return null;
        }
        if(method.getAnnotation(DuckTyped.class)!=null) {
            return invokeDuckMethod(method,proxy,args);
        }

        ConfigModel.Property p = model.toProperty(method);
        if(p==null)
            throw new IllegalArgumentException("No corresponding property found for method: "+method);

        if(args==null || args.length==0) {
            // getter
            return getter(p, method.getGenericReturnType());
        } else {
            throw new PropertyVetoException("Instance of " + typeName() + " named '" + getKey() +
                    "' is not locked for writing when invoking method " + method.getName()
                    + " you must use transaction semantics to access it.", null);
        }
    }

    public Habitat getHabitat() {
        return habitat;
    }
    

    /**
     * Another version of the {@link #invoke(Object, Method, Object[])},
     * but instead of {@link Method} object, it takes the method name and argument types.
     */
    public Object invoke(Object proxy, String methodName, Class[] argTypes, Object[] args) throws Throwable {
        return invoke( proxy, getProxyType().getMethod(methodName, argTypes), args );
    }

    /**
     * Invoke the user defined static method in the nested "Duck" class so that
     * the user can define convenience methods on the config beans.
     */
    Object invokeDuckMethod(Method method, Object proxy, Object[] args) throws Exception {
        Method duckMethod = model.getDuckMethod(method);

        Object[] duckArgs;
        if(args==null) {
            duckArgs = new Object[]{proxy};
        } else {
            duckArgs = new Object[args.length+1];
            duckArgs[0] = proxy;
            System.arraycopy(args,0,duckArgs,1,args.length);
        }

        try {
            return duckMethod.invoke(null,duckArgs);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof Exception)
                throw (Exception) t;
            if (t instanceof Error)
                throw (Error) t;
            throw e;
        }
    }

    protected Object getter(ConfigModel.Property target, Type t) {
        return target.get(this, t);
    }

    protected void setter(ConfigModel.Property target, Object value) throws Exception {
        target.set(this, value);
    }
    
    public static String convertName(String name) {
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
        return buf.toString();        
    }

    /**
     * Used to tokenize the property name into XML name.
     */
    static final Pattern TOKENIZER;
    private static String split(String lookback,String lookahead) {
        return "((?<="+lookback+")(?="+lookahead+"))";
    }
    private static String or(String... tokens) {
        StringBuilder buf = new StringBuilder();
        for (String t : tokens) {
            if(buf.length()>0)  buf.append('|');
            buf.append(t);
        }
        return buf.toString();
    }
    static {
        String pattern = or(
                split("x","X"),     // AbcDef -> Abc|Def
                split("X","Xx"),    // USArmy -> US|Army
                //split("\\D","\\d"), // SSL2 -> SSL|2
                split("\\d","\\D")  // SSL2Connector -> SSL|2|Connector
        );
        pattern = pattern.replace("x","\\p{Lower}").replace("X","\\p{Upper}");
        TOKENIZER = Pattern.compile(pattern);
    }

    static final String[] PROPERTY_PREFIX = new String[]{"get","set","is","has"};

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
    protected Creator createCreator(Class c) {
        return (ConfigBeanProxy.class.isAssignableFrom(c)?new DomProxyCreator(c,metadata(),this):new ConfiguredCreator(super.createCreator(c),this));
        // turning off @Configured and @CagedBy until we get a clear picture on how this should work together.
/*        Womb womb = (ConfigBeanProxy.class.isAssignableFrom(c)?new DomProxyWomb(c,metadata(),this):new ConfiguredWomb(super.createWomb(c),this));
        if (cagedBy==null) {
            return womb;
        } else {
            Class<? extends CageBuilder> value = cagedBy.value();
            CageBuilder builder = habitat.getByType(value);
            return new CagedConfiguredWomb(womb, this, builder);
        }
        */
    }

    public static <T extends Annotation> T digAnnotation(Class<?> target, Class<T> annotationType) {
        return digAnnotation(target, annotationType, new ArrayList<Class<? extends Annotation>>());
    }

    public static <T extends Annotation> T digAnnotation(Class<?> target, Class<T> annotationType, List<Class<? extends Annotation>> visited) {
        T result = target.getAnnotation(annotationType);
        if (result==null) {
            for (Annotation a : target.getAnnotations()) {
                if (!visited.contains(a.annotationType())) {
                    visited.add(a.annotationType());
                    result = digAnnotation(a.annotationType(), annotationType, visited);
                    if (result!=null) {
                        return result;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Used by the parser to set a list of children.
     */
    /*package*/ void setChildren(List<Child> children) {
       this.children = children;
    }

    /**
     * Returns the map of attributes names and values for attributes which
     * value is neither null or the default value. These attributes are
     * considered having a non default value and must be written out.
     *
     * @return map of attributes indexed by name that must be persisted
     */
    private Map<String, String> attributesToWrite() {

        Map<String, String> attributesToWrite = new HashMap<String, String>();
        Map<String, String> localAttr = new HashMap<String, String>(attributes);
        for (Map.Entry<String, String> a : localAttr.entrySet()) {
            ConfigModel.AttributeLeaf am = model.attributes.get(a.getKey());
            String dv = am.getDefaultValue();
            if (dv==null || !dv.equals(a.getValue())) {
                attributesToWrite.put(a.getKey(), a.getValue());
            }
        }
        return attributesToWrite;
    }

    /**
     * Returns true if this element is empty
     * meaning all their attributes have default values and it has
     * no descendants.
     *
     * @return true if the element is empty, false otherwise
     */
    private boolean isEmpty() {
        Map<String, String> attributesToWrite = attributesToWrite();

        if (!attributesToWrite.isEmpty()) {
            return false;
        }

        // if we have children, we are not empty.
        return children.isEmpty();


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
        
        for (Map.Entry<String, String> attributeToWrite : attributesToWrite().entrySet()) {
            w.writeAttribute(attributeToWrite.getKey(), attributeToWrite.getValue());
        }

        List<Child> localChildren = new ArrayList<Child>(children);
        for (Child c : localChildren)
            c.writeTo(w);

        w.writeEndElement();
    }

    protected void injectInto(Dom injectable, Object target) {
        for (Class intf : target.getClass().getInterfaces()) {
            if (ConfigListener.class.isAssignableFrom(intf)) {
                ConfigListener listener = (ConfigListener) target;
                addListener(listener);
                return;
            }
        }
    }

    @Override
    public void release() {
        listeners.clear();
        super.release();
    }

    Set<ConfigListener> listeners = new HashSet<ConfigListener>();

    public void addListener(ConfigListener listener) {
        if (listener==null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners.add(listener);
    }

    public boolean removeListener(ConfigListener listener) {
        return listeners.remove(listener);
    }

    Collection<ConfigListener> getListeners() {
        return listeners;
    }
}
