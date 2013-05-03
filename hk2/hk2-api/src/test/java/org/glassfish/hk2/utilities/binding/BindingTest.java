package org.glassfish.hk2.utilities.binding;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.glassfish.hk2.api.*;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Test;

import javax.inject.Singleton;

import static org.easymock.EasyMock.*;
import static org.glassfish.hk2.utilities.binding.BindingBuilderFactory.newBinder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BindingTest {
    private final static String MY_CUSTOM_ANALYZER = "MyCustomAnalyzer";

    @Test
    public void testBindingBuilderFactory () {
        ServiceBindingBuilder<Foo> binderFactory = newBinder(Foo.class);

        HK2Loader hk2Loader = new HK2Loader() {
            @Override
            public Class<?> loadClass(String className) throws MultiException {
                try {
                    return getClass().getClassLoader().loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new MultiException(e);
                }

        }};

        ScopedNamedBindingBuilder<Foo> bindingBuilder = binderFactory.
                in(Singleton.class).
                loadedBy(hk2Loader).
                named("foo").
                withMetadata("foo", "bar").
                to(MyContract.class).
                analyzeWith(MY_CUSTOM_ANALYZER);

        DynamicConfiguration dc = EasyMock.createMock(DynamicConfiguration.class);

        DescriptorImpl expectedDescriptor = new DescriptorImpl();
        expectedDescriptor.setImplementation("org.glassfish.hk2.utilities.binding.BindingTest$Foo");
        expectedDescriptor.setScope(Singleton.class.getName());
        expectedDescriptor.setLoader(hk2Loader);
        expectedDescriptor.setName("foo");
        expectedDescriptor.addMetadata("foo", "bar");
        expectedDescriptor.addQualifier("javax.inject.Named");
        expectedDescriptor.addAdvertisedContract("org.glassfish.hk2.utilities.binding.BindingTest$MyContract");
        expectedDescriptor.setClassAnalysisName(MY_CUSTOM_ANALYZER);

        EasyMock.expect(dc.bind(expectedDescriptor, false)).andReturn(null);

        EasyMock.replay(dc);
        BindingBuilderFactory.addBinding(bindingBuilder, dc);

        EasyMock.verify(dc);

    }

    @Test
    public void testAbstractBinder() {
        Binder b = new AbstractBinder() {

            @Override
            protected void configure() {
                bind(new Foo()).to(MyContract.class);
            }
        };

      ServiceLocator sl = createMock(ServiceLocator.class);
      DynamicConfigurationService dcs = createMock(DynamicConfigurationService.class);
      DynamicConfiguration dc = createMock(DynamicConfiguration.class);

      // Find DynamicConfigurationService and create a DynamicConfiguration
      expect(sl.getService(DynamicConfigurationService.class)).andReturn(dcs);
      expect(dcs.createDynamicConfiguration()).andReturn(dc);

      // expect a descriptor to be bound, capture it so the fields can be checked later
      Capture<Descriptor> capturedDescriptor = new Capture<Descriptor>();

      expect(dc.bind(capture(capturedDescriptor), eq(false))).andReturn(null);

      dc.commit();
      expectLastCall();

      EasyMock.replay(sl,dcs,dc);
      ServiceLocatorUtilities.bind(sl, b);

      verify(sl, dcs, dc);

      assertEquals("Wrong implementation", "org.glassfish.hk2.utilities.binding.BindingTest$Foo", capturedDescriptor.getValue().getImplementation());
      assertTrue( "Missing contract", capturedDescriptor.getValue().getAdvertisedContracts().contains("org.glassfish.hk2.utilities.binding.BindingTest$MyContract"));

    }

    class Foo implements MyContract{

    }


    public interface MyContract {}
}