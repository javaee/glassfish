package org.glassfish.hk2.bootstrap.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.common_impl.AbstractFactory;
import com.sun.enterprise.module.impl.HK2Factory;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.bootstrap.DescriptorFileFinder;
import org.glassfish.hk2.bootstrap.HK2Populator;
import org.glassfish.hk2.bootstrap.PopulatorPostProcessor;
import org.glassfish.hk2.inhabitants.InhabitantsParser;
import org.glassfish.hk2.inhabitants.InhabitantsScanner;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.StartupContext;

public class ServiceLocatorTest {

	static File testFile;

	static final String text = "# generated on 2 Apr 2012 18:04:09 GMT\n"
			+ "class=com.sun.enterprise.admin.cli.optional.RestoreDomainCommand,index=com.sun.enterprise.admin.cli.CLICommand:restore-domain\n"
			+ "class=com.sun.enterprise.admin.cli.optional.ListBackupsCommand,index=com.sun.enterprise.admin.cli.CLICommand:list-backups\n";

	@BeforeClass
	public static void createTestInhabitantsFile() throws Exception {
		testFile = File.createTempFile("aaaa", "bbbb");
		testFile.deleteOnExit();
		BufferedWriter output = null;
		try {
			output = new BufferedWriter(new FileWriter(testFile));
			output.write(text);
		} finally {
			output.close();
		}
        HK2Factory.initialize();
	}

	@AfterClass
	public static void deleteTestInhabitantsFile() throws Exception {
		testFile.delete();
	}

	@Test
	public void testInhabitantsScanner() throws Exception {

		HK2Loader loader = new HK2Loader() {

			@Override
			public Class<?> loadClass(String className) throws MultiException {
				try {
					return getClass().getClassLoader().loadClass(className);
				} catch (ClassNotFoundException cnfe) {
					throw new MultiException(cnfe);
				}
			}

		};

		final URL resource = testFile.toURL();

		assertNotNull(resource);

		final String SERVICE_LOCATOR_NAME = getClass().getCanonicalName()
				+ "_SERVICELOCATOR";
		ServiceLocator sl = ServiceLocatorFactory.getInstance().create(
				SERVICE_LOCATOR_NAME);

		final InhabitantsScanner scanner;

		scanner = new InhabitantsScanner(resource.openConnection()
				.getInputStream(), SERVICE_LOCATOR_NAME);

		final InhabitantsParser inhabitantsParser = new InhabitantsParser(sl);

		inhabitantsParser.parse(scanner, loader);

		List<ActiveDescriptor<?>> ds = sl.getDescriptors(BuilderHelper
				.createNameAndContractFilter(
						"com.sun.enterprise.admin.cli.CLICommand",
						"restore-domain"));

		assertNotNull(ds);
		assertEquals("Expecting one restore-domain descriptor", 1, ds.size());
		for (ActiveDescriptor<?> d : ds) {

			assertEquals(
					"com.sun.enterprise.admin.cli.optional.RestoreDomainCommand",
					d.getImplementation());
			assertEquals("restore-domain", d.getName());

			Set<String> contracts = d.getAdvertisedContracts();
			assertEquals(2, contracts.size());

			assertTrue(contracts
					.contains("com.sun.enterprise.admin.cli.CLICommand"));

		}

	}

	@Test
	public void testCreateServiceLocator() throws BootException {

		StartupContext context = new StartupContext();

		Main main = new Main();

        ModulesRegistry mr = AbstractFactory.getInstance().createModulesRegistry();

		ServiceLocator serviceLocator = main.createServiceLocator(
                mr, new StartupContext(), null, null);

                assertNotNull("Main.createServiceLocator(StartupContext) should return a ServiceLocator", serviceLocator);

		assertEquals("ServiceLocator should be bound", serviceLocator,
				serviceLocator.getService(ServiceLocator.class));

	}

	@Test
	public void testPopulator() throws IOException {
		final String descriptorText = "[org.jvnet.hk2.config.provider.internal.ConfigTransactionImpl]\n"
				+ "contract={org.jvnet.hk2.config.provider.internal.ConfigTransactionImpl}\n"
				+ "scope=javax.inject.Singleton\n";

		ServiceLocator serviceLocator = ServiceLocatorFactory.getInstance()
				.create("" + new Random().nextInt());

		HK2Populator.populate(serviceLocator,
				new DescriptorFileFinder() {

					@Override
					public List<InputStream> findDescriptorFiles()
							throws IOException {
						ArrayList<InputStream> returnList = new ArrayList<InputStream>();

						InputStream is = new ByteArrayInputStream(
								descriptorText.getBytes());
						returnList.add(is);
						return returnList;
					}

				});

		List<ActiveDescriptor<?>> descriptors = serviceLocator
				.getDescriptors(BuilderHelper
						.createContractFilter("org.jvnet.hk2.config.provider.internal.ConfigTransactionImpl"));

		assertNotNull(descriptors);
		assertEquals(1, descriptors.size());
		Descriptor d = descriptors.iterator().next();

		assertEquals(
				"org.jvnet.hk2.config.provider.internal.ConfigTransactionImpl",
				d.getImplementation());
		assertEquals("javax.inject.Singleton", d.getScope());

		Set<String> advertisedContracts = d.getAdvertisedContracts();

		assertEquals(1, advertisedContracts.size());

		assertTrue(advertisedContracts
				.contains("org.jvnet.hk2.config.provider.internal.ConfigTransactionImpl"));
	}

	static boolean postProcessorWasCalled;

	@Test
	public void testPopulatorPostProcessorWithoutAdd() throws IOException {
		postProcessorWasCalled = false;

		final String descriptorText = "[org.jvnet.hk2.config.provider.internal.ConfigTransactionImpl]\n"
				+ "contract={org.jvnet.hk2.config.provider.internal.ConfigTransactionImpl}\n"
				+ "scope=javax.inject.Singleton\n";

		ServiceLocator serviceLocator = ServiceLocatorFactory.getInstance()
				.create("" + new Random().nextInt());

		HK2Populator.populate(serviceLocator,
				new DescriptorFileFinder() {

					@Override
					public List<InputStream> findDescriptorFiles()
							throws IOException {
						ArrayList<InputStream> returnList = new ArrayList<InputStream>();

						InputStream is = new ByteArrayInputStream(
								descriptorText.getBytes());
						returnList.add(is);
						return returnList;
					}

				}, new PopulatorPostProcessor() {

					@Override
					public DescriptorImpl process(DescriptorImpl d) {
						postProcessorWasCalled = true;

						assertEquals(
								"org.jvnet.hk2.config.provider.internal.ConfigTransactionImpl",
								d.getImplementation());
						assertEquals("javax.inject.Singleton", d.getScope());

						Set<String> advertisedContracts = d
								.getAdvertisedContracts();

						assertEquals(1, advertisedContracts.size());

						assertTrue(advertisedContracts
								.contains("org.jvnet.hk2.config.provider.internal.ConfigTransactionImpl"));

						return null;
					}

					@Override
					public void setServiceLocator(ServiceLocator serviceLocator) {
						// TODO Auto-generated method stub
						
					}
				});

		List<ActiveDescriptor<?>> descriptors = serviceLocator
				.getDescriptors(BuilderHelper
						.createContractFilter("org.jvnet.hk2.config.provider.internal.ConfigTransactionImpl"));

		assertNotNull(descriptors);
		assertEquals(0, descriptors.size());
	}

	@Test
	public void testPopulatorPostProcessorWithOverride() throws IOException {
		postProcessorWasCalled = false;

		final String descriptorText = "[org.jvnet.hk2.config.provider.internal.ConfigTransactionImpl]\n"
				+ "contract={org.jvnet.hk2.config.provider.internal.ConfigTransactionImpl}\n"
				+ "scope=javax.inject.Singleton\n";

		ServiceLocator serviceLocator = ServiceLocatorFactory.getInstance()
				.create("" + new Random().nextInt());

		HK2Populator.populate(serviceLocator,
				new DescriptorFileFinder() {

					@Override
					public List<InputStream> findDescriptorFiles()
							throws IOException {
						ArrayList<InputStream> returnList = new ArrayList<InputStream>();

						InputStream is = new ByteArrayInputStream(
								descriptorText.getBytes());
						returnList.add(is);
						return returnList;
					}

				}, new PopulatorPostProcessor() {

					@Override
					public DescriptorImpl process(DescriptorImpl d) {
						postProcessorWasCalled = true;

						d.setImplementation("OVERRIDDEN");
						
						return d;
					}

					@Override
					public void setServiceLocator(ServiceLocator serviceLocator) {
						// TODO Auto-generated method stub
						
					}
				});

		List<ActiveDescriptor<?>> descriptors = serviceLocator
				.getDescriptors(BuilderHelper
						.createContractFilter("org.jvnet.hk2.config.provider.internal.ConfigTransactionImpl"));

		assertNotNull(descriptors);
		assertEquals(1, descriptors.size());

		assertEquals("OVERRIDDEN", descriptors.iterator().next()
				.getImplementation());
	}

}
