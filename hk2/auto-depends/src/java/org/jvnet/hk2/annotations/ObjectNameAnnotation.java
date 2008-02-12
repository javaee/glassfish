package org.jvnet.hk2.annotations;

import org.jvnet.hk2.component.ObjectNameBuilder;

/**
 * @author Kohsuke Kawaguchi
 */
public @interface ObjectNameAnnotation {
    Class<? extends ObjectNameBuilder> value();
}
