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

import org.glassfish.hk2.api.ServiceLocator;
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

	static boolean postProcessorWasCalled;

	

}
