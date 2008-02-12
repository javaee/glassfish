package org.jvnet.hk2.component;

import org.jvnet.hk2.component.Inhabitants;
import org.jvnet.hk2.component.Inhabitant;

import javax.management.ObjectName;

/**
 * @author Kohsuke Kawaguchi
 */
public interface ObjectNameBuilder {
    ObjectName getObejctName(Inhabitant<?> i);
}
