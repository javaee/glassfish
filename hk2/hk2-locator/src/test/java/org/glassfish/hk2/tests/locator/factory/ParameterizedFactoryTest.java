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

        FooFactory factory
                = locator.getService((new TypeLiteral<Factory<Foo>>() {}).getType());

        Assert.assertNotNull(factory);

        Foo foo = factory.provide();

        Assert.assertNotNull(foo);

        FooInjectee injectee = locator.getService(FooInjectee.class);

        Assert.assertNotNull(injectee);

        Assert.assertTrue(injectee.getInjected() instanceof FooImpl);

        Factory<Foo> fooFactory = injectee.getFactory();
        Assert.assertNotNull(fooFactory);

    }

    public static class Module implements TestModule {

        /* (non-Javadoc)
        * @see org.glassfish.hk2.api.Module#configure(org.glassfish.hk2.api.Configuration)
        */
        @Override
        public void configure(DynamicConfiguration configurator) {
            configurator.bind(BuilderHelper.link(FooFactory.class).to(Foo.class).buildFactory());
            configurator.bind(BuilderHelper.link(FooInjectee.class).in(Singleton.class).build());
        }
    }

    public static interface Foo {
    }

    public static class FooImpl implements Foo {
    }

    public static abstract class AbstractFactory<T> extends EmptyClass1 implements Factory<T> {
        @Override
        public void dispose(T instance) {
        }
    }

    public static class FooFactory extends AbstractFactory<Foo> implements EmptyInterface3<Foo, String, Boolean>{
        @Override
        public Foo provide() {
            return new FooImpl();
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
    public static class FooInjectee {

        @Inject
        Foo injected;

        @Inject
        Factory<Foo> factory;

        public Foo getInjected() {
            return injected;
        }

        public Factory<Foo> getFactory() {
            return factory;
        }
    }
}
