package org.jvnet.hk2.config;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Womb;
import org.jvnet.hk2.component.MultiMap;

/**
 * {@link Womb} decorator that uses {@link ConfigInjector} to set values to objects
 * that are created.
 *
 * @author Kohsuke Kawaguchi
 */
class ConfiguredWomb<T> implements Womb<T> {
    private final Womb<T> core;
    private final Dom dom;

    public ConfiguredWomb(Womb<T> core, Dom dom) {
        this.core = core;
        this.dom = dom;
    }

    public String typeName() {
        return core.typeName();
    }

    public Class<T> type() {
        return core.type();
    }

    public T get() {
        T t = create();
        initialize(t);
        return t;
    }

    public T create() throws ComponentException {
        return core.create();
    }

    public void initialize(T t) throws ComponentException {
        injectConfig(t);
        core.initialize(t);
    }

    private void injectConfig(T t) {
        dom.inject(t);
    }

    public MultiMap<String, String> metadata() {
        return core.metadata();
    }

    public void release() {
        core.release();
    }
}
