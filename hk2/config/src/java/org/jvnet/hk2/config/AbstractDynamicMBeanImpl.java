package org.jvnet.hk2.config;

import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.Attribute;
import javax.management.InvalidAttributeValueException;
import javax.management.DynamicMBean;
import java.util.List;

/**
 * Partial implementation of {@link DynamicMBean}.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractDynamicMBeanImpl implements DynamicMBean {
    public final AttributeList getAttributes(String[] attributes) {
        AttributeList r = new AttributeList(attributes.length);
        for (String name : attributes) {
            Object value = null;
            try {
                value = getAttribute(name);
            } catch (AttributeNotFoundException e) {
                // error is reported as the lack of value
            } catch (MBeanException e) {
                // error is reported as the lack of value
            } catch (ReflectionException e) {
                // error is reported as the lack of value
            }
            r.add(new Attribute(name, value));
        }
        return r;
    }

    public final AttributeList setAttributes(AttributeList attributes) {
        AttributeList r = new AttributeList(attributes.size());
        for (Attribute a : (List<Attribute>)attributes) {
            try {
                setAttribute(a);
                r.add(a);
            } catch (AttributeNotFoundException e) {
                // error is silently ignored
            } catch (ReflectionException e) {
                // error is silently ignored
            } catch (MBeanException e) {
                // error is silently ignored
            } catch (InvalidAttributeValueException e) {
                // error is silently ignored
            }
        }
        return r;
    }
}
