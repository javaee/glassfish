package org.glassfish.hk2.core.utilities;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.NamedImpl;
import org.junit.Test;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Singleton;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the AliasDescriptor.
 *
 * @author tbeerbower
 */
public class AliasDescriptorTest {

    @Test
    public void testAddAliasDescriptors() throws Exception {
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

        config = dcs.createDynamicConfiguration();
        config.addActiveDescriptor(new AliasDescriptor<MyService>(locator, descriptor, MyInterface2.class, "foo"));
        config.addActiveDescriptor(new AliasDescriptor<MyService>(locator, descriptor, MyInterface3.class, "bar"));
        config.commit();

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

    @Test
    public void testIsReified() throws Exception {
        AliasDescriptor<MyService> aliasDescriptor = getAliasDescriptor();

        assertTrue(aliasDescriptor.isReified());
    }

    @Test
    public void testGetImplementation() throws Exception {
        AliasDescriptor<MyService> aliasDescriptor = getAliasDescriptor();

        assertEquals(MyService.class.getName(), aliasDescriptor.getImplementation());
    }

    @Test
    public void testGetContractTypes() throws Exception {
        AliasDescriptor<MyService> aliasDescriptor = getAliasDescriptor();

        final Set<Type> contractTypes = aliasDescriptor.getContractTypes();

        assertEquals(1, contractTypes.size());
        assertEquals(MyInterface2.class, contractTypes.iterator().next());
    }

    @Test
    public void testGetScopeAnnotation() throws Exception {
        AliasDescriptor<MyService> aliasDescriptor = getAliasDescriptor();

        assertEquals(Singleton.class, aliasDescriptor.getScopeAnnotation());
    }

    @Test
    public void testGetQualifierAnnotations() throws Exception {
        AliasDescriptor<MyService> aliasDescriptor = getAliasDescriptor();

        final Set<Annotation> qualifierAnnotations = aliasDescriptor.getQualifierAnnotations();

        assertEquals(1, qualifierAnnotations.size());
        Annotation annotation = qualifierAnnotations.iterator().next();
        assertTrue(annotation instanceof NamedImpl);
        assertEquals("foo", ((NamedImpl)annotation).value());
    }

    @Test
    public void testGetImplementationClass() throws Exception {
        AliasDescriptor<MyService> aliasDescriptor = getAliasDescriptor();

        ActiveDescriptor<MyService> descriptor = aliasDescriptor.getDescriptor();
        assertFalse(descriptor.isReified());

        assertEquals(MyService.class, aliasDescriptor.getImplementationClass());

        assertTrue(descriptor.isReified());
    }

    @Test
    public void testCreate() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("testAddDescriptor");
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        ActiveDescriptor<MyService> descriptor =
                (ActiveDescriptor<MyService>)config.bind(BuilderHelper.link(MyService.class).
                        to(MyInterface1.class).in(Singleton.class.getName()).build());

        AliasDescriptor<MyService> aliasDescriptor =
                new AliasDescriptor<MyService>(locator, descriptor, MyInterface2.class, "foo");

        config = dcs.createDynamicConfiguration();
        config.addActiveDescriptor(aliasDescriptor);
        config.commit();

        MyService s1 = locator.getService(descriptor, null);
        MyService s2 = aliasDescriptor.create(null);
        assertSame(s1, s2);
    }

    // ----- Utility methods ------------------------------------------------

    private AliasDescriptor<MyService> getAliasDescriptor() {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("testAddDescriptor");
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        ActiveDescriptor<MyService> descriptor =
                (ActiveDescriptor<MyService>)config.bind(BuilderHelper.link(MyService.class).
                        to(MyInterface1.class).in(Singleton.class.getName()).build());

        config.commit();

        return new AliasDescriptor<MyService>(locator, descriptor, MyInterface2.class, "foo");
    }


    // ----- inner classes --------------------------------------------------

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
