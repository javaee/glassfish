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
import java.util.*;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.common_impl.AbstractFactory;
import com.sun.enterprise.module.impl.HK2Factory;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.bootstrap.DescriptorFileFinder;
import org.glassfish.hk2.bootstrap.HK2Populator;
import org.glassfish.hk2.bootstrap.PopulatorPostProcessor;
import org.glassfish.hk2.utilities.Binder;
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

				}, null);

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

		final PopulatorPostProcessor pp = new PopulatorPostProcessor() {

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
			}};
			
		HK2Populator.populate(serviceLocator, new DescriptorFileFinder() {

			@Override
			public List<InputStream> findDescriptorFiles() throws IOException {
				ArrayList<InputStream> returnList = new ArrayList<InputStream>();

				InputStream is = new ByteArrayInputStream(descriptorText
						.getBytes());
				returnList.add(is);
				return returnList;
			}

		}, Arrays.asList(pp));

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

		final PopulatorPostProcessor pp = new PopulatorPostProcessor() {

			@Override
			public DescriptorImpl process(DescriptorImpl d) {
				postProcessorWasCalled = true;

				d.setImplementation("OVERRIDDEN");

				return d;
			}

		};

		HK2Populator.populate(serviceLocator, new DescriptorFileFinder() {

			@Override
			public List<InputStream> findDescriptorFiles() throws IOException {
				ArrayList<InputStream> returnList = new ArrayList<InputStream>();

				InputStream is = new ByteArrayInputStream(descriptorText
						.getBytes());
				returnList.add(is);
				return returnList;
			}

		}, Arrays.asList(pp));

		List<ActiveDescriptor<?>> descriptors = serviceLocator
				.getDescriptors(BuilderHelper
						.createContractFilter("org.jvnet.hk2.config.provider.internal.ConfigTransactionImpl"));

		assertNotNull(descriptors);
		assertEquals(1, descriptors.size());

		assertEquals("OVERRIDDEN", descriptors.iterator().next()
				.getImplementation());
	}

}
