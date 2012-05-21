package org.jvnet.hk2.osgiadapter;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.easymock.Capture;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;

public class ServiceLocatorActivatorTest {



	@Test
	public void testStartAndStop() throws Exception {
		BundleContext bc = createMock(BundleContext.class);

		ServiceLocatorActivator serviceLocatorActivator = new ServiceLocatorActivator(
				bc);

		// assert that a ServiceLocator gets bound in the osgi service registry
		ServiceRegistration serviceLocatorRegistration = createMock(ServiceRegistration.class);
		Capture<ServiceLocator> serviceLocatorCapture = new Capture<ServiceLocator>();
		expect(
				bc.registerService(eq(ServiceLocator.class.getCanonicalName()),
						capture(serviceLocatorCapture),
						(Dictionary) anyObject())).andReturn(
				serviceLocatorRegistration);

		// assert that ServiceLocatorActivator is added as a BundleListener
		bc.addBundleListener(serviceLocatorActivator); // expect...

		replay(bc);

		serviceLocatorActivator.start(bc);

		ServiceLocator serviceLocator = serviceLocatorCapture.getValue();

		verify(bc);

		// stop... Make sure ModuleStartup.stop is called

		assertNotNull(serviceLocator);

		DynamicConfigurationService dcs = serviceLocator
				.getService(DynamicConfigurationService.class);
		DynamicConfiguration config = dcs.createDynamicConfiguration();

		final AtomicBoolean stopWasCalled = new AtomicBoolean(false);

		ModuleStartup tms = new ModuleStartup() {

			@Override
			public void stop() {
				stopWasCalled.set(true);
			}

			@Override
			public void start() {

			}

			@Override
			public void setStartupContext(StartupContext context) {
			}
		};

		config.addActiveDescriptor(BuilderHelper.createConstantDescriptor(tms));

		config.commit();

		serviceLocatorRegistration.unregister(); // expect...

		replay(serviceLocatorRegistration);

		assertFalse(stopWasCalled.get()); // sanity
		serviceLocatorActivator.stop(bc);

		assertTrue(stopWasCalled.get());
		verify(bc, serviceLocatorRegistration);

		// assert servicelocator was destroyed
		try {
			serviceLocator.getAllServices(ModuleStartup.class);
			fail("ServiceLocator was not destroyed");
		} catch (IllegalStateException ise) {
			// expected...
		}

	}

	@Test
	public void testBundleChanged() throws Exception {

		// Test that a bundle transitioning to start with a descriptor file
		// populates ServiceLocator...

		// start the activator to initialize the ServiceLocator...
		BundleContext bc = createMock(BundleContext.class);

		ServiceLocatorActivator serviceLocatorActivator = new ServiceLocatorActivator(
				bc);

		ServiceRegistration serviceLocatorRegistration = createMock(ServiceRegistration.class);
		
		Capture<ServiceLocator> serviceLocatorCapture = new Capture<ServiceLocator>();
		expect(
				bc.registerService(eq(ServiceLocator.class.getCanonicalName()),
						capture(serviceLocatorCapture),
						(Dictionary) anyObject())).andReturn(serviceLocatorRegistration);

		bc.addBundleListener(serviceLocatorActivator); // expect...

		replay(bc);

		serviceLocatorActivator.start(bc);

		ServiceLocator serviceLocator = serviceLocatorCapture.getValue();

		verify(bc);

		final URL url = createTestDescriptorFile();

		Bundle bundle = new FakeBundleToReturnDescriptorResource(url);

		BundleEvent event = new BundleEvent(BundleEvent.STARTED, bundle);

		ActiveDescriptor des = serviceLocator.getBestDescriptor(BuilderHelper
				.createContractFilter(ModuleStartup.class.getCanonicalName()));

		assertNull(des);
		
		serviceLocatorActivator.bundleChanged(event);

	    des = serviceLocator.getBestDescriptor(BuilderHelper
				.createContractFilter(ModuleStartup.class.getCanonicalName()));

		assertNotNull(des);
		assertEquals("TestModuleStartup",
				des.getImplementation());
	}

	private URL createTestDescriptorFile() throws FileNotFoundException,
			IOException {
		DescriptorImpl descriptor = new DescriptorImpl();

		descriptor
				.setImplementation("TestModuleStartup");
		descriptor
				.addAdvertisedContract(ModuleStartup.class.getCanonicalName());

		File f = File.createTempFile("sfsdf", "dfsdfds");
		f.deleteOnExit();

		PrintWriter pw = new PrintWriter(f);

		descriptor.writeObject(pw);

		pw.close();

		return f.toURL();
	}

	private final class FakeBundleToReturnDescriptorResource implements Bundle {
		private final URL url;

		private FakeBundleToReturnDescriptorResource(URL url) {
			this.url = url;
		}

		@Override
		public int getState() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void start(int options) throws BundleException {
			// TODO Auto-generated method stub

		}

		@Override
		public void start() throws BundleException {
			// TODO Auto-generated method stub

		}

		@Override
		public void stop(int options) throws BundleException {
			// TODO Auto-generated method stub

		}

		@Override
		public void stop() throws BundleException {
			// TODO Auto-generated method stub

		}

		@Override
		public void update(InputStream input) throws BundleException {
			// TODO Auto-generated method stub

		}

		@Override
		public void update() throws BundleException {
			// TODO Auto-generated method stub

		}

		@Override
		public void uninstall() throws BundleException {
			// TODO Auto-generated method stub

		}

		@Override
		public Dictionary getHeaders() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getBundleId() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getLocation() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ServiceReference[] getRegisteredServices() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ServiceReference[] getServicesInUse() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean hasPermission(Object permission) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public URL getResource(String name) {
			return url;
		}

		@Override
		public Dictionary getHeaders(String locale) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getSymbolicName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Class loadClass(String name) throws ClassNotFoundException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Enumeration getResources(String name) throws IOException {
			return null;
		}

		@Override
		public Enumeration getEntryPaths(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public URL getEntry(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getLastModified() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Enumeration findEntries(String path, String filePattern,
				boolean recurse) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BundleContext getBundleContext() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map getSignerCertificates(int signersType) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Version getVersion() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
