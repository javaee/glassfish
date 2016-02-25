/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.hk2.utilities.binding;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.glassfish.hk2.api.*;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.FactoryDescriptorsImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Test;

import javax.inject.Singleton;

import static org.easymock.EasyMock.*;
import static org.glassfish.hk2.utilities.binding.BindingBuilderFactory.newBinder;
import static org.glassfish.hk2.utilities.binding.BindingBuilderFactory.newFactoryBinder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    
    /**
     * Ensures that the provide method and the service both get the qualifiers
     */
    @Test
    public void testBindingBuilderFactoryPutsQualifiesOnBothProvideMethodAndService() {
        final Fantastic fantasticAnnotationLiteral = new FantasticLiteral(4);
        BindingBuilder<Widget> bb = newFactoryBinder(WidgetFactory.class, Singleton.class)
            .to(Widget.class)
            .in(PerLookup.class)
            .qualifiedBy(fantasticAnnotationLiteral);
        assertNotNull(bb);
        
        final DynamicConfiguration dc = createMock(DynamicConfiguration.class);

        final DescriptorImpl descriptorForFactoryAsAService = new DescriptorImpl();
        descriptorForFactoryAsAService.setImplementation(WidgetFactory.class.getName());
        descriptorForFactoryAsAService.setScope(Singleton.class.getName());
        descriptorForFactoryAsAService.addAdvertisedContract(Factory.class.getName());
        descriptorForFactoryAsAService.addQualifier(Fantastic.class.getName());

        final DescriptorImpl descriptorForFactoryAsProvideMethod = new DescriptorImpl();
        descriptorForFactoryAsProvideMethod.setImplementation(WidgetFactory.class.getName());
        descriptorForFactoryAsProvideMethod.setScope(PerLookup.class.getName());
        descriptorForFactoryAsProvideMethod.setDescriptorType(DescriptorType.PROVIDE_METHOD);
        descriptorForFactoryAsProvideMethod.addAdvertisedContract(Widget.class.getName());
        descriptorForFactoryAsProvideMethod.addQualifier(Fantastic.class.getName());

        final FactoryDescriptorsImpl factoryDescriptors = new FactoryDescriptorsImpl(descriptorForFactoryAsAService, descriptorForFactoryAsProvideMethod);

        expect(dc.bind(factoryDescriptors)).andReturn(null);
        replay(dc);
        
        BindingBuilderFactory.addBinding(bb, dc);

        verify(dc);
    }
    
    /**
     * Ensures that the provide method and the service with metadata and
     * onlye the provide method gets the metadata
     */
    @Test
    public void testBindingBuilderFactoryWithMetadata() {
        final Fantastic fantasticAnnotationLiteral = new FantasticLiteral(4);
        BindingBuilder<Widget> bb = newFactoryBinder(WidgetFactory.class, Singleton.class)
            .to(Widget.class)
            .in(PerLookup.class)
            .withMetadata("key", "value");
        assertNotNull(bb);
        
        final DynamicConfiguration dc = createMock(DynamicConfiguration.class);

        final DescriptorImpl descriptorForFactoryAsAService = new DescriptorImpl();
        descriptorForFactoryAsAService.setImplementation(WidgetFactory.class.getName());
        descriptorForFactoryAsAService.setScope(Singleton.class.getName());
        descriptorForFactoryAsAService.addAdvertisedContract(Factory.class.getName());
        descriptorForFactoryAsAService.addMetadata("key", "value");

        final DescriptorImpl descriptorForFactoryAsProvideMethod = new DescriptorImpl();
        descriptorForFactoryAsProvideMethod.setImplementation(WidgetFactory.class.getName());
        descriptorForFactoryAsProvideMethod.setScope(PerLookup.class.getName());
        descriptorForFactoryAsProvideMethod.setDescriptorType(DescriptorType.PROVIDE_METHOD);
        descriptorForFactoryAsProvideMethod.addAdvertisedContract(Widget.class.getName());
        descriptorForFactoryAsProvideMethod.addMetadata("key", "value");

        final FactoryDescriptorsImpl factoryDescriptors = new FactoryDescriptorsImpl(descriptorForFactoryAsAService, descriptorForFactoryAsProvideMethod);

        expect(dc.bind(factoryDescriptors)).andReturn(null);
        replay(dc);
        
        BindingBuilderFactory.addBinding(bb, dc);

        verify(dc);
    }
    
    /**
     * Makes sure this fails out with an NPE
     */
    @Test(expected=NullPointerException.class)
    public void testNullPassedToCreateWithInstanceFails() {
        AbstractBindingBuilder.create(null);
    }

    class Foo implements MyContract{

    }


    public interface MyContract {}
    
    private static final class FantasticLiteral extends AnnotationLiteral<Fantastic> implements Fantastic {

        private final int level;

        private FantasticLiteral(final int level) {
            super();
            this.level = level;
        }

        @Override
        public int level() {
            return this.level;
        }
    }
}
