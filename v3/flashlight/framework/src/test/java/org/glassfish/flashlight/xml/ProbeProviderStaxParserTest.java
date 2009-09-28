/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.xml;

import java.io.*;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bnevins
 */
public class ProbeProviderStaxParserTest {
    public ProbeProviderStaxParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ClassLoader cl = ProbeProviderStaxParserTest.class.getClassLoader();
        oneProbeStream = cl.getResourceAsStream("one_probe.xml");
        twoProbeStream = cl.getResourceAsStream("two_probe.xml");
        assertNotNull(oneProbeStream);
        assertNotNull(twoProbeStream);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of read method, of class ProbeProviderStaxParser.
     */
    @Test
    public void testRead() throws Exception {
        System.out.println("read");
        ProbeProviderStaxParser ppsp = new ProbeProviderStaxParser(oneProbeStream);
        List<Provider> providers = ppsp.getProviders();

        for(Provider p : providers) {
            System.out.println("******** PROVIDER: *******\n" + p);
        }
    }
   private static InputStream oneProbeStream;
   private static InputStream twoProbeStream;
}