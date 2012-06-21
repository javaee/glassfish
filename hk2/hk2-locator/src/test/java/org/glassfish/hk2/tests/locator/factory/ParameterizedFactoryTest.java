package org.glassfish.hk2.tests.locator.factory;

import junit.framework.Assert;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.tests.locator.utilities.TestModule;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Test for complex parameterized type factories.
 */
public class ParameterizedFactoryTest {

    @Test
    public void testParameterizedFactory() {
        ServiceLocator locator = LocatorHelper.create("testParameterizedFactory", new Module());

        FooFactory fooFactory
                = locator.getService((new TypeLiteral<Factory<Foo>>() {}).getType());
        Assert.assertNotNull(fooFactory);

        Foo foo = fooFactory.provide();
        Assert.assertNotNull(foo);

        foo = locator.getService(Foo.class);
        Assert.assertNotNull(foo);

        // -----

        BarFactory barfactory
                = locator.getService((new TypeLiteral<Factory<Bar>>() {}).getType());
        Assert.assertNotNull(barfactory);

        Bar bar = barfactory.provide();
        Assert.assertNotNull(bar);

        bar = locator.getService(Bar.class);
        Assert.assertNotNull(bar);

        // -----

        Injectee injectee = locator.getService(Injectee.class);
        Assert.assertNotNull(injectee);
        Assert.assertTrue(injectee.getInjectedFoo() instanceof FooImpl);
        Assert.assertTrue(injectee.getInjectedBar() instanceof BarImpl);
    }

    public static class Module implements TestModule {

        /* (non-Javadoc)
        * @see org.glassfish.hk2.api.Module#configure(org.glassfish.hk2.api.Configuration)
        */
        @Override
        public void configure(DynamicConfiguration configurator) {
            configurator.bind(BuilderHelper.link(FooFactory.class).to(Foo.class).buildFactory());
            configurator.bind(BuilderHelper.link(BarFactory.class).to(Bar.class).buildFactory());
            configurator.bind(BuilderHelper.link(Injectee.class).in(Singleton.class).build());
        }
    }

    public static interface Foo {
    }

    public static class FooImpl implements Foo {
    }


    public static interface Bar {
    }

    public static class BarImpl implements Bar {
    }


    public static abstract class AbstractFactory<T> extends EmptyClass1 implements Factory<T> {
        @Override
        public void dispose(T instance) {
        }
    }

    public static abstract class AbstractFactory2<T> extends AbstractFactory<T> {
        @Override
        public T provide() {
            return get();
        }

        public abstract T get();
    }

    public static class FooFactory extends AbstractFactory<Foo> implements EmptyInterface3<Foo, String, Boolean>{
        @Override
        public Foo provide() {
            return new FooImpl();
        }
    }

    public static class BarFactory extends AbstractFactory2<Bar> {
        @Override
        public Bar get() {
            return new BarImpl();
        }
    }

    public static interface EmptyInterface1<T> {
    }

    public static interface EmptyInterface2<T, U> {
    }

    public static interface EmptyInterface3<T, U, V> {
    }

    public static class EmptyClass1 extends EmptyClass2<Integer> {
    }

    public static class EmptyClass2<T> implements EmptyInterface2<String, T> {
    }

    @Singleton
    public static class Injectee {
        @Inject
        Foo injectedFoo;

        @Inject
        Bar injectedBar;

        public Foo getInjectedFoo() {
            return injectedFoo;
        }

        public Bar getInjectedBar() {
            return injectedBar;
        }
    }
}
