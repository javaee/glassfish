package org.glassfish.hk2.bootstrap.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.inhabitants.InhabitantsParser;
import org.glassfish.hk2.inhabitants.InhabitantsScanner;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.hk2.component.Holder;

import static org.junit.Assert.*;

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

	    List<ActiveDescriptor<?>> ds = sl.getDescriptors(BuilderHelper.createNameAndContractFilter("com.sun.enterprise.admin.cli.CLICommand", "restore-domain"));
	    
	    assertNotNull(ds);
	    assertEquals("Expecting one restore-domain descriptor", 1,ds.size());
	    for (ActiveDescriptor<?> d:ds) {
	    	
	       assertEquals( "com.sun.enterprise.admin.cli.optional.RestoreDomainCommand", d.getImplementation());
	       assertEquals( "restore-domain", d.getName());
	       
	       Set<String> contracts = d.getAdvertisedContracts();
	       assertEquals(1, contracts.size());
	       
	       assertTrue(contracts.contains("com.sun.enterprise.admin.cli.CLICommand"));
	      
	    }
	    
	}
	
}
