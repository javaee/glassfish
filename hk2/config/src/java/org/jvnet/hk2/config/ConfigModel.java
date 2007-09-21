package org.jvnet.hk2.config;

import com.sun.hk2.component.Holder;
import com.sun.hk2.component.InhabitantsFile;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;

import javax.management.MBeanInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.ArrayList;

/**
 * Describes the configuration model for a particular class (called "target type" in this class.)
 *
 * TODO: we need to remember if element values are single-valued or multi-valued.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ConfigModel {
    /**
     * Reference to the {@link ConfigInjector} used to inject values to
     * objects of this model.
     */
    public final Inhabitant<? extends ConfigInjector> injector;

    /**
     * Legal attribute names.
     */
    final Map<String,AttributeAction> attributes = new HashMap<String,AttributeAction>();

    /**
     * Legal child element names and how they should be handled
     */
    final Map<String,ElementAction> elements = new HashMap<String,ElementAction>();

    /**
     * Contracts under which the inhabitant should be registered.
     */
    final List<String> contracts;

    /**
     * Type names for which this type creates a symbol space. 
     */
    final Set<String> symoblSpaces;

    /**
     * Deferred reference to the class loader that loaded the injector.
     * This classloader can also load the configurable object.
     */
    public final Holder<ClassLoader> classLoaderHolder = new Holder<ClassLoader>() {
        public ClassLoader get() {
            return injector.get().getClass().getClassLoader();
        }
    };

    /**
     * Fully-qualified name of the target type that this injector works on.
     */
    public final String targetTypeName;

    /**
     * Fully-qualified name under which this type is indexed.
     * This is the class name where the key property is defined.
     *
     * <p>
     * Null if this type is not keyed.
     */
    public final String keyedAs;

    /**
     * If this model has any property that works as a key.
     *
     * @see ConfigMetadata#KEY
     */
    public final String key;

    /**
     * Lazily created {@link MBeanInfo} for this model.
     */
    private volatile MBeanInfo mbeanInfo;

    /**
     * Performs injection to the given object.
     */
    /*package*/ void inject(Dom dom, Object target) {
        try {
            injector.get().inject(dom,target);
        } catch (ConfigurationException e) {
            e.setLocation(dom.getLocation());
            throw e;
        }
    }

    abstract static class ElementAction {
        /**
         * Is multiple values allowed?
         */
        public final boolean collection;

        protected ElementAction(boolean collection) {
            this.collection = collection;
        }

        public abstract boolean isLeaf();
    }
    public static final ElementAction LEAF_SINGLE = new Leaf(false);
    public static final ElementAction LEAF_COLLECTION = new Leaf(true);
    static final class Node extends ElementAction {
        final ConfigModel model;
        public Node(ConfigModel model, boolean collection) {
            super(collection);
            this.model = model;
        }

        public boolean isLeaf() {
            return false;
        }
    }
    static final class Leaf extends ElementAction {
        public Leaf(boolean collection) {
            super(collection);
        }

        public boolean isLeaf() {
            return true;
        }
    }

    static enum AttributeAction { OPTIONAL, REQUIRED }

    /**
     * @param description
     *      The description of the model as written in {@link InhabitantsFile the inhabitants file}.
     */
    public ConfigModel(ConfigParser parent, Inhabitant<? extends ConfigInjector> injector, MultiMap<String,String> description) {
        if(description==null)
            throw new ComponentException("%s doesn't have any metadata",injector.type());

        parent.models.put(injector,this); // register now so that cyclic references are handled correctly.
        this.injector = injector;
        String targetTypeName=null,indexTypeName=null;
        String key = null;
        for (Entry<String, List<String>> e : description.entrySet()) {
            String name = e.getKey();
            String value = e.getValue().size()>0 ? e.getValue().get(0) : null;
            if(name.startsWith("@"))
                attributes.put(name.substring(1),AttributeAction.valueOf(value.toUpperCase()));
            else
            if(name.startsWith("<"))
                elements.put(name.substring(1,name.length()-1),parseValue(parent,value));
            else
            if(name.equals(ConfigMetadata.TARGET))
                targetTypeName = value;
            else
            if(name.equals(ConfigMetadata.KEYED_AS))
                indexTypeName = value;
            else
            if(name.equals(ConfigMetadata.KEY))
                key = value;
        }
        if(targetTypeName==null)
            throw new ComponentException("%s doesn't have the mandatory '%s' metadata", injector.type(), ConfigMetadata.TARGET);
        if(key==null ^ indexTypeName==null)
            throw new ComponentException("%s has inconsistent '%s=%s' and '%s=%s' metadata",
                ConfigMetadata.KEY, key, ConfigMetadata.TARGET, indexTypeName);
        this.targetTypeName = targetTypeName;
        this.keyedAs = indexTypeName;
        this.key = key;
        this.contracts = description.get(ConfigMetadata.TARGET_CONTRACTS);
        this.symoblSpaces = new HashSet<String>(description.get("symbolSpaces"));
    }

    /**
     * Parses {@link ElementAction} object from a value in the metadata description.
     */
    private ElementAction parseValue(ConfigParser parent, String value) {
        boolean collection = false;
        if(value.startsWith("collection:")) {
            collection = true;
            value = value.substring(11);
        }

        if(value.equals("leaf")) {
            if(collection)  return LEAF_COLLECTION;
            else            return LEAF_SINGLE;
        }

        // this element is a reference to another configured inhabitant.
        // figure that out.
        Inhabitant i = parent.habitat.getInhabitantByAnnotation(InjectionTarget.class, value);
        if(i==null)
            throw new ComponentException(
                "%s is referenced from %s but its ConfigInjector is not found",value,injector.typeName());

        return new Node(parent.buildModel(i),collection);
    }

    public MBeanInfo getMBeanInfo() {
        if(mbeanInfo==null)
            // worst case scenario is we end up building redundant MBeanInfo if two threads execute this concurrently.
            // It's a waste, but it's not incorrect.
            mbeanInfo = buildMBeanInfo();
        return mbeanInfo;
    }

    private MBeanInfo buildMBeanInfo() {
        List<MBeanAttributeInfo> properties = new ArrayList<MBeanAttributeInfo>();
        List<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>();

        for (Map.Entry<String,AttributeAction> a : attributes.entrySet()) {
            properties.add(new MBeanAttributeInfo(a.getKey(),
                String.class.getName(),
                a.getKey(), // TODO: fetch from javadoc
                true, true, false));
        }
        // TODO: fetch element properties
        // TODO: fetch operations

        return new MBeanInfo(targetTypeName,
            targetTypeName, // TODO: capture from javadoc
            properties.toArray(new MBeanAttributeInfo[properties.size()]),
            EMPTY_MBEAN_CONSTRUCTORS,
            operations.toArray(new MBeanOperationInfo[operations.size()]),
            EMPTY_MBEAN_NOTIFICATIONS);
    }

    private static final MBeanNotificationInfo[] EMPTY_MBEAN_NOTIFICATIONS = new MBeanNotificationInfo[0];
    private static final MBeanConstructorInfo[] EMPTY_MBEAN_CONSTRUCTORS = new MBeanConstructorInfo[0];
}
