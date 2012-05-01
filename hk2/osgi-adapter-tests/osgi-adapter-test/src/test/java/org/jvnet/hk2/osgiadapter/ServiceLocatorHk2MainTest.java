package org.jvnet.hk2.osgiadapter;

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;

@RunWith(org.ops4j.pax.exam.junit.JUnit4TestRunner.class)
public class ServiceLocatorHk2MainTest {

	private static final String GROUP_ID = "org.glassfish.hk2";
	private static final String EXT_GROUP_ID = "org.glassfish.hk2.external";

	@org.ops4j.pax.exam.Inject
	BundleContext bundleContext;

	static File cacheDir;
	static File testFile;

	static final String text = "# generated on 2 Apr 2012 18:04:09 GMT\n"
			+ "class=com.sun.enterprise.admin.cli.optional.RestoreDomainCommand,index=com.sun.enterprise.admin.cli.CLICommand:restore-domain\n"
			+ "class=com.sun.enterprise.admin.cli.optional.ListBackupsCommand,index=com.sun.enterprise.admin.cli.CLICommand:list-backups\n";

	@Configuration
	public static Option[] configuration() {
		String projectVersion = System.getProperty("project.version");
		return options(
				felix(),
				systemPackage("sun.misc"),
				provision(mavenBundle().groupId(GROUP_ID).artifactId(
				"hk2-utils")), provision(mavenBundle()
				.groupId(GROUP_ID).artifactId("hk2-deprecated").startLevel(4)),

				provision(mavenBundle().groupId(GROUP_ID).artifactId("core").startLevel(4)),
				provision(mavenBundle().groupId(GROUP_ID).artifactId("hk2-api").startLevel(4)),
				provision(mavenBundle().groupId(GROUP_ID).artifactId(
						"hk2-locator").startLevel(4)),
				provision(mavenBundle().groupId(EXT_GROUP_ID).artifactId(
						"javax.inject").startLevel(4)),
				provision(mavenBundle().groupId(EXT_GROUP_ID).artifactId(
						"cglib").startLevel(4)),
				provision(mavenBundle().groupId(EXT_GROUP_ID).artifactId(
						"asm-all-repackaged").startLevel(4)),
				provision(mavenBundle().groupId(GROUP_ID)
						.artifactId("osgi-resource-locator").version("1.0.1").startLevel(4)),
				provision(mavenBundle().groupId(GROUP_ID).artifactId(
						"class-model").startLevel(4)),
				provision(mavenBundle().groupId(GROUP_ID).artifactId(
						"osgi-adapter").startLevel(1)),
				provision(mavenBundle().groupId(GROUP_ID).artifactId(
						"test-module-startup").startLevel(4)),

				systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level")
						.value("DEBUG"), logProfile(), cleanCaches()
		// systemProperty("com.sun.enterprise.hk2.repositories").value(cacheDir.toURI().toString()),
		// vmOption(
		// "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005" )
		);
	}

	@Test
	@Ignore
	public void testHK2Main() throws Throwable {

		try {
			assertNotNull("OSGi did not properly boot", this.bundleContext);

			final StartupContext startupContext = new StartupContext();
			final ServiceTracker hk2Tracker = new ServiceTracker(
					this.bundleContext, Main.class.getName(), null);
			hk2Tracker.open();
			final Main main = (Main) hk2Tracker.waitForService(0);

			// Expect correct subclass of Main to be registered as OSGi service
			assertEquals("org.jvnet.hk2.osgiadapter.HK2Main", main.getClass()
					.getCanonicalName());
			hk2Tracker.close();
			final ModulesRegistry mr = ModulesRegistry.class.cast(bundleContext
					.getService(bundleContext
							.getServiceReference(ModulesRegistry.class
									.getName())));

			assertEquals("org.jvnet.hk2.osgiadapter.OSGiModulesRegistryImpl",
					mr.getClass().getCanonicalName());

			final ServiceLocator serviceLocator = main.createServiceLocator(mr,
					startupContext);

			ModulesRegistry mrFromServiceLocator = serviceLocator
					.getService(ModulesRegistry.class);
			assertEquals(mr, mrFromServiceLocator);

			// serviceLocator should have been registered as an OSGi service
			checkServiceLocatorOSGiRegistration(serviceLocator);

			// check osgi services got registered
			List<?> startLevelServices = serviceLocator
					.getAllServices(BuilderHelper
							.createContractFilter("org.osgi.service.startlevel.StartLevel"));

			assertEquals(1, startLevelServices.size());

			List<?> startups = serviceLocator.getAllServices(BuilderHelper
					.createContractFilter(ModuleStartup.class
							.getCanonicalName()));
			assertEquals("Cannot find ModuleStartup", 1, startups.size());

			final ModuleStartup moduleStartup = main.findStartupService(mr,
					serviceLocator, null, startupContext);

			assertNotNull(
					"Expected a ModuleStartup that was provisioned as part of this test",
					moduleStartup);

			moduleStartup.start();

		} catch (Exception ex) {
			if (ex.getCause() != null)
				throw ex.getCause();

			throw ex;
		}
	}

	@Test
	public void testHK2OSGiAdapter() throws Throwable {

		try {
			assertNotNull("OSGi did not properly boot", this.bundleContext);

			ServiceReference serviceLocatorRef = bundleContext
					.getServiceReference(ServiceLocator.class.getName());

			assertNotNull(serviceLocatorRef);
			ServiceLocator serviceLocator = (ServiceLocator) bundleContext
					.getService(serviceLocatorRef);

			assertNotNull(serviceLocator);

			List<ModuleStartup> startups = (List<ModuleStartup>) serviceLocator.getAllServices(BuilderHelper
					.createContractFilter(ModuleStartup.class
							.getCanonicalName()));
			assertEquals("Cannot find ModuleStartup", 1, startups.size());

			startups.iterator().next().start();
			
		} catch (Exception ex) {
			if (ex.getCause() != null)
				throw ex.getCause();

			throw ex;
		}
	}

	private void checkServiceLocatorOSGiRegistration(
			final ServiceLocator serviceLocator) {
		ServiceReference serviceLocatorRef = bundleContext
				.getServiceReference(ServiceLocator.class.getName());

		ServiceLocator serviceLocatorFromOSGi = (ServiceLocator) bundleContext
				.getService(serviceLocatorRef);

		assertNotNull("Expected ServiceLocator to be registed in OSGi",
				serviceLocatorFromOSGi);
		assertEquals(
				"Expected same ServiceLocator in OSGi as the one passed in",
				serviceLocator, serviceLocatorFromOSGi);
	}

}
