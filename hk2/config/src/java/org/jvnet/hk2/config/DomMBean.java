package org.jvnet.hk2.config;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

/**
 * Exposes {@link Dom} as a dynamic "config" mbean, which exposes
 * the configuration values over JMX.
 *
 * <p>
 * Config mbeans work at pre-variable-expansion level.
 *
 * TODO: let component define additional properties and operations.
 * TODO: how to assign ObjectNames?
 *
 * @author Kohsuke Kawaguchi
 */
final class DomMBean extends AbstractDynamicMBeanImpl {
    private final Dom dom;

    DomMBean(Dom dom) {
        this.dom = dom;
    }

    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        ConfigModel model = dom.model;

        if(model.attributes.containsKey(attribute))
            return dom.rawAttribute(attribute);

        ConfigModel.Property element = model.elements.get(attribute);
        if(element==null)
            throw new AttributeNotFoundException(attribute);

        if(element.isLeaf())
            return dom.rawLeafElements(attribute);

        // otherwise reference
        throw new UnsupportedOperationException();
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        ConfigModel model = dom.model;
        String name = attribute.getName();
        Object value = attribute.getValue();

        if(model.attributes.containsKey(name)) {
            dom.attribute(name, cast(String.class,value));
            return;
        }

        ConfigModel.Property element = model.elements.get(attribute);
        if(element==null)
            throw new AttributeNotFoundException(name);

        if(element.isLeaf()) {
            if(element.isCollection())
                dom.setLeafElements(name,cast(String.class,value));
            else
                dom.setLeafElements(name,cast(String[].class,value));
            return;
        }

        // otherwise reference
        // TODO: how do you do these?
        throw new UnsupportedOperationException();
    }

    /**
     * Makes sure that the value is of the expected type. If not, throw {@link InvalidAttributeValueException}.
     */
    private <T> T cast(Class<T> type, Object value) throws InvalidAttributeValueException {
        if(type.isInstance(value))
                return type.cast(value);
        throw new InvalidAttributeValueException(String.format("%s was expected but found %s", type, value.getClass()));
    }

    public Object invoke(String actionName, Object params[], String signature[]) throws MBeanException, ReflectionException {
        throw new UnsupportedOperationException();
    }

    public MBeanInfo getMBeanInfo() {
        return dom.model.getMBeanInfo();
    }
}
