package com.sun.hk2.component;

/**
 * @author Kohsuke Kawaguchi
 */
public interface Holder<T> {
    T get();

    public static final class Impl<T> implements Holder<T> {
        private final T t;

        public Impl(T t) {
            this.t = t;
        }

        public T get() {
            return t;
        }
    }
}
