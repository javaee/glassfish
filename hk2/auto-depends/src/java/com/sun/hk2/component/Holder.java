package com.sun.hk2.component;

/**
 * Indirection to a value. That is, instead of referring to the value itself,
 * this class allows you to obtain the value when you need it.
 *
 * <p>
 * This is the basis for all the lazy computation.
 *
 *
 *
 * @author Kohsuke Kawaguchi
 */
public interface Holder<T> {
    T get();

    /**
     * {@link Holder} implementation that doesn't do any deferred computation,
     * where the value is given in the constructor.
     */
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
