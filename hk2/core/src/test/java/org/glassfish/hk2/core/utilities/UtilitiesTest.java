package org.glassfish.hk2.core.utilities;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.Test;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Singleton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Utilities tests.
 *
 * @author tbeerbower
 */
public class UtilitiesTest {

    @Test
    public void testAddIndex() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("testAddDescriptor");
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        ActiveDescriptor<MyService> descriptor =
                (ActiveDescriptor<MyService>)config.bind(BuilderHelper.link(MyService.class).
                        to(MyInterface1.class).in(Singleton.class.getName()).build());

        config.commit();

        MyService s1 = locator.getService(MyService.class);
        assertNotNull(s1);

        assertEquals(s1, locator.getService(MyInterface1.class));

        Utilities.addIndex(locator, descriptor, MyInterface2.class, "foo");
        Utilities.addIndex(locator, descriptor, MyInterface3.class, "bar");

        MyInterface2 s2 = locator.getService(MyInterface2.class, "foo");

        assertEquals(s1, s2);

        MyInterface3 s3 = locator.getService(MyInterface3.class, "bar");

        assertEquals(s1, s3);

        assertNull(locator.getService(MyInterface1.class, "foo"));
        assertNull(locator.getService(MyInterface1.class, "bar"));

        assertNull(locator.getService(MyInterface2.class, ""));
        assertNull(locator.getService(MyInterface2.class, "bar"));

        assertNull(locator.getService(MyInterface3.class, ""));
        assertNull(locator.getService(MyInterface3.class, "foo"));
    }

    public interface MyInterface1 {
        public void doSomething();
    }

    public interface MyInterface2 {
        public void doSomethingElse();
    }

    public interface MyInterface3 {
        public void doSomethingCompletelyDifferent();
    }

    @Service
    public static class MyService implements MyInterface1, MyInterface2, MyInterface3{
        @Override
        public void doSomething() {
        }

        @Override
        public void doSomethingElse() {
        }

        @Override
        public void doSomethingCompletelyDifferent() {
        }
    }
}
