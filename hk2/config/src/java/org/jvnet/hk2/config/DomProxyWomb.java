package org.jvnet.hk2.config;

import com.sun.hk2.component.AbstractWombImpl;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.Womb;

/**
 * {@link Womb} that returns a typed proxy to {@link Dom}.
 *
 * @author Kohsuke Kawaguchi
 */
final class DomProxyWomb<T extends ConfigBeanProxy> extends AbstractWombImpl<T> {
    private final Dom dom;

    public DomProxyWomb(Class<T> type, MultiMap<String, String> metadata, Dom dom) {
        super(type, metadata);
        this.dom = dom;
    }

    public T create() throws ComponentException {
        return dom.createProxy(type());
    }
}

